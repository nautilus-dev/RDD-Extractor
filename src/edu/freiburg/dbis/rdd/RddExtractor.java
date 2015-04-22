/*
Copyright 2015, Philip Schledermann

This software is licensed to you under the Apache License, 
Version 2.0 (the "License"); you may not use this file 
except in compliance with the License. You may obtain 
a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
*/
package edu.freiburg.dbis.rdd;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.commons.cli.*;
/**
 * 
 */

/**
 * @author Philip Schledermann
 *
 */
public class RddExtractor {

	private static Logger logger = Logger.getLogger(RddExtractor.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String Classname = RddExtractor.class.getName();
		// Creating and parsing commandline options
		Options options = createOptions();
		CommandLineParser parser = new BasicParser();
		HelpFormatter help = new HelpFormatter();
		CommandLine cmd = null;
		
		try {
			cmd = parser.parse(options, args);
			if (cmd.hasOption("h")) {
				help.printHelp(Classname, options);
				return;
			}
			// XOR (either input-file or sparql-endpoint) --> Throw error if it is the same
			if (cmd.hasOption("i") == cmd.hasOption("e")) {
				help.printHelp(Classname, options);
				System.err.println("Use either the the option to read from an input file or (XOR) the option to send queries against an SPARQL Endpoint");
				return;
			}
			// the graph variable can only be used with the sparql-enpoint
			if ((cmd.hasOption("g")==true) && (cmd.hasOption("e")==false)) {
				help.printHelp(Classname, options);
				System.err.println("The graph variable can only be used when sending queries against a SPARQL Endpoint");
				return;
			}
			
		} catch (ParseException e) {
			help.printHelp(Classname, options);
			System.err.println("Command line parsing failed. Reason: "+e.getMessage());
			return;
		}
	
		String INPUT_FILE = cmd.getOptionValue("i"); 
		String OUTPUT_FILE = cmd.getOptionValue("o");
		String ENDPOINTURL = cmd.getOptionValue("e");
		String VIRTUOSO_GRAPH_NAME = cmd.getOptionValue("g");
		String WA = cmd.getOptionValue("w");
		String propertyFile = "src/main/resources/log4j.properties2";
		if (cmd.hasOption("v")) {
			propertyFile = "src/main/resources/log4j.properties";
		} else if (cmd.hasOption("l")) {
			propertyFile = cmd.getOptionValue("l");
		}
//		String propertyFile = (cmd.hasOption("l")) ? cmd.getOptionValue("l") : "src/main/resources/log4j.properties2";
				
		PropertyConfigurator.configure(propertyFile);
		
		logger.debug("INPUT FILE  : "+INPUT_FILE);
		logger.debug("OUTPUT FILE : "+OUTPUT_FILE);
		logger.debug("ENDPOINT URL: "+ENDPOINTURL);
		logger.debug("GRAPH NAME  : "+VIRTUOSO_GRAPH_NAME);
		logger.debug("WORLD ASSUMP: "+WA);
		
		
		long start = System.currentTimeMillis();
		long end = 0;
		logger.info("Starting The Generator");
		Backend back = new Backend(INPUT_FILE, OUTPUT_FILE, ENDPOINTURL, VIRTUOSO_GRAPH_NAME, WA);
		end = System.currentTimeMillis();
		logger.info("Runtime of Generator in ms: "+ (end-start));
	}

	private static Options createOptions() {
		Options options = new Options();
		options.addOption(OptionBuilder.withLongOpt("input-file").withDescription("Input file that contains the RDF graph").hasArg().create("i"));
		options.addOption(OptionBuilder.withLongOpt("output-file").withDescription("Output filename where the RDD should be written").isRequired().hasArg().create("o"));
		options.addOption(OptionBuilder.withLongOpt("sparql-enpoint").withDescription("URL of the sparql enpoint that should be queried").hasArg().create("e"));
		options.addOption(OptionBuilder.withLongOpt("graph-name").withDescription("Name of the Graph that should be queried (only works for virtuoso endpoints)").hasArg().create("g"));
		options.addOption(OptionBuilder.withLongOpt("WA").withDescription("Modeling a subset (OWA) or the full dataset (CWA)").isRequired().hasArg().create("w"));
		options.addOption(OptionBuilder.withLongOpt("log4j-properties").withDescription("log4j.properties file location if not using the default configuration.").hasArg().create("l"));
		options.addOption(OptionBuilder.withLongOpt("help").withDescription("show this help").hasArg().create("h"));
		options.addOption(OptionBuilder.withLongOpt("verbose").withDescription("use verbose mode").create("v"));
		return options;
	}

}
