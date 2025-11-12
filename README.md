# Dff

A D-flip-flop.

## Description

The Dff is a parameterized D-flip-flop. This is a toy example that can be 
used as a template for serious designs.

## Getting Started

It is recommended that the user reads the Dff Users Guide which can be 
found in the ```.modules/dff/docs/user-guide``` directory.

### Dependencies

There are non-Chisel-related dependencies for two other open-source tools 
that are needed (only) for running the included synthesis regression tests:

* **[Yosys](https://yosyshq.net/yosys/)** (version 0.9) A synthesis and optimization (using ABC) tool
* **[OpenSTA](https://github.com/The-OpenROAD-Project/OpenSTA)** (version 2.4.0) A static timing analysis tool

### Installation

There are no special installation requirements. The code can be cloned in any 
directory for standalone use.


### Generating Verilog RTL

To generate an example configuration of SystemVerilog RTL, a helper app can be 
found in the main class file (Dff.scala) and executed as follows:

```
$ sbt
sbt:chiselware>
sbt:chiselware> project core
sbt:chiselware-core-dff> run
```

The RTL will be generated in the ```./modules/dff/generated``` directory.

### Running a Simulation  

Multiple options are available for running simulations:

* iVerilog (open-source Verilog simulator)
* VCS (commercial simulator from Synopsys)
* Verilator (open source compiled Verilog simulator) 

An exhaustive constrained-random verilog test is included and can be executed 
as follows:

```
$ sbt
sbt:chiselware>
sbt:chiselware> project core
sbt:chiselware-core-dff> test
```

### Synthesis

Dff is a DFT-clean, fully synthesizable core. 

A ```.sdc``` file is generated together with the RTL code for each configuration
in the ```./modules/dff/generated/synTestCases/ directory.```  Synthesis scripts 
for the synthesis tool Yosys and timing analysis scripts for the OpenSTA static
timing analysis tool are included in the same directory and can be easily ported 
to commercial synthesis tools.

Included also is the Nangate 45nm technology library to allow users to run
included synthesis regressions out-of-the-box and later change to their 
technology library of choice.

## Authors

Warren Savage
[@twsavage59]

## Version History

* 0.1.2
    * Tweaks to DEVELOPERS.md and README.md
* 0.1.2
    * Tweaks to DEVELOPERS.md
* 0.1.1
    * Add DEVELOPERS.md
* 0.1.0
    * Initial Release with full functionality

## License

See the [LICENSE.MD](https://github.com/chiselWare/dff/blob/main/LICENSE.MD) file for license rights and limitations (Apache2).
