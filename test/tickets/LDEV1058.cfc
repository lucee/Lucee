/*
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
 */
 component extends="org.lucee.cfml.test.LuceeTestCase"	{
	
	function afterAll(){
		// create dir
		if(directoryExists("LDEV1058/")) directoryDelete("LDEV1058/",true);
	}

	public void function test() {
		if(!directoryExists("LDEV1058/")) directoryCreate("LDEV1058/");
		fileWrite("LDEV1058/Test.cfc",'component {}');
		
		// create temp file with udf
		fileWrite("LDEV1058/udfs.cfm",'<cfscript>function test123(){return 123;}</cfscript>');
		// include that file so we get the udf
		include "LDEV1058/udfs.cfm";

		// load the component
		var cfc=new LDEV1058.Test();
		// prototyping
		cfc.test=variables.test123;

		// now we delete the file holding the udf
		fileDelete("LDEV1058/udfs.cfm");

		// now we make lucee recoize the template is gone
		//try{include "LDEV1058/udfs.cfm";}catch(local.e){dump(e.message);}
		pagePoolClear();

		cfc.test();
	}

} 



