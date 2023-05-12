/*
 * Experiments related to optimizing genetic algorithm operators.
 * Copyright (C) 2023 Vincent A. Cicirello
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
import java.util.concurrent.ThreadLocalRandom;
import org.cicirello.math.rand.RandomSampler;
import org.cicirello.math.rand.RandomVariates;
import org.cicirello.math.stats.Statistics;
import org.cicirello.util.DoubleList;

/**
 * Experiment comparing CPU time of two alternatives for generating a random bit mask of a specified
 * length and with a specified probability of a 1-bit.
 *
 * @author <a href=https://www.cicirello.org/ target=_top>Vincent A. Cicirello</a>, <a
 *     href=https://www.cicirello.org/ target=_top>https://www.cicirello.org/</a>
 */
public class BitmaskGenerationExperiment {

  /** Number of trials to average. */
  private static final int TRIALS = 100;

  /** Number of samples for each trial. Need multiple to ensure times are measurable. */
  private static final int SAMPLES_PER_TRIAL = 10000;

  /**
   * Generates a random bit mask.
   *
   * @param n length of the bit mask
   * @param u probability of a 1-bit
   * @return returns the bit mask
   */
  public static int[] bitMaskOptimized(int n, double u) {
    int[] bits = new int[(n + 31) >> 5];
    int[] bitsToSet =
        RandomSampler.sample(
            n, RandomVariates.nextBinomial(n, u), null, ThreadLocalRandom.current());
    for (int index : bitsToSet) {
      int i = index >> 5;
      bits[i] ^= (1 << (index - (i << 5)));
    }
    return bits;
  }

  /**
   * Generates a random bit mask.
   *
   * @param n length of the bit mask
   * @param u probability of a 1-bit
   * @return returns the bit mask
   */
  public static int[] bitMaskSimple(int n, double u) {
    int[] bits = new int[(n + 31) >> 5];
    for (int index = 0; index < n; index++) {
      if (ThreadLocalRandom.current().nextDouble() < u) {
        int i = index >> 5;
        bits[i] ^= (1 << (index - (i << 5)));
      }
    }
    return bits;
  }

  /**
   * Runs the experiment.
   *
   * @param args no command line arguments
   */
  public static void main(String[] args) {

    ThreadMXBean bean = ManagementFactory.getThreadMXBean();
    int useToPreventOptimizingAway = 0;

    // Attempt to "warm-up" Java's JIT compiler.
    System.out.println("Warming up the Java JIT");
    for (double u = 1.0 / 1024; u - 0.5 <= 1E-10; u *= 2) {
      for (int k = 0; k < SAMPLES_PER_TRIAL; k++) {
        int[] maskSimple = bitMaskSimple(1024, u);
        useToPreventOptimizingAway += maskSimple[k % maskSimple.length];
      }
    }
    for (double u = 1.0 / 1024; u - 0.5 <= 1E-10; u *= 2) {
      for (int k = 0; k < SAMPLES_PER_TRIAL; k++) {
        int[] maskOptimized = bitMaskOptimized(1024, u);
        useToPreventOptimizingAway += maskOptimized[k % maskOptimized.length];
      }
    }
    System.out.println("End Warmup Phase");
    System.out.println();

    for (int bitLength = 16; bitLength <= 1024; bitLength *= 2) {
      System.out.printf(
          "%4s\t%11s\t%10s\t%10s\t%11s\t%10s\t%10s\t%10s\n",
          "n", "u", "simple", "optimized", "%less-time", "t", "dof", "p");
      DoubleList valuesOfU = new DoubleList();
      for (double u = 1.0 / bitLength; u - 0.5 <= 1E-10; u *= 2) {
        valuesOfU.add(u);
      }
      valuesOfU.add(0.625);
      valuesOfU.add(0.75);
      valuesOfU.add(0.875);
      for (int i = 0; i < valuesOfU.size(); i++) {
        double u = valuesOfU.get(i);
        double[][] ms = new double[2][TRIALS];
        for (int j = 0; j < TRIALS; j++) {
          long start = bean.getCurrentThreadCpuTime();
          for (int k = 0; k < SAMPLES_PER_TRIAL; k++) {
            int[] maskSimple = bitMaskSimple(bitLength, u);
            useToPreventOptimizingAway += maskSimple[k % maskSimple.length];
          }
          long middle = bean.getCurrentThreadCpuTime();
          for (int k = 0; k < SAMPLES_PER_TRIAL; k++) {
            int[] maskOptimized = bitMaskOptimized(bitLength, u);
            useToPreventOptimizingAway += maskOptimized[k % maskOptimized.length];
          }
          long end = bean.getCurrentThreadCpuTime();
          // compute elapsed times in nanoseconds
          ms[0][j] = (middle - start);
          ms[1][j] = (end - middle);
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
            "%4d\t%10.9f\t%10.7f\t%10.7f\t%10.2f%%\t%10.4f\t%10d\t%10.3g\n",
            bitLength, u, timeSimpleSeconds, timeOptimizedSeconds, percentLessTime, t, dof, p);
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
}
