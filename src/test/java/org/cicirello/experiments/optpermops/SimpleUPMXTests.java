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

import static org.junit.jupiter.api.Assertions.*;

import java.util.random.RandomGenerator;
import java.util.stream.Stream;
import org.cicirello.math.rand.EnhancedSplittableGenerator;
import org.cicirello.permutations.Permutation;
import org.junit.jupiter.api.*;

/** JUnit tests for SimpleUPMX. */
public class SimpleUPMXTests {

  // Insert @Test here to activate during testing to visually inspect cross results
  public void visuallyInspectCrossResult() {
    int reps = 3;

    SimpleUPMX upmx = new SimpleUPMX(0.33);
    for (int i = 0; i < reps; i++) {
      Permutation p1 = new Permutation(10);
      Permutation p2 = new Permutation(10);

      Permutation child1 = new Permutation(p1);
      Permutation child2 = new Permutation(p2);
      upmx.cross(child1, child2);
      System.out.println("UPMX Result");
      System.out.println("Parent 1: " + p1);
      System.out.println("Parent 2: " + p2);
      System.out.println("Child 1 : " + child1);
      System.out.println("Child 2 : " + child2);
      System.out.println();
    }
  }

  @Test
  public void testUPMXIdentical() {
    SimpleUPMX upmx = new SimpleUPMX(0.33);
    for (int n = 1; n <= 32; n *= 2) {
      Permutation p1 = new Permutation(n);
      Permutation p2 = new Permutation(p1);
      Permutation child1 = new Permutation(p1);
      Permutation child2 = new Permutation(p2);
      upmx.cross(child1, child2);
      assertEquals(p1, child1);
      assertEquals(p2, child2);
    }
    SimpleUPMX s = upmx.split();
    assertNotSame(upmx, s);
    for (int n = 1; n <= 32; n *= 2) {
      Permutation p1 = new Permutation(n);
      Permutation p2 = new Permutation(p1);
      Permutation child1 = new Permutation(p1);
      Permutation child2 = new Permutation(p2);
      s.cross(child1, child2);
      assertEquals(p1, child1);
      assertEquals(p2, child2);
    }
  }

  @Test
  public void testUPMXNear0U() {
    SimpleUPMX upmx = new SimpleUPMX(Math.ulp(0.0));
    for (int n = 1; n <= 32; n *= 2) {
      Permutation p1 = new Permutation(n);
      Permutation p2 = new Permutation(n);
      Permutation parent1 = new Permutation(p1);
      Permutation parent2 = new Permutation(p2);
      upmx.cross(parent1, parent2);
      // the near 0 u should essentially keep all of the parents
      // other than a low probability statistical anomaly
      assertEquals(p1, parent1);
      assertEquals(p2, parent2);
    }
    SimpleUPMX s = upmx.split();
    assertNotSame(upmx, s);
    for (int n = 1; n <= 32; n *= 2) {
      Permutation p1 = new Permutation(n);
      Permutation p2 = new Permutation(n);
      Permutation parent1 = new Permutation(p1);
      Permutation parent2 = new Permutation(p2);
      s.cross(parent1, parent2);
      // the near 0 u should essentially keep all of the parents
      // other than a low probability statistical anomaly
      assertEquals(p1, parent1);
      assertEquals(p2, parent2);
    }
  }

  @Test
  public void testUPMXNear1U() {
    SimpleUPMX upmx = new SimpleUPMX(1.0 - Math.ulp(1.0));
    for (int n = 1; n <= 32; n *= 2) {
      Permutation p1 = new Permutation(n);
      Permutation p2 = new Permutation(n);
      Permutation parent1 = new Permutation(p1);
      Permutation parent2 = new Permutation(p2);
      upmx.cross(parent1, parent2);
      // the near 1.0 u should essentially swap the parents
      // other than a low probability statistical anomaly
      assertEquals(p2, parent1);
      assertEquals(p1, parent2);
    }
    SimpleUPMX s = upmx.split();
    assertNotSame(upmx, s);
    for (int n = 1; n <= 32; n *= 2) {
      Permutation p1 = new Permutation(n);
      Permutation p2 = new Permutation(n);
      Permutation parent1 = new Permutation(p1);
      Permutation parent2 = new Permutation(p2);
      s.cross(parent1, parent2);
      // the near 1.0 u should essentially swap the parents
      // other than a low probability statistical anomaly
      assertEquals(p2, parent1);
      assertEquals(p1, parent2);
    }
  }

