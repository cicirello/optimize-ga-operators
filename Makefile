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
	$(py) -B src/analysis/figures.py data/mutation.txt
	$(py) -B src/analysis/figures.py data/crossover.txt
	$(py) -B src/analysis/gafigures.py data/ga.txt
	$(py) -B src/analysis/gafigures.py data/ga2.txt

.PHONY: epstopdf
epstopdf:
	epstopdf ${pathToDataFiles}/mutation.16.eps
	epstopdf ${pathToDataFiles}/mutation.64.eps
	epstopdf ${pathToDataFiles}/mutation.256.eps
	epstopdf ${pathToDataFiles}/mutation.1024.eps
	epstopdf ${pathToDataFiles}/crossover.16.eps
	epstopdf ${pathToDataFiles}/crossover.64.eps
	epstopdf ${pathToDataFiles}/crossover.256.eps
	epstopdf ${pathToDataFiles}/crossover.1024.eps
	epstopdf ${pathToDataFiles}/ga.U33.eps
	epstopdf ${pathToDataFiles}/ga.U49.eps
	epstopdf ${pathToDataFiles}/ga2.1p.eps
	epstopdf ${pathToDataFiles}/ga2.2p.eps

# Runs all experiments from both original and extended papers

.PHONY: experiments
experiments: permOnePlusOne permEA

# Runs all experiments from the extended journal article

.PHONY: experimentsExtended
experimentsExtended: uobx ox2 upmx uppx scramble

# Runs all experiments from the ECTA 2024 paper

.PHONY: experimentsECTA
experimentsECTA: bitmasks mutation crossover generation ga ga2

# Experiments with permutation EA
.PHONY: permEA
permEA:
	java -cp ${JARFILE} org.cicirello.experiments.optpermops.ExperimentPermutationEA > ${pathToDataFiles}/perm.ea.txt

# Experiments with permutation (1+1)-EA
.PHONY: permOnePlusOne
permOnePlusOne:
	java -cp ${JARFILE} org.cicirello.experiments.optpermops.ExperimentPermutationOnePlusOneEA > ${pathToDataFiles}/perm.ea1p1.txt

# Experiments with two variations of UOBX crossover for permutations

.PHONY: uobx
uobx: 
	java -cp ${JARFILE} org.cicirello.experiments.optpermops.ExperimentUOBX > ${pathToDataFiles}/perm.uobx.txt

# Experiments with two variations of OX2 crossover for permutations

.PHONY: ox2
ox2: 
	java -cp ${JARFILE} org.cicirello.experiments.optpermops.ExperimentOX2 > ${pathToDataFiles}/perm.ox2.txt

# Experiments with two variations of UPMX crossover for permutations

.PHONY: upmx
upmx: 
	java -cp ${JARFILE} org.cicirello.experiments.optpermops.ExperimentUPMX > ${pathToDataFiles}/perm.upmx.txt

# Experiments with two variations of UPPX crossover for permutations

.PHONY: uppx
uppx: 
	java -cp ${JARFILE} org.cicirello.experiments.optpermops.ExperimentUPPX > ${pathToDataFiles}/perm.uppx.txt

# Experiments with two variations of uniform scramble mutation for permutations

.PHONY: scramble
scramble: 
	java -cp ${JARFILE} org.cicirello.experiments.optpermops.ExperimentUniformScrambleMutation > ${pathToDataFiles}/perm.uniformscramble.txt
	
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
