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

/**
 * @author Philip Schledermann
 *
 */
public class PrefixedIRI {

	
	// ONE HAS THE COLON ":"!!!
	
	private String prefix;
	private String type;
	/**
	 * Set prefixed IRI
	 * @param prefix
	 * @param type
	 */
	public PrefixedIRI(String prefix, String type) {		
	this.prefix = prefix;
	this.type = type;
	}
	/**
	 * Get prefix
	 * @return the prefix
	 */
	public String getPrefix() {
		return prefix;
	}
	/**
	 * Get Type
	 * @return the type
	 */
	public String getType() {
		return type;
	}
	/**
	 * Set refix
	 * @param prefix
	 */
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	/**
	 * Set type
	 * @param type
	 */
	public void setType(String type) {
		this.type = type;
	}
}
