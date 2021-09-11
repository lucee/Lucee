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

 	this.datasources ={ 
 		"ds1":{
	  		class: 'org.h2.Driver', 
	  		connectionString: 'jdbc:h2:#getDirectoryFromPath(getCurrentTemplatePath())#/datasources/ds_1/db;MODE=MySQL'}
	  	,"ds2":{
	  		class: 'org.h2.Driver', 
	  		connectionString: 'jdbc:h2:#getDirectoryFromPath(getCurrentTemplatePath())#/datasources/ds_2/db;MODE=MySQL'}
	  	,"ds3":{
	  		class: 'org.h2.Driver', 
	  		connectionString: 'jdbc:h2:#getDirectoryFromPath(getCurrentTemplatePath())#/datasources/ds_3/db;MODE=MySQL'}
	  	,"ds4":{
	  		class: 'org.h2.Driver', 
	  		connectionString: 'jdbc:h2:#getDirectoryFromPath(getCurrentTemplatePath())#/datasources/ds_4/db;MODE=MySQL'}
	  	
	};

	this.ormEnabled = true; 
	this.ormSettings = { 
		savemapping=true,
		dbcreate = 'update' ,
		cfcLocation="orm",
		logSQL=false,
		datasource="ds1"
	};
    function onRequestEnd() {
		var javaIoFile=createObject("java","java.io.File");
		loop array=DirectoryList(
			path=getDirectoryFromPath(getCurrentTemplatePath()), 
			recurse=true, filter="*.db") item="local.path"  {
			fileDeleteOnExit(javaIoFile,path);
		}
	}

	private function fileDeleteOnExit(required javaIoFile, required string path) {
		var file=javaIoFile.init(arguments.path);
		if(!file.isFile())file=javaIoFile.init(expandPath(arguments.path));
		if(file.isFile()) file.deleteOnExit();
	}
} 