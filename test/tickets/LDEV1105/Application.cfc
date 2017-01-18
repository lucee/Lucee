/**
 *
 * Copyright (c) 2016, Lucee Assosication Switzerland. All rights reserved.*
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 **/
component {

	this.name = hash( getCurrentTemplatePath() );
    this.sessionmanagement="Yes" 
	this.sessiontimeout=createTimeSpan(0,0,3,0);
	this.datasources.test={
	  	class: 'org.hsqldb.jdbcDriver'
		, bundleName: 'org.hsqldb.hsqldb'
		, bundleVersion: '2.3.2'
		, connectionString: 'jdbc:hsqldb:file:#getDirectoryFromPath(getCurrentTemplatePath())#/datasource/db'
		, storage: true
	};
	this.sessionStorage="test";
	this.sessionCluster=true;

	function onSessionStart() {
		//session.susi="sorglos";
		sessionRotate();
	}
}