# gitsbe

<!-- badges: start -->
[![Java CI with Maven](https://github.com/druglogics/gitsbe/workflows/Java%20CI%20with%20Maven/badge.svg)](https://github.com/druglogics/gitsbe/actions)
[![License](https://img.shields.io/github/license/druglogics/gitsbe)](https://github.com/druglogics/gitsbe/blob/master/LICENSE)
<!-- badges: end -->

Gitsbe is an acronym for *Generic Interactions To Specific Boolean Equations*.
This module defines boolean models compliant with observed behavior (e.g. steady state or perturbation data) using an automated, model parameterization genetic algorithm.

For a full documentation of this package check [here](https://druglogics.github.io/druglogics-doc/gitsbe.html).

## Install

```
git clone https://github.com/druglogics/gitsbe.git
cd gitsbe
mvn clean install
```

The above command creates a package `<name>-jar-with-dependencies.jar` file with all dependencies installed, in the `target` directory.

Alternatively, you could just use directly one of the [released packages](https://github.com/druglogics/gitsbe/packages/).

## Run example

The recommended way to run Gitsbe is to use its `Launcher`.
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
