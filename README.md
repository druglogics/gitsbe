# gitsbe

This module defines models compliant with observed behavior (steady state or
perturbation data) using an automated, model parameterization genetic algorithm. 
For the steady state calculation, the [BNReduction tool](https://github.com/alanavc/BNReduction) 
is used.

## Input

- A network file (in Cytoscape's `.sif` format, tab-delimited, binary signed and 
directed interactions).
- A training data file (can include both the steady state and perturbation data)
- A model outputs file (nodes with weights that affect the calculation of the 
model's global output/growth)
- A configuration file (includes parameters essential for the genetic algorithm 
simulation)

## Output

- A **models directory** with files in `.gitsbe` format, which represent the 
boolean models that best fitted to the configuration and training data that the 
simulation of the genetic algorithm was based on.
- A **summary file** that includes the models' fitness evolution.
- A **network file** can be exported to many standard formats 
(e.g. .gitsbe, .sif, .ginml)

## Method

The model interactions are first assembled to logical equations, based on a 
**default equation** relating a node with its regulators:

`Target *= (A or B or C) and not (D or E or F)`

where the activating regulators `A`, `B` and `C` and the inhibitory regulators 
`D`, `E` and `F` of a target are combined with logical `or` operators between 
them and with the `and not` operator in total, to determine the state of the 
target node in the next time step.

The **genetic algorithms** are used to obtain a parameterization that produces 
logical models with stable state(s) matching the specified input steady state
and global output value based on the specified perturbations.

First, an initial generation of models is formulated, where a large number of 
mutations to the parameterization is introduced: randomly selected equations 
are mutated from `and not` to `or not`, or vice versa for example. A summary of
the possible **mutations** that can be applied to randomly chosen equations of 
a logical model are:

- Balance: `and not` <=> `or not` 
- Random: `(A or B)` <=> `(A and B)` (change of logical role)
- Shuffle: `(A or B)` <=> `(B or A)` (priority)
- Topology: `(A or B)` <=> `(B)` (addition and removal of regulation edges)

For each model, a **fitness score** is computed as the weighted average 
over all fitness values for each condition specified in the training data file.
So for each condition, a separate fitness score is calculated:

- If the condition is to **match a steady state pattern**, then each matching 
boolean value in the vector of a stable state and the steady state increments 
the sub-fitness score. A fitness of 1 means that a stable state matched exactly 
all the node values that were specified in the steady state vector. Models 
without a stable state have a fitness of zero. Models with several stable states 
`n`, obtain a final fitness after integrating all Boolean values in the stable 
state vector and dividing by `n`. 
- If the condition is for a **specified perturbation**, then an expected 
**global output value** is computed by integrating a weighted score across the 
states of model output nodes. This is contrasted with the observed global output 
response value in the training data (for that particular condition) to produce 
the sub-fitness score. A fitness of 1 means that the expected and observed 
global output scores are the same.

For each generation, a user-specified number of models are selected for 
populating the next generation (**selection phase**). During **crossover phase**,
each selected model would exchange logical equations with other selected models 
(including itself, thus also enabling asexual reproduction). Then a number of 
mutations are introduced as described above. After a stable state is obtained, 
the number of mutations introduced per generation is reduced by a user-specified 
factor. The large number of mutations in the initial phase ensures that a large 
variation in parameterization can be explored.

**Evolution** is halted either when a user-specified threshold fitness is reached, 
or when a user-specified maximum number of generations had been spanned. Many
simulations of evolution can be run from the initial model (even in parallel), 
using a different seed each time and thus creating different output models in 
each case.

## Install

Install the `BNReduction` script and its dependencies [here](https://bitbucket.org/asmundf/druglogics_dep/src/develop/).

```
# tested with maven 3.6.0
git clone https://bitbucket.org/asmundf/gitsbe.git
mvn clean install
```

## Run example

From the gitsbe root directory, run (remember to change the `{version}` to the 
appropriate one, e.g. `1.0.8`):

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
