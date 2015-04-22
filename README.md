# RDD-Extractor
This project is an extractor of RDD (RDF Data Description) from RDF datasets.

For more information about RDD visit: http://dbis.informatik.uni-freiburg.de/forschung/projekte/rdd/
There you also find the SP2bench tool to generate rdf datasets to use with the RDD-Extractor

## Preconditions
This is tested with java 8 targeting compatibility with java 7.

## Usage
For standalone usage run
`java -jar RDD-extractor-0.0.2.jar`

Commandline arguments are displayed when invoked without them:
```
 -e,--sparql-enpoint <arg>     URL of the sparql enpoint that should be
                               queried
 -g,--graph-name <arg>         Name of the Graph that should be queried
                               (only works for virtuoso endpoints)
 -h,--help <arg>               show this help
 -i,--input-file               Input file that contains the RDF graph
 -l,--log4j-properties <arg>   log4j.properties file location if not using
                               the default configuration.
 -o,--output-file <arg>        Output filename where the RDD should be
                               written
 -v,--verbose                  use verbose mode
 -w,--WA <arg>                 Modeling a subset (OWA) or the full dataset
                               (CWA)
```                               
o and w Properties are mandatory.
          
For a standalone test you can use the preconfigured and bundeled configurations:
`java -jar RDD-extractor-0.0.2.jar -i SIMPLE_example.n3 -o output.rdd -w OWA`
 
## Building
This is a maven project, you can clean/build/package/test the application with:
`mvn clean package exec:exec`