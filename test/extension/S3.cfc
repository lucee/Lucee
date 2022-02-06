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
component extends="org.lucee.cfml.test.LuceeTestCase" labels="s3"	{
	
	
	//public function afterTests(){}
	
	public function setUp(){
		if(!(isNotSupported() || isNotSupportedCustom() || isNotSupportedGoogle())) return;
		bucketName = "lucee-s3ext-#lcase(hash(CreateGUID()))#";
		variables.s3ExtVersion = extensionList().filter( function( row ){ return row.name contains "s3" }).version;
		systemOutput( "", true );
		systemOutput( "Running S3 Extension: #variables.s3ExtVersion#", true );
	}


	// check s3 creds using s3:// resource url

	public void function testS3() skip="isNotSupported"{
		if(isNotSupported()) return;
		var s3Details = getCredentials("s3");
		runS3Tests("s3://#s3Details.ACCESS_KEY_ID#:#s3Details.SECRET_KEY#@/#bucketName#");
	}

	public void function testS3custom() skip="isNotSupportedCustom"{
		if(isNotSupportedCustom()) return;
		var s3Details = getCredentials("s3_custom");
		runS3Tests("s3://#s3Details.ACCESS_KEY_ID#:#s3Details.SECRET_KEY#@#s3Details.HOST#/#bucketName#");
	}

	public void function testS3google() skip="isNotSupportedGoogle"{
		if(isNotSupportedGoogle()) return;
		var s3Details = getCredentials("s3_google");
		runS3Tests("s3://#s3Details.ACCESS_KEY_ID#:#s3Details.SECRET_KEY#@#s3Details.HOST#/#bucketName#");
	}

	// check s3 creds via this.vfs.s3..
	
	public void function testS3application() skip="isNotSupported"{
		runApplicationTest("s3");
	}
	
	public void function testS3applicationCustom() skip="isNotSupportedCustom"{
		runApplicationTest("s3_custom");
	}

	public void function testS3applicationGoogle() skip="isNotSupportedGoogle"{
		runApplicationTest("s3_google");
	}

	private  void function runApplicationTest(service){
		if ( ! checkMinExtVersion( 2 ) )
			return; // only available in s3 ext v2
		var uri = createUri( "s3_application" );
		local.res = _InternalRequest(
			template: uri & "/index.cfm",
			urls: {
				vfs: arguments.service, 
				bucketName: "lucee-testsuite"
			}
		);
	}

	private boolean function checkMinExtVersion( min ){
		return ListFirst( variables.s3ExtVersion, "." ) gte arguments.min;
	}

	private void function runS3Tests(base) {
		
		if( directoryExists(base))
			directoryDelete(base, true);
		try{
			assertFalse(directoryExists(base));
			assertFalse(fileExists(base));
			directoryCreate(base);
			assertTrue(directoryExists(base));
			assertFalse(fileExists(base));

			// we accept this because S3 accept this, so if ACF does not, that is a bug/limitation in ACF.
			// minio doesn't either https://github.com/minio/minio/issues/7335#issuecomment-470683398
			if ( arguments.base NCT "minio") {
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

				var children = directoryList(sub, true,'query');
				assertEquals(1,children.recordcount);
			}
		}
		finally {
			if( directoryExists(base))
				directoryDelete(base, true);
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

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}

} 
</cfscript>