# Dangerous patterns detectors

Our approach to pattern detection leverages the `JavaParser` library, transforming each `Java` file into an `Abstract Syntax Tree (AST)`, referred to as `AST` from now on in this documentation.

## Basic pattern detector

The foundation of our pattern detection is syntax-based, code scanning for specific `methods`, `objects`, `arguments`, and their occurrences within the code. This methodology allows researchers to pinpoint particular segments of a program for in-depth analysis.

A standout feature of our detector is its capability to dynamically incorporate dangerous patterns from a `json` file. The structure and details of the `dangerousPatterns.json` file are outlined [here](./dangerousPatterns.md).

Designed with researchers in mind, our basic pattern detector flags code segments, for further analysis by interested parties. It is important to note that it does not assess the interconnections between different code elements.

## Dataflow pattern detector

Our advanced pattern detector is a `superset` of our basic pattern detector.

#### Objectives of the Dataflow Detector

The primary aim of our dataflow detector is to ascertain the existence of any correlation between the `initial` and `final` methods described in the dangerous pattern, aiming to minimize false positives where the identified methods do not form dangerous pattern.

#### Tracking dataflow between two methods

The `dataflow` detector traces potential data exchanges between two methods through the following procedure:

1. Capture the output of the `initial method` (source) and add it to a tracking list.
1. Continuously monitor the utilization of variables within this tracking list.
1. Append the outcome variables of expressions that uses these variables to the list.
1. Recursively apply step 2 to the newly included variables.
1. Confirm whether any variables in the list are utilized by the `final method` (sink).
