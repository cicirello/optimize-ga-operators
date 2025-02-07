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

/** JUnit tests for SimpleOX2. */
public class SimpleOX2Tests extends SharedTestCodeOrderingCrossovers {

  // Insert @Test here to activate during testing to visually inspect cross results
  public void visuallyInspectCrossResult() {
    visualInspection(3, new SimpleOX2(0.5));
  }

  @Test
  public void testOX2IdenticalParents() {
    SimpleOX2 ox2 = new SimpleOX2(0.5);
    for (int n = 1; n <= 32; n *= 2) {
      Permutation p1 = new Permutation(n);
      Permutation p2 = new Permutation(p1);
      Permutation parent1 = new Permutation(p1);
      Permutation parent2 = new Permutation(p2);
      ox2.cross(parent1, parent2);
      assertEquals(p1, parent1);
      assertEquals(p2, parent2);
    }
    SimpleOX2 s = ox2.split();
    assertNotSame(ox2, s);
    for (int n = 1; n <= 32; n *= 2) {
      Permutation p1 = new Permutation(n);
      Permutation p2 = new Permutation(p1);
      Permutation parent1 = new Permutation(p1);
      Permutation parent2 = new Permutation(p2);
      s.cross(parent1, parent2);
      assertEquals(p1, parent1);
      assertEquals(p2, parent2);
    }
  }

