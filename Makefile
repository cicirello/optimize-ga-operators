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
experiments: bitmasks
	
# Experiments with two variations of generating random bit masks

.PHONY: bitmasks
bitmasks:
	java -cp ${JARFILE} org.cicirello.experiments.gaops.BitmaskGenerationExperiment > ${pathToDataFiles}/bitmasks.txt
