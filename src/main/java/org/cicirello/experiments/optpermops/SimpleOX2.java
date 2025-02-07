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
import org.cicirello.util.IntegerArray;

/**
 * Non-optimized version of Order Crossover 2 (OX2) operator, implemented in the obvious way.
 *
 * @author <a href=https://www.cicirello.org/ target=_top>Vincent A. Cicirello</a>, <a
 *     href=https://www.cicirello.org/ target=_top>https://www.cicirello.org/</a>
 */
public final class SimpleOX2
    implements CrossoverOperator<Permutation>, PermutationFullBinaryOperator {

  private final double u;
  private final EnhancedSplittableGenerator generator;

  /**
   * Constructs a non-optimized Order Crossover 2 (OX2) operator, implemented in the obvious way.
   *
   * @param u The probability of an index being among the fixed-point positions.
   * @throws IllegalArgumentException if u is less than or equal to 0.0, or if u is greater than or
   *     equal to 1.0.
   */
  public SimpleOX2(double u) {
    if (u <= 0 || u >= 1.0) throw new IllegalArgumentException("u must be: 0.0 < u < 1.0");
    this.u = u;
    generator =
        new EnhancedSplittableGenerator(RandomGenerator.SplittableGenerator.of("SplittableRandom"));
  }

  private SimpleOX2(SimpleOX2 other) {
    generator = other.generator.split();
    u = other.u;
  }

  @Override
  public void cross(Permutation c1, Permutation c2) {
    c1.apply(this, c2);
  }

  @Override
  public SimpleOX2 split() {
    return new SimpleOX2(this);
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
   * package private to facilitate testing
   */
  final void internalCross(
      int[] raw1, int[] raw2, Permutation p1, Permutation p2, EnhancedSplittableGenerator r) {
    int[] inv1 = p1.getInverse();
    int[] inv2 = p2.getInverse();
    IntegerArray elementOrder1 = new IntegerArray(raw1.length);
    IntegerArray elementOrder2 = new IntegerArray(raw1.length);
    boolean[] indexes1 = new boolean[raw1.length];
    boolean[] indexes2 = new boolean[raw1.length];
    for (int i = 0; i < raw1.length; i++) {
      if (r.nextDouble() < u) {
        elementOrder1.add(raw2[i]);
        elementOrder2.add(raw1[i]);
        indexes1[inv1[raw2[i]]] = true;
        indexes2[inv2[raw1[i]]] = true;
      }
    }
    int j = 0;
    int k = 0;
    for (int i = 0; i < indexes1.length; i++) {
      if (indexes1[i]) {
        raw1[i] = elementOrder1.get(j);
        j++;
      }
      if (indexes2[i]) {
        raw2[i] = elementOrder2.get(k);
        k++;
      }
    }
  }
}
