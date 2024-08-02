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
import org.cicirello.search.SolutionCostPair;
import org.cicirello.search.evo.FitnessFunction;
import org.cicirello.search.evo.GenerationalEvolutionaryAlgorithm;
import org.cicirello.search.evo.NaiveGenerationalEvolutionaryAlgorithm;
import org.cicirello.search.evo.StochasticUniversalSampling;
import org.cicirello.search.operators.bits.BitFlipMutation;
import org.cicirello.search.operators.bits.BitVectorInitializer;
import org.cicirello.search.operators.bits.UniformCrossover;
import org.cicirello.search.problems.IntegerCostOptimizationProblem;
import org.cicirello.search.representations.BitVector;
import org.cicirello.util.DoubleList;

/**
 * Experiment comparing CPU time of two alternative GAs: typical implementation of bit flip
 * mutation, uniform mutation, and generation loop; vs optimized version where binomial is used for
 * choosing which bits to mutate, cross, and which pairs of parents to cross.
 *
 * @author <a href=https://www.cicirello.org/ target=_top>Vincent A. Cicirello</a>, <a
 *     href=https://www.cicirello.org/ target=_top>https://www.cicirello.org/</a>
 */
public class GAExperiment {

  /** Number of trials to average. */
  private static final int TRIALS = 100;

  /** Number of generations for each trial. */
  private static final int GENERATIONS_PER_TRIAL = 1000;

  /** Size of the population. */
  private static final int POPULATION_SIZE = 100;

