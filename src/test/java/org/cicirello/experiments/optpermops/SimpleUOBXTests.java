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

import org.cicirello.permutations.Permutation;
import org.junit.jupiter.api.*;

/** JUnit tests for SimpleUOBX. */
public class SimpleUOBXTests extends SharedTestCodeOrderingCrossovers {

  // Insert @Test here to activate during testing to visually inspect cross results
  public void visuallyInspectCrossResult() {
    visualInspection(3, new SimpleUOBX(0.5));
  }

  @Test
  public void testUOBXIdenticalParents() {
    SimpleUOBX uobx = new SimpleUOBX(0.5);
    for (int n = 1; n <= 32; n *= 2) {
      Permutation p1 = new Permutation(n);
      Permutation p2 = new Permutation(p1);
      Permutation parent1 = new Permutation(p1);
      Permutation parent2 = new Permutation(p2);
      uobx.cross(parent1, parent2);
      assertEquals(p1, parent1);
      assertEquals(p2, parent2);
    }
    SimpleUOBX s = uobx.split();
    assertNotSame(uobx, s);
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
  public void testUOBXNear0U() {
    SimpleUOBX uobx = new SimpleUOBX(Math.ulp(0.0));
    for (int n = 1; n <= 32; n *= 2) {
      Permutation p1 = new Permutation(n);
      Permutation p2 = new Permutation(n);
      Permutation parent1 = new Permutation(p1);
      Permutation parent2 = new Permutation(p2);
      uobx.cross(parent1, parent2);
      // the near 0 u should essentially swap the parents
      // other than a low probability statistical anomaly
      assertEquals(p2, parent1);
      assertEquals(p1, parent2);
    }
    SimpleUOBX s = uobx.split();
    assertNotSame(uobx, s);
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
  public void testUOBXNear1U() {
    SimpleUOBX uobx = new SimpleUOBX(1.0 - Math.ulp(1.0));
    for (int n = 1; n <= 32; n *= 2) {
      Permutation p1 = new Permutation(n);
      Permutation p2 = new Permutation(n);
      Permutation parent1 = new Permutation(p1);
      Permutation parent2 = new Permutation(p2);
      uobx.cross(parent1, parent2);
      // the near 1.0 u should essentially keep all of the parents
      // other than a low probability statistical anomaly
      assertEquals(p1, parent1);
      assertEquals(p2, parent2);
    }
    SimpleUOBX s = uobx.split();
    assertNotSame(uobx, s);
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

  @Test
  public void testUOBXTypicalCase() {
    SimpleUOBX uobx = new SimpleUOBX(0.5);
    for (int n = 1; n <= 32; n *= 2) {
      Permutation p1 = new Permutation(n);
      Permutation p2 = new Permutation(n);
      Permutation parent1 = new Permutation(p1);
      Permutation parent2 = new Permutation(p2);
      uobx.cross(parent1, parent2);
      assertTrue(validPermutation(parent1));
      assertTrue(validPermutation(parent2));
      boolean[] fixedPoints = findFixedPoints(parent1, parent2, p1, p2);
      validateOrderingUOBX(parent1, p2, fixedPoints);
      validateOrderingUOBX(parent2, p1, fixedPoints);
    }

    uobx = new SimpleUOBX(0.25);
    for (int n = 8; n <= 32; n *= 2) {
      Permutation p1 = new Permutation(n);
      Permutation p2 = new Permutation(n);
      Permutation parent1 = new Permutation(p1);
      Permutation parent2 = new Permutation(p2);
      uobx.cross(parent1, parent2);
      assertTrue(validPermutation(parent1));
      assertTrue(validPermutation(parent2));
      boolean[] fixedPoints = findFixedPoints(parent1, parent2, p1, p2);
      validateOrderingUOBX(parent1, p2, fixedPoints);
      validateOrderingUOBX(parent2, p1, fixedPoints);
    }

    uobx = new SimpleUOBX(0.75);
    for (int n = 8; n <= 32; n *= 2) {
      Permutation p1 = new Permutation(n);
      Permutation p2 = new Permutation(n);
      Permutation parent1 = new Permutation(p1);
      Permutation parent2 = new Permutation(p2);
      uobx.cross(parent1, parent2);
      assertTrue(validPermutation(parent1));
      assertTrue(validPermutation(parent2));
      boolean[] fixedPoints = findFixedPoints(parent1, parent2, p1, p2);
      validateOrderingUOBX(parent1, p2, fixedPoints);
      validateOrderingUOBX(parent2, p1, fixedPoints);
    }
  }

  @Test
  public void testUOBXTypicalCaseSplit() {
    SimpleUOBX original = new SimpleUOBX(0.5);
    SimpleUOBX uobx = original.split();
    for (int n = 1; n <= 32; n *= 2) {
      Permutation p1 = new Permutation(n);
      Permutation p2 = new Permutation(n);
      Permutation parent1 = new Permutation(p1);
      Permutation parent2 = new Permutation(p2);
      uobx.cross(parent1, parent2);
      assertTrue(validPermutation(parent1));
      assertTrue(validPermutation(parent2));
      boolean[] fixedPoints = findFixedPoints(parent1, parent2, p1, p2);
      validateOrderingUOBX(parent1, p2, fixedPoints);
      validateOrderingUOBX(parent2, p1, fixedPoints);
    }

    uobx = new SimpleUOBX(0.25);
    for (int n = 8; n <= 32; n *= 2) {
      Permutation p1 = new Permutation(n);
      Permutation p2 = new Permutation(n);
      Permutation parent1 = new Permutation(p1);
      Permutation parent2 = new Permutation(p2);
      uobx.cross(parent1, parent2);
      assertTrue(validPermutation(parent1));
      assertTrue(validPermutation(parent2));
      boolean[] fixedPoints = findFixedPoints(parent1, parent2, p1, p2);
      validateOrderingUOBX(parent1, p2, fixedPoints);
      validateOrderingUOBX(parent2, p1, fixedPoints);
    }

    uobx = new SimpleUOBX(0.75);
    for (int n = 8; n <= 32; n *= 2) {
      Permutation p1 = new Permutation(n);
      Permutation p2 = new Permutation(n);
      Permutation parent1 = new Permutation(p1);
      Permutation parent2 = new Permutation(p2);
      uobx.cross(parent1, parent2);
      assertTrue(validPermutation(parent1));
      assertTrue(validPermutation(parent2));
      boolean[] fixedPoints = findFixedPoints(parent1, parent2, p1, p2);
      validateOrderingUOBX(parent1, p2, fixedPoints);
      validateOrderingUOBX(parent2, p1, fixedPoints);
    }
  }

  @Test
  public void testExceptionsUOBX() {
    IllegalArgumentException thrown =
        assertThrows(IllegalArgumentException.class, () -> new SimpleUOBX(0.0));
    thrown = assertThrows(IllegalArgumentException.class, () -> new SimpleUOBX(1.0));
  }
}
