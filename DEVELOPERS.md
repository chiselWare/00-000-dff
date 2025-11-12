# DEVELOPER NOTES

This template contains a template Chisel code to be used for your starting your chiselWare-compliant core. Note that this template also contains code that is part of a harness that chiselWare uses to periodically perform regression testing. It is important that you do not break this when you make your customizations.

**This file (DEVELOPERS.md) should be removed from your core repository before release**

## Organizationa and Team IDs

chiselWare contributors are organized into organizations and teams, including teams of one that are unaffiliated with a company.

When you request to develop a chiselWare core, you will be given an 2-digit organization ID and a 3-digit team ID. These IDs will show up in the directory 
structure and package name of your core. You can see the Dff template has a
organization ID of ```00``` and a team ID of ```000```.

## Root directory modifications

The following files should be modified according to your requirements. No other files should be added or deleted at this root level.

### LICENSE.MD

The default license for open-source chiselWare cores in Apache 2. If you 
prefer a different license, populate this file with your license.

For closed-source designs, modify the content with information about your
license terms as appropriate per your legal counsel.

### build.sbt

This file needs to be adjusted according to the needs of your core. For most
cores this file can be used as-is with only simple replacement of ```dff``` 
to the name of your core.

Note that we use a "modules" hierarchy to handle the possibility of complex
cores that may have more than one build package. Do not try to simplify the
hierarchy as it will break the regression harness.

### Makefile

This file is the backbone of the regression harness. It should be minimally 
modified with only changes associated with the main class name of your core.

You should confirm that ```make all``` passes before submitting your core as 
a release candidate.

## Directory name changes

The directory hierarchy of all chiselWare cores follow this deeply hierarchical
structure which is consistent with Scala best practices.

Below is an example for the Dff template. Notice the directories ```o01``` and ```t001```. These should be renamed with your own organization and team IDs.

```

├── build.sbt
├── DEVELOPERS.md
├── docs
├── LICENSE.MD
├── Makefile
├── modules
│   └── dff
│       ├── docs
│       │   └── user-guide
│       │       ├── Dff.pdf
│       │       ├── Dff.tex
│       │       ├── draw.io
│       │       │   └── Dff.io
│       │       ├── errata.tex
│       │       ├── images
│       │       │   ├── chiselWare_Logo_RGB_300.png
│       │       │   ├── Dff.io.drawio.png
│       │       │   ├── Dff.wavedrom.png
│       │       │   └── timing.png
│       │       ├── params.tex
│       │       ├── pdf
│       │       │   └── Dff.pdf
│       │       ├── ports.tex
│       │       ├── sim.tex
│       │       ├── syn.tex
│       │       ├── tops.tex
│       │       └── wavedrom
│       │           └── wavedrom.json
│       └── src
│           ├── main
│           │   ├── resources
│           │   │   ├── stdcells.lib
│           │   │   └── vsrc
│           │   └── scala
│           │       └── org
│           │           └── chiselware
│           │               └── cores
│           │                   └── o01
│           │                       └── t001
│           │                           └── dff
│           │                               ├── DffParams.scala
│           │                               ├── Dff.scala
│           │                               └── DffTb.scala
│           └── test
│               └── scala
│                   └── org
│                       └── chiselware
│                           └── cores
│                               └── o01
│                                   └── t001
│                                       └── dff
│                                           └── DffTest.scala
├── project
│   ├── build.properties
│   ├── metals.sbt
│   └── plugins.sbt
└── README.md
```

## Scala file customizations

In the above tree diagram you will see four Chisel files. These are the 
minimum files required for every chiselWare core. Your core will likely have
many more and also subdirectories specific to how you organize your design.

The following Dff Chisel files are should be renamed and adapted for your design.

### ./modules/dff/src/main/scala/.../dff/Dff.scala

This file contains the main class (Dff) and a helper object class (Main).

#### Dff class

The main class contains the top-level object which will generate a top-level Verilog module of the same name. Per Scala coding guidelines, the main class name is always in UpperCamelCase.

Completely replace this code with your own.

#### Main object

This object generated the Verilog code for each of the configurations. Only replace ```Dff``` wtih the main class name of your core.

### ./modules/dff/src/main/scala/.../dff/DffParams.scala

This is a very important file that follows the naming convention ```<MainClassName>Params.scala```. This case class contains a number of items:
- A complete list of all parameters for your core
- Contraints/assertions for each parameter
- Two sets of configurations for testing. One for synthesis, one for simulation
- A object containing timing information for generating an SDC file for each configuration

This case class is used extensively throughout the design to pass parameters
 into other Chisel/Scala code in your design.

### ./modules/dff/src/main/scala/.../dff/DffTb.scala

This is a test bench that instantiates at least one copy of the core. In some cases it will instantiate only the core itself, in other cases it may instantiate other non-core items like memory models or bus functional models.

It should follow the naming convention ```<MainClassNameTb.scala>```.

### ./modules/dff/src/test/scala/.../dff/DffTest.scala

This is the main test class that is used to run tests. It follows the naming convention of ````<MainClassNameTest.scala>.