  /**
   * Runs the experiment.
   *
   * @param args no command line arguments
   */
  public static void main(String[] args) {

    ThreadMXBean bean = ManagementFactory.getThreadMXBean();
    int useToPreventOptimizingAway = 0;

    OneMaxFitness fitness = new OneMaxFitness();
    final int BIT_LENGTH = 1024;
    final double MUTATION_RATE = 1.0 / BIT_LENGTH;
    double[] U_VALUES = {0.33, 0.49};

    DoubleList valuesOfC = new DoubleList();
    for (double c = 0.05; c < 1; c += 0.1) {
      valuesOfC.add(c);
    }

    // NEED TO WARM UP JIT HERE.
    System.out.println("Warming up the Java JIT");
    for (double u : U_VALUES) {
      for (int i = 0; i < valuesOfC.size(); i++) {
        double c = valuesOfC.get(i);

        @SuppressWarnings("deprecation")
        NaiveGenerationalEvolutionaryAlgorithm<BitVector> simple =
            new NaiveGenerationalEvolutionaryAlgorithm<BitVector>(
                POPULATION_SIZE,
                new MutationExperiment.SimpleBitFlipMutation(MUTATION_RATE),
                1,
                new CrossoverExperiment.SimpleUniformCrossover(u),
                c,
                new BitVectorInitializer(BIT_LENGTH),
                fitness,
                new StochasticUniversalSampling());
        GenerationalEvolutionaryAlgorithm<BitVector> optimized =
            new GenerationalEvolutionaryAlgorithm<BitVector>(
                POPULATION_SIZE,
                new BitFlipMutation(MUTATION_RATE),
                1,
                new UniformCrossover(u),
                c,
                new BitVectorInitializer(BIT_LENGTH),
                fitness,
                new StochasticUniversalSampling());

        useToPreventOptimizingAway += simple.optimize(GENERATIONS_PER_TRIAL).getCost();
        useToPreventOptimizingAway += optimized.optimize(GENERATIONS_PER_TRIAL).getCost();
      }
    }
    System.out.println("End Warmup Phase");
    System.out.println();

    for (double u : U_VALUES) {
      System.out.printf("Uniform Crossover Parameter U=%3.2f%n", u);
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
                POPULATION_SIZE,
                new MutationExperiment.SimpleBitFlipMutation(MUTATION_RATE),
                1,
                new CrossoverExperiment.SimpleUniformCrossover(u),
                c,
                new BitVectorInitializer(BIT_LENGTH),
                fitness,
                new StochasticUniversalSampling());
        GenerationalEvolutionaryAlgorithm<BitVector> optimized =
            new GenerationalEvolutionaryAlgorithm<BitVector>(
                POPULATION_SIZE,
                new BitFlipMutation(MUTATION_RATE),
                1,
                new UniformCrossover(u),
                c,
                new BitVectorInitializer(BIT_LENGTH),
                fitness,
                new StochasticUniversalSampling());
        double[][] ms = new double[2][TRIALS];
        int[][] onesCounts = new int[2][TRIALS];
        for (int j = 0; j < TRIALS; j++) {
          long start = bean.getCurrentThreadCpuTime();
          SolutionCostPair<BitVector> s1 = simple.optimize(GENERATIONS_PER_TRIAL);
          long middle = bean.getCurrentThreadCpuTime();
          SolutionCostPair<BitVector> s2 = optimized.optimize(GENERATIONS_PER_TRIAL);
          long end = bean.getCurrentThreadCpuTime();
          // compute elapsed times in nanoseconds
          ms[0][j] = (middle - start);
          ms[1][j] = (end - middle);
          onesCounts[0][j] = fitness.value(s1.getSolution());
          onesCounts[1][j] = fitness.value(s2.getSolution());
          simple.setProgressTracker(new ProgressTracker<BitVector>());
          optimized.setProgressTracker(new ProgressTracker<BitVector>());
        }

        Number[] tTest = Statistics.tTestWelch(ms[0], ms[1]);
        double t = tTest[0].doubleValue();
        int dof = tTest[1].intValue();
        double p = Statistics.p(t, dof);

        Number[] tTestCounts = Statistics.tTestWelch(onesCounts[0], onesCounts[1]);
        double tCounts = tTestCounts[0].doubleValue();
        int dofCounts = tTestCounts[1].intValue();
        double pCounts = Statistics.p(tCounts, dofCounts);

        // times are converted to seconds during output
        double timeSimpleSeconds = Statistics.mean(ms[0]) / 1000000000.0;
        double timeOptimizedSeconds = Statistics.mean(ms[1]) / 1000000000.0;
        double percentLessTime =
            100 * ((timeSimpleSeconds - timeOptimizedSeconds) / timeSimpleSeconds);

        System.out.printf(
            "%3.2f\t%12.3g\t%12.3g\t%10.2f%%\t%10.4f\t%10d\t%10.3g\t%12.2f\t%12.2f\t%10.4f\t%10d\t%10.3g%n",
            c,
            timeSimpleSeconds,
            timeOptimizedSeconds,
            percentLessTime,
            t,
            dof,
            p,
            Statistics.mean(onesCounts[0]),
            Statistics.mean(onesCounts[1]),
            tCounts,
            dofCounts,
            pCounts);
      }
      System.out.println();
    }

    System.out.println("Interpreting Above Results:");
    System.out.println("1) Negative t value implies simple version is faster.");
    System.out.println("2) Positive t value implies optimized version is faster.");
    System.out.println("3) The p column is, well, the p value.");

    System.out.println(
        "\nOutput to ensure can't optimize away anything: " + useToPreventOptimizingAway);
  }

  /**
   * OneMaxFitness is a fitness function for the One Max problem. Chips-n-Salsa actually has a
   * OneMax class, however, the EA implementations in the library will terminate if a solution is
   * found matching a simple bound on the optimal (i.e., for OneMax it is easy to determine if the
   * optimal is found and then terminate). This class doesn't specify a bound on the optimal
   * preventing the EA from early termination.
   */
  private static class OneMaxFitness
      implements FitnessFunction.Integer<BitVector>, IntegerCostOptimizationProblem<BitVector> {

    @Override
    public int fitness(BitVector b) {
      // The plus 1 is to ensure positive fitness.
      return b.countOnes() + 1;
    }

    @Override
    public int cost(BitVector b) {
      return b.countZeros();
    }

    @Override
    public int value(BitVector b) {
      return b.countOnes();
    }

    @Override
    public OneMaxFitness getProblem() {
      return this;
    }
  }
}
