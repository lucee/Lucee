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
		/*  note, we're testing a range of s3 functions in a single test so we can re-use buckets,
			rather than creating and deleting lots of buckets with a test suite for each individual s3Function
		*/

		variables.bucket = "lucee-s3func1-#lcase(hash(CreateGUID()))#";
		variables.bucket2 = "lucee-s3func2-#lcase(hash(CreateGUID()))#";

		variables.dir = "s3://#bucket#";
		variables.dir2 = "s3://#bucket2#";
	}

	public void function testS3functions() skip="isNotSupported"{
		var s3=getCredentials("s3");
		application action="update" s3={
			accessKeyId: s3.ACCESS_KEY_ID,
			awsSecretKey: s3.SECRET_KEY
		};
		testfunctions("s3");
	}
	
	public void function testS3functionsCustom() skip="isNotSupportedCustom"{
		var s3=getCredentials("s3_custom");
		application action="update" s3={
			accessKeyId: s3.ACCESS_KEY_ID,
			awsSecretKey: s3.SECRET_KEY,
			host: s3.HOST
		};
		testfunctions("s3_custom");
	}

	public void function testS3functionsGoogle() skip="isNotSupportedGoogle"{
		var s3=getCredentials("s3_google");
		application action="update" s3={
			accessKeyId: s3.ACCESS_KEY_ID,
			awsSecretKey: s3.SECRET_KEY,
			host: s3.HOST
		};
		testfunctions("s3_google");
	}

	private function testfunctions() localMode=true {
		
		if ( directoryExists( dir ) ) 
			directoryDelete( dir, true );
		directoryCreate( dir  );

		if ( directoryExists( dir2 ) ) 
			directoryDelete( dir2, true );
		directoryCreate( dir2 );

		try {
			file = "/test.txt";
			moved = "/moved.txt";
			content = "1234";

			s3Write( bucket, file, content );

			expect( s3listBucket( bucket ).recordcount ).toBe( 1 );

			s3copy( srcBucketName=bucket, srcObjectName=file, trgBucketName=bucket2, trgObjectName=file );

			expect( s3listBucket( bucket2 ).recordcount ).toBe( 1 );

			expect( s3read( bucketName=bucket2, objectName=file ) ).toBe( content );

			s3move( srcBucketName=bucket2, srcObjectName=file, trgBucketName=bucket, trgObjectName=moved );

			expect( s3listBucket( bucket2 ).recordcount ).toBe( 0 );

			expect( s3listBucket( bucket ).recordcount ).toBe( 2 );

			expect( s3read( bucketName=bucket, objectName=moved ) ).toBe( content );

			s3clearBucket( bucketName=bucket );

			expect( s3listBucket( bucket ).recordcount ).toBe( 0 );

		}
		finally {
			if ( directoryExists( dir ) )
				directoryDelete( dir, true );
			if ( directoryExists( dir2 ) )
				directoryDelete( dir2, true );
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
