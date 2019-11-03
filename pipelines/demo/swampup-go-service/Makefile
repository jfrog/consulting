SHELL := /bin/bash

export PROJECT_DIR = $(shell pwd)

.DEFAULT_GOAL = help

GO_MOD_REPO_RESOLVE ?= go
export GOCMD = xargs -I {} jfrog rt go {} $(GO_MOD_REPO_RESOLVE)

##   clean     Clean all files/dirs created by this makefile
.PHONY: clean
clean:
	@rm -rf target

##   build     Build all the modules in the project
.PHONY: build
build:
	@echo "build ./..." | ${GOCMD}

##   publish   Publish all the modules in the project to Artifactory
.PHONY: publish
publish:
	@jfrog rt go-publish go-local ${GO_COMMONS_VERSION} --build-name jfrog-go-commons --build-number ${BUILD_NUMBER}

##   help      Show this help
.PHONY: help
help: Makefile
	@printf "\
	Usage: make <goal>... [<variable>...] \n\
	\n\
	Makefile of Demo Project \n\
	\n\
	 Goals:\n"
	@sed -n 's/^##//p' $<
	@printf "\n"
	@printf "\
	Variables:\n\
     DEMO_VERSION=<version>  The version to set when publishing modules\n\
     BUILD_NUMBER=<number>         The build number to set when publishing modules\n\
     DEBUG=<true|false>            Whether to print debug information (default: false)\n"