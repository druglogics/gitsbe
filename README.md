# gitsbe

Gitsbe is an acronym for *Genetic Interactions To Specific Boolean Equations*. 
This module defines boolean models compliant with observed behavior (e.g. steady state or perturbation data) using an automated, model parameterization genetic algorithm.

For a full documentation of this package check [here](https://druglogics.github.io/druglogics-doc/gitsbe.html).

## Install

```
# tested with maven 3.6.0
git clone https://bitbucket.org/asmundf/gitsbe.git
mvn clean install
```

## Run example

The recommended way to run Gitsbe is to use it’s `Launcher`. 
From the root directory of the repo run: (remember to change the `{version}` to the 
appropriate one, e.g. `1.3.0`):

```
cd example_run_ags
java -cp ../target/gitsbe-{version}-jar-with-dependencies.jar eu.druglogics.gitsbe.Launcher --project=test --network=toy_ags_network.sif --trainingdata=toy_ags_training_data.tab --config=toy_ags_config.tab --modeloutputs=toy_ags_modeloutputs.tab
```

or run the mvn profile directly:
```
mvn compile -P runExampleAGS
```
