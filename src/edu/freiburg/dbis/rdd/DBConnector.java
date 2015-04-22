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


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import org.apache.log4j.Logger;
import org.apache.jena.jdbc.mem.MemDriver;
import org.apache.jena.jdbc.remote.RemoteEndpointDriver;
import org.aspectj.runtime.internal.PerObjectMap;


/**
 * @author Philip Schledermann
 *
 */
public class DBConnector {
	private static Logger logger = Logger.getLogger(DBConnector.class);
	private static Logger perflog = Logger.getLogger("performanceLogger");
	private Connection conn;
	private String graphName = null;
	public void DBConnectorEndpoint(String endointUrl) {
		try {
			RemoteEndpointDriver.register();
		} catch (SQLException e) {
			logger.warn("ERROR In registering Remote-Endpoint Driver: ", e);
		}
		logger.info("connecting database");
		try {
			conn = DriverManager.getConnection("jdbc:jena:remote:query="+endointUrl);
		} catch (SQLException e) {
			logger.warn("ERROR In getting Connection: ", e);
		}
	}
	public void DBConnectorVirtuosoEndpointGraph(String endointUrl, String virtuosoGraphName) {
		graphName = virtuosoGraphName;
		DBConnectorEndpoint(endointUrl);
	}
	public void DBConnectorFile(String pathToFile){
		try {
			MemDriver.register();
		} catch (SQLException e) {
			logger.warn("ERROR In registering In-Memory Driver: ", e);
		}
		logger.info("connecting database");
		try {
			conn = DriverManager.getConnection("jdbc:jena:mem:dataset="+pathToFile);
		} catch (SQLException e) {
			logger.warn("ERROR In getting Connection: ", e);
		}
	}
	public String[] performQuery(String WhatYouWantToKnow) {
		logger.info("SPARQL Query: "+WhatYouWantToKnow);
		long start = System.currentTimeMillis();
		long end = 0;
		Statement stmt = null;
		List<String> result = new ArrayList<String>();
		try {
			stmt = conn.createStatement();
			ResultSet rset = stmt.executeQuery(WhatYouWantToKnow);
			end = System.currentTimeMillis();
			logger.debug("ResultSet MetaData: "+rset.getMetaData());
			 try {
				while (rset.next()) {
					    if(!rset.getString(1).equals(null))result.add(rset.getString(1));
					    logger.debug("rset.getString(1): "+rset.getString(1).toString());
					  }
			} catch (SQLException e) {
				logger.error("ERROR In ResultSet.next(): ", e);
			}
			 logger.debug("closing ResultSet");
			 rset.close();
		} catch (Exception e) {
			logger.warn("ERROR In query: ", e);
		} finally {
			try {
				logger.debug("closing statement");
				stmt.close();
			} catch (SQLException e) {
				logger.error("ERROR In closing statement: ", e);
			}
				}
		String[] returnSet = new String[result.size()];
		if (returnSet.length == 0) return new String[0];
		for (int i = 0; i < result.size(); i++) {
			returnSet[i] = result.get(i).toString();
		}
		perflog.debug("Time (ms): "+(end-start)+" for Statement: "+WhatYouWantToKnow);
		return returnSet;
	}
	public String [] performQuery(String Prefix, String Select, String Where) {
		if (graphName == null) {
			return performQuery(Prefix+" "+Select+" "+Where);
		} else if (graphName != null) {
			return performQuery(Prefix+" "+Select+" FROM <"+graphName+"> "+Where);
		}
		logger.error("This should never happen, graph name is not null but is null");
		return null;
	}
}
