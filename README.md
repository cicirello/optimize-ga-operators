# Code to reproduce the experiments from: Optimizing Genetic Algorithms Using the Binomial Distribution

Copyright &copy; 2023-2024 Vincent A. Cicirello

This repository contains code to reproduce the experiments, and analysis of 
experimental data, from the following paper:

> Vincent A. Cicirello. 2024. Optimizing Genetic Algorithms Using the Binomial Distribution. *Proceedings of the 16th International Joint Conference on Computational Intelligence*, pages 159-169. November 2024.

| __Publication__ | |
| :--- | :--- |
| __License__ | [![GitHub](https://img.shields.io/github/license/cicirello/optimize-ga-operators)](LICENSE) |

## Dependencies

The experiments depend upon the following libraries, which are automatically downloaded from
Maven Central during the build process:
* [Chips-n-Salsa](https://chips-n-salsa.cicirello.org/) 7.0.0
* [JavaPermutationTools](https://jpt.cicirello.org/) 6.0.0
* [&rho;&mu;](https://rho-mu.cicirello.org) 4.1.0
* [org.cicirello.core](https://core.cicirello.org) 2.7.0

## Requirements to Build and Run the Experiments

To build and run the experiments on your own machine, you will need the following:
* __JDK 17__: I used OpenJDK 17, but other distributions should be fine. 
* __Apache Maven__: In the root of the repository, there is a `pom.xml` 
  for building the Java programs for the experiments. Using this `pom.xml`, 
  Maven will take care of downloading the exact version of 
  [Chips-n-Salsa](https://chips-n-salsa.cicirello.org/) (release 7.0.0) 
  and its dependencies that were used in the experiments. 
* __Python 3__: The repository contains Python programs that were used to 
  process the raw data for the paper. If you want to run the Python programs, 
  you will need Python 3. 
* __Make__: The repository contains a Makefile to simplify running the build, 
  running the experiment's Java programs, and running the Python program to 
  analyze the data. If you are familiar with using the Maven build tool, 
  and running Python programs, then you can just run these directly, although 
  the Makefile may be useful to see the specific commands needed.

## Building the Java Programs

The source code of the Java programs implementing the experiments
is in the [src/main/java](src/main/java) directory. You can build 
the experiment programs in one of the following ways.

__Using Maven__: Execute the following from the root of the
repository.

```shell
mvn clean package
```

__Using Make__: Or, you can execute the following from the root
of the repository.

```shell
make build
```

## Running the Experiments

If you just want to inspect the data from my runs, then you can find that output
in the [/data](data) directory. If you instead want to run the experiments yourself,
you must first follow the build instructions. Once the jar of the experiments is 
built, you can then run the experiments with the following executed at the root of 
the repository:

```shell
make experiments
```

If you don't want to overwrite my original data files, then first change the variable
`pathToDataFiles` in the `Makefile` before running the above command.

## Analyzing the Experimental Data

To run the Python programs that process the raw data and generate the figures 
from the paper, you need Python 3 installed. The source 
code of the Python programs is found in the [src/analysis](src/analysis) 
directory.  To run the analysis, execute the following at the root of the 
repository:

```shell
make figures
```

This make command will also take care of installing any required Python packages 
if you don't already have them installed.

If you want to generate the figures in `pdf` format, then after executing the
above, proceed to execute the following (which assumes that you have `epstopdf` 
installed):

```shell
make epstopdf
```

If you don't want to overwrite my original data files, and figures, then change the 
variable `pathToDataFiles` in the `Makefile` before running the above commands.

## Other Files in the Repository

There are other files, potentially of interest, in the repository, including:
* `system-stats.txt`: This file contains details of the system I 
  used to run the experiments, such as operating system, processor 
  specs, Java JDK and VM. It is in the [/data](data) directory.

## License

The code to replicate the experiments from the paper, as well as the
Chips-n-Salsa library and its dependencies, are licensed under 
the [GNU General Public License 3.0](https://www.gnu.org/licenses/gpl-3.0.en.html).
