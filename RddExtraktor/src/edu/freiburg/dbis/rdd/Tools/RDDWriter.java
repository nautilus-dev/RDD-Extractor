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

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Philip Schledermann
 *
 */
public class RDDWriter {
	// needed Filewriter, composing three parts
	// 1 Prefixes
	// 2 Classes
	// 3 Properties
	
	private String OUTPUT_FILE_NAME;
	final static Charset ENCODING = StandardCharsets.UTF_8;
	private List<String> lines = new ArrayList<String>();
	
	public RDDWriter(String outputfile) {
		OUTPUT_FILE_NAME = outputfile;
	}
	/**
	 * Add prefixes to file
	 * @param prefixes
	 */
//	public void addPrefix(List<String> prefixes) {
//		lines.addAll(prefixes);
//	}
	public void addNewline() {
		lines.add("");
	}
	public void addPrefix(List<Prefix> prefixes) {
		
		for (Prefix prefix : prefixes) {
			lines.add("PREFIX " + prefix.getName() + ": " + prefix.getIri());
		}
	}
	/**
	 * Add classes to file
	 * @param classes
	 */
	public void addClasses(List<String> classes) {
		lines.addAll(classes);
	}
	/**
	 * Add properties to file
	 * @param properties
	 */
	public void addPropoperties(List<String> properties) {
		lines.addAll(properties);
	}
	/**
	 * Write file with content that was insertet before
	 */
	public void writeFile() {
		writeFile(lines);
	}
	/**
	 * File writer for complete content at once	
	 * @param aLines
	 */
	public void writeFile(List<String> aLines) {
		    Path path = Paths.get(OUTPUT_FILE_NAME);
		    try (BufferedWriter writer = Files.newBufferedWriter(path, ENCODING)){
		      for(String line : aLines){
		        writer.write(line);
		        writer.newLine();
		      }
		      writer.close();
		    } catch (IOException e) {
		    	e.printStackTrace();
			}
		  }

	}
