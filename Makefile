ifeq ($(OS),Windows_NT)
	py = "python"
else
	py = "python3"
endif

JARFILE = "target/optimize-ga-operators-1.0.0-jar-with-dependencies.jar"
pathToDataFiles = "data"

.PHONY: build
build:
	mvn clean package

# Generates figures for article

.PHONY: figures
figures:
	$(py) -m pip install --user pycairo
	$(py) -m pip install --user matplotlib
	python -B src/analysis/figures.py data/mutation.txt
	python -B src/analysis/figures.py data/crossover.txt

# Runs all experiments

.PHONY: experiments
experiments: bitmasks mutation crossover generation ga ga2
	
# Experiments with two variations of generating random bit masks

.PHONY: bitmasks
bitmasks:
	java -cp ${JARFILE} org.cicirello.experiments.gaops.BitmaskGenerationExperiment > ${pathToDataFiles}/bitmasks.txt

# Experiments with two variations of bit flip mutation

.PHONY: mutation
mutation:
	java -cp ${JARFILE} org.cicirello.experiments.gaops.MutationExperiment > ${pathToDataFiles}/mutation.txt

# Experiments with two variations of uniform crossover

.PHONY: crossover
crossover:
	java -cp ${JARFILE} org.cicirello.experiments.gaops.CrossoverExperiment > ${pathToDataFiles}/crossover.txt

# Experiments with two variations of a generation implementation

.PHONY: generation
generation:
	java -cp ${JARFILE} org.cicirello.experiments.gaops.GenerationLoopExperiment > ${pathToDataFiles}/generation.txt

# Experiments with two variations of the GA implementation (uniform crossover, bit flip mutation)

.PHONY: ga
ga:
	java -cp ${JARFILE} org.cicirello.experiments.gaops.GAExperiment > ${pathToDataFiles}/ga.txt

# Experiments with two variations of the GA implementation (bit flip mutation, but 1-point or 2-point crossover)

.PHONY: ga2
ga2:
	java -cp ${JARFILE} org.cicirello.experiments.gaops.GAExperiment2 > ${pathToDataFiles}/ga2.txt
