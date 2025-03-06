/*
 * Experiments related to optimizing permutation evolutionary operators.
 * Copyright (C) 2025 Vincent A. Cicirello
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

package org.cicirello.experiments.optpermops;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import org.cicirello.math.stats.Statistics;
import org.cicirello.permutations.Permutation;
import org.cicirello.permutations.distance.ExactMatchDistance;
import org.cicirello.search.Configurator;
import org.cicirello.search.ProgressTracker;
import org.cicirello.search.SolutionCostPair;
import org.cicirello.search.evo.FitnessFunction;
import org.cicirello.search.evo.GenerationalEvolutionaryAlgorithm;
import org.cicirello.search.evo.NaiveGenerationalEvolutionaryAlgorithm;
import org.cicirello.search.evo.TruncationSelection;
import org.cicirello.search.operators.CrossoverOperator;
import org.cicirello.search.operators.permutations.OrderCrossoverTwo;
import org.cicirello.search.operators.permutations.PermutationInitializer;
import org.cicirello.search.operators.permutations.SwapMutation;
import org.cicirello.search.operators.permutations.UniformOrderBasedCrossover;
import org.cicirello.search.operators.permutations.UniformPartiallyMatchedCrossover;
import org.cicirello.search.operators.permutations.UniformPrecedencePreservativeCrossover;
import org.cicirello.search.problems.IntegerCostOptimizationProblem;
import org.cicirello.search.problems.PermutationInAHaystack;

/**
 * Experiment comparing CPU times of two alternative implementations of an EA with different
 * permutation crossover operators.
 *
 * @author <a href=https://www.cicirello.org/ target=_top>Vincent A. Cicirello</a>, <a
 *     href=https://www.cicirello.org/ target=_top>https://www.cicirello.org/</a>
 */
public class ExperimentPermutationEA {

  /** Number of trials to average. */
  private static final int TRIALS = 100;

  /** Number of trials for warmup. */
  private static final int WARMUP_TRIALS = 10;

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

    // Seed the random number generator.
    Configurator.configureRandomGenerator(42);

    ArrayList<CrossoverOperator<Permutation>> simpleCrossoverOperators =
        new ArrayList<CrossoverOperator<Permutation>>();
    simpleCrossoverOperators.add(new SimpleUPMX(1.0 / 3.0));
    simpleCrossoverOperators.add(new SimpleUOBX(0.5));
    simpleCrossoverOperators.add(new SimpleOX2(0.5));
    simpleCrossoverOperators.add(new SimpleUPPX(0.5));

    ArrayList<CrossoverOperator<Permutation>> optimizedCrossoverOperators =
        new ArrayList<CrossoverOperator<Permutation>>();
    optimizedCrossoverOperators.add(new UniformPartiallyMatchedCrossover(1.0 / 3.0));
    optimizedCrossoverOperators.add(new UniformOrderBasedCrossover(0.5));
    optimizedCrossoverOperators.add(new OrderCrossoverTwo(0.5));
    optimizedCrossoverOperators.add(new UniformPrecedencePreservativeCrossover(0.5));

    String[] crossoverNames = {"UPMX", "UOBX", "OX2", "UPPX"};

    final int PERMUTATION_LENGTH = 128;

    PermutationInAHaystackFitness fitness = new PermutationInAHaystackFitness(PERMUTATION_LENGTH);

    // Using the simplest possible mutation operator (swap) so that experiment
    // focus is primarily on simple vs optimized version of the crossover operator.
    SwapMutation mutation = new SwapMutation();

    double[] rates = {0.05, 0.15, 0.25, 0.35, 0.45, 0.55, 0.65, 0.75, 0.85, 0.95};

    // NEED TO WARM UP JIT HERE.
    System.out.println("Warming up the Java JIT");
    int useToPreventFalseDeadCodeElimination = 0;
    for (int i = 0; i < simpleCrossoverOperators.size(); i++) {
      CrossoverOperator<Permutation> simpleCrossover = simpleCrossoverOperators.get(i);
      CrossoverOperator<Permutation> optimizedCrossover = optimizedCrossoverOperators.get(i);
      String experimentName = crossoverNames[i];

      for (double c : rates) {
        for (int j = 0; j < WARMUP_TRIALS; j++) {
          @SuppressWarnings("deprecation")
          NaiveGenerationalEvolutionaryAlgorithm<Permutation> simple =
              new NaiveGenerationalEvolutionaryAlgorithm<Permutation>(
                  POPULATION_SIZE,
                  mutation.split(),
                  1,
                  simpleCrossover.split(),
                  c,
                  new PermutationInitializer(PERMUTATION_LENGTH),
                  fitness,
                  new TruncationSelection(32));

          GenerationalEvolutionaryAlgorithm<Permutation> optimized =
              new GenerationalEvolutionaryAlgorithm<Permutation>(
                  POPULATION_SIZE,
                  mutation.split(),
                  1,
                  optimizedCrossover.split(),
                  c,
                  new PermutationInitializer(PERMUTATION_LENGTH),
                  fitness,
                  new TruncationSelection(32));

          useToPreventFalseDeadCodeElimination += simple.optimize(GENERATIONS_PER_TRIAL).getCost();
          useToPreventFalseDeadCodeElimination +=
              optimized.optimize(GENERATIONS_PER_TRIAL).getCost();
        }
      }
    }
    System.out.println("End Warmup Phase:" + useToPreventFalseDeadCodeElimination);
    System.out.println();

