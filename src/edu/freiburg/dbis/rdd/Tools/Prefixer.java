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
package edu.freiburg.dbis.rdd.Tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

import edu.freiburg.dbis.rdd.PrefixedIRI;

/**
 * @author Philip Schledermann
 *
 */
public class Prefixer {
	private ArrayList<Prefix> prefixes = new ArrayList<Prefix>();
	private List<rdfType> types;
	private List<Properties> props;
	private static Logger logger = Logger.getLogger(Prefixer.class);	

	public Prefixer() {
		
	}
	public void addPrefix(Prefix pref) {
		prefixes.add(pref);
	}
	/*
	 * Returns the complete Prefix list as a single String seperated with newline sequences
	 */
	public String getPrefixListAsString() {
		String prefixList ="";
		for (int i=0; i<prefixes.size();i++) {
			Prefix tmpPrefix = prefixes.get(i); 
			prefixList.concat("PREFIX "+tmpPrefix.getName()+": "+tmpPrefix.getIri()+"\n");
		}
		return prefixList;
	}
	/*
	 * Returns the complete Prefix list as list of Prefixes
	 */
	public List<Prefix> getPrefixList() {
		return prefixes;
	}
	
//	public List<rdfType> cleanup(List<rdfType> rawTypes, PrefixReader inputPrefixes) {
//		prefixes = inputPrefixes.getCleanPrefixes();
//		String tempIRI;
//		String tempPrefix;
//		types = new ArrayList<rdfType>();
//		// check for each rawType if there is a prefix
//		for (int i=0; i<rawTypes.size(); i++) {
//			// iterate over all prefixes
//			for (int j=0; j<prefixes.size(); j++) {
//				tempIRI = prefixes.get(j).getIri();
//				tempPrefix = prefixes.get(j).getName();
//				tempIRI = tempIRI.substring(1, tempIRI.length()-1);
//				if(rawTypes.get(i).getType().startsWith(tempIRI)){
//					// String manipulation may not be quick
//					types.add(new rdfType(new PrefixedIRI(tempPrefix, tempIRI), rawTypes.get(i).getType().substring(tempIRI.length())));
//					
//				}
//			}
//			
//		}
//		return types;
//		
//	}
	// generate a property with a clean prefix and a name.
//	public List<Properties> cleanupProperties(List<String> TempResult, PrefixReader inputPrefixes) {
//		props = new ArrayList<Properties>();
//		prefixes = inputPrefixes.getCleanPrefixes();
//		String tempIRI;
//		String tempPrefix;
//		
//		// input is a List of String that should be cleaned up, like Raw Types
//		
//		// check for each result if there is a prefix
//		// Error handling if prefix is not found...?
//		for (int i=0; i<TempResult.size(); i++) {
//			// iterate over all prefixes
//			for (int j=0; j<prefixes.size(); j++) {
//				tempIRI = prefixes.get(j).getIri();
//				tempPrefix = prefixes.get(j).getName();
//				tempIRI = tempIRI.substring(1, tempIRI.length()-1);
//				if(TempResult.get(i).startsWith(tempIRI)){
//					// String manipulation may not be quick
//					props.add(new Properties(tempPrefix, TempResult.get(i).substring(tempIRI.length())));
//				}
//				logger.info("tempPrefix: "+tempPrefix+" tempName: "+TempResult.get(i));
//			}
//		}
//		return props;
//	}

//	public List<Properties> cleanupProperties2(List<String> tempResult,
//			PrefixReader prefixes2) {
//		props = new ArrayList<Properties>();
//		prefixes = prefixes2.getCleanPrefixes();
//		String tempIRI;
//		String tempPrefix;
//		
//		// input is a List of String that should be cleaned up, like Raw Types
//		
//		// check for each result if there is a prefix
//		// Error handling if prefix is not found...?
//		for (int i=0; i<tempResult.size(); i++) {
//			props.add(new Properties("", "<"+tempResult.get(i)+">"));
//			}
//		return props;
//	}
	public List<Properties> cleanupPropertyPrefixes(String[] resultSet) {
		ArrayList<Properties> cleanProperties = new ArrayList<Properties>();
//		TODO: Fill content here.
		
		CharSequence hash = "#";
		ArrayList<String> retrievedPrefixes = new ArrayList<String>();
		ArrayList<String> retrivedPrefixNames = new ArrayList<String>();
		ArrayList<String> retrievedPropertyNames = new ArrayList<String>();
		ArrayList<PrefixedIRI> retrievedPrefixesIRI = new ArrayList<PrefixedIRI>();

		// stage 1   : chop result set using standard procedure (using either the last "#"or "/" symbol as seperator)
		for (int i=0; i<resultSet.length; i++) {
			String iri = resultSet[i];
			String tmpPrefix ="";
			String tmpPropName ="";
			String seperator ="";
			// check if contains "#"
			if (iri.contains(hash)) {
				logger.info("hash found --> using # as seperator for prefix:name");
				seperator = hash.toString();
			} else {
				logger.info("no hash found --> using last / as seperator for prefix:name");
				seperator = "/";
			}
			tmpPrefix = iri.substring(0,iri.lastIndexOf(seperator)+1);
			tmpPropName = iri.substring(iri.lastIndexOf(seperator)+1,iri.length());
			retrievedPrefixes.add("<"+tmpPrefix+">");
			String tmpPrefixName = tmpPrefix.substring(0, tmpPrefix.length()-1).replace("/", "").replace(":", "").replace(".", "");
			retrivedPrefixNames.add(tmpPrefixName);
			retrievedPropertyNames.add(tmpPropName);
			logger.info("found prefix "+tmpPrefixName+" with iri "+"<"+tmpPrefix+">");
			
			// if prefixes are not containing the signature already then copy over:
			Prefix tmpPrefixInstance = null;
			logger.info("prefix array size: "+prefixes.size());
			Boolean PrefixKnown = false; 
			for (int j=0; j<prefixes.size();j++) {
				PrefixKnown = false;
				logger.info("Prefix name to check: "+prefixes.get(j).getName());
				logger.info("Found prefix to compare: "+tmpPrefixName);
				// this here is not working... It will check the rdf type first and will always create a new prefix
				if (prefixes.get(j).getName().equals(tmpPrefixName)) {
					PrefixKnown = true;
					logger.debug("Found Prefix "+tmpPrefixName+" with iri <"+tmpPrefix+"> in the prefix list.");
					break;
				} 
			}
			if (!PrefixKnown) {
				prefixes.add(new Prefix(tmpPrefixName, "<"+tmpPrefix+">"));
				logger.debug("added Prefix "+tmpPrefixName+" with iri <"+tmpPrefix+"> to the prefix list.");
			}
			cleanProperties.add(new Properties(tmpPrefixName, tmpPropName));
		}
		
		// with this method only the prefix and the ProptertyName is set to a property.
		return cleanProperties;
	}
	/*
	 * Function to clean a resultSet and return rdfType Array
	 */
	public rdfType[] cleanupResultSet(String[] resultSet) {
		ArrayList<rdfType> cleanProperties = new ArrayList<rdfType>();
		CharSequence hash = "#";
		ArrayList<String> retrievedPrefixes = new ArrayList<String>();
		ArrayList<String> retrivedPrefixNames = new ArrayList<String>();
		ArrayList<String> retrievedPropertyNames = new ArrayList<String>();
		ArrayList<PrefixedIRI> retrievedPrefixesIRI = new ArrayList<PrefixedIRI>();

		// stage 1   : chop result set using standard procedure (using either the last "#"or "/" symbol as seperator)
		for (int i=0; i<resultSet.length; i++) {
			String iri = resultSet[i];
			String tmpPrefix ="";
			String tmpPropName ="";
			String seperator ="";
			// check if contains "#"
			if (iri.contains(hash)) {
				logger.info("hash found --> using # as seperator for prefix:name");
				seperator = hash.toString();
			} else {
				logger.info("no hash found --> using last / as seperator for prefix:name");
				seperator = "/";
			}
			tmpPrefix = iri.substring(0,iri.lastIndexOf(seperator)+1);
			tmpPropName = iri.substring(iri.lastIndexOf(seperator)+1,iri.length());
			retrievedPrefixes.add("<"+tmpPrefix+">");
			String tmpPrefixName = tmpPrefix.substring(0, tmpPrefix.length()-1).replace("/", "").replace(":", "").replace(".", "");
			retrivedPrefixNames.add(tmpPrefixName);
			retrievedPropertyNames.add(tmpPropName);
			logger.info("found prefix "+tmpPrefixName+" with iri "+"<"+tmpPrefix+">");
			
			// if prefixes are not containing the signature already then copy over:
			logger.info("prefix array size: "+prefixes.size());
			Boolean PrefixKnown = false; 
			for (int j=0; j<prefixes.size();j++) {
				PrefixKnown = false;
				logger.info("Prefix name to check: "+prefixes.get(j).getName());
				logger.info("Found prefix to compare: "+tmpPrefixName);
				// this here is not working... It will check the rdf type first and will always create a new prefix
				if (prefixes.get(j).getName().equals(tmpPrefixName)) {
					PrefixKnown = true;
					logger.debug("Found Prefix "+tmpPrefixName+" with iri <"+tmpPrefix+"> in the prefix list.");
					break;
				} 
			}
			if (!PrefixKnown) {
				prefixes.add(new Prefix(tmpPrefixName, "<"+tmpPrefix+">"));
				logger.debug("added Prefix "+tmpPrefixName+" with iri <"+tmpPrefix+"> to the prefix list.");
			}
			retrievedPrefixesIRI.add(new PrefixedIRI(tmpPrefixName, "<"+tmpPrefix+">"));
			
		}
		
//		// copy over prefixes
//		for (int i=0; i<retrivedPrefixNames.size(); i++) {
//			Prefix tempPrefix = new Prefix(retrivedPrefixNames.get(i), retrievedPrefixes.get(i));
//			
//			// the if will not work here.
//			if (!prefixes.contains(tempPrefix)) {
//				String prefixName = prefix.substring(0, prefix.length()-1).replace("/", "").replace(":", "").replace(".", "");
//				prefixes.add(new Prefix(prefixName, "<"+prefix+">"));
//				retrievedPrefixesIRI.add(new PrefixedIRI(prefixName, "<"+prefix+">"));
//			}
//		}
		
		// add property to the property list

		for (int i=0; i< retrievedPropertyNames.size(); i++) {
			// TODO: extend the definition here to use the right prefix! ==> ?
			cleanProperties.add(new rdfType(retrievedPrefixesIRI.get(i), retrievedPropertyNames.get(i)));
		}
		
		// copy over to Array
		
		rdfType[] cleanPropertiesAsArray= new rdfType[cleanProperties.size()];
		for (int i=0; i<cleanProperties.size();i++) {
			cleanPropertiesAsArray[i] = cleanProperties.get(i);
		}
		
		return cleanPropertiesAsArray;
	}
}
