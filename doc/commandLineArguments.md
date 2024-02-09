# Command line arguments

Command line arguments are inputs provided by the user when running a program from a command line or terminal. They enable the user to customize the program's operation, such as specifying a file to process or a mode of operation, without changing the program's code.

Our application supports these command line arguments:

- Any value not preceded by a `-flag` is considered a path to the application.
  _e. g._ `java -jar sus.jar path/to/apk`

- `-mt` - Toggles multithreading support.
  _e. g._ `java -jar sus.jar -mt`

- `-outpdf` - Expects the user to input a path for the analysis pdf file save location
  _e. g._ `java -jar sus.jar -outpdf PathToSavePdf`

- `-v` - Toggles verbose output of conversion and parsing processes. Default value is set to false
  _e. g._ `java -jar sus.jar -v`
  
- `-outdir` - Expects the user to input a path for the APK extraction location
  _e. g._ `java -jar sus.jar -outdir PathToExtractApk`

- `-help` - Outputs the user manual for all available flags
  _e. g._ `java -jar sus.jar -help`

- `-analyze` - Expects the user to input a path to a directory of already converted files that are ready to be analyzed.
_e. g._ `java -jar sus.jar -analyze PathToAnalysisDir`

- `-json` - Expects the user to input a path for the json file containing dangerous patterns
  _e. g._  `java -jar sus.jar -json PathToJson`

- `-forceAnalysis` - Forces analysis of APK even if it exists in the database
  _e. g._  `java -jar sus.jar -forceAnalysis`
