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
import org.cicirello.util.IntegerArray;

/** */
public final class SimpleUOBX implements CrossoverOperator<Permutation>, PermutationBinaryOperator {

  private final double u;
  private final EnhancedSplittableGenerator generator;

  /**
   * Constructs a non-optimized Uniform Order-Based Crossover (UOBX) operator, implemented in the
   * obvious way.
   *
   * @param u The probability of an index being among the fixed-point positions.
   * @throws IllegalArgumentException if u is less than or equal to 0.0, or if u is greater than or
   *     equal to 1.0.
   */
  public SimpleUOBX(double u) {
    if (u <= 0 || u >= 1.0) throw new IllegalArgumentException("u must be: 0.0 < u < 1.0");
    this.u = u;
    generator =
        new EnhancedSplittableGenerator(RandomGenerator.SplittableGenerator.of("SplittableRandom"));
  }

  private SimpleUOBX(SimpleUOBX other) {
    generator = other.generator.split();
    u = other.u;
  }

  @Override
  public void cross(Permutation c1, Permutation c2) {
    c1.apply(this, c2);
  }

  /**
   * See {@link PermutationBinaryOperator} for details of this method. This method is not intended
   * for direct usage. Use the {@link #cross} method instead.
   *
   * @param raw1 The raw representation of the first permutation.
   * @param raw2 The raw representation of the second permutation.
   */
  @Override
  public void apply(int[] raw1, int[] raw2) {
    int orderedCount = raw1.length;
    boolean[] mask = new boolean[raw1.length];
    boolean[] in1 = new boolean[raw1.length];
    boolean[] in2 = new boolean[raw1.length];
    for (int k = 0; k < mask.length; k++) {
      if (generator.nextDouble() < u) {
        mask[k] = true;
        in1[raw1[k]] = true;
        in2[raw2[k]] = true;
        orderedCount--;
      }
    }
    IntegerArray list1 = new IntegerArray(orderedCount);
    IntegerArray list2 = new IntegerArray(orderedCount);
    for (int k = 0; k < raw1.length; k++) {
      if (!in2[raw1[k]]) {
        list1.add(raw1[k]);
      }
      if (!in1[raw2[k]]) {
        list2.add(raw2[k]);
      }
    }
    int w = 0;
    for (int k = 0; k < mask.length; k++) {
      if (!mask[k]) {
        raw1[k] = list2.get(w);
        raw2[k] = list1.get(w);
        w++;
      }
    }
  }

  @Override
  public SimpleUOBX split() {
    return new SimpleUOBX(this);
  }
}
