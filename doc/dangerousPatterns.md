# Documenting Dangerous Patterns in JSON

## Advantages of Documenting Patterns in JSON

### Dynamic pattern loading

Updating `dangerousPatterns.json` allows for the addition of new patterns without needing to recompile the application. This file is dynamically loaded at each startup, ensuring the application is always up-to-date with the latest pattern definitions.

### Simplified Complexity

Users can define dangerous patterns in the JSON file without deep knowledge of the application's code base. This abstraction allows users to focus solely on the pattern structure, without needing to understand the intricacies of the detection algorithm.

## Pattern structure

Patterns in `dangerousPatterns.json` must include:

- **name**: `String` - Pattern name
- **requiredState**: `Integer` - The number of detectors that must match in a single file. Typically, this is `equal` to the number of described detectors.
- **dataFlow:**`Boolean` - A Boolean that toggles advanced dataflow detection.
- **detectors**:`List` - A list of detectors that define the dangerous pattern.

### Types of Detectors

Detectors are characterized by these common fields:

- **type**: `String` - Name of the detector.
- **methodName**: `String` - The target method or object name for detection.

**MethodCallDetector** - Identifies the presence of a method with a specified name in a Java source file.

**MethodArgumentDetector** - Builds upon the _MethodCallDetector_ with an additional field:

- **argumentPattern**: `String` - The specific argument that the method must include.

- **exactMatch**: `Boolean` - A Boolean that toggles if `argument` can start with *argumentPattern* or needs to be exact match.

**ObjectCreationDetector** - Detects the creation of an object with a specified name in a Java source file.

Example of `dangerousPatterns.json` file:

```
[
  ...
  {
    "name": "User provided link inside webview",
    "requiredState": 3,
    "dataFlow": true,
    "detectors": [
      {
        "type": "MethodCallDetector",
        "methodName": "getIntent"
      },
      {
        "type": "MethodArgumentDetector",
        "methodName": "getQueryParameter",
        "argumentPattern": "url",
        "exactMatch": "true"
      },
      {
        "type": "MethodCallDetector",
        "methodName": "loadUrl"
      }
    ]
  },
  ...
]
```
