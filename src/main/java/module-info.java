/*
 * Experiments related to optimizing genetic algorithm operators.
 * Copyright (C) 2023-2024 Vincent A. Cicirello
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

/**
 * Code to reproduce the experiments from the following article:
 *
 * <p>Vincent A. Cicirello. Optimizing Genetic Algorithms Using the Binomial Distribution.
 * <i>Proceedings of the 16th International Joint Conference on Computational Intelligence</i>,
 * pages 159-169. November 2024.
 *
 * @author <a href=https://www.cicirello.org/ target=_top>Vincent A. Cicirello</a>, <a
 *     href=https://www.cicirello.org/ target=_top>https://www.cicirello.org/</a>
 */
module org.cicirello.optimize_ga_operators {
  exports org.cicirello.experiments.gaops;

  requires org.cicirello.chips_n_salsa;
  requires org.cicirello.jpt;
  requires org.cicirello.rho_mu;
  requires org.cicirello.core;
  requires java.management;
}