  @Test
  public void testUPMXInternalCross() {
    class ControlledDoubleSequence implements RandomGenerator.SplittableGenerator {
      private final boolean[] mask;
      private int next;

      public ControlledDoubleSequence(boolean[] mask) {
        this.mask = mask;
        next = 0;
      }

      @Override
      public long nextLong() {
        // doesn't matter... not going to be used
        return 0;
      }

      @Override
      public double nextDouble() {
        double d = mask[next] ? 0 : 1;
        next++;
        return d;
      }

      @Override
      public Stream<RandomGenerator.SplittableGenerator> splits(
          long streamSize, RandomGenerator.SplittableGenerator source) {
        // Tests don't use this, so doesn't matter what this returns.
        return null;
      }

      @Override
      public Stream<RandomGenerator.SplittableGenerator> splits(
          RandomGenerator.SplittableGenerator source) {
        // Tests don't use this, so doesn't matter what this returns.
        return null;
      }

      @Override
      public Stream<RandomGenerator.SplittableGenerator> splits(long streamSize) {
        // Tests don't use this, so doesn't matter what this returns.
        return null;
      }

      @Override
      public RandomGenerator.SplittableGenerator split(RandomGenerator.SplittableGenerator source) {
        // Tests don't use this, so doesn't matter what this returns.
        return this;
      }

      @Override
      public RandomGenerator.SplittableGenerator split() {
        // Tests don't use this, so doesn't matter what this returns.
        return this;
      }
    }

    SimpleUPMX upmx = new SimpleUPMX(0.33);
    Permutation p1 = new Permutation(new int[] {7, 6, 5, 4, 3, 2, 1, 0});
    Permutation p2 = new Permutation(new int[] {1, 2, 0, 5, 6, 4, 7, 3});
    boolean[][] mask = { // int[][] indexes = {
      {false, false, false, true, false, false, false, false}, // {3}, // 4, 5
      {false, true, false, true, false, false, false, false}, // {3, 1}, // 6, 2
      {false, true, false, true, false, false, true, false}, // {3, 1, 6}, // 1, 7
      {true, true, false, true, false, false, true, false}, // {3, 1, 6, 0}, // 7, 1
      {true, true, true, true, false, false, true, false}, // {3, 1, 6, 0, 2}, // 5, 0
      {true, true, true, true, false, true, true, false}, // {3, 1, 6, 0, 2, 5}, // 2, 4
      {true, true, true, true, true, true, true, false}, // {3, 1, 6, 0, 2, 5, 4}, // 3, 6
      {true, true, true, true, true, true, true, true} // {3, 1, 6, 0, 2, 5, 4, 7} // 0, 3
    };
    Permutation[][] expected = {
      {
        new Permutation(new int[] {7, 6, 4, 5, 3, 2, 1, 0}),
        new Permutation(new int[] {1, 2, 0, 4, 6, 5, 7, 3})
      },
      {
        new Permutation(new int[] {7, 2, 4, 5, 3, 6, 1, 0}),
        new Permutation(new int[] {1, 6, 0, 4, 2, 5, 7, 3})
      },
      {
        new Permutation(new int[] {1, 2, 4, 5, 3, 6, 7, 0}),
        new Permutation(new int[] {7, 6, 0, 4, 2, 5, 1, 3})
      },
      {
        new Permutation(new int[] {1, 2, 4, 5, 3, 6, 7, 0}),
        new Permutation(new int[] {7, 6, 0, 4, 2, 5, 1, 3})
      },
      {
        new Permutation(new int[] {1, 2, 0, 5, 3, 6, 7, 4}),
        new Permutation(new int[] {7, 6, 5, 4, 2, 0, 1, 3})
      },
      {
        new Permutation(new int[] {1, 2, 0, 5, 3, 4, 7, 6}),
        new Permutation(new int[] {7, 6, 5, 4, 0, 2, 1, 3})
      },
      {
        new Permutation(new int[] {1, 2, 0, 5, 6, 4, 7, 3}),
        new Permutation(new int[] {7, 6, 5, 4, 3, 2, 1, 0})
      },
      {
        new Permutation(new int[] {1, 2, 0, 5, 6, 4, 7, 3}),
        new Permutation(new int[] {7, 6, 5, 4, 3, 2, 1, 0})
      }
    };
    final int[][] wrapper = new int[2][];
    for (int k = 0; k < mask.length; k++) {
      Permutation child1 = new Permutation(p1);
      Permutation child2 = new Permutation(p2);
      child1.apply(raw -> wrapper[0] = raw);
      child2.apply(raw -> wrapper[1] = raw);
      upmx.internalCross(
          wrapper[0],
          wrapper[1],
          child1,
          child2,
          new EnhancedSplittableGenerator(new ControlledDoubleSequence(mask[k])));
      assertEquals(expected[k][0], child1);
      assertEquals(expected[k][1], child2);
    }
  }

  @Test
  public void testUPMXValidPermutations() {
    SimpleUPMX upmx = new SimpleUPMX(0.33);
    for (int n = 1; n <= 32; n *= 2) {
      Permutation p1 = new Permutation(n);
      Permutation p2 = new Permutation(n);
      Permutation parent1 = new Permutation(p1);
      Permutation parent2 = new Permutation(p2);
      upmx.cross(parent1, parent2);
      assertTrue(validPermutation(parent1));
      assertTrue(validPermutation(parent2));
    }

    upmx = new SimpleUPMX(0.5);
    for (int n = 1; n <= 32; n *= 2) {
      Permutation p1 = new Permutation(n);
      Permutation p2 = new Permutation(n);
      Permutation parent1 = new Permutation(p1);
      Permutation parent2 = new Permutation(p2);
      upmx.cross(parent1, parent2);
      assertTrue(validPermutation(parent1));
      assertTrue(validPermutation(parent2));
    }
  }

  @Test
  public void testExceptionsUPMX() {
    IllegalArgumentException thrown =
        assertThrows(IllegalArgumentException.class, () -> new SimpleUPMX(0.0));
    thrown = assertThrows(IllegalArgumentException.class, () -> new SimpleUPMX(1.0));
  }

  private boolean validPermutation(Permutation p) {
    boolean[] foundIt = new boolean[p.length()];
    for (int i = 0; i < p.length(); i++) {
      if (foundIt[p.get(i)]) return false;
      foundIt[p.get(i)] = true;
    }
    return true;
  }
}
