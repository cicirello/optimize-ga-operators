/*
 * Experiments related to optimizing genetic algorithm operators.
 * Copyright (C) 2023-2024 Vincent A. Cicirello
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.cicirello.experiments.gaops;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import org.cicirello.math.stats.Statistics;
import org.cicirello.search.ProgressTracker;
import org.cicirello.search.evo.FitnessFunction;
import org.cicirello.search.evo.GenerationalEvolutionaryAlgorithm;
import org.cicirello.search.evo.NaiveGenerationalEvolutionaryAlgorithm;
import org.cicirello.search.evo.PopulationFitnessVector;
import org.cicirello.search.evo.SelectionOperator;
import org.cicirello.search.operators.CrossoverOperator;
import org.cicirello.search.operators.MutationOperator;
import org.cicirello.search.operators.bits.BitVectorInitializer;
import org.cicirello.search.problems.IntegerCostOptimizationProblem;
import org.cicirello.search.representations.BitVector;
import org.cicirello.util.DoubleList;

/**
 * Experiment comparing CPU time of two alternative implementations of the logic of a generation
 * (e.g., using the binomial optimization to determine which pairs of population members undergo
 * crossover, vs the typical iterate over pairs approach).
 *
 * <p>Vincent A. Cicirello. Optimizing Genetic Algorithms Using the Binomial Distribution.
 * <i>Proceedings of the 16th International Joint Conference on Computational Intelligence</i>,
 * pages 159-169. November 2024.
 *
 * @author <a href=https://www.cicirello.org/ target=_top>Vincent A. Cicirello</a>, <a
 *     href=https://www.cicirello.org/ target=_top>https://www.cicirello.org/</a>
 */
public class GenerationLoopExperiment {

  /** Number of trials to average. */
  private static final int TRIALS = 100;

  /** Number of generations for each trial. */
  private static final int GENERATIONS_PER_TRIAL = 100000;

  /** Size of the population. */
  private static final int POPULATION_SIZE = 200;

  /**
   * Runs the experiment.
   *
   * @param args no command line arguments
   */
  public static void main(String[] args) {

    ThreadMXBean bean = ManagementFactory.getThreadMXBean();
    int useToPreventOptimizingAway = 0;
    NoOpMutation mutation = new NoOpMutation();
    NoOpCrossover crossover1 = new NoOpCrossover();
    NoOpCrossover crossover2 = new NoOpCrossover();
    BitVectorInitializer initializer = new BitVectorInitializer(1);
    NoOpFitness fitness = new NoOpFitness();
    NoOpSelection selection = new NoOpSelection();

    DoubleList valuesOfC = new DoubleList();
    double[] rates = {0.05, 0.15, 0.25, 0.35, 0.45, 0.55, 0.65, 0.75, 0.85, 0.95};
    for (double c : rates) {
      valuesOfC.add(c);
    }

    // NEED TO WARM UP JIT HERE.
    System.out.println("Warming up the Java JIT");
    for (int i = 0; i < valuesOfC.size(); i++) {
      double c = valuesOfC.get(i);
      crossover1.reset();
      crossover2.reset();

      @SuppressWarnings("deprecation")
      NaiveGenerationalEvolutionaryAlgorithm<BitVector> simple =
          new NaiveGenerationalEvolutionaryAlgorithm<BitVector>(
              POPULATION_SIZE, mutation, 1, crossover1, c, initializer, fitness, selection);
      GenerationalEvolutionaryAlgorithm<BitVector> optimized =
          new GenerationalEvolutionaryAlgorithm<BitVector>(
              POPULATION_SIZE, mutation, 1, crossover2, c, initializer, fitness, selection);
      useToPreventOptimizingAway += simple.optimize(GENERATIONS_PER_TRIAL).getCost();
      useToPreventOptimizingAway += optimized.optimize(GENERATIONS_PER_TRIAL).getCost();
      useToPreventOptimizingAway += mutation.state() + crossover1.count() + crossover2.count();
    }
    System.out.println("End Warmup Phase");
    System.out.println();

    System.out.printf(
        "%4s\t%12s\t%12s\t%11s\t%10s\t%10s\t%10s\t%12s\t%12s\t%10s\t%10s\t%10s%n",
        "c",
        "simple",
        "optimized",
        "%less-time",
        "t",
        "dof",
        "p",
        "simple-calls",
        "opt-calls",
        "t-calls",
        "dof-calls",
        "p-calls");
    for (int i = 0; i < valuesOfC.size(); i++) {
      double c = valuesOfC.get(i);

      @SuppressWarnings("deprecation")
      NaiveGenerationalEvolutionaryAlgorithm<BitVector> simple =
          new NaiveGenerationalEvolutionaryAlgorithm<BitVector>(
              POPULATION_SIZE, mutation, 1, crossover1, c, initializer, fitness, selection);
      GenerationalEvolutionaryAlgorithm<BitVector> optimized =
          new GenerationalEvolutionaryAlgorithm<BitVector>(
              POPULATION_SIZE, mutation, 1, crossover2, c, initializer, fitness, selection);
      double[][] ms = new double[2][TRIALS];
      int[][] crossCounts = new int[2][TRIALS];
      for (int j = 0; j < TRIALS; j++) {
        crossover1.reset();
        crossover2.reset();
        long start = bean.getCurrentThreadCpuTime();
        useToPreventOptimizingAway += simple.optimize(GENERATIONS_PER_TRIAL).getCost();
        long middle = bean.getCurrentThreadCpuTime();
        useToPreventOptimizingAway += optimized.optimize(GENERATIONS_PER_TRIAL).getCost();
        long end = bean.getCurrentThreadCpuTime();
        // compute elapsed times in nanoseconds
        ms[0][j] = (middle - start);
        ms[1][j] = (end - middle);
        crossCounts[0][j] = crossover1.count();
        crossCounts[1][j] = crossover2.count();
        useToPreventOptimizingAway += mutation.state();
        simple.setProgressTracker(new ProgressTracker<BitVector>());
        optimized.setProgressTracker(new ProgressTracker<BitVector>());
      }

      Number[] tTest = Statistics.tTestWelch(ms[0], ms[1]);
      double t = tTest[0].doubleValue();
      int dof = tTest[1].intValue();
      double p = Statistics.p(t, dof);

      Number[] tTestCounts = Statistics.tTestWelch(crossCounts[0], crossCounts[1]);
      double tCounts = tTestCounts[0].doubleValue();
      int dofCounts = tTestCounts[1].intValue();
      double pCounts = Statistics.p(tCounts, dofCounts);

      // times are converted to seconds during output
      double timeSimpleSeconds = Statistics.mean(ms[0]) / 1000000000.0;
      double timeOptimizedSeconds = Statistics.mean(ms[1]) / 1000000000.0;
      double percentLessTime =
          100 * ((timeSimpleSeconds - timeOptimizedSeconds) / timeSimpleSeconds);

      System.out.printf(
          "%3.2f\t%12.3g\t%12.3g\t%10.2f%%\t%10.4f\t%10d\t%10.3g\t%12.3g\t%12.3g\t%10.4f\t%10d\t%10.3g%n",
          c,
          timeSimpleSeconds,
          timeOptimizedSeconds,
          percentLessTime,
          t,
          dof,
          p,
          Statistics.mean(crossCounts[0]),
          Statistics.mean(crossCounts[1]),
          tCounts,
          dofCounts,
          pCounts);
    }
    System.out.println();

    System.out.println("Interpreting Above Results:");
    System.out.println("1) Negative t value implies simple version is faster.");
    System.out.println("2) Positive t value implies optimized version is faster.");
    System.out.println("3) The p column is, well, the p value.");

    System.out.println(
        "\nOutput to ensure can't optimize away anything: " + useToPreventOptimizingAway);
  }

