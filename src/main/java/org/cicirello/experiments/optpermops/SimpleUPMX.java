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
import org.cicirello.permutations.PermutationFullBinaryOperator;
import org.cicirello.search.operators.CrossoverOperator;

/**
 * Non-optimized version of Uniform Partially Matched Crossover (UPMX) operator, implemented in the
 * obvious way.
 *
 * @author <a href=https://www.cicirello.org/ target=_top>Vincent A. Cicirello</a>, <a
 *     href=https://www.cicirello.org/ target=_top>https://www.cicirello.org/</a>
 */
public final class SimpleUPMX
    implements CrossoverOperator<Permutation>, PermutationFullBinaryOperator {

  private final double u;
  private final EnhancedSplittableGenerator generator;

  /**
   * Constructs a non-optimized version of Uniform Partially Matched Crossover (UPMX) operator,
   * implemented in the obvious way.
   *
   * @param u The probability of an index being among the cross points.
   * @throws IllegalArgumentException if u is less than or equal to 0.0, or if u is greater than or
   *     equal to 1.0.
   */
  public SimpleUPMX(double u) {
    if (u <= 0 || u >= 1.0) throw new IllegalArgumentException("u must be: 0.0 < u < 1.0");
    this.u = u;
    generator =
        new EnhancedSplittableGenerator(RandomGenerator.SplittableGenerator.of("SplittableRandom"));
  }

  private SimpleUPMX(SimpleUPMX other) {
    generator = other.generator.split();
    u = other.u;
  }

  @Override
  public void cross(Permutation c1, Permutation c2) {
    c1.apply(this, c2);
  }

  @Override
  public SimpleUPMX split() {
    return new SimpleUPMX(this);
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
  public void apply(int[] raw1, int[] raw2, Permutation p1, Permutation p2) {
    internalCross(raw1, raw2, p1, p2, generator);
  }

  /*
   * package private to facilitate unit testing
   */
  final void internalCross(
      int[] raw1, int[] raw2, Permutation c1, Permutation c2, EnhancedSplittableGenerator r) {
    boolean[] mask = new boolean[raw1.length];
    for (int k = 0; k < raw1.length; k++) {
      if (r.nextDouble() < u) {
        mask[k] = true;
      }
    }
    int[] inv1 = c1.getInverse();
    int[] inv2 = c2.getInverse();
    int[] old1 = raw1.clone();
    int[] old2 = raw2.clone();
    for (int k = 0; k < raw1.length; k++) {
      if (mask[k]) {
        int g = inv1[old2[k]];
        if (k != g) {
          int temp = raw1[k];
          raw1[k] = raw1[g];
          raw1[g] = temp;
          inv1[raw1[g]] = g;
          inv1[old2[k]] = k;
        }
        g = inv2[old1[k]];
        if (k != g) {
          int temp = raw2[k];
          raw2[k] = raw2[g];
          raw2[g] = temp;
          inv2[raw2[g]] = g;
          inv2[old1[k]] = k;
        }
      }
    }
  }
}