    // START EXPERIMENT BELOW

    for (int i = 0; i < simpleCrossoverOperators.size(); i++) {
      CrossoverOperator<Permutation> simpleCrossover = simpleCrossoverOperators.get(i);
      CrossoverOperator<Permutation> optimizedCrossover = optimizedCrossoverOperators.get(i);
      String experimentName = crossoverNames[i];

      System.out.printf("%s%n", experimentName);
      System.out.printf(
          "%4s\t%12s\t%12s\t%11s\t%10s\t%10s\t%10s\t%12s\t%12s\t%10s\t%10s\t%10s%n",
          "c",
          "simple",
          "optimized",
          "%less-time",
          "t",
          "dof",
          "p",
          "simple-cost",
          "opt-cost",
          "t-cost",
          "dof-cost",
          "p-cost");

      for (double c : rates) {
        @SuppressWarnings("deprecation")
        NaiveGenerationalEvolutionaryAlgorithm<Permutation> simple =
            new NaiveGenerationalEvolutionaryAlgorithm<Permutation>(
                POPULATION_SIZE,
                mutation.split(),
                1,
                simpleCrossover.split(),
                c,
                new PermutationInitializer(PERMUTATION_LENGTH),
                fitness,
                new TruncationSelection(32));

        GenerationalEvolutionaryAlgorithm<Permutation> optimized =
            new GenerationalEvolutionaryAlgorithm<Permutation>(
                POPULATION_SIZE,
                mutation.split(),
                1,
                optimizedCrossover.split(),
                c,
                new PermutationInitializer(PERMUTATION_LENGTH),
                fitness,
                new TruncationSelection(32));

        double[][] ms = new double[2][TRIALS];
        int[][] solutionCost = new int[2][TRIALS];
        for (int j = 0; j < TRIALS; j++) {
          long start = bean.getCurrentThreadCpuTime();
          SolutionCostPair<Permutation> s1 = simple.optimize(GENERATIONS_PER_TRIAL);
          long middle = bean.getCurrentThreadCpuTime();
          SolutionCostPair<Permutation> s2 = optimized.optimize(GENERATIONS_PER_TRIAL);
          long end = bean.getCurrentThreadCpuTime();
          // compute elapsed times in nanoseconds
          ms[0][j] = (middle - start);
          ms[1][j] = (end - middle);

          solutionCost[0][j] = fitness.cost(s1.getSolution());
          solutionCost[1][j] = fitness.cost(s2.getSolution());
          // reset the ProgressTrackers to avoid accidental dependence across trials.
          simple.setProgressTracker(new ProgressTracker<Permutation>());
          optimized.setProgressTracker(new ProgressTracker<Permutation>());
        }

        Number[] tTest = Statistics.tTestWelch(ms[0], ms[1]);
        double t = tTest[0].doubleValue();
        int dof = tTest[1].intValue();
        double p = Statistics.p(t, dof);

        Number[] tTestCounts = Statistics.tTestWelch(solutionCost[0], solutionCost[1]);
        double tCost = tTestCounts[0].doubleValue();
        int dofCost = tTestCounts[1].intValue();
        double pCost = 1;
        try {
          double temp = Statistics.p(tCost, dofCost);
          pCost = temp;
        } catch (ArithmeticException e) {
          pCost = 1;
        }

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
            Statistics.mean(solutionCost[0]),
            Statistics.mean(solutionCost[1]),
            tCost,
            dofCost,
            pCost);
      }
      System.out.println();
    }

    System.out.println("Interpreting Above Results:");
    System.out.println("1) Negative t value implies simple version is faster.");
    System.out.println("2) Positive t value implies optimized version is faster.");
    System.out.println("3) The p column is, well, the p value.");
  }

  /**
   * PermutationInAHaystackFitness is a fitness function for the Permutation in a Haystack problem.
   * Chips-n-Salsa actually has an implementation of this, however, the EA implementations in the
   * library will terminate if a solution is found matching a simple bound on the optimal (i.e., for
   * the Permutation in a Haystack problem it is easy to determine if the optimal is found and then
   * terminate since the optimal will have a cost of 0). This class doesn't specify a bound on the
   * optimal preventing the EA from early termination.
   */
  private static final class PermutationInAHaystackFitness
      implements FitnessFunction.Integer<Permutation>, IntegerCostOptimizationProblem<Permutation> {

    private final int length;
    private final PermutationInAHaystack problem;

    public PermutationInAHaystackFitness(int length) {
      this.length = length;
      problem = new PermutationInAHaystack(new ExactMatchDistance(), length);
    }

    @Override
    public int fitness(Permutation p) {
      // The plus 1 is to ensure positive fitness.
      return length - problem.cost(p) + 1;
    }

    @Override
    public int cost(Permutation p) {
      return problem.cost(p);
    }

    @Override
    public int value(Permutation p) {
      return problem.value(p);
    }

    @Override
    public PermutationInAHaystackFitness getProblem() {
      return this;
    }
  }
}
