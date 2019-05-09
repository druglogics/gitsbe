# gitsbe

This module defines models compliant with observed behavior (training data) 
based on a genetic algorithm simulation. For the steady state calculation, 
the [BNReduction tool](https://github.com/alanavc/BNReduction) is used.

## Input

- A network file (in `.sif` format, tab-delimited).
- A training data file (can include both the steady state and perturbation data)
- A model outputs file (nodes with weights that affect the calculation of the 
model simulation output)
- A configuration file (includes parameters essential for the genetic algorithm 
simulation)

## Output

- A **models directory** with files in `.gitsbe` format, which represent the boolean 
models that best fitted to the configuration and training data that the simulation 
of the genetic algorithm was based on.
- A **summary file** that includes the models' fitness evolution.
- A **network file** can be exported to many standard formats 
(e.g. .gitsbe, .sif, .ginml)

## Install

Install the `BNReduction` script and its dependencies [here](https://bitbucket.org/asmundf/druglogics_dep/src/develop/)

```
# tested with maven 3.5.2
git clone https://bitbucket.org/asmundf/gitsbe.git
mvn clean install
```

## Run example

From the gitsbe root directory, run (remember to change the `{version}` to the 
appropriate one, e.g. `1.0`):

```
cd example_run_ags
java -cp ../target/gitsbe-{version}-jar-with-dependencies.jar eu.druglogics.gitsbe.Launcher --project=test --network=toy_ags_network.sif --trainingdata=toy_ags_training_data.tab --config=toy_ags_config.tab --modeloutputs=toy_ags_modeloutputs.tab
```

or run the mvn profile from the gitsbe root directory:
```
mvn compile -P runExampleAGS
```

- Note that its best that all input files are on the same directory like in the 
example above. The output directory with all the generated result files when 
running the `gitsbe.Launcher` will be in the same directory where the 
configuration file is.
- Running the `gitsbe.Launcher` with no parameters generates a usage/options 
message.
