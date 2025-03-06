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
import org.cicirello.math.stats.Statistics;
import org.cicirello.permutations.Permutation;
import org.cicirello.permutations.distance.ExactMatchDistance;
import org.cicirello.search.Configurator;
import org.cicirello.search.ProgressTracker;
import org.cicirello.search.SolutionCostPair;
import org.cicirello.search.evo.FitnessFunction;
import org.cicirello.search.evo.OnePlusOneEvolutionaryAlgorithm;
import org.cicirello.search.operators.permutations.PermutationInitializer;
import org.cicirello.search.operators.permutations.UndoableUniformScrambleMutation;
import org.cicirello.search.problems.IntegerCostOptimizationProblem;
import org.cicirello.search.problems.PermutationInAHaystack;

/**
 * Experiment comparing CPU times of two alternative implementations of a (1+1)-EA with Uniform
 * Scramble Mutation.
 *
 * @author <a href=https://www.cicirello.org/ target=_top>Vincent A. Cicirello</a>, <a
 *     href=https://www.cicirello.org/ target=_top>https://www.cicirello.org/</a>
 */
public class ExperimentPermutationOnePlusOneEA {

  /** Number of trials to average. */
  private static final int TRIALS = 100;

  /** Number of trials for warmup. */
  private static final int WARMUP_TRIALS = 10;

  /** Number of evaluations for each trial. */
  private static final int EVALUATIONS_PER_TRIAL = 100000;

  /**
   * Runs the experiment.
   *
   * @param args no command line arguments
   */
  public static void main(String[] args) {
    ThreadMXBean bean = ManagementFactory.getThreadMXBean();

    // Seed the random number generator.
    Configurator.configureRandomGenerator(42);

    final int PERMUTATION_LENGTH = 128;

    PermutationInAHaystackFitness fitness = new PermutationInAHaystackFitness(PERMUTATION_LENGTH);

    double[] rates = {
      2.0 / PERMUTATION_LENGTH,
      3.0 / PERMUTATION_LENGTH,
      4.0 / PERMUTATION_LENGTH,
      5.0 / PERMUTATION_LENGTH,
      6.0 / PERMUTATION_LENGTH,
      7.0 / PERMUTATION_LENGTH,
      8.0 / PERMUTATION_LENGTH,
      9.0 / PERMUTATION_LENGTH,
      10.0 / PERMUTATION_LENGTH,
      11.0 / PERMUTATION_LENGTH,
      12.0 / PERMUTATION_LENGTH,
      13.0 / PERMUTATION_LENGTH,
      14.0 / PERMUTATION_LENGTH,
      15.0 / PERMUTATION_LENGTH,
      16.0 / PERMUTATION_LENGTH
    };

    // NEED TO WARM UP JIT HERE.
    System.out.println("Warming up the Java JIT");
    int useToPreventFalseDeadCodeElimination = 0;
    for (double u : rates) {
      for (int j = 0; j < WARMUP_TRIALS; j++) {
        OnePlusOneEvolutionaryAlgorithm<Permutation> simple =
            new OnePlusOneEvolutionaryAlgorithm<Permutation>(
                fitness,
                new SimpleUndoableUniformScramble(u),
                new PermutationInitializer(PERMUTATION_LENGTH));

        OnePlusOneEvolutionaryAlgorithm<Permutation> optimized =
            new OnePlusOneEvolutionaryAlgorithm<Permutation>(
                fitness,
                new UndoableUniformScrambleMutation(u),
                new PermutationInitializer(PERMUTATION_LENGTH));

        useToPreventFalseDeadCodeElimination += simple.optimize(EVALUATIONS_PER_TRIAL).getCost();
        useToPreventFalseDeadCodeElimination += optimized.optimize(EVALUATIONS_PER_TRIAL).getCost();
      }
    }
    System.out.println("End Warmup Phase:" + useToPreventFalseDeadCodeElimination);
    System.out.println();

    // START EXPERIMENT BELOW

    String experimentName = "(1+1)-EA with Uniform Scramble Mutation";

    System.out.printf("%s%n", experimentName);
    System.out.printf(
        "%10s\t%12s\t%12s\t%11s\t%10s\t%10s\t%10s\t%12s\t%12s\t%10s\t%10s\t%10s%n",
        "u",
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

    for (double u : rates) {
      OnePlusOneEvolutionaryAlgorithm<Permutation> simple =
          new OnePlusOneEvolutionaryAlgorithm<Permutation>(
              fitness,
              new SimpleUndoableUniformScramble(u),
              new PermutationInitializer(PERMUTATION_LENGTH));

      OnePlusOneEvolutionaryAlgorithm<Permutation> optimized =
          new OnePlusOneEvolutionaryAlgorithm<Permutation>(
              fitness,
              new UndoableUniformScrambleMutation(u),
              new PermutationInitializer(PERMUTATION_LENGTH));

      double[][] ms = new double[2][TRIALS];
      int[][] solutionCost = new int[2][TRIALS];
      for (int j = 0; j < TRIALS; j++) {
        long start = bean.getCurrentThreadCpuTime();
        SolutionCostPair<Permutation> s1 = simple.optimize(EVALUATIONS_PER_TRIAL);
        long middle = bean.getCurrentThreadCpuTime();
        SolutionCostPair<Permutation> s2 = optimized.optimize(EVALUATIONS_PER_TRIAL);
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
          "%10.9f\t%12.3g\t%12.3g\t%10.2f%%\t%10.4f\t%10d\t%10.3g\t%12.2f\t%12.2f\t%10.4f\t%10d\t%10.3g%n",
          u,
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
