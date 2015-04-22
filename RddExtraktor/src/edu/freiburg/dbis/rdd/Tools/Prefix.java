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

/**
 * @author Philip Schledermann
 *
 */
public class Prefix {

	// A prefix has a name and an IRI
	private String name;
	private String iri;
	
	public Prefix(String name, String iri) {
		setName(name);
		setIri(iri);
	}
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIri() {
		return iri;
	}

	public void setIri(String iri) {
		this.iri = iri;
	}

}
