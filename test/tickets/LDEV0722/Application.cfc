/**
 *
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.*
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
    request.baseURL="http://#cgi.HTTP_HOST##GetDirectoryFromPath(cgi.SCRIPT_NAME)#";
	request.currentPath=GetDirectoryFromPath(getCurrentTemplatePath());


 	this.datasource = {
	  class: 'org.h2.Driver'
		, connectionString: 'jdbc:h2:#getDirectoryFromPath(getCurrentTemplatePath())#/datasource/db;MODE=MySQL'
	};

	this.ormEnabled = true; 
	this.ormSettings = { 
		secondarycacheenabled = false,
		cfclocation = [ "/models" ],
		savemapping=true,
		dbcreate = 'update' ,
		flushAtRequestEnd = false,
		eventhandling = true,
		skipcfcWithError = false,
		logSQL=true
	}; 

	// application start
	public boolean function onApplicationStart(){
		return true;
	}

	// request start
	public boolean function onRequestStart(String targetPage){
		ormReload();
		entityLoad("User",1,true);

		return true;
	}
} 