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

import java.util.random.RandomGenerator;
import org.cicirello.math.rand.EnhancedSplittableGenerator;
import org.cicirello.permutations.Permutation;
import org.cicirello.permutations.PermutationBinaryOperator;
import org.cicirello.search.operators.CrossoverOperator;

/**
 * Non-optimized version of Uniform Precedence Preservative Crossover (UPPX) operator, implemented
 * in the obvious way.
 *
 * @author <a href=https://www.cicirello.org/ target=_top>Vincent A. Cicirello</a>, <a
 *     href=https://www.cicirello.org/ target=_top>https://www.cicirello.org/</a>
 */
public final class SimpleUPPX implements CrossoverOperator<Permutation>, PermutationBinaryOperator {

  private final double u;
  private final EnhancedSplittableGenerator generator;

  /**
   * Constructs a non-optimized Uniform Precedence Preservative Crossover (UPPX) operator,
   * implemented in the obvious way.
   *
   * @param u The probability of an index being among the fixed-point positions.
   * @throws IllegalArgumentException if u is less than or equal to 0.0, or if u is greater than or
   *     equal to 1.0.
   */
  public SimpleUPPX(double u) {
    if (u <= 0 || u >= 1.0) throw new IllegalArgumentException("u must be: 0.0 < u < 1.0");
    this.u = u;
    generator =
        new EnhancedSplittableGenerator(RandomGenerator.SplittableGenerator.of("SplittableRandom"));
  }

  private SimpleUPPX(SimpleUPPX other) {
    generator = other.generator.split();
    u = other.u;
  }

  @Override
  public void cross(Permutation c1, Permutation c2) {
    c1.apply(this, c2);
  }

  @Override
  public SimpleUPPX split() {
    return new SimpleUPPX(this);
  }

  /**
   * See {@link PermutationFullBinaryOperator} for details of this method. This method is not
   * intended for direct usage. Use the {@link #cross} method instead.
   *
   * @param raw1 The raw representation of the first permutation.
   * @param raw2 The raw representation of the second permutation.
   * @param p1 The first permutation.
   * @param p2 The second permutation.
   */
  @Override
  public void apply(int[] raw1, int[] raw2) {
    internalCross(raw1, raw2, generator);
  }

  /*
   * package private to facilitate testing
   */
  final void internalCross(int[] raw1, int[] raw2, EnhancedSplittableGenerator r) {
    boolean[] mask = new boolean[raw1.length];
    for (int i = 0; i < mask.length; i++) {
      if (r.nextDouble() < u) {
        mask[i] = true;
      }
    }

    int[] old1 = raw1.clone();
    int[] old2 = raw2.clone();
    boolean[] used1 = new boolean[raw1.length];
    boolean[] used2 = new boolean[raw1.length];

    int i = 0;
    int j = 0;
    int x = 0;
    int y = 0;
    for (int k = 0; k < mask.length; k++) {
      if (mask[k]) {
        while (used1[old1[i]]) {
          i++;
        }
        while (used2[old2[j]]) {
          j++;
        }
        used1[raw1[k] = old1[i]] = true;
        used2[raw2[k] = old2[j]] = true;
        i++;
        j++;
      } else {
        while (used1[old2[x]]) {
          x++;
        }
        while (used2[old1[y]]) {
          y++;
        }
        used1[raw1[k] = old2[x]] = true;
        used2[raw2[k] = old1[y]] = true;
        x++;
        y++;
      }
    }
  }
}
