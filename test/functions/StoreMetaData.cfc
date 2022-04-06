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
component extends="org.lucee.cfml.test.LuceeTestCase" labels="s3" {

	public function setUp(){
		variables.dir = "s3://lucee-s3meta-#lcase(hash(CreateGUID()))#";
	}

	public void function testS3storeMetaData() skip="isNotSupported"{
		var s3=getCredentials("s3");
		application action="update" s3={
			accessKeyId: s3.ACCESS_KEY_ID,
			awsSecretKey: s3.SECRET_KEY
		}; 
		testStoreMetadata("s3");
	}
	
	public void function testS3storeMetaDataCustom() skip="isNotSupportedCustom"{
		var s3=getCredentials("s3_custom");
		application action="update" s3={
			accessKeyId: s3.ACCESS_KEY_ID,
			awsSecretKey: s3.SECRET_KEY,
			host: s3.HOST
		}; 
		testStoreMetadata("s3_custom");
	}

	public void function testS3storeMetaDataGoogle() skip="isNotSupportedGoogle"{
		var s3=getCredentials("s3_google");
		application action="update" s3={
			accessKeyId: s3.ACCESS_KEY_ID,
			awsSecretKey: s3.SECRET_KEY,
			host: s3.HOST
		}; 
		testStoreMetadata("s3_google");
	}

	private void function testStoreMetadata() localMode=true {
		
		if (DirectoryExists(dir)) 
			directoryDelete(dir,true);
		try {
			assertFalse(DirectoryExists(dir));
			directoryCreate(dir);
		
			var md=storeGetMetaData(dir);
			var countBefore=structCount(md);
			storeSetMetaData(dir,{"susi":"Susanne"});

			var md=storeGetMetaData(dir);
			assertEquals(countBefore+1,structCount(md));
			assertEquals("Susanne",md.susi);
		}
		finally {
			if (DirectoryExists(dir))
				directoryDelete(dir,true);
		}  
	}

	public boolean function isNotSupported() {
		return structCount( getCredentials("s3") ) == 0;
	}

	public boolean function isNotSupportedCustom() {
		return structCount( getCredentials("s3_custom") ) == 0;
	}

	public boolean function isNotSupportedGoogle() {
		return structCount( getCredentials("s3_google") ) == 0 ;
	}

	private struct function getCredentials(s3_cfg) {
		return server.getTestService(s3_cfg);
	}
} 
</cfscript>