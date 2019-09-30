# Getting Started

## Introduction

A demo project showing the pros and cons of using the **Project Reactor Context** to *propagate and access data transparently through reactive pipelines*, as opposed to polluting/altering method signatures with unnecessary local variables or messy tuples.

Check out the [MultiplesController.java](src/main/java/it/dsibilio/reactorctxdemo/api/MultiplesController.java) and the [PrefixingService.java](src/main/java/it/dsibilio/reactorctxdemo/service/PrefixingService.java) classes, showing propagation of data through usage of:

- **The Good:** Reactor Context *(MultiplesController#getMultiples)*
- **The Bad:** Local Variables *(MultiplesController#getMultiplesWithLocalVarPrefix)*
- **The Ugly:** Tuples *(MultiplesController#getMultiplesWithTuplesPrefix)*

## Reference Documentation
For further reference, please consider the following resources:

* [Project Reactor Docs - Context](https://projectreactor.io/docs/core/release/reference/#context)
