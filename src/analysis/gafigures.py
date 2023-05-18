# Experiments related to optimizing genetic algorithm operators.
# Copyright (C) 2023 Vincent A. Cicirello
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <https://www.gnu.org/licenses/>.
#

import sys
import os
import matplotlib.pyplot


def parse(filename):
    """Parses a data file.

    Keyword arguments:
    filename - The name of the data file
    """
    with open(filename, "r") as f:
        ready_for_data = False
        headings = []
        data = []
        ops = []
        for line in f:
            row = line.strip().split()
            if len(row) == 0:
                ready_for_data = False
            elif row[0] == "Uniform":
                ops.append("U"+row[-1][-2:])
            elif row[0] == "SinglePointCrossover":
                ops.append("1p")
            elif row[0] == "TwoPointCrossover":
                ops.append("2p")
            elif row[0] == "c":
                ready_for_data = True
                headings.append(row)
                data.append([])
            elif ready_for_data:
                row[0] = float(row[0])
                row[1] = float(row[1])
                row[2] = float(row[2])
                row[4] = float(row[4])
                row[5] = int(row[5])
                row[6] = float(row[6])
                row[7] = float(row[7])
                row[8] = float(row[8])
                row[9] = float(row[9])
                row[10] = int(row[10])
                row[11] = float(row[11])
                data[-1].append(row)
    return headings, data, ops

if __name__ == "__main__":
    datafile = sys.argv[1]
    headings, data, ops = parse(datafile)

    base_filename = os.path.basename(datafile)[:-4]

    x_label = "crossover rate C"

    y_label = "CPU time (seconds)"

    w = 3.5
    h = w / 2
    matplotlib.pyplot.rc('font', size=9)
    matplotlib.pyplot.rc('text', usetex=True)
    for header, d, operator in zip(headings, data, ops):
        fig, ax = matplotlib.pyplot.subplots(figsize=(w,h), constrained_layout=True)
        matplotlib.pyplot.xlabel(x_label)
        matplotlib.pyplot.ylabel(y_label)
        x = [row[0] for row in d]
        simple = [row[1] for row in d]
        optimized = [row[2] for row in d]
        line, = ax.plot(
            x,
            simple,
            "k-",
            label = "simple")
        line, = ax.plot(
            x,
            optimized,
            "k--",
            label = "optimized")
        ax.legend(loc="center right")
        figure_filename = "{0}.{1}.{2}".format(datafile[:-4], operator, "svg")
        eps_filename = "{0}.{1}.{2}".format(datafile[:-4], operator, "eps")
        matplotlib.pyplot.savefig(figure_filename)
        matplotlib.pyplot.savefig(eps_filename)