  /**
   * The NoOpMutation does absolutely nothing to the BitVector. This set of experiments is to study
   * the effects of optimizing the generation loop, so using a mutation that does nothing to
   * eliminate the time effects of mutation.
   */
  private static final class NoOpMutation implements MutationOperator<BitVector> {

    private int state;

    @Override
    public void mutate(BitVector c) {
      // just change the state of the object to avoid
      // call being optimized away.
      state++;
    }

    @Override
    public NoOpMutation split() {
      return new NoOpMutation();
    }

    /**
     * Gets the state value.
     *
     * @return the value of state
     */
    public int state() {
      return state;
    }
  }

  /**
   * The NoOpCrossover does absolutely nothing to the BitVectors. This set of experiments is to
   * study the effects of optimizing the generation loop, so using a crossover that does nothing to
   * eliminate the time effects of crossover.
   */
  private static final class NoOpCrossover implements CrossoverOperator<BitVector> {

    private int count;

    @Override
    public void cross(BitVector c1, BitVector c2) {
      count++;
    }

    @Override
    public NoOpCrossover split() {
      return new NoOpCrossover();
    }

    /**
     * Gets the count value.
     *
     * @return the value of count
     */
    public int count() {
      return count;
    }

    /** Resets the counter. */
    public void reset() {
      count = 0;
    }
  }

  /**
   * The NoOpSelection is not a real selection operator. It simply selects the same members of the
   * current population without examining fitness. The purpose is to eliminate the time cost
   * associated with selection to enable studying strictly the time of the two versions of the
   * generation loop.
   */
  private static final class NoOpSelection implements SelectionOperator {

    @Override
    public void select(PopulationFitnessVector.Integer fitnesses, int[] selected) {
      for (int i = 0; i < selected.length; i++) {
        selected[i] = i;
      }
    }

    @Override
    public void select(PopulationFitnessVector.Double fitnesses, int[] selected) {
      for (int i = 0; i < selected.length; i++) {
        selected[i] = i;
      }
    }

    @Override
    public NoOpSelection split() {
      return new NoOpSelection();
    }
  }

  /**
   * The NoOpFitness is not a real fitness function. It simply returns a constant. The purpose is to
   * eliminate the time cost associated with fitness evaluation to enable studying strictly the time
   * of the two versions of the generation loop.
   */
  private static final class NoOpFitness
      implements FitnessFunction.Integer<BitVector>, IntegerCostOptimizationProblem<BitVector> {

    @Override
    public int fitness(BitVector b) {
      return 2;
    }

    @Override
    public int cost(BitVector b) {
      return 1;
    }

    @Override
    public int value(BitVector b) {
      return 2;
    }

    @Override
    public NoOpFitness getProblem() {
      return this;
    }
  }
}
