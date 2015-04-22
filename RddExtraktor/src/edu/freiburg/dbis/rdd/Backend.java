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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.jena.riot.out.EscapeStr;
import org.apache.log4j.Logger;

import edu.freiburg.dbis.rdd.Tools.Prefix;
import edu.freiburg.dbis.rdd.Tools.PrefixReader;
import edu.freiburg.dbis.rdd.Tools.Prefixer;
import edu.freiburg.dbis.rdd.Tools.Properties;
import edu.freiburg.dbis.rdd.Tools.RDDWriter;
import edu.freiburg.dbis.rdd.Tools.rdfType;

/**
 * @author Philip Schledermann
 *
 */
public class Backend {
	private String ENDPOINTURL;
	private String INPUT_FILE;
	private String OUTPUT_FILE;
	private String VIRTUOSO_GRAPH_NAME;
	private String WORLD_ASSUMPTION;
	private static Logger logger = Logger.getLogger(Backend.class);	
	private static DBConnector database;
	// rdf:types
	private List<rdfType> types = new ArrayList<rdfType>();
	private Prefixer prefixMapper = new Prefixer();
	// all Prefixes as a String are here. Used to speed up query building
//	private String AllPrefixesForQueries() = new String();
	private String AllPrefixesForQueries() {
		String prefixList ="";
		List<Prefix> prefixes = prefixMapper.getPrefixList();
		for (Prefix pref: prefixes) {
			prefixList = prefixList.concat("PREFIX "+pref.getName()+": "+pref.getIri()+" ");
		}
		return prefixList;
	}
//	private String AllPrefixesForQueries() = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>";
	// Classes definition gets in here:
	private List<String> ClassesDefinition = new ArrayList<String>();
	// Properties definition gets in here:
	private List<String> PropertiesDefinition = new ArrayList<String>();
	// type list for properties:
	private List<Properties> propertiesConstraints = new ArrayList<Properties>();
	// Output file writer
	private RDDWriter writer;
	private String rdfTypePrefix = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"; 
	/**
	 * RDD generation backend
	 * @param inFolder folder where input file is located
	 * @param inFile input file name with type (tested only for *n3)
	 * @param outFile output file name with type (*.rdd)
	 * @param worldAssumption
	 */
	public Backend(String inFile, String outFile, String endpoint, String virtuosoGraphName, String worldAssumption) {
		logger.info("Backend initialization");
		INPUT_FILE = inFile;
		OUTPUT_FILE = outFile;
		ENDPOINTURL = endpoint;
		VIRTUOSO_GRAPH_NAME = virtuosoGraphName;
		WORLD_ASSUMPTION = worldAssumption;
		writer = new RDDWriter(OUTPUT_FILE);
		DBinitialize();
		logger.info("starting RDD extraction process");
		// search for all Prefixes in the file beginning and load them in the PrefixReader
//		GetPrefixes();
		logger.info("initialize prefixes, add rdf prefix as <http://www.w3.org/1999/02/22-rdf-syntax-ns#>");
		prefixMapper.addPrefix(new Prefix("rdf", "<http://www.w3.org/1999/02/22-rdf-syntax-ns#>"));
		
		// get all rdf:type and store them in types
		GetRdfTypes();
		// for each type in types now get all the properties
		GetPropertiesForTypes();
		// process cardinality of each type and save them in the properties
		GetConstraintsForProperties();
		
		// TODO: Implement the use of the PrefixMapper starting from here:
		
		// process CLASS section
		ProcessClassesSection();
		
		// get PROPERTIES, searching for constraints regardless of rdf:type
		// needs to be tested
		GetPropertiesRegardlessType();
		// process cardinality for each property regardless type
		ProcessCardinalityForPropertiesRegardlessType();
		
		// process PROPERTIES section
		ProcessPropertiesSection();
		// TODO: pathfinding (PATH constraint)
				
		// in the very end
		WriteRDD();
		logger.info("Finished");
	}
	private void ProcessCardinalityForPropertiesRegardlessType() {
		logger.info("performing query to to get cardinality of all properties regardless types");
		// for each property in each type do
		String[] ResultSet;
		String[] VerifyResultSet;
		String tempProperty;

			// for every property
			for (Properties prop : propertiesConstraints) {
				tempProperty = prop.getPrefixedName();
//				tempProperty = prop.getPropertyName();
				logger.debug("Processing property: "+tempProperty);
				// count the apperance of this property per node
				// 1. does all nodes have this Property?
				// 2. how many of this property has each node at least?
				// 3. how many of this property has each node at most?
				// 4. is it KEY?
				// 5. RANGE
				// 6. datatype
				
				// 1.
				ResultSet = database.performQuery(AllPrefixesForQueries() + "SELECT ?node WHERE {?node ?prop1 ?name1 MINUS {?node "+tempProperty+" ?name.}} LIMIT 1");
				if (ResultSet.length==1) {
					logger.debug(tempProperty+": not all have this property");
					prop.setMin(0);
					// TODO: remove the next line if also PARTIAL constraints should be added to Properties. And the loop must be altered too!
					prop.setMax(0);
					prop.setUnique(false);
				} else {
					logger.debug(tempProperty+": All have this property.");
					logger.debug(tempProperty+": getting MIN");
					// 2. Group by node (counting property per node)
					ResultSet = database.performQuery(AllPrefixesForQueries() + "SELECT (COUNT(?name) as ?count) WHERE {?node "+tempProperty+" ?name.} GROUP BY ?node ORDER BY ASC(?count) LIMIT 1");
					if (Integer.parseInt(ResultSet[0])==0) {
						logger.warn(tempProperty+": Not all have this property, this query or that before must be wrong!");
					} else {
						prop.setMin(Integer.parseInt(ResultSet[0]));
					}
					logger.debug(tempProperty+": getting MAX");
					// 3. Group by node (counting property per node)
					ResultSet = database.performQuery(AllPrefixesForQueries() + "SELECT (COUNT(?name) as ?count) WHERE {?node "+tempProperty+" ?name.} GROUP BY ?node ORDER BY DESC(?count) LIMIT 1");
					if (Integer.parseInt(ResultSet[0])==0) {
						logger.warn(tempProperty+": MAX Property is 0!, this query or these before must be wrong!");
					} else {
						prop.setMax(Integer.parseInt(ResultSet[0]));
					}
					// 4. KEY over all?
					if ((prop.getMax()==1) && (prop.getMin()==1)) {
						logger.debug("getting UNIQUE for Prop: "+tempProperty+" MIN: "+prop.getMin()+" MAX: "+prop.getMax());
						ResultSet = database.performQuery(AllPrefixesForQueries() + "SELECT ?name WHERE { ?node "+tempProperty+" ?name. ?node2 "+tempProperty+" ?name. FILTER (?node != ?node2)} LIMIT 2");
						// if ResultSet is empty, then the name is unique
						if (ResultSet.length==0) {
							prop.setUnique(true);
						} else {
							prop.setUnique(false);
						}
					} else {
						prop.setUnique(false);
					}
				}
				// 5. Range
				logger.info("performing queries to get RANGE information");
				ResultSet = database.performQuery(AllPrefixesForQueries() + "SELECT ?name1 WHERE {?node "+tempProperty+" ?name. ?name rdf:type ?name1.} GROUP BY ?name1 LIMIT 2");
//				ResultSet = database.performQuery(AllPrefixesForQueries() + "SELECT ?name1 WHERE { ?node rdf:type "+tempType+". ?node "+tempProperty+" ?name. ?name rdf:type ?name1.}");
				if (ResultSet.length==0) {
					logger.debug("no type information found");
				}else if (ResultSet.length>1) {
					logger.debug("Too many different types found, no RANGE property can be derived");
				}else if (ResultSet.length==1) {
					logger.debug("Checking whether the property "+tempProperty+" has the RANGE "+ResultSet[0]);
					String tempRange = ResultSet[0];
					// save old resultSet
					VerifyResultSet = database.performQuery(AllPrefixesForQueries() + " SELECT ?name1 WHERE {?node "+tempProperty+" ?name. ?name rdf:type ?name1. FILTER(?name1 != <"+tempRange+">) } LIMIT 1");
					if (VerifyResultSet.length==0) {
//						List<String> templist = new ArrayList<String>();
//						templist.add(tempRange);
//						List<Properties> tempProp = prefixMapper.cleanupProperties(templist, prefixes);
//						String temporaryRange = tempProp.get(0).getPrefix()+":"+tempProp.get(0).getPropertyName();
//						prop.setRange(temporaryRange);
						// cleanup the one-elemented list
						prop.setRange(prefixMapper.cleanupPropertyPrefixes(ResultSet).get(0).getPrefixedName());
						// prop.setRange("<"+tempRange+">");
						logger.debug("Has Range: "+tempRange);
					} else {
						logger.debug("No consistent Range type found");
					}
				}
				logger.info("performing queries to get type of the object");
				// 6. Type
				logger.debug("is IRI?");
				ResultSet = database.performQuery(AllPrefixesForQueries() + "SELECT DISTINCT ?name WHERE {?node "+tempProperty+" ?name. FILTER (!isIRI(?name)) } LIMIT 1");
				if (ResultSet.length==0) {
					prop.setType("IRI");
					logger.debug("is IRI!");
				}
				logger.debug("is BNODE?");
				ResultSet = database.performQuery(AllPrefixesForQueries() + "SELECT DISTINCT ?name WHERE {?node "+tempProperty+" ?name. FILTER (!isBlank(?name)) } LIMIT 1");
				if (ResultSet.length==0) {
					prop.setType("BNODE");
					logger.debug("is BNODE!");
				}			
				logger.debug("is LITERAL?");
				ResultSet = database.performQuery(AllPrefixesForQueries() + "SELECT DISTINCT ?name WHERE {?node "+tempProperty+" ?name. FILTER (!isLiteral(?name)) } LIMIT 1");
				if (ResultSet.length==0) {
					prop.setType("LITERAL");
					logger.debug("is LITERAL!");
				}
				logger.debug("datatype?");
				ResultSet = database.performQuery(AllPrefixesForQueries() + "SELECT DISTINCT ?type WHERE {?node "+tempProperty+" ?name. FILTER (datatype(?name) = ?type) } LIMIT 1");
				if (ResultSet.length==1) {
					String testDatatype = ResultSet[0];
					ResultSet = database.performQuery(AllPrefixesForQueries() + "SELECT DISTINCT ?type WHERE {?node "+tempProperty+" ?name. FILTER (!"+testDatatype+"(?name)) } LIMIT 1");
					if (ResultSet.length==0) {
						prop.setType("LITERAL("+testDatatype+")");
						logger.debug("datatype: "+testDatatype);	
					} else {
						prop.setType("LITERAL");
						logger.debug("is undefined datatype LITERAL ");
					}
				}
				// copied...
				logger.info("Prop: "+tempProperty+" MIN: "+prop.getMin());
				logger.info("Prop: "+tempProperty+" MAX: "+prop.getMax());
				logger.info("Prop: "+tempProperty+" UNIQUE: "+prop.getUnique().toString());
				logger.info("Prop: "+tempProperty+" TYPE: "+prop.getType());

			}
			
		
	}
	/**
	 * Retrieving all types (without rdf:type) for the PROPERTIES section
	 */
	private void GetPropertiesRegardlessType() {
		logger.info("performing query to to get all properties regardless rdf:type");
		// use propertiesConstraints to store properties
		String[] ResultSet;
//		List<String> TempResult = new ArrayList<String>();
		ResultSet = database.performQuery(AllPrefixesForQueries() + "SELECT DISTINCT ?name WHERE {?node ?name ?name2. FILTER (?name != rdf:type) }");
		logger.info("processing property predicates");
		for (String result : ResultSet) {
			logger.info("predicate : "+result+" found, that has no type");
//			TempResult.add(result);
		}
		logger.info("cleaning prefixes");
		propertiesConstraints=prefixMapper.cleanupPropertyPrefixes(ResultSet);
	}
	/**
	 * Processing the properties in the PROPERTIES section as String[] for the output-file
	 */
	private void ProcessPropertiesSection() {
		PropertiesDefinition.add(WORLD_ASSUMPTION+" PROPERTIES {");
		String tempType= "";
		String property = "";
		String constraintType = "";
		boolean PropertyTypeUsable = true;
		for (Properties prop : propertiesConstraints) {
			PropertyTypeUsable = true;
			if (prop.getMax()>1) {
				constraintType = ("MIN("+prop.getMin()+"), MAX("+prop.getMax()+")");
			} else if (prop.getMin()==0 &&  prop.getMax()==1) {
				constraintType = ("PARTIAL");
			} else if (prop.getMin()==1 && prop.getMax()==1 && !prop.getUnique()) {
				constraintType = "TOTAL";
			} else if (prop.getMin()==1 && prop.getMax()==1 && prop.getUnique()) {
				constraintType = "KEY";
			} else {
				PropertyTypeUsable = false;
//				constraintType = "ERROR WITH:";
			}
			// add the RANGE property if available
			if (prop.getRange() != null) {
				property = constraintType+", RANGE("+prop.getRange()+")";
			} else {
				property = constraintType;
			}
			// process Property Type
			if (prop.getType() == null) {
				tempType = "";
			} else {
				tempType = " : "+prop.getType();
			}
			if (PropertyTypeUsable) PropertiesDefinition.add("\t"+property+" "+prop.getPrefixedName()+tempType+";");
		}
		PropertiesDefinition.add("}");
	}
	/**
	 * Processing the properties in each class as String[] for the output-file
	 */
	private void ProcessClassesSection() {
		List<Properties> tempProperties;
		String tempType= "";
		String cardinality = "";
		String property = "";
//		int constraintTypeModel;
		// Classes will be Modeled as OWA, every Class as CWA
		ClassesDefinition.add(WORLD_ASSUMPTION+" CLASSES {");
		for (rdfType type : types) {
			// TODO: model SINGLETON
			// TODO: model SUBCLASSES
			// begin class
			ClassesDefinition.add("\t"+WORLD_ASSUMPTION+" CLASS " + type.getrdfType()+ " {");
//			ClassesDefinition.add("\t"+"OWA CLASS " + type.getPrefix()+":"+type.getType()+ " {");
			tempProperties = type.getProperties();
			for (Properties prop : tempProperties) {
				// 1st is MIN, 2nd is MAX, 3 is UNIQUE as INT in form X Y Z
				if (prop.getMin()>0 &&prop.getMax()>1) {
					cardinality = ("MIN("+prop.getMin()+"), MAX("+prop.getMax()+")");
				} else if (prop.getMin()==0 &&  prop.getMax()>1) {
					cardinality = ("MAX("+prop.getMax()+")");
				} else if (prop.getMin()==0 &&  prop.getMax()==1) {
					cardinality = ("PARTIAL");
				} else if (prop.getMin()==1 && prop.getMax()==1 && !prop.getUnique()) {
					cardinality = "TOTAL";
				} else if (prop.getMin()==1 && prop.getMax()==1 && prop.getUnique()) {
					cardinality = "KEY";
				} else {
					cardinality = "ERROR WITH:";
				}
				// add the RANGE property if available
				if (prop.getRange() != null) {
					property = cardinality+", RANGE("+prop.getRange()+")";
				} else {
					property = cardinality;
				}
				// process Property Type
				if (prop.getType() == null) {
					tempType = "";
				} else {
					tempType = " : "+prop.getType();
				}
				ClassesDefinition.add("\t"+"\t"+property+" "+prop.getPrefixedName()+tempType+";");
//				ClassesDefinition.add("\t"+"\t"+property+" "+prop.getPropertyName()+tempType+";");

			}
			
			// close Class
			ClassesDefinition.add("\t"+"}");
		}
		
		// in the end close Classes Section
		ClassesDefinition.add("}");
	}
	/**
	 * getting the cardinality for each property of each type
	 */
	private void GetConstraintsForProperties() {
		logger.info("performing query to to get cardinality of all properties for all types");
		// for each property in each type do
		String[] ResultSet;
		String[] VerifyResultSet;
		String tempType;
		String tempProperty;
//		List<Properties> tempProperties;
		// List<String> TempResult  = new ArrayList<String>();
		// for every type
		for (rdfType type : types) {
			tempType = type.getrdfType();
//			tempType = type.getType();
			logger.debug("Processing type: "+tempType);
//			tempProperties = type.getProperties();
			// for every property
			for (Properties prop : type.getProperties()) {
				tempProperty = prop.getPrefixedName();
//				tempProperty = prop.getPropertyName();
				logger.debug("Processing property: "+tempProperty);
				logger.debug("getting MIN for Prop: "+tempProperty+" Type: "+tempType);
				ResultSet = database.performQuery(AllPrefixesForQueries() + "SELECT (COUNT(?name) as ?count) WHERE { ?node rdf:type "+tempType+". OPTIONAL {?node "+tempProperty+" ?name.}} GROUP BY ?node ORDER BY ASC(?count) LIMIT 1");
				 if (ResultSet.length==1) {
						prop.setMin(Integer.parseInt(ResultSet[0]));
				} else {
					prop.setMin(0);
					logger.warn("Error getting MIN: There is no Prop: "+tempProperty+" Type: "+tempType+" But it was found in the first place!");
				}
				logger.debug("getting MAX for Prop: "+tempProperty+" Type: "+tempType);
				ResultSet = database.performQuery(AllPrefixesForQueries() + "SELECT (COUNT(?name) as ?count) WHERE { ?node rdf:type "+tempType+". ?node "+tempProperty+" ?name.} GROUP BY ?node ORDER BY DESC(?count) LIMIT 1");
				// if ResultSet is not empty, then Max is (the result)
				if (ResultSet.length==1) {
					prop.setMax(Integer.parseInt(ResultSet[0]));
				} else {
					prop.setMax(0);
					logger.warn("Error getting MAX: There is no Prop: "+tempProperty+" Type: "+tempType+" But it was found in the first place!" );

				}
				if ((prop.getMax()==1) && (prop.getMin()==1)) {
					logger.debug("getting UNIQUE for Prop: "+tempProperty+" Type: "+tempType+" MIN: "+prop.getMin()+" MAX: "+prop.getMax());
//					ResultSet = database.performQuery(AllPrefixesForQueries() + "SELECT ?node ?node2 WHERE { ?node rdf:type "+tempType+". ?node "+tempProperty+" ?name. ?node2 rdf:type "+tempType+". ?node2 "+tempProperty+" ?name. FILTER (?node != ?node2)} LIMIT 2");
					ResultSet = database.performQuery(AllPrefixesForQueries() + "SELECT ?name WHERE { ?node rdf:type "+tempType+". ?node "+tempProperty+" ?name. ?node2 rdf:type "+tempType+". ?node2 "+tempProperty+" ?name. FILTER (?node != ?node2)} LIMIT 2");
					// if ResultSet is empty, then the name is unique
					if (ResultSet.length==0) {
						prop.setUnique(true);
					} else {
						prop.setUnique(false);
					}
				} else {
					prop.setUnique(false);
				}
				// Process RANGE of constraints
				logger.info("performing queries to get RANGE information");
				ResultSet = database.performQuery(AllPrefixesForQueries() + "SELECT ?name1 WHERE { ?node rdf:type "+tempType+". ?node "+tempProperty+" ?name. ?name rdf:type ?name1.} GROUP BY ?name1 LIMIT 2");
//				ResultSet = database.performQuery(AllPrefixesForQueries() + "SELECT ?name1 WHERE { ?node rdf:type "+tempType+". ?node "+tempProperty+" ?name. ?name rdf:type ?name1.}");
				if (ResultSet.length==0) {
					logger.debug("no type information found");
				}else if (ResultSet.length>1) {
					logger.debug("Too many different types found, no RANGE property can be derived");
				}else if (ResultSet.length==1) {
					logger.debug("Checking whether the property "+tempProperty+" has the RANGE "+ResultSet[0]);
					String tempRange = ResultSet[0];
					// save old resultSet
					VerifyResultSet = database.performQuery(AllPrefixesForQueries() + " SELECT ?name1 WHERE {?node rdf:type "+tempType+". ?node "+tempProperty+" ?name. ?name rdf:type ?name1. FILTER(?name1 != <"+tempRange+">) } LIMIT 1");
					if (VerifyResultSet.length==0) {
//						List<String> templist = new ArrayList<String>();
//						templist.add(tempRange);
//						List<Properties> tempProp = prefixMapper.cleanupProperties(templist, prefixes);
//						String temporaryRange = tempProp.get(0).getPrefix()+":"+tempProp.get(0).getPropertyName();
//						prop.setRange(temporaryRange);
						// cleanup the one-elemented list
						prop.setRange(prefixMapper.cleanupPropertyPrefixes(ResultSet).get(0).getPrefixedName());
						// prop.setRange("<"+tempRange+">");
						logger.debug("Has Range: "+tempRange);
					} else {
						logger.debug("No consistent Range type found");
					}
				}
				
				logger.info("performing queries to get type of the object");

				logger.debug("is IRI?");
				ResultSet = database.performQuery(AllPrefixesForQueries() + "SELECT DISTINCT ?name WHERE { ?node rdf:type "+tempType+". ?node "+tempProperty+" ?name. FILTER (!isIRI(?name)) } LIMIT 1");
				if (ResultSet.length==0) {
					prop.setType("IRI");
					logger.debug("is IRI!");
				}
				logger.debug("is BNODE?");
				ResultSet = database.performQuery(AllPrefixesForQueries() + "SELECT DISTINCT ?name WHERE { ?node rdf:type "+tempType+". ?node "+tempProperty+" ?name. FILTER (!isBlank(?name)) } LIMIT 1");
				if (ResultSet.length==0) {
					prop.setType("BNODE");
					logger.debug("is BNODE!");
				}			
				logger.debug("is LITERAL?");
				ResultSet = database.performQuery(AllPrefixesForQueries() + "SELECT DISTINCT ?name WHERE { ?node rdf:type "+tempType+". ?node "+tempProperty+" ?name. FILTER (!isLiteral(?name)) } LIMIT 1");
				if (ResultSet.length==0) {
					prop.setType("LITERAL");
					logger.debug("is LITERAL!");
				}
				// TODO: Does not work here:
				logger.debug("datatype?");
				ResultSet = database.performQuery(AllPrefixesForQueries() + "SELECT DISTINCT ?type WHERE { ?node rdf:type "+tempType+". ?node "+tempProperty+" ?name. FILTER (datatype(?name) = ?type) } LIMIT 1");
				if (ResultSet.length==1) {
					String testDatatype = ResultSet[0];
					ResultSet = database.performQuery(AllPrefixesForQueries() + "SELECT DISTINCT ?type WHERE { ?node rdf:type "+tempType+". ?node "+tempProperty+" ?name. FILTER (!"+testDatatype+"(?name)) } LIMIT 1");
					if (ResultSet.length==0) {
						prop.setType("LITERAL("+testDatatype+")");
						logger.debug("datatype: "+testDatatype);	
					} else {
						prop.setType("LITERAL");
						logger.debug("is undefined datatype LITERAL ");
					}
				}
				logger.info("Prop: "+tempProperty+" CLASS: "+tempType+" MIN: "+prop.getMin());
				logger.info("Prop: "+tempProperty+" CLASS: "+tempType+" MAX: "+prop.getMax());
				logger.info("Prop: "+tempProperty+" CLASS: "+tempType+" UNIQUE: "+prop.getUnique().toString());
				logger.info("Prop: "+tempProperty+" CLASS: "+tempType+" TYPE: "+prop.getType());
				
				
			}
		}
	}
	/**
	 * getting all properties (including prefix cleanup) for each type
	 */
	private void GetPropertiesForTypes() {
		logger.info("performing query to to get all properties for types");
		String[] ResultSet;
		String TempType;
		List<String> TempResult;
		for (int i=0; i<types.size(); i++) {
			rdfType type = types.get(i);
			TempResult  = new ArrayList<String>();
			TempType = type.getrdfType();
//			TempType = type.getType();
			logger.info("Processing type: "+TempType);
			String prefixList = AllPrefixesForQueries();
			ResultSet = database.performQuery(prefixList+ "\n SELECT DISTINCT ?name WHERE { ?node rdf:type "+TempType+". ?node ?name ?name2. FILTER (?name != rdf:type) }");
			logger.info("Processing result");
			for (String result : ResultSet) {
				logger.debug("Property found  : "+result);
				TempResult.add(result);
			}
//			type.setProperties(prefixMapper.cleanupProperties2(TempResult, prefixes));
			type.setProperties(prefixMapper.cleanupPropertyPrefixes(ResultSet));
			types.set(i, type);
		}
		// no read only access...
//		for (rdfType type : types) {
//			TempResult  = new ArrayList<String>();
//			TempType = type.getrdfType();
////			TempType = type.getType();
//			logger.info("Processing type: "+TempType);
//			String prefixList = AllPrefixesForQueries();
//			ResultSet = database.performQuery(prefixList+ "\n SELECT DISTINCT ?name WHERE { ?node rdf:type "+TempType+". ?node ?name ?name2. FILTER (?name != rdf:type) }");
//			logger.info("Processing result");
//			for (String result : ResultSet) {
//				logger.debug("Property found  : "+result);
//				TempResult.add(result);
//			}
//			// needed for serialization... strange
//			// TODO: Have a look at the next line!
//			// TODO: rework above procedure
//			// TODO: use prefixer here.
////			type.setProperties(prefixMapper.cleanupProperties2(TempResult, prefixes));
//			type.setProperties(prefixMapper.cleanupPropertyPrefixes(ResultSet));
//		}
		logger.info("All Properties for the classes detected.");
		// each property has a prefix(Prefix), popertyname(String), and properties like MIN(int), MAX(int), UNIQUE(bool), TYPE(String)
		
	}

