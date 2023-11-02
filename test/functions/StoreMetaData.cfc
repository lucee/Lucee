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


	private struct function getCredencials() {
		// getting the credetials from the enviroment variables
		var s3={};
		if(!isNull(server.system.environment.S3_ACCESS_ID) && !isNull(server.system.environment.S3_SECRET_KEY)) {
			s3.accessKeyId=server.system.environment.S3_ACCESS_ID;
			s3.awsSecretKey=server.system.environment.S3_SECRET_KEY;
		}
		// getting the credetials from the system variables
		else if(!isNull(server.system.properties.S3_ACCESS_ID) && !isNull(server.system.properties.S3_SECRET_KEY)) {
			s3.accessKeyId=server.system.properties.S3_ACCESS_ID;
			s3.awsSecretKey=server.system.properties.S3_SECRET_KEY;
		}
		return s3;
	}
	  
	public function setUp(){
		var s3=getCredencials();
		if(!isNull(s3.accessKeyId)) {
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