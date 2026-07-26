# -- Core Configuration ------------------------------------------------------
ORG_ID := "00"
TEAM_ID := "000"
CORE_NAME := "Dff"
# -- End Core Configuration -- do not modify below this line ------------------

MAKEFLAGS += --silent
SHELL := /bin/bash
CORE_NAME_LC=$(shell echo $(CORE_NAME) | tr '[:upper:]' '[:lower:]')
CORE_DIR := "modules/$(CORE_NAME_LC)"
GEN_DIR := "${CORE_DIR}/generated"
ERROR_REP := "error.rpt"
TC_DIR := "${GEN_DIR}/synTestCases"

# Some gymnastics are required to get Firefox to behave nicely in both native
# Linux and containerized Linux environments through the use of profiles. Also
# included in the profiles are switches to prevent new user startup tabs.
FIREFOX_PROFILE_BASE = .firefox-profiles
FIREFOX = bash -c 'mkdir -p $(FIREFOX_PROFILE_BASE)/ff-$$$$ && \
    echo "user_pref(\"browser.startup.homepage_override.mstone\", \"ignore\");" > $(FIREFOX_PROFILE_BASE)/ff-$$$$/user.js && \
    echo "user_pref(\"datareporting.policy.dataSubmissionPolicyBypassNotification\", true);" >> $(FIREFOX_PROFILE_BASE)/ff-$$$$/user.js && \
    firefox --no-remote --profile $(FIREFOX_PROFILE_BASE)/ff-$$$$ "$$1" 2>/dev/null &' --

# Run everything and scan for errors
.PHONY: list
list:
	@grep '^[^#[:space:]].*:' Makefile

.PHONY: all
all: clean lint publish cov yosys docs ipf check

.PHONY: check
check: 
	@echo 
	@echo Checking for errors
	rm -rf ${ERROR_REP}
# lint (scalafix) and format (scalafmt) checks
	grep -Hn -E "\[error] scalafmt" lint.rpt | tee -a ${ERROR_REP} 
	grep -Hn -E "\[error] ---" lint.rpt | tee -a ${ERROR_REP} 
	grep -Hn -E "Error|error" lint.rpt | tee -a ${ERROR_REP}

# docs
	grep -Hn -E "Error|error" docs/doc.rpt | tee -a ${ERROR_REP}
	grep -Hn -E "Error|error" ${CORE_DIR}/docs/doc.rpt | tee -a ${ERROR_REP} 

# publish
	grep -Hn -E "Error|error|Warn|warn" ./docs/publish.rpt | tee -a ${ERROR_REP} 

# test and verilog reports
	grep -Hn -E "Error|error" ${GEN_DIR}/verilog.rpt | tee -a ${ERROR_REP} 
	grep -Hn -E "Error|error" ${GEN_DIR}/test.rpt | tee -a ${ERROR_REP} 
	grep -Hn -E "fail" ${GEN_DIR}/test.rpt | grep -v "failed 0" | tee -a ${ERROR_REP} 

# summary reports
	grep -Hn -E "Error|error" ${TC_DIR}/area_summary.rpt | tee -a ${ERROR_REP} 
	grep -Hn -E "Error|error" ${TC_DIR}/timing_summary.rpt | tee -a ${ERROR_REP} 

# synTestCases
	grep -Hn -E "Error|error" ${TC_DIR}/*/timing.rpt | tee -a ${ERROR_REP} 
	grep -Hn -E "Error|error" ${TC_DIR}/*/yosys.log | tee -a ${ERROR_REP} 

# check that IP Factory objects were correctly generated
	@if [ ! -f .ipf/filelist.f ]; then \
		printf "Error - Missing filelist file\n" | tee -a ${ERROR_REP}; \
	fi
	 @if [ ! -f .ipf/${CORE_NAME}.sv -a -f .ipf/${CORE_NAME}.v ]; then \
	printf "Error - Missing Verilog file\n" | tee -a ${ERROR_REP}; \
	fi
	@if [ ! -f .ipf/${CORE_NAME}.json ]; then \
		printf "Error - Missing JSON file\n" | tee -a ${ERROR_REP}; \
	fi
	@if [ ! -f .ipf/${CORE_NAME}.pdf ]; then \
		printf "Error - Missing User Guide\n" | tee -a ${ERROR_REP}; \
	fi
	@if [ ! -f .ipf/${CORE_NAME}.sdc  ]; then \
		printf "Error - Missing SDC file\n" | tee -a ${ERROR_REP}; \
	fi
	grep -q "8" .ipf/${CORE_NAME}.sv || echo "Error: Parameter not passed" >> ${ERROR_REP}

