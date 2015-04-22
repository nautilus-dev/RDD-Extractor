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

import edu.freiburg.dbis.rdd.PrefixedIRI;

/**
 * @author Philip Schledermann
 *
 */
public class rdfType {

	// each rdfType has a List of properties
	private List<Properties> properties = new ArrayList<Properties>();
	/**
	 * @return the properties
	 */
	public List<Properties> getProperties() {
		return properties;
	}

	/**
	 * @param properties the properties to set
	 */
	public void setProperties(List<Properties> properties) {
		this.properties = properties;
	}
	
	public void addProperty(Properties prop) {
		properties.add(prop);
	}
	// A rdf:type has a name and an IRI
	private PrefixedIRI prefix;
	private String type;
	
	public PrefixedIRI getPrefix() {
		return prefix;
	}

	public void setPrefix(PrefixedIRI prefix) {
		this.prefix = prefix;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public rdfType(PrefixedIRI prefix, String type) {
		setPrefix(prefix);
		setType(type);
	}
	
	public rdfType(String type) {
		setType(type);
		setPrefix(null);
	}
	
	public rdfType() {
	}
	/**
	 * returns the complete rdf:type
	 * @return "prefix:type"
	 */
	public String getrdfType() {
		return prefix.getPrefix().concat(":").concat(type);
	}

}
