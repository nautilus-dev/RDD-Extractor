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
import java.util.List;

import org.apache.log4j.Logger;
/**
 * @author Philip Schledermann
 *
 */
public class CLAZZ {
	private static Logger logger = Logger.getLogger(CLAZZ.class);

	// a rdf:type ?any has a prefixed Iri, and some properties
	private PrefixedIRI name;
	private List<Props> props;
	/**
	 * Setter in the final format
	 * @param name
	 * @param props
	 */
	public CLAZZ(PrefixedIRI name, List<Props> props) {
		this.name = name;
		this.props = props;
	}
	/**
	 * Complete Setter
	 * @param name
	 * @param props
	 */
	public CLAZZ(String name, String[] props){
		setName(name);
		setProps(props);
	}
	/**
	 * Setter with the name only
	 * @param name
	 */
	public CLAZZ(String name){
		setName(name);
	}
	public void setName(String name){
		String[] typeSplit = name.split(":");
		if (typeSplit.length != 2) {
			logger.warn("rdf:type has length: "+typeSplit.length);
		}else {
			logger.info("rdf:type has length: "+typeSplit.length);
		}
		this.name = new PrefixedIRI(typeSplit[0], typeSplit[1]);
	}
	
	public void setProps(String[] props) {
		// storing props
		for (String prop : props) {
			this.props.add(new Props(prop));
		}
	}
}