# existence of this file is an implicit check there are also Verilog files
	@if [ ! -f ".ipf/filelist.f" ]; then \
		printf "Error - Missing Verilog file list\n" | tee ${ERROR_REP}; \
	fi

# check for errors
	@if [ ! -s ${ERROR_REP} ]; then \
	  printf "\033[1;32mALL TESTS PASSED WITH NO ERRORS\033[0m\n" ;\
	else \
	  printf "\033[1;31mTESTS COMPLETED WITH ERRORS\033[0m\n" ;\
	fi

# Start with a fresh directory
.PHONY: clean
clean: 
	@echo Cleaning
	rm -rf docs/*.rpt
	rm -rf ipf.rpt
	rm -rf .ipf/*
	rm -rf lint.rpt
	rm -rf error.rpt
	rm -rf target
	rm -rf project/target
	rm -rf project/project 
	rm -rf ${CORE_DIR}/docs/*.rpt
	rm -rf ${CORE_DIR}/generated 
	rm -rf ${CORE_DIR}/target 
	rm -rf ${CORE_DIR}/project/project 
	rm -rf ${CORE_DIR}/project/target
	rm -rf $(FIREFOX_PROFILE_BASE)

# Run the tests with Scala code coverage enables
.PHONY: cov
cov:
	@echo Running tests with coverage enabled
	mkdir -p ${CORE_DIR}/generated
	sbt clean \
	coverageOn \
	"project core" \
	test \
	run  \
	"runMain org.chiselware.cores.o${ORG_ID}.t${TEAM_ID}.${CORE_NAME_LC}.GenVerWithParamCli -- --params='(width=1)'" \
	coverageReport | tee ${CORE_DIR}/generated/test.rpt
	rm -rf *.anno.json
	${FIREFOX} ${CORE_DIR}/generated/scalaCoverage/scoverage-report/index.html 2>/dev/null &

# Generate the documentation
.PHONY: docs
docs:
	@echo Building API docs
	sbt "project core" doc | tee docs/doc.rpt
	${FIREFOX} ${CORE_DIR}/target/scala-2.13/api/org/chiselware/cores/o${ORG_ID}/t${TEAM_ID}/${CORE_NAME_LC}/index.html 2>/dev/null &

	@echo Building User Guide
	cd ${CORE_DIR}/docs/user-guide && pdflatex ${CORE_NAME}.tex 
# Rerun to generate TOC
	cd ${CORE_DIR}/docs/user-guide && pdflatex ${CORE_NAME}.tex | tee -a ../doc.rpt 
# Clean up temp files
	cd ${CORE_DIR}/docs/user-guide && rm *.aux *.toc *.out *.log
	${FIREFOX} ${CORE_DIR}/docs/user-guide/${CORE_NAME}.pdf 2>/dev/null & 

.PHONY: ipf 
ipf:
# Generate IP Factory deliverables
	@echo Building artifacts for the IP Factory to download
	sbt "project core" "runMain org.chiselware.cores.o${ORG_ID}.t${TEAM_ID}.${CORE_NAME_LC}.GenVerWithParamCli -- --params='(width=8)'" | tee -a ipf.rpt 
# Copy the User Guide to the .ipf/ directory for upload
	cp -f ${CORE_DIR}/docs/user-guide/${CORE_NAME}.pdf .ipf/${CORE_NAME}.pdf
	rm -rf *.anno.json

# Run the scalafix and scalafmt linters
.PHONY: lint
lint: 
	rm -rf lint.rpt
	@echo Running scalafmt checks
	sbt "scalafmtCheck" | tee -a lint.rpt
	@echo Running scalafix lint checks
	sbt "Compile/scalafixAll --check" "Test/scalafixAll --check" | tee -a lint.rpt

# Publish locally
.PHONY: publish
publish: 
	@echo Publishing libraries locally
	sbt "project core" publishLocal | tee docs/publish.rpt

# Run the tests
.PHONY: test
test:
	@echo Running tests
	mkdir -p ${CORE_DIR}/generated
	sbt "project core" test | tee ${CORE_DIR}/generated/test.rpt
	rm -rf *anno.json

# Generate Verilog and synthesize
.PHONY: verilog
verilog:
	@echo Generate Verilog for synthesis
	mkdir -p ${CORE_DIR}/generated
	sbt "project core" run | tee ${CORE_DIR}/generated/verilog.rpt
	rm -rf *anno.json

# Run synthesis on generated Verilog; generate timing and area reports
.PHONY: yosys
yosys: 
	make verilog
	cd ${CORE_DIR}/generated/synTestCases && source run.sh
	echo "---------------------------------------------------------"
	echo "                      SUMMARY                            "
	echo "---------------------------------------------------------"
	cat ${CORE_DIR}/generated/synTestCases/{area,timing}_summary.rpt