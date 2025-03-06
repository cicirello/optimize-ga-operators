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
import org.cicirello.math.rand.EnhancedRandomGenerator;
import org.cicirello.math.stats.Statistics;
import org.cicirello.permutations.Permutation;
import org.cicirello.search.operators.MutationOperator;
import org.cicirello.search.operators.permutations.UniformScrambleMutation;

/**
 * Experiment comparing CPU time of two alternative implementations of uniform scramble mutation.
 *
 * @author <a href=https://www.cicirello.org/ target=_top>Vincent A. Cicirello</a>, <a
 *     href=https://www.cicirello.org/ target=_top>https://www.cicirello.org/</a>
 */
public class ExperimentUniformScrambleMutation {

  /** Number of trials to average. */
  private static final int TRIALS = 100;

  /** Number of samples for each trial. Need multiple to ensure times are measurable. */
  private static final int SAMPLES_PER_TRIAL = 100000;

  /**
   * Mutates one Permutation SAMPLES_PER_TRIAL times.
   *
   * @param mutation the mutation operator
   * @param p a Permutation
   */
  public static void mutationCodeToTime(MutationOperator<Permutation> mutation, Permutation p) {
    for (int i = 0; i < SAMPLES_PER_TRIAL; i++) {
      mutation.mutate(p);
    }
    // Use a blackhole to consume the permutation to prevent
    // just-in-time compiler from falsely eliminating the
    // benchmark code as dead code.
    blackhole.consume(p);
  }

  private static final Blackhole blackhole = new Blackhole();

  /**
   * Runs the experiment.
   *
   * @param args no command line arguments
   */
  public static void main(String[] args) {

    ThreadMXBean bean = ManagementFactory.getThreadMXBean();

    // Attempt to "warm-up" Java's JIT compiler.
    System.out.println("Warming up the Java JIT");
    double[] rates = {
      2.0 / 1024,
      4.0 / 1024,
      8.0 / 1024,
      16.0 / 1024,
      32.0 / 1024,
      64.0 / 1024,
      128.0 / 1024,
      256.0 / 1024
    };
    for (double u : rates) {
      SimpleUniformScramble simple = new SimpleUniformScramble(u);
      UniformScrambleMutation optimized = new UniformScrambleMutation(u);
      Permutation p1 = new Permutation(1024);
      Permutation p2 = new Permutation(1024);
      mutationCodeToTime(simple, p1);
      mutationCodeToTime(optimized, p2);

      // Use a blackhole to consume the permutation to prevent
      // just-in-time compiler from falsely eliminating the
      // benchmark code as dead code.
      blackhole.consume(p1);
      blackhole.consume(p2);
    }
    System.out.println("End Warmup Phase");
    System.out.println();

    for (int permutationLength = 128; permutationLength <= 1024; permutationLength *= 8) {
      System.out.printf(
          "%4s\t%10s\t%12s\t%12s\t%11s\t%10s\t%10s\t%10s%n",
          "n", "u", "simple", "optimized", "%less-time", "t", "dof", "p");
      /*DoubleList valuesOfU = new DoubleList();
      for (int uNum = 2; uNum <= permutationLength / 4; uNum *= 2) {
        valuesOfU.add(((double)uNum) / permutationLength);
      }*/

      Permutation p1 = new Permutation(permutationLength);
      Permutation p2 = new Permutation(permutationLength);

      for (double u : rates) {
        if (u - 2.0 / permutationLength < 0.0) {
          continue;
        }
        // double u = valuesOfU.get(i);
        SimpleUniformScramble simple = new SimpleUniformScramble(u);
        UniformScrambleMutation optimized = new UniformScrambleMutation(u);

        double[][] ms = new double[2][TRIALS];
        for (int j = 0; j < TRIALS; j++) {
          long start = bean.getCurrentThreadCpuTime();
          mutationCodeToTime(simple, p1);
          long middle = bean.getCurrentThreadCpuTime();
          mutationCodeToTime(optimized, p2);
          long end = bean.getCurrentThreadCpuTime();
          // compute elapsed times in nanoseconds
          ms[0][j] = (middle - start);
          ms[1][j] = (end - middle);

          // Use a blackhole to consume the permutation to prevent
          // just-in-time compiler from falsely eliminating the
          // benchmark code as dead code.
          blackhole.consume(p1);
          blackhole.consume(p2);
        }
        Number[] tTest = Statistics.tTestWelch(ms[0], ms[1]);
        double t = tTest[0].doubleValue();
        int dof = tTest[1].intValue();
        double p = Statistics.p(t, dof);
        // times are converted to seconds during output
        double timeSimpleSeconds = Statistics.mean(ms[0]) / 1000000000.0;
        double timeOptimizedSeconds = Statistics.mean(ms[1]) / 1000000000.0;
        double percentLessTime =
            100 * ((timeSimpleSeconds - timeOptimizedSeconds) / timeSimpleSeconds);
        System.out.printf(
            "%4d\t%10.9f\t%12.3g\t%12.3g\t%10.2f%%\t%10.4f\t%10d\t%10.3g%n",
            permutationLength,
            u,
            timeSimpleSeconds,
            timeOptimizedSeconds,
            percentLessTime,
            t,
            dof,
            p);
      }
      System.out.println();
    }
    System.out.println("Interpreting Above Results:");
    System.out.println("1) Negative t value implies simple version is faster.");
    System.out.println("2) Positive t value implies optimized version is faster.");
    System.out.println("3) The p column is, well, the p value.");

    System.out.println("\nIGNORE: " + blackhole.box1.o);
  }

  public static final class Blackhole {

    // This is based on Java JMH's blackhole implementation, but
    // simplified and streamlined for this specific case.

    public int tlr;
    public volatile int tlrMask;
    public volatile Box box1;
    public volatile Box box2;

    public Blackhole() {
      EnhancedRandomGenerator r = new EnhancedRandomGenerator(System.nanoTime());
      tlr = r.nextInt();
      tlrMask = 1;
      box1 = new Box();
      box2 = box1;
    }

    public void consume(Object obj) {
      int tlrMask = this.tlrMask; // volatile read
      int tlr = (this.tlr = (this.tlr * 1664525 + 1013904223));
      if ((tlr & tlrMask) == 0) {
        // SHOULD ALMOST NEVER HAPPEN IN MEASUREMENT.
        this.box1.o = obj;
        clearBox();
        this.tlrMask = (tlrMask << 1) + 1;
      }
    }

    private void clearBox() {
      this.box2.o = null;
    }

    protected static class Box {
      public Object o;
    }
  }
}
