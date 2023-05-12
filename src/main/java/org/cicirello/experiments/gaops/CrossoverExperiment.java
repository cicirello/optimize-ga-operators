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
import org.cicirello.math.stats.Statistics;
import org.cicirello.search.operators.CrossoverOperator;
import org.cicirello.search.operators.bits.UniformCrossover;
import org.cicirello.search.representations.BitVector;
import org.cicirello.util.DoubleList;

/**
 * Experiment comparing CPU time of two alternative uniform mutation implementations.
 *
 * @author <a href=https://www.cicirello.org/ target=_top>Vincent A. Cicirello</a>, <a
 *     href=https://www.cicirello.org/ target=_top>https://www.cicirello.org/</a>
 */
public class CrossoverExperiment {

  /** Number of trials to average. */
  private static final int TRIALS = 100;

  /** Number of samples for each trial. Need multiple to ensure times are measurable. */
  private static final int SAMPLES_PER_TRIAL = 100000;

  /**
   * Crosses 2 BitVectors SAMPLES_PER_TRIAL times.
   *
   * @param crossover the crossover operator
   * @param v1 a BitVector
   * @param v2 another BitVector
   * @return a meaningless value dependent upon result of all crossovers for purpose of preventing
   *     JIT from optimizing away the computation
   */
  public static int crossoverCodeToTime(
      CrossoverOperator<BitVector> crossover, BitVector v1, BitVector v2) {
    int useToPreventOptimizingAway = 0;
    int length32 = (v1.length() + 31) >> 5;
    for (int i = 0; i < SAMPLES_PER_TRIAL; i++) {
      crossover.cross(v1, v2);
      int j = i % length32;
      useToPreventOptimizingAway += v1.get32(j) + v2.get32(j);
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
    for (double u = 0.1; u < 0.55; u += 0.1) {
      SimpleUniformCrossover simple = new SimpleUniformCrossover(u);
      UniformCrossover optimized = new UniformCrossover(u);
      BitVector v1 = new BitVector(1024, true);
      BitVector v2 = new BitVector(1024, true);
      useToPreventOptimizingAway += crossoverCodeToTime(simple, v1, v2);
      useToPreventOptimizingAway += crossoverCodeToTime(optimized, v1, v2);
    }
    System.out.println("End Warmup Phase");
    System.out.println();

    for (int bitLength = 16; bitLength <= 1024; bitLength *= 2) {
      System.out.printf(
          "%4s\t%2s\t%12s\t%12s\t%11s\t%10s\t%10s\t%10s\n",
          "n", "u", "simple", "optimized", "%less-time", "t", "dof", "p");
      DoubleList valuesOfU = new DoubleList();
      for (double u = 0.1; u < 0.55; u += 0.1) {
        valuesOfU.add(u);
      }
      for (int i = 0; i < valuesOfU.size(); i++) {
        double u = valuesOfU.get(i);
        SimpleUniformCrossover simple = new SimpleUniformCrossover(u);
        UniformCrossover optimized = new UniformCrossover(u);
        BitVector bits1 = new BitVector(bitLength, true);
        BitVector bits2 = new BitVector(bitLength, true);
        double[][] ms = new double[2][TRIALS];
        for (int j = 0; j < TRIALS; j++) {
          long start = bean.getCurrentThreadCpuTime();
          useToPreventOptimizingAway += crossoverCodeToTime(simple, bits1, bits2);
          long middle = bean.getCurrentThreadCpuTime();
          useToPreventOptimizingAway += crossoverCodeToTime(optimized, bits1, bits2);
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
            "%4d\t%2.1f\t%12.3g\t%12.3g\t%10.2f%%\t%10.4f\t%10d\t%10.3g\n",
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

  private static class SimpleUniformCrossover implements CrossoverOperator<BitVector> {

    private final double p;

    /**
     * Constructs a uniform crossover operator.
     *
     * @param p The per-bit probability of exchanging each bit between the parents in forming the
     *     children. The expected number of bits exchanged during a single call to {@link #cross} is
     *     thus p*N, where N is the length of the BitVector.
     */
    public SimpleUniformCrossover(double p) {
      this.p = p <= 0.0 ? 0.0 : (p >= 1.0 ? 1.0 : p);
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if c1.length() is not equal to c2.length()
     */
    @Override
    public void cross(BitVector c1, BitVector c2) {
      int n = c1.length();
      BitVector mask = new BitVector(n);
      for (int index = 0; index < n; index++) {
        if (ThreadLocalRandom.current().nextDouble() < p) {
          mask.flip(index);
        }
      }
      BitVector.exchangeBits(c1, c2, mask);
    }

    @Override
    public SimpleUniformCrossover split() {
      // Maintains no mutable state, so just return this.
      return this;
    }
  }
}
