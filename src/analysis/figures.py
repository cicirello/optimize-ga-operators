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
        for line in f:
            row = line.strip().split()
            if len(row)==0:
                ready_for_data = False
            elif row[0]=="n":
                ready_for_data = True
                headings.append(row)
                data.append([])
            elif ready_for_data:
                row[0] = int(row[0])
                row[1] = float(row[1])
                row[2] = float(row[2])
                row[3] = float(row[3])
                row[5] = float(row[5])
                row[6] = int(row[6])
                row[7] = float(row[7])
                data[-1].append(row)
    return headings, data
            
if __name__ == "__main__":
    datafile = sys.argv[1]
    headings, data = parse(datafile)

    base_filename = os.path.basename(datafile)[:-4]

    x_label = "mutation rate M (log scale)" if "mutation" == base_filename else "uniform crossover bit-rate U"

    y_label = "CPU time (seconds)"

    w = 3.5
    h = w / 2
    matplotlib.pyplot.rc('font', size=9)
    matplotlib.pyplot.rc('text', usetex=True)
    for header, d in zip(headings, data):
        fig, ax = matplotlib.pyplot.subplots(figsize=(w,h), constrained_layout=True)
        matplotlib.pyplot.xlabel(x_label)
        matplotlib.pyplot.ylabel(y_label)
        x = [row[1] for row in d]
        simple = [row[2] for row in d]
        optimized = [row[3] for row in d]
        if "mutation" == base_filename:
            ax.set_xscale('log', base=2)
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
        if "mutation" == base_filename:
            ax.legend(loc="center")
        elif "crossover" == base_filename and d[0][0]>=128 and d[0][0] <= 512:
            ax.legend(loc="center")
        else:
            ax.legend()
        figure_filename = "{0}.{1}.{2}".format(datafile[:-4], d[0][0], "svg")
        eps_filename = "{0}.{1}.{2}".format(datafile[:-4], d[0][0], "eps") 
        matplotlib.pyplot.savefig(figure_filename)
        matplotlib.pyplot.savefig(eps_filename)
        
        
        

    
