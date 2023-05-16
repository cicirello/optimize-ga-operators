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

# Runs all experiments

.PHONY: experiments
experiments: bitmasks mutation crossover generation ga
	
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

# Experiments with two variations of the GA implementation

.PHONY: ga
ga:
	java -cp ${JARFILE} org.cicirello.experiments.gaops.GAExperiment > ${pathToDataFiles}/ga.txt
