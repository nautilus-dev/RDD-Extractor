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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Philip Schledermann
 *
 */
public class PrefixReader {

	private static String TEST_DIR = null;
	private FileInputStream fis = null;
	private InputStreamReader isr = null;
	private BufferedReader file = null;
	private List<String> RawPrefixes = new ArrayList<String>();
	private List<Prefix> cleanPrefixes = new ArrayList<Prefix>();
	/**
	 * 
	 */
	public PrefixReader(String Directory) {
		TEST_DIR = Directory;
	}	
	
	public List<Prefix> getCleanPrefixes() {
		return cleanPrefixes;
	}
	public void setCleanPrefixes(List<Prefix> cleanPrefixes) {
		this.cleanPrefixes = cleanPrefixes;
	}
	public void LoadNProcess(String filename) {
		load(filename);
		process();
	}

	private void process() {
		for (String prefix : RawPrefixes) {
			String tempPrefix;
			// Offset is 7 for "@prefix "
			// split at ":"
			// remove "." in the end
			// cutoff Front and Back
			// If there is " ." in the end?! search for ">" and cut at this position
			int cutIndexEnd = (prefix.length()-(prefix.length()-prefix.indexOf(">")))+1;
			tempPrefix = prefix.substring(8, cutIndexEnd);
			// remove blanks
			tempPrefix.replace(" ", "");
			String[] CleanPrefix = tempPrefix.split(": ");
			// TODO: remove System.out.print(CleanPrefix.length + "\n");
			cleanPrefixes.add(new Prefix(CleanPrefix[0], CleanPrefix[1]));
		}
		// check if the rdf:type prefix is set.
//		if (!cleanPrefixes.contains(new Prefix("rdf", "<http://www.w3.org/1999/02/22-rdf-syntax-ns#>"))) {
//			cleanPrefixes.add(new Prefix("rdf", "<http://www.w3.org/1999/02/22-rdf-syntax-ns#>"));
//		}
	}
	private void load(String filename) {
		String line;
		try {
			fis = new FileInputStream(TEST_DIR + filename);
			isr = new InputStreamReader(fis, "UTF-8");
			file = new BufferedReader(isr);
			while ((line = file.readLine()).startsWith("@")) {
				RawPrefixes.add(line);
			}
	
				fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
	// read in file. use file reader from ui project.
	// read out lines until no @ is the first sign.
	// then process and return
}