	/**
	 * Database initialization
	 */
	private void DBinitialize(){
		logger.info("setting up database connection");
		database = new DBConnector();
		if (ENDPOINTURL != null && VIRTUOSO_GRAPH_NAME == null) {
			logger.info("Using sparql-endpoint: "+ENDPOINTURL);
			database.DBConnectorEndpoint(ENDPOINTURL);
		} else if (ENDPOINTURL != null && VIRTUOSO_GRAPH_NAME != null){
			logger.info("Using sparql-endpoint: "+ENDPOINTURL);
			logger.info("Using graph: "+VIRTUOSO_GRAPH_NAME);
			database.DBConnectorVirtuosoEndpointGraph(ENDPOINTURL, VIRTUOSO_GRAPH_NAME);
		} else if (ENDPOINTURL == null && INPUT_FILE != null) {
			logger.info("Using file to read: "+INPUT_FILE);
			database.DBConnectorFile(INPUT_FILE);
		} else {
			logger.error("Nothing to do. Neither URL not InputFile given.");
			System.exit(0);
		}
	}
	/**
	 * load Prefixes from input file
	 * WARNING:DEPRECATED
	 */
//	private void GetPrefixes() {
//		prefixes.LoadNProcess(INPUT_FILE);
//		String tempString = "";
//		List<Prefix> tempPrefixes = prefixes.getCleanPrefixes();
//		for (Prefix pref : tempPrefixes) {
//			tempString = "PREFIX "+pref.getName()+": "+pref.getIri()+" ";
//			// AllPrefixesForQueries().concat(tempString);
//			AllPrefixesForQueries() = AllPrefixesForQueries() + tempString;
//		}
//		logger.info("prefixes in a line: "+AllPrefixesForQueries());
//	}
	/**
	 * get all rdf:type and process them 
	 */
	private void GetRdfTypes() {
		logger.info("performing query to to get all rdf:types");
		String[] ResultSet;
		rdfType[] ResultTypesCleaned;
//		ResultSet = database.performQuery(rdfTypePrefix+"\n SELECT DISTINCT ?name WHERE { ?node rdf:type ?name. }");
		ResultSet = database.performQuery(rdfTypePrefix,"SELECT DISTINCT ?name","WHERE { ?node rdf:type ?name. }");
		logger.info("divide result in prefix and name");
		ResultTypesCleaned = prefixMapper.cleanupResultSet(ResultSet);
		for (int i=0;i<ResultTypesCleaned.length;i++) {
			logger.info("Found and cleaned rdf:type "+ResultTypesCleaned[i].getrdfType());
			types.add(ResultTypesCleaned[i]);

		}

		// give Prefixing object the iri list
		// Prefixer refreshes its list of prefixes
		// and returns the list of cleaned types to be added to the type list.
		
//		logger.info("remapping prefixes of rdf:type");
//		types = prefixMapper.cleanup(types, prefixes);
		logger.info("mapping of prefixes succesful");
	}
	
	/**
	 * Processing RDD to write it
	 */
	private void WriteRDD() {
		logger.info("generate RDD file with prefixes, and Class definition, a.s.o.");
		writer.addPrefix(prefixMapper.getPrefixList());
		writer.addNewline();
		writer.addClasses(ClassesDefinition);
		writer.addNewline();
		writer.addPropoperties(PropertiesDefinition);
		writer.writeFile();
	}
	
	
}
