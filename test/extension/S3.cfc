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
		bucketName = "lucee-testsuite";
		base = "s3://#s3Details.S3_ACCESS_KEY_ID#:#s3Details.S3_SECRET_KEY#@/#bucketName#";

	}

	public void function test() skip="isNotSupported"{
		if(isNotSupported()) return;
		
		if( directoryExists(base))
			directoryDelete(base, true);
		try{
		assertFalse(directoryExists(base));
		assertFalse(fileExists(base));
		directoryCreate(base);
		assertTrue(directoryExists(base));
		assertFalse(fileExists(base));

		// we accept this because S3 accept this, so if ACF does not, that is a bug/limitation in ACF.
		var sub=base & "/a";
		if(!fileExists(sub))
			fileWrite(sub, "");

		assertTrue(directoryExists(sub));
		assertFalse(fileExists(sub));

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
		finally {
			if( directoryExists(base))
				directoryDelete(base, true);
		}
	}


	public boolean function isNotSupported() {
		return structCount(getCredentials())==0;
	}

	private struct function getCredentials() {
		return server.getTestService("s3");
	}


} 
</cfscript>