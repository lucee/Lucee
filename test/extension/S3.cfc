<!--- 
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
 ---><cfscript>
component extends="org.lucee.cfml.test.LuceeTestCase"	{
	
	
	//public function afterTests(){}
	
	public function setUp(){
		if(isNotSupported()) return;
		s3Details = getCredentials();
		bucketName = "testcaseS3";
		base = "s3://#s3Details.ACCESSKEYID#:#s3Details.AWSSECRETKEY#@/#bucketName#";

	}

	public void function test() skip="isNotSupported"{
		if(isNotSupported()) return;
		
		if( directoryExists(base))
			directoryDelete(base, true);
		
		assertFalse(directoryExists(base));
		assertFalse(fileExists(base));
		directoryCreate(base);
		assertTrue(directoryExists(base));
		assertFalse(fileExists(base));

		// we accept this because S3 accept this, so if ACF does not, that is a bug/limitation in ACF.
		var sub=base & "/a";
		if(!fileExists(sub))
			fileWrite(sub, "");

		assertFalse(directoryExists(sub));
		assertTrue(fileExists(sub));

		// because previous file is empty it is accepted as directory
		var subsub=sub & "/foo.txt";
		if(!fileExists(subsub))
			fileWrite(subsub, "hello there");

		assertFalse(directoryExists(subsub));
		assertTrue(fileExists(subsub));

		assertTrue(directoryExists(sub));
		assertFalse(fileExists(sub));

		children = directoryList(sub, true,'query');
		assertEquals(1,children.recordcount);
		
	}


	public boolean function isNotSupported() {
		return structCount(getCredentials())==0;
	}

	private struct function getCredentials() {
		var s3 = {};
		if(!isNull(server.system.environment.S3_ACCESS_ID) && !isNull(server.system.environment.S3_SECRET_KEY)) {
			// getting the credentials from the environment variables
			s3.ACCESSKEYID=server.system.environment.S3_ACCESS_ID;
			s3.AWSSECRETKEY=server.system.environment.S3_SECRET_KEY;
		}
		else if(!isNull(server.system.properties.S3_ACCESS_ID) && !isNull(server.system.properties.S3_SECRET_KEY)) {
			// getting the credentials from the system variables
			s3.ACCESSKEYID=server.system.properties.S3_ACCESS_ID;
			s3.AWSSECRETKEY=server.system.properties.S3_SECRET_KEY;
		}

		s3 = {
			 ACCESSKEYID:'AKIAJ4OOM4POF5ZYOJPA'
			,AWSSECRETKEY:'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'
		};
		return s3;
	}


} 
</cfscript>