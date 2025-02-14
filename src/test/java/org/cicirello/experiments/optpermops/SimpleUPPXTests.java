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

/** JUnit tests for SimpleUPPX. */
public class SimpleUPPXTests {
  @Test
  public void testUPPXIdentical() {
    SimpleUPPX ppx = new SimpleUPPX(0.5);
    for (int n = 1; n <= 32; n *= 2) {
      Permutation p1 = new Permutation(n);
      Permutation p2 = new Permutation(p1);
      Permutation child1 = new Permutation(p1);
      Permutation child2 = new Permutation(p2);
      ppx.cross(child1, child2);
      assertEquals(p1, child1);
      assertEquals(p2, child2);
    }
    SimpleUPPX s = ppx.split();
    assertNotSame(ppx, s);
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
  public void testUPPXRandom() {
    SimpleUPPX ppx = new SimpleUPPX(0.5);
    for (int n = 1; n <= 32; n *= 2) {
      Permutation p1 = new Permutation(n);
      Permutation p2 = new Permutation(n);
      Permutation child1 = new Permutation(p1);
      Permutation child2 = new Permutation(p2);
      ppx.cross(child1, child2);
      assertTrue(validPermutation(child1));
      assertTrue(validPermutation(child2));
    }
    SimpleUPPX s = ppx.split();
    assertNotSame(ppx, s);
    for (int n = 1; n <= 32; n *= 2) {
      Permutation p1 = new Permutation(n);
      Permutation p2 = new Permutation(n);
      Permutation child1 = new Permutation(p1);
      Permutation child2 = new Permutation(p2);
      s.cross(child1, child2);
      assertTrue(validPermutation(child1));
      assertTrue(validPermutation(child2));
    }

    ppx = new SimpleUPPX(0.25);
    for (int n = 1; n <= 32; n *= 2) {
      Permutation p1 = new Permutation(n);
      Permutation p2 = new Permutation(n);
      Permutation child1 = new Permutation(p1);
      Permutation child2 = new Permutation(p2);
      ppx.cross(child1, child2);
      assertTrue(validPermutation(child1));
      assertTrue(validPermutation(child2));
    }
    s = ppx.split();
    assertNotSame(ppx, s);
    for (int n = 1; n <= 32; n *= 2) {
      Permutation p1 = new Permutation(n);
      Permutation p2 = new Permutation(n);
      Permutation child1 = new Permutation(p1);
      Permutation child2 = new Permutation(p2);
      s.cross(child1, child2);
      assertTrue(validPermutation(child1));
      assertTrue(validPermutation(child2));
    }

    ppx = new SimpleUPPX(0.75);
    for (int n = 1; n <= 32; n *= 2) {
      Permutation p1 = new Permutation(n);
      Permutation p2 = new Permutation(n);
      Permutation child1 = new Permutation(p1);
      Permutation child2 = new Permutation(p2);
      ppx.cross(child1, child2);
      assertTrue(validPermutation(child1));
      assertTrue(validPermutation(child2));
    }
    s = ppx.split();
    assertNotSame(ppx, s);
    for (int n = 1; n <= 32; n *= 2) {
      Permutation p1 = new Permutation(n);
      Permutation p2 = new Permutation(n);
      Permutation child1 = new Permutation(p1);
      Permutation child2 = new Permutation(p2);
      s.cross(child1, child2);
      assertTrue(validPermutation(child1));
      assertTrue(validPermutation(child2));
    }
  }

  @Test
  public void testExceptionsUPPX() {
    IllegalArgumentException thrown =
        assertThrows(IllegalArgumentException.class, () -> new SimpleUPPX(0.0));
    thrown = assertThrows(IllegalArgumentException.class, () -> new SimpleUPPX(1.0));
  }

  @Test
  public void testInternalCrossUPPX() {
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

    SimpleUPPX ppx = new SimpleUPPX(0.5);
    int[] p1 = {7, 6, 5, 4, 3, 2, 1, 0};
    int[] p2 = {0, 1, 2, 3, 4, 5, 6, 7};
    int[] expected1 = {0, 7, 1, 2, 3, 6, 4, 5};
    int[] expected2 = {7, 0, 6, 5, 4, 1, 3, 2};
    boolean[] mask = {false, true, false, false, false, true, false, true};
    ppx.internalCross(p1, p2, new EnhancedSplittableGenerator(new ControlledDoubleSequence(mask)));
    assertArrayEquals(expected1, p1);
    assertArrayEquals(expected2, p2);
  }

  @Test
  public void testUPPXNear0U() {
    SimpleUPPX ppx = new SimpleUPPX(Math.ulp(0.0));
    for (int n = 1; n <= 32; n *= 2) {
      Permutation p1 = new Permutation(n);
      Permutation p2 = new Permutation(n);
      Permutation parent1 = new Permutation(p1);
      Permutation parent2 = new Permutation(p2);
      ppx.cross(parent1, parent2);
      // the near 0 u should essentially swap the parents
      // other than a low probability statistical anomaly
      assertEquals(p2, parent1);
      assertEquals(p1, parent2);
    }
    SimpleUPPX s = ppx.split();
    assertNotSame(ppx, s);
    for (int n = 1; n <= 32; n *= 2) {
      Permutation p1 = new Permutation(n);
      Permutation p2 = new Permutation(n);
      Permutation parent1 = new Permutation(p1);
      Permutation parent2 = new Permutation(p2);
      s.cross(parent1, parent2);
      // the near 0 u should essentially swap the parents
      // other than a low probability statistical anomaly
      assertEquals(p2, parent1);
      assertEquals(p1, parent2);
    }
  }

  @Test
  public void testUPPXNear1U() {
    SimpleUPPX ppx = new SimpleUPPX(1.0 - Math.ulp(1.0));
    for (int n = 1; n <= 32; n *= 2) {
      Permutation p1 = new Permutation(n);
      Permutation p2 = new Permutation(n);
      Permutation parent1 = new Permutation(p1);
      Permutation parent2 = new Permutation(p2);
      ppx.cross(parent1, parent2);
      // the near 1.0 u should essentially keep all of the parents
      // other than a low probability statistical anomaly
      assertEquals(p1, parent1);
      assertEquals(p2, parent2);
    }
    SimpleUPPX s = ppx.split();
    assertNotSame(ppx, s);
    for (int n = 1; n <= 32; n *= 2) {
      Permutation p1 = new Permutation(n);
      Permutation p2 = new Permutation(n);
      Permutation parent1 = new Permutation(p1);
      Permutation parent2 = new Permutation(p2);
      s.cross(parent1, parent2);
      // the near 1.0 u should essentially keep all of the parents
      // other than a low probability statistical anomaly
      assertEquals(p1, parent1);
      assertEquals(p2, parent2);
    }
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
