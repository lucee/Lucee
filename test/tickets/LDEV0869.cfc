<!--- 
 *
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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

	public void function test(){

		var content="
<cfscript>
a=1;
b=2;
c=3;
</cfscript>";
		var content2="
<cfscript>
d=4;
</cfscript>";


		try {
			//Create a file foo.cfm
			// Add a few lines of CFML code setting variables
			fileWrite("LDEV0869/foo.cfm",content);

			// Hit the file as /foo.cfm in the browser
			uri=createURI("LDEV0869/foo.cfm");
			local.res=_InternalRequest(uri);
			assertEquals("200",res.status);
			assertEquals("",res.filecontent.trim());

			// Now hit the file as Foo.cfm in the browser (upper case "F")
			uri=createURI("LDEV0869/Foo.cfm");
			local.res=_InternalRequest(uri);
			assertEquals("200",res.status);
			assertEquals("",res.filecontent.trim());
			
			//Now edit the file and add an additional line of CF code to the end
			fileAppend("LDEV0869/foo.cfm",content2);

			// Now hit the file as Foo.cfm in the browser (upper case "F")
			uri=createURI("LDEV0869/Foo.cfm");
			local.res=_InternalRequest(uri);
			assertEquals("200",res.status);
			assertEquals("",res.filecontent.trim());
			
			// Now load /foo.cfm in the browser (back to the original lower case "f")
			uri=createURI("LDEV0869/foo.cfm");
			local.res=_InternalRequest(uri);
			assertEquals("200",res.status);
			assertEquals("",res.filecontent.trim());
		}
		finally {
			try {fileDelete("LDEV0869/foo.cfm"); } catch(local.e) {}
		}



	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}

} 
</cfscript>