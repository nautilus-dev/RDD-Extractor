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
public class Properties {
	// each property has a prefix(String), popertyname(String), and properties like MIN(int), MAX(int), UNIQUE(bool), TYPE(String)
	private String prefix;
	private String propertyName;
	private int min;
	private int max;
	private Boolean unique;
	private String type;
	private String range;
	
	
	public String getPrefixedName(){
		return prefix+":"+propertyName;
	}
	
	/**
	 * @return the prefix
	 */
	public String getPrefix() {
		return prefix;
	}
	/**
	 * @param prefix the prefix to set
	 */
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	/**
	 * @return the propertyName
	 */
	public String getPropertyName() {
		return propertyName;
	}
	/**
	 * @param propertyName the propertyName to set
	 */
	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}
	/**
	 * @return the min
	 */
	public int getMin() {
		return min;
	}
	/**
	 * @param min the min to set
	 */
	public void setMin(int min) {
		this.min = min;
	}
	/**
	 * @return the max
	 */
	public int getMax() {
		return max;
	}
	/**
	 * @param max the max to set
	 */
	public void setMax(int max) {
		this.max = max;
	}
	/**
	 * @return the unique
	 */
	public Boolean getUnique() {
		return unique;
	}
	/**
	 * @param unique the unique to set
	 */
	public void setUnique(Boolean unique) {
		this.unique = unique;
	}
	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}
	/**
	 * 
	 * @return the range type
	 */
	public String getRange() {
		return range;
	}
	/**
	 * 
	 * @param type the range type to set
	 */
	public void setRange(String range) {
		this.range = range;
	}	
	public Properties() {
		
	}
	public Properties(String prefix, String propertyName, int min, int max, Boolean unique, String type, String range) {
		this.prefix = prefix;
		this.propertyName = propertyName;
		this.min = min;
		this.max = max;
		this.unique = unique;
		this.type = type;
		this.range = range;
	}
	public Properties(String prefix, String propertyName) {
		this.prefix = prefix;
		this.propertyName = propertyName;
	}


}
