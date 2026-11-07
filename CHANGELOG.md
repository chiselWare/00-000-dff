# Changelog
## [0.9.3] - 2026-11-07
### Added
-  
### Fixed
- Corrected issue with dirty commit message in Verilog headers
### Changed
- 
## [0.9.2] - 2026-11-06
### Added
-  
### Fixed
- Corrected build.sbt to point at correct verilogHeaders package in Maven
### Changed
- 

## [0.9.1] - 2026-11-06
### Added
-  Add 'conformance' target to Makefile
### Fixed
- scalafix.conf, scalafix-test.conf, scalafmt.conf updates to support CWS rules
### Changed
- Tweak to build.sbt and Makefile to support new 'conformance' make target

## [0.8.5] - 2026-10-07
### Added
- build.sbt to allow images in Scaladoc pages
### Fixed
- Use variables for params in cov and ipf targets in Makefile
- Update UG to use factory method as required
- Fixed formatting on LICENSE.MD
### Changed
- Simplified fromMap code to remove trap for bad values; checking should be pushed to IPF code
- Changed license header to use SPDX identifier

## [0.8.4] - 2026-09-26
### Changed
- Changed self-hosted runner to GitHub-hosted runner with container

## [0.8.3] - 2026-07-26
### Added
- Dev Container
### Fixed
- Typo is GitHub URL in User Guid<!--  -->e
### Changed
- Improvements to build.sbt to reduce customizations
- Improvements to Makefile reduce customizations
- Removed DEVELOPERS.md consolidate developer instructions in CWDG

## [0.8.0] - 2025-06-05
### Added
- Add support for formal verification
- Add helper object to support factory method instantiation
### Fixed
- 
### Changed
- 
