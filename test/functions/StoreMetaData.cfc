<!--- 
 *
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
 * Copyright (c) 2014, the Railo Company LLC. All rights reserved.
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
 ---><cfscript>
component extends="org.lucee.cfml.test.LuceeTestCase"	{
	
	//public function beforeTests(){}
	
	//public function afterTests(){}


	private struct function getCredentials() {
		// getting the credentials from the environment variables
		return server.getTestService("s3");
	}
	  
	public function setUp(){
		var s3=getCredentials();
		if(!isNull(s3.ACCESS_KEY_ID)) {
			application action="update" s3=s3; 
			variables.s3Supported=true;
		}
		else 
			variables.s3Supported=false;
	}

	public function testStoreMetadata() localMode=true {
		if(!variables.s3Supported) return;
		
		var dir="s3://lucee-testsuite-metadata/object/";
		if(DirectoryExists(dir)) directoryDelete(dir,true);
		try {
			assertFalse(DirectoryExists(dir));
			directoryCreate(dir);

		
			var md=storeGetMetaData(dir);
			var countBefore=structCount(md);
			storesetMetaData(dir,{"susi":"Susanne"});
    		var md=storeGetMetaData(dir);
    		assertEquals(countBefore+1,structCount(md));
			assertEquals("Susanne",md.susi);
		}
		finally {
    		if(DirectoryExists(dir))
    			directoryDelete(dir,true);
    	}  
	}
} 
</cfscript>