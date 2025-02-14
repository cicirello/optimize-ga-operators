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
import org.cicirello.math.rand.EnhancedRandomGenerator;
import org.cicirello.math.stats.Statistics;
import org.cicirello.permutations.Permutation;
import org.cicirello.search.operators.CrossoverOperator;
import org.cicirello.search.operators.permutations.UniformPrecedencePreservativeCrossover;
import org.cicirello.util.DoubleList;

/**
 * Experiment comparing CPU time of two alternative UPPX implementations.
 *
 * @author <a href=https://www.cicirello.org/ target=_top>Vincent A. Cicirello</a>, <a
 *     href=https://www.cicirello.org/ target=_top>https://www.cicirello.org/</a>
 */
public class ExperimentUPPX {

  /** Number of trials to average. */
  private static final int TRIALS = 100;

  /** Number of samples for each trial. Need multiple to ensure times are measurable. */
  private static final int SAMPLES_PER_TRIAL = 10000;

  /**
   * Crosses 2 Permutations SAMPLES_PER_TRIAL times.
   *
   * @param crossover the crossover operator
   * @param v1 a Permutation
   * @param v2 another Permutation
   * @return a meaningless value dependent upon result of all crossovers for purpose of preventing
   *     JIT from optimizing away the computation
   */
  public static void crossoverCodeToTime(
      CrossoverOperator<Permutation> crossover,
      ArrayList<Permutation> perms1,
      ArrayList<Permutation> perms2) {
    for (int i = 0; i < SAMPLES_PER_TRIAL; i++) {
      Permutation p1 = perms1.get(i);
      Permutation p2 = perms2.get(i);
      crossover.cross(p1, p2);

      // Use a blackhole to consume the permutations after the cross
      // to prevent just-in-time compiler from falsely eliminating the
      // benchmark code as dead code.
      blackhole.consume(p1);
      blackhole.consume(p2);
    }
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
    double[] rates = {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};
    for (double u : rates) {
      SimpleUPPX simple = new SimpleUPPX(u);
      UniformPrecedencePreservativeCrossover optimized =
          new UniformPrecedencePreservativeCrossover(u);
      ArrayList<Permutation> perms1 = new ArrayList<Permutation>(SAMPLES_PER_TRIAL);
      ArrayList<Permutation> perms2 = new ArrayList<Permutation>(SAMPLES_PER_TRIAL);
      ArrayList<Permutation> perms1opt = new ArrayList<Permutation>(SAMPLES_PER_TRIAL);
      ArrayList<Permutation> perms2opt = new ArrayList<Permutation>(SAMPLES_PER_TRIAL);
      for (int j = 0; j < SAMPLES_PER_TRIAL; j++) {
        perms1.add(new Permutation(1024));
        perms2.add(new Permutation(1024));
        perms1opt.add(perms1.get(j).copy());
        perms2opt.add(perms2.get(j).copy());
      }
      crossoverCodeToTime(simple, perms1, perms2);
      crossoverCodeToTime(optimized, perms1opt, perms2opt);
    }
    System.out.println("End Warmup Phase");
    System.out.println();

    ArrayList<Permutation> perms1 = new ArrayList<Permutation>(SAMPLES_PER_TRIAL);
    ArrayList<Permutation> perms2 = new ArrayList<Permutation>(SAMPLES_PER_TRIAL);
    ArrayList<Permutation> perms1opt = new ArrayList<Permutation>(SAMPLES_PER_TRIAL);
    ArrayList<Permutation> perms2opt = new ArrayList<Permutation>(SAMPLES_PER_TRIAL);

    for (int permutationLength = 128; permutationLength <= 1024; permutationLength *= 8) {
      System.out.printf(
          "%4s\t%2s\t%12s\t%12s\t%11s\t%10s\t%10s\t%10s%n",
          "n", "u", "simple", "optimized", "%less-time", "t", "dof", "p");
      DoubleList valuesOfU = new DoubleList();
      for (double u : rates) {
        valuesOfU.add(u);
      }

      for (int i = 0; i < valuesOfU.size(); i++) {
        double u = valuesOfU.get(i);
        SimpleUPPX simple = new SimpleUPPX(u);
        UniformPrecedencePreservativeCrossover optimized =
            new UniformPrecedencePreservativeCrossover(u);

        double[][] ms = new double[2][TRIALS];
        for (int j = 0; j < TRIALS; j++) {
          perms1.clear();
          perms2.clear();
          perms1opt.clear();
          perms2opt.clear();
          for (int k = 0; k < SAMPLES_PER_TRIAL; k++) {
            perms1.add(new Permutation(permutationLength));
            perms2.add(new Permutation(permutationLength));
            perms1opt.add(perms1.get(k).copy());
            perms2opt.add(perms2.get(k).copy());
          }
          long start = bean.getCurrentThreadCpuTime();
          crossoverCodeToTime(simple, perms1, perms2);
          long middle = bean.getCurrentThreadCpuTime();
          crossoverCodeToTime(optimized, perms1opt, perms2opt);
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
            "%4d\t%2.1f\t%12.3g\t%12.3g\t%10.2f%%\t%10.4f\t%10d\t%10.3g%n",
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
