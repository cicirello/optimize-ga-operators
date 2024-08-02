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
import java.util.concurrent.ThreadLocalRandom;
import org.cicirello.math.stats.Statistics;
import org.cicirello.search.operators.MutationOperator;
import org.cicirello.search.operators.bits.BitFlipMutation;
import org.cicirello.search.representations.BitVector;
import org.cicirello.util.DoubleList;

/**
 * Experiment comparing CPU time of two alternative bit-flip mutation implementations.
 *
 * @author <a href=https://www.cicirello.org/ target=_top>Vincent A. Cicirello</a>, <a
 *     href=https://www.cicirello.org/ target=_top>https://www.cicirello.org/</a>
 */
public class MutationExperiment {

  /** Number of trials to average. */
  private static final int TRIALS = 100;

  /** Number of samples for each trial. Need multiple to ensure times are measurable. */
  private static final int SAMPLES_PER_TRIAL = 100000;

  /**
   * Mutates a BitVector SAMPLES_PER_TRIAL times.
   *
   * @param mutation the mutation operator
   * @param mutateMe the BitVector to mutate
   * @return a meaningless value dependent upon result of all mutations for purpose of preventing
   *     JIT from optimizing away the computation
   */
  public static int mutationCodeToTime(MutationOperator<BitVector> mutation, BitVector mutateMe) {
    int useToPreventOptimizingAway = 0;
    int length32 = (mutateMe.length() + 31) >> 5;
    for (int i = 0; i < SAMPLES_PER_TRIAL; i++) {
      mutation.mutate(mutateMe);
      useToPreventOptimizingAway += mutateMe.get32(i % length32);
    }
    return useToPreventOptimizingAway;
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
    for (double m = 1.0 / 1024; m - 0.25 <= 1E-10; m *= 2) {
      SimpleBitFlipMutation simple = new SimpleBitFlipMutation(m);
      BitFlipMutation optimized = new BitFlipMutation(m);
      BitVector v = new BitVector(1024);
      useToPreventOptimizingAway += mutationCodeToTime(simple, v);
      useToPreventOptimizingAway += mutationCodeToTime(optimized, v);
    }
    System.out.println("End Warmup Phase");
    System.out.println();

    for (int bitLength = 16; bitLength <= 1024; bitLength *= 2) {
      System.out.printf(
          "%4s\t%12s\t%12s\t%12s\t%11s\t%10s\t%10s\t%10s%n",
          "n", "u", "simple", "optimized", "%less-time", "t", "dof", "p");
      DoubleList valuesOfM = new DoubleList();
      for (double m = 1.0 / bitLength; m - 0.25 <= 1E-10; m *= 2) {
        valuesOfM.add(m);
      }
      for (int i = 0; i < valuesOfM.size(); i++) {
        double m = valuesOfM.get(i);
        SimpleBitFlipMutation simple = new SimpleBitFlipMutation(m);
        BitFlipMutation optimized = new BitFlipMutation(m);
        BitVector bits = new BitVector(bitLength);
        double[][] ms = new double[2][TRIALS];
        for (int j = 0; j < TRIALS; j++) {
          long start = bean.getCurrentThreadCpuTime();
          useToPreventOptimizingAway += mutationCodeToTime(simple, bits);
          long middle = bean.getCurrentThreadCpuTime();
          useToPreventOptimizingAway += mutationCodeToTime(optimized, bits);
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
            "%4d\t%11.10f\t%12.3g\t%12.3g\t%10.2f%%\t%10.4f\t%10d\t%10.3g%n",
            bitLength, m, timeSimpleSeconds, timeOptimizedSeconds, percentLessTime, t, dof, p);
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

  /** The commonly found implementation of bit-flip mutation. */
  public static final class SimpleBitFlipMutation implements MutationOperator<BitVector> {

    private final double m;

    /**
     * Constructs a SimpleBitFlipMutation operator with a specified mutation rate.
     *
     * @param m The mutation rate, which is the probability of flipping any individual bit. The
     *     expected number of bits flipped during a call to the {@link #mutate} method is m*N where
     *     N is the length of the mutated BitVector. There is no guarantee that any bits will be
     *     flipped during a mutation (e.g., if m is close to 0).
     * @throws IllegalArgumentException if m &le; 0 or if m &ge; 1.
     */
    public SimpleBitFlipMutation(double m) {
      if (m <= 0 || m >= 1) throw new IllegalArgumentException("m constrained by: 0.0 < m < 1.0");
      this.m = m;
    }

    /*
     * internal copy constructor
     */
    private SimpleBitFlipMutation(SimpleBitFlipMutation other) {
      m = other.m;
      // deliberately don't copy bitMask (each instance needs to maintain its own for undo)
    }

    @Override
    public void mutate(BitVector c) {
      int numBits = c.length();
      for (int index = 0; index < numBits; index++) {
        if (ThreadLocalRandom.current().nextDouble() < m) {
          c.flip(index);
        }
      }
    }

    @Override
    public SimpleBitFlipMutation split() {
      return new SimpleBitFlipMutation(this);
    }
  }
}
