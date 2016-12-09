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

    private void function test(required boolean storage,required boolean _cluster) {
		var tmpl=createURI("LDEV0677/index.cfm");
		
		var res1=_internalRequest(template:tmpl,urls:{scene:1,storage:storage,cluster:_cluster});
		var res2=_internalRequest(template:tmpl,urls:{scene:2,storage:storage,cluster:_cluster},cookies:res1.cookies);
		var res3=_internalRequest(template:tmpl,urls:{scene:3,storage:storage,cluster:_cluster},cookies:res2.cookies);
		//var res4=_internalRequest(template:tmpl,urls:{scene:4,storage:storage,cluster:_cluster},cookies:res2.cookies);
		//echo(res4.filecontent.trim());

		// _internalrequest(template:string [, method:string [, urls:struct [, forms:struct [, cookies:struct [, headers:struct [, body:any [, charset:string [, addtoken:boolean]]]]]]]]):struct
		sess1=evaluate(res1.filecontent.trim());
		sess2=evaluate(res2.filecontent.trim());
		sess3=evaluate(res3.filecontent.trim());

		// session id changed between scene 1 and 2, but not to 3
		assertNotEquals(sess1.cfid,sess2.cfid);
		assertEquals(sess2.cfid,sess3.cfid);

		// data should be copied over
		assertEquals("sorglos",sess1.susi);
		dump(sess2);
		assertEquals("sorglos",sess2.susi);
		assertEquals("sorglos",sess3.susi);

	}


	public void function testMemory() {
		test(false,false);
	}
	public void function testDatasource() {
		test(true,false);
	}
	public void function testDatasourceCluster() {
		test(true,true);
	}

 	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI & "" & calledName;
	}

}
</cfscript>