  @Test
  public void testOX2Near0U() {
    SimpleOX2 ox2 = new SimpleOX2(Math.ulp(0.0));
    for (int n = 1; n <= 32; n *= 2) {
      Permutation p1 = new Permutation(n);
      Permutation p2 = new Permutation(n);
      Permutation parent1 = new Permutation(p1);
      Permutation parent2 = new Permutation(p2);
      ox2.cross(parent1, parent2);
      // the near 0 u should essentially keep all of the parents
      // other than a low probability statistical anomaly
      assertEquals(p1, parent1);
      assertEquals(p2, parent2);
    }
    SimpleOX2 s = ox2.split();
    assertNotSame(ox2, s);
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
  public void testOX2Near1U() {
    SimpleOX2 ox2 = new SimpleOX2(1.0 - Math.ulp(1.0));
    for (int n = 1; n <= 32; n *= 2) {
      Permutation p1 = new Permutation(n);
      Permutation p2 = new Permutation(n);
      Permutation parent1 = new Permutation(p1);
      Permutation parent2 = new Permutation(p2);
      ox2.cross(parent1, parent2);
      // the near 1.0 u should essentially swap the parents
      // other than a low probability statistical anomaly
      assertEquals(p2, parent1);
      assertEquals(p1, parent2);
    }
    SimpleOX2 s = ox2.split();
    assertNotSame(ox2, s);
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
  public void testOX2Validity() {
    // Validates children as valid permutations only.
    // Does not validate behavior of the OX2.

    SimpleOX2 ox2 = new SimpleOX2(0.5);
    for (int n = 1; n <= 32; n *= 2) {
      Permutation p1 = new Permutation(n);
      Permutation p2 = new Permutation(n);
      Permutation parent1 = new Permutation(p1);
      Permutation parent2 = new Permutation(p2);
      ox2.cross(parent1, parent2);
      assertTrue(validPermutation(parent1));
      assertTrue(validPermutation(parent2));
    }

    ox2 = new SimpleOX2(0.25);
    for (int n = 1; n <= 32; n *= 2) {
      Permutation p1 = new Permutation(n);
      Permutation p2 = new Permutation(n);
      Permutation parent1 = new Permutation(p1);
      Permutation parent2 = new Permutation(p2);
      ox2.cross(parent1, parent2);
      assertTrue(validPermutation(parent1));
      assertTrue(validPermutation(parent2));
    }

    ox2 = new SimpleOX2(0.75);
    for (int n = 1; n <= 32; n *= 2) {
      Permutation p1 = new Permutation(n);
      Permutation p2 = new Permutation(n);
      Permutation parent1 = new Permutation(p1);
      Permutation parent2 = new Permutation(p2);
      ox2.cross(parent1, parent2);
      assertTrue(validPermutation(parent1));
      assertTrue(validPermutation(parent2));
    }
  }

  @Test
  public void testOX2ValiditySplit() {
    // Validates children as valid permutations only.
    // Does not validate behavior of the OX2.

    SimpleOX2 original = new SimpleOX2(0.5);
    SimpleOX2 ox2 = original.split();

    for (int n = 1; n <= 32; n *= 2) {
      Permutation p1 = new Permutation(n);
      Permutation p2 = new Permutation(n);
      Permutation parent1 = new Permutation(p1);
      Permutation parent2 = new Permutation(p2);
      ox2.cross(parent1, parent2);
      assertTrue(validPermutation(parent1));
      assertTrue(validPermutation(parent2));
    }

    ox2 = new SimpleOX2(0.25);
    for (int n = 1; n <= 32; n *= 2) {
      Permutation p1 = new Permutation(n);
      Permutation p2 = new Permutation(n);
      Permutation parent1 = new Permutation(p1);
      Permutation parent2 = new Permutation(p2);
      ox2.cross(parent1, parent2);
      assertTrue(validPermutation(parent1));
      assertTrue(validPermutation(parent2));
    }

    ox2 = new SimpleOX2(0.75);
    for (int n = 1; n <= 32; n *= 2) {
      Permutation p1 = new Permutation(n);
      Permutation p2 = new Permutation(n);
      Permutation parent1 = new Permutation(p1);
      Permutation parent2 = new Permutation(p2);
      ox2.cross(parent1, parent2);
      assertTrue(validPermutation(parent1));
      assertTrue(validPermutation(parent2));
    }
  }

  @Test
  public void testInternalCrossOX2() {
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

    SimpleOX2 ox2 = new SimpleOX2(0.5);
    {
      Permutation c1 = new Permutation(new int[] {1, 0, 3, 2, 5, 4, 7, 6});
      Permutation c2 = new Permutation(new int[] {6, 7, 4, 5, 2, 3, 0, 1});
      boolean[] mask = {false, true, false, true, false, true, false, true};
      Permutation expected1 = new Permutation(new int[] {7, 0, 5, 2, 3, 4, 1, 6});
      Permutation expected2 = new Permutation(new int[] {0, 7, 2, 5, 4, 3, 6, 1});
      final int[][] raw = new int[2][];
      c1.apply(
          (r1, r2) -> {
            raw[0] = r1;
            raw[1] = r2;
          },
          c2);
      ox2.internalCross(
          raw[0],
          raw[1],
          c1,
          c2,
          new EnhancedSplittableGenerator(new ControlledDoubleSequence(mask)));
      assertEquals(expected1, c1);
      assertEquals(expected2, c2);
    }
    {
      Permutation c1 = new Permutation(new int[] {1, 0, 3, 2, 5, 4, 7, 6});
      Permutation c2 = new Permutation(new int[] {6, 7, 4, 5, 2, 3, 0, 1});
      boolean[] mask = {true, false, true, false, true, false, true, false};
      Permutation expected1 = new Permutation(new int[] {1, 6, 3, 4, 5, 2, 7, 0});
      Permutation expected2 = new Permutation(new int[] {6, 1, 4, 3, 2, 5, 0, 7});
      final int[][] raw = new int[2][];
      c1.apply(
          (r1, r2) -> {
            raw[0] = r1;
            raw[1] = r2;
          },
          c2);
      ox2.internalCross(
          raw[0],
          raw[1],
          c1,
          c2,
          new EnhancedSplittableGenerator(new ControlledDoubleSequence(mask)));
      assertEquals(expected1, c1);
      assertEquals(expected2, c2);
    }
    {
      Permutation c1 = new Permutation(new int[] {1, 0, 3, 2, 5, 4, 7, 6});
      Permutation c2 = new Permutation(new int[] {6, 7, 4, 5, 2, 3, 0, 1});
      boolean[] mask = {false, true, true, false, false, false, true, true};
      Permutation expected1 = new Permutation(new int[] {7, 4, 3, 2, 5, 0, 1, 6});
      Permutation expected2 = new Permutation(new int[] {0, 3, 4, 5, 2, 7, 6, 1});
      final int[][] raw = new int[2][];
      c1.apply(
          (r1, r2) -> {
            raw[0] = r1;
            raw[1] = r2;
          },
          c2);
      ox2.internalCross(
          raw[0],
          raw[1],
          c1,
          c2,
          new EnhancedSplittableGenerator(new ControlledDoubleSequence(mask)));
      assertEquals(expected1, c1);
      assertEquals(expected2, c2);
    }
  }

  @Test
  public void testExceptionsOX2() {
    IllegalArgumentException thrown =
        assertThrows(IllegalArgumentException.class, () -> new SimpleOX2(0.0));
    thrown = assertThrows(IllegalArgumentException.class, () -> new SimpleOX2(1.0));
  }
}
