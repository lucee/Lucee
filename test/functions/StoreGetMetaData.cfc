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
component extends="org.lucee.cfml.test.LuceeTestCase" labels="s3"	{
	//public function beforeTests(){}

	//public function afterTests(){}


	private struct function getCredentials() {
		return server.getTestService("s3");
	}

	public function setUp(){
		var s3=getCredentials();
		if(!isNull(s3.ACCESS_KEY_ID)) {
			application action="update" s3={
				accessKeyId: s3.ACCESS_KEY_ID,
				awsSecretKey: s3.SECRET_KEY
			};
			variables.s3Supported=true;
		} else {
			variables.s3Supported=false;
		}
	}

	public function testStoreMetadata() localMode=true {
		if(!variables.s3Supported) return;

		var dir="s3://" & server.getTestService("s3").bucket_prefix & "metadata-#lcase(hash(CreateGUID()))#/";
		if ( directoryExists( dir ) )
			directoryDelete( dir, true );
		try {
			assertFalse(DirectoryExists( dir ) );
			directoryCreate( dir ) ;

			var obj = dir & "/object/"; // can't create metadata on a bucket
			directoryCreate( obj );

			var md = storeGetMetaData( obj );
			var countBefore = structCount( md );
			storesetMetaData( obj, {"susi":"Susanne"} );
			var md = storeGetMetaData( obj );
			assertEquals( countBefore+1, structCount( md ) );
			assertEquals( "Susanne", md.susi );
		}
		finally {
			if ( directoryExists( dir ) )
				directoryDelete( dir, true );
		}
	}

	private string function getTestBucketUrl() localmode=true {
		s3Details = getCredentials();
		bucketName = server.getTestService("s3").bucket_prefix & lcase("metadata2-#lcase(hash(CreateGUID()))#");
		return "s3://#s3Details.ACCESS_KEY_ID#:#s3Details.SECRET_KEY#@/#bucketName#";
	}

	private numeric function checkS3Version(){
		var s3Version = extensionList().filter(function(row){
			return (row.name contains "s3");
		}).version;
		return listFirst( s3Version, "." ) ;
	};

	public function testS3Url(){
		if(!variables.s3Supported) return;
		if ( checkS3Version() gt 0 )
			return; // only works with v2 due to https://luceeserver.atlassian.net/browse/LDEV-4202
		var bucket = getTestBucketUrl();
		try {
			expect( directoryExists( bucket ) ).toBeFalse();
			directory action="create" directory="#bucket#";
			expect( directoryExists( bucket ) ).toBeTrue();
			var info = StoreGetMetadata( bucket );
			expect( info ).toHaveKey( "region" );
			expect( info.region ).toBe( "us-east-1" );
		} finally {
			if ( directoryExists( bucket ) )
				directoryDelete( bucket );
		}
	}
}
</cfscript>