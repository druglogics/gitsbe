# gitsbe

This module defines models compliant with observed behavior (training data) based on a genetic algorithm simulation. For the steady state calculation, the [BNReduction tool](https://github.com/alanavc/BNReduction) is used.

## Input

- A network file (in `.sif` format).
- A training data file (can include both the steady state and perturbation data)
- A model outputs file (nodes with weights that affect the calculation of the model simulation output)
- A configuration file (includes parameters essential for the genetic algorithm simulation)

## Output

- A `model` directory with files in `.gitsbe` format, which represent the boolean models that best fitted to the configuration and training data that the simulation of the genetic algorithm was based on.
- A summary file that includes the models' fitness evolution.
- The network file can be exported to many standard formats (e.g. gitsbe)

## Install

```
git clone https://bitbucket.org/asmundf/gitsbe.git
mvn clean install
```

## Run example

From the gitsbe root directory, run (remember to change the `{version}` to the appropriate one, e.g. `1.0`):

```
cd example_run_ags
java -cp ../target/gitsbe-{version}-jar-with-dependencies.jar eu.druglogics.gitsbe.Launcher --project=test --network=toy_ags_network.sif --trainingdata=toy_ags_training_data.tab --config=toy_ags_config.tab --modeloutputs=toy_ags_modeloutputs.tab
```

or run the mvn profile from the gitsbe root directory:
```
mvn compile -P runExampleAGS
```

- Note that its best that all input files are on the same directory like in the example above. The output files when using the the `gitsbe.Launcher` will be inside a generated directory where the network file is located.