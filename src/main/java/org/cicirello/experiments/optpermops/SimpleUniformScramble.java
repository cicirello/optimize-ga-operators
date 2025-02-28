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

import java.util.Arrays;
import java.util.random.RandomGenerator;
import org.cicirello.math.rand.EnhancedSplittableGenerator;
import org.cicirello.permutations.Permutation;
import org.cicirello.search.operators.MutationOperator;

/**
 * Non-optimized version of uniform scramble mutation, implemented in the obvious way.
 *
 * @author <a href=https://www.cicirello.org/ target=_top>Vincent A. Cicirello</a>, <a
 *     href=https://www.cicirello.org/ target=_top>https://www.cicirello.org/</a>
 */
public final class SimpleUniformScramble implements MutationOperator<Permutation> {

  private final double u;
  private final boolean guaranteeChange;
  private final EnhancedSplittableGenerator generator;

  /**
   * Constructs a non-optimized uniform scramble mutation operator, implemented in the obvious way.
   *
   * @param u The probability of an index being among the fixed-point positions.
   * @throws IllegalArgumentException if u is less than or equal to 0.0, or if u is greater than or
   *     equal to 1.0.
   */
  public SimpleUniformScramble(double u) {
    this(u, false);
  }

  /**
   * Constructs a non-optimized uniform scramble mutation operator, implemented in the obvious way.
   *
   * @param u The probability of an index being among the fixed-point positions.
   * @param guaranteeChange If true, then the {@link #mutate(Permutation) mutate} method will be
   *     guaranteed to change the locations of at least 2 elements. Otherwise, if false, it may be
   *     possible (e.g., for low values of u) for the mutate method not to change anything during
   *     some calls.
   * @throws IllegalArgumentException if u is less than or equal to 0.0, or if u is greater than or
   *     equal to 1.0.
   */
  public SimpleUniformScramble(double u, boolean guaranteeChange) {
    if (u < 0 || u > 1.0) throw new IllegalArgumentException("u must be in [0.0, 1.0].");
    this.u = u;
    this.guaranteeChange = guaranteeChange;
    generator =
        new EnhancedSplittableGenerator(RandomGenerator.SplittableGenerator.of("SplittableRandom"));
  }

  private SimpleUniformScramble(SimpleUniformScramble other) {
    generator = other.generator.split();
    guaranteeChange = other.guaranteeChange;
    u = other.u;
  }

  @Override
  public SimpleUniformScramble split() {
    return new SimpleUniformScramble(this);
  }

  @Override
  public void mutate(Permutation c) {
    if (c.length() >= 2) {
      c.scramble(indexes(c.length(), u), generator);
    }
  }

  private int[] indexes(int n, double u) {
    int[] indexes = new int[n];
    int count = 0;
    for (int i = 0; i < n; i++) {
      if (generator.nextDouble() < u) {
        indexes[count] = i;
        count++;
      }
    }
    if (guaranteeChange && count < 2) {
      if (count == 0) {
        indexes[0] = generator.nextInt(n);
      }
      indexes[1] = generator.nextInt(n - 1);
      if (indexes[1] == indexes[0]) {
        indexes[1] = n - 1;
      }
      count = 2;
    }
    return Arrays.copyOf(indexes, count);
  }
}
