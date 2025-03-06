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

/** JUnit tests for SimpleUndoableUniformScramble. */
public class SimpleUndoableUniformScrambleTests {

  @Test
  public void testSimpleUndoableUniformScramble() {
    SimpleUndoableUniformScramble m = new SimpleUndoableUniformScramble(0.0, true);
    mutateTester(m);
    splitTester(m);
    undoTester(m);
    m = new SimpleUndoableUniformScramble(1.0);
    mutateTester(m);
    splitTester(m);
    undoTester(m);
    m = new SimpleUndoableUniformScramble(0.5, true);
    mutateTester(m);
    splitTester(m);
    undoTester(m);
    m = new SimpleUndoableUniformScramble(0.0, false);
    for (int n = 0; n <= 6; n++) {
      Permutation p1 = new Permutation(n);
      Permutation p2 = new Permutation(p1);
      m.mutate(p2);
      assertEquals(p1, p2);
    }
    IllegalArgumentException thrown =
        assertThrows(
            IllegalArgumentException.class, () -> new SimpleUndoableUniformScramble(-0.000001));
    thrown =
        assertThrows(
            IllegalArgumentException.class, () -> new SimpleUndoableUniformScramble(1.000001));
  }

  private static final int NUM_RAND_TESTS = 40;

  private void mutateTester(SimpleUndoableUniformScramble m) {
    for (int i = 0; i <= 6; i++) {
      Permutation p = new Permutation(i);
      for (int t = 0; t < NUM_RAND_TESTS; t++) {
        Permutation mutant = new Permutation(p);
        m.mutate(mutant);
        // verify mutation produced a valid permutation
        validate(mutant);
        if (i < 2) {
          assertEquals(p, mutant);
        } else {
          assertNotEquals(p, mutant);
        }
      }
    }
  }

  private void validate(Permutation p) {
    boolean[] inP = new boolean[p.length()];
    for (int i = 0; i < inP.length; i++) {
      int j = p.get(i);
      assertTrue(j >= 0 && j < inP.length);
      inP[j] = true;
    }
    for (int i = 0; i < inP.length; i++) {
      assertTrue(inP[i]);
    }
  }

  private void splitTester(SimpleUndoableUniformScramble m) {
    SimpleUndoableUniformScramble s = m.split();
    assertNotSame(m, s);
    mutateTester(s);
  }

  private void undoTester(SimpleUndoableUniformScramble m) {
    // iterate over different length permutations beginning with 0 length
    for (int i = 0; i <= 6; i++) {
      Permutation p = new Permutation(i);
      for (int t = 0; t < NUM_RAND_TESTS; t++) {
        Permutation mutant = new Permutation(p);
        m.mutate(mutant);
        m.undo(mutant);
        assertEquals(p, mutant);
      }
    }
  }
}
