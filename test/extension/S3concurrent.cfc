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
component extends="org.lucee.cfml.test.LuceeTestCase" labels="s3" {
	
	public function setUp(){
		if (! isNotSupported() || isNotSupportedCustom())// || isNotSupportedGoogle()) ) 
			return;
		variables.bucketName = "lucee-con-#lcase(hash(CreateGUID()))#";
		variables.s3ExtVersion = extensionList().filter( function( row ){ return row.name contains "s3" }).version;
		//systemOutput( "", true );
		//systemOutput( "Running S3 Extension: #variables.s3ExtVersion#", true );
	}

	public function testS3Concurrent(){
		//systemOutput("testS3Concurrent", true);
		if ( isNotSupported() || isNotSupportedCustom() )
			return; // need two s3 profiles to test!
		var testUrls = {};
		try {
			
			var testUrls = {
				s3: setUpConcurrent("s3", variables.bucketName & "-s3"),
				s3_custom: setUpConcurrent("s3_custom", variables.bucketName & "-custom")
			};

			// systemOutput(testUrls, true);

			var tests = [];
			ArraySet(tests, 1, 33, "");
			// call a set of test s3 urls randomly in parallel to test concurrency
			ArrayEach(tests, 
				function(el, idx, arr){
					var cfg;
					if ( arguments.idx mod 2 )
						cfg = "s3"; 
					else
						cfg = "s3_custom"; 

					var res = testUrls[ cfg ];
					//systemOutput("#arguments.idx#::#cfg#", true );
					expect( FileExists( res & "/test.txt" ) ).toBeTrue();
					expect( FileRead( res & "/test.txt" ) ).toBe( res );
				}, 
				true, 
				3
			);
		} catch (e){
			systemOutput( e, true );
		} finally {
			cleanUpConcurrent( testUrls );
		}
	}

	private function setUpConcurrent( s3, bucket ) localmode=true {
		if ( arguments.s3 eq "s3" ){
			s3Details = getCredentials( "s3" );
			res = "s3://#s3Details.ACCESS_KEY_ID#:#s3Details.SECRET_KEY#@/#arguments.bucket#";
		} else if ( arguments.s3 eq "s3_custom" ) {
			s3Details = getCredentials( "s3_custom" );
			res = "s3://#s3Details.ACCESS_KEY_ID#:#s3Details.SECRET_KEY#@#s3Details.HOST#/#arguments.bucket#";
		} else {
			throw "Unsupported s3 profile [#arguments.s3#]";
		}
		directoryCreate( res );
		fileWrite( res & "/" & "test.txt", res );
		return res;
	}

	private function cleanUpConcurrent( resUrls ){
		loop collection=arguments.resUrls key="local.k" value="local.u"{
			directoryDelete( local.u, true );
		}
	}

	public boolean function isNotSupported() {
		return structCount( getCredentials( "s3" ) ) == 0;
	}

	public boolean function isNotSupportedCustom() {
		return structCount( getCredentials( "s3_custom" ) ) == 0;
	}

	public boolean function isNotSupportedGoogle() {
		return structCount( getCredentials( "s3_google" ) ) == 0 ;
	}

	private struct function getCredentials(s3_cfg) {
		return server.getTestService(s3_cfg);
	}

} 
</cfscript>