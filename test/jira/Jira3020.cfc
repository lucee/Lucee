<!--- 
 *
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

	//public function setUp(){}

	public void function test() localmode="true"{
		uri =createURI("Jira3020/index.cfm");
		local.result=_InternalRequest(template:uri,cookies:{
			cfId:cookie.cfid
			,cfToken:cookie.cftoken
			,
			});


		/*http method="get" result="local.result"  url="#createURL("Jira3020/index.cfm")#" addtoken="false"{
			httpparam type="cookie" name="" value="#c#";
			httpparam type="cookie" name="" value="##";
			httpparam type="cookie" name="cfiD" value="1234";
			httpparam type="cookie" name="cftokeN" value="1234";
			
		}*/
		qry=result.cookies;
		loop query="#qry#" {
			// invalid case for cfid
			if(compare(qry.name,"cfId")==0 || compare(qry.name,"cfiD")==0) {
				assertEquals("",qry.value);
				assertEquals(createDate(1970,1,1,"UTC"),qry.expires);
			}
			// valid case for cfid
			else if(compare(qry.name,"cfid")==0) {
				assertEquals(cookie.cfid,qry.value);
				assertEquals(true,qry.expires>now());
			}
			// invalid case for cfid
			else if(compare(qry.name,"cfToken")==0 || compare(qry.name,"cftokeN")==0) {
				assertEquals("",qry.value);
				assertEquals(createDate(1970,1,1,"UTC"),qry.expires);
			}
			// valid case for cfid
			else if(compare(qry.name,"cftoken")==0) {
				assertEquals(cookie.cftoken,qry.value);
				assertEquals(true,qry.expires>now());
			}
		}
	}
	
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
} 
</cfscript>