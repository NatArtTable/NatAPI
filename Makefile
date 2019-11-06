.PHONY: help
.DEFAULT_GOAL := help

## Detect OS and pick the appropriate command for opening for opening files with the default program
ifeq ($(OS),Windows_NT)
    OPEN := start
else
    UNAME := $(shell uname -s)
    ifeq ($(UNAME),Linux)
        OPEN := xdg-open
    endif
    ifeq ($(UNAME_S),Darwin)
        OPEN := open
    endif
endif

help: ## Show this help message
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'

test:	## Run all tests
	sbt test

coverage: ## Run tests with coverage support and open the result in the browser
	sbt clean coverage test
	sbt coverageReport
	$(OPEN) "target/scala-2.12/scoverage-report/index.html"

build: ## Build with sbt-native-packager
	sbt compile stage
