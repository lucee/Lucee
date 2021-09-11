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
	
	
		private struct function getCredentials(service) {
			// getting the credentials from the environment variables
			return server.getTestService( arguments.service );
		}
		
		public function setUp(){
			var s3=getCredentials("s3");
			if (!isNull(s3.ACCESS_KEY_ID)) {
				var creds = {
					accessKeyId: s3.ACCESS_KEY_ID,
					awsSecretKey: s3.SECRET_KEY
				};
				if ( structKeyExists(s3, "host") )
					creds.host = s3.host;
				application action="update" s3=creds; 
				variables.s3Supported=true;
				variables.s3_bucket_hash = lcase( hash( CreateGUID() ) ); // always use a unique bucket name
			}
			else 
				variables.s3Supported=false;
		}
	
		public function testStoreAddACLBucket() localMode=true {
			if (variables.s3Supported) {
				try{
					testStoreACL("s3://lucee-#s3_bucket_hash#-addaclbucket",true,true);
				}
				finally {
					directoryDelete("s3://lucee-#s3_bucket_hash#-addaclbucket",true);
				}
			}
		}
	
		public function testStoreSetACLBucket() localMode=true {
			if (variables.s3Supported) {
				try{
					testStoreACL("s3://lucee-#s3_bucket_hash#-setaclbucket2",true,false);
				}
				finally {
					directoryDelete("s3://lucee-#s3_bucket_hash#-setaclbucket2",true);
				}
			}
		}
	
		public function testStoreAddACLObject() localMode=true {
			if (variables.s3Supported) {
				try{
					testStoreACL("s3://lucee-#s3_bucket_hash#-addaclobject/sub12234",false,true);
				}
				finally {
					directoryDelete("s3://lucee-#s3_bucket_hash#-addaclobject",true);
				}
			}
		}
	
		public function testStoreSetACLObject() localMode=true {
			if (variables.s3Supported) {
				try{
					testStoreACL("s3://lucee-#s3_bucket_hash#-setaclobject2/sub12234",false,false);
				}
				finally {
					directoryDelete("s3://lucee-#s3_bucket_hash#-setaclobject2",true);
				}
			}
		}
	
		private function testStoreACL(required dir, required boolean bucket, required boolean add) localMode=true {
				start=getTickCount();
				
				if (DirectoryExists(dir)) directoryDelete(dir,true);
	
				assertFalse(DirectoryExists(dir));
				directoryCreate(dir);
				
				// check inital data
				var acl=StoreGetACL(dir);
				if (bucket) {
					assertEquals(1,acl.len());
					assertEquals("FULL_CONTROL",toList(acl,"permission"));
					assertEquals("info",toList(acl,"displayName"));
					//var id=acl[1].id;
				}
				else {
					assertEquals(2,acl.len());
					assertEquals("FULL_CONTROL,READ",toList(acl,"permission"));
					assertEquals("all",toList(acl,"group"));
					assertEquals("info",toList(acl,"displayName"));
				}
	
	
				// add ACL
				if (add) {
					arr=[{'group':"authenticated",'permission':"WRITE"}];
					StoreAddACL(dir,arr); 
	
					// test output
					var acl=StoreGetACL(dir);
					
					if (bucket) {
						assertEquals(2,acl.len());
						assertEquals("FULL_CONTROL,WRITE",toList(acl,"permission"));
						assertEquals("authenticated",toList(acl,"group"));
					}
					else {
						assertEquals(3,acl.len());
						assertEquals("FULL_CONTROL,READ,WRITE",toList(acl,"permission"));
						assertEquals("all,authenticated",toList(acl,"group"));
					}
				}
				// set ACL 
				else {
					arr=[{'group':"authenticated",'permission':"WRITE"}];
					StoreSetACL(dir,arr); 
	
					// test output
					var acl=StoreGetACL(dir);
					
						assertEquals(1,acl.len());
						assertEquals("WRITE",toList(acl,"permission"));
						assertEquals("authenticated",toList(acl,"group"));
				}
		}
	
	
		private function toList(arr,key){
			var rtn="";
			loop array=arr item="local.sct" {
				if (!isNull(sct[key]))rtn=listAppend(rtn,sct[key]);
			}
			return listSort(rtn,"textnoCase");
		 }
	
	} 
	</cfscript>