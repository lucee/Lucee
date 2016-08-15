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

	private void function testConnections(){
		request.results757=[];
		local.names="";
		loop from=1 to=30 index="local.i" {
			names=listAppend(names,"t757_#i#")
			thread name="t757_#i#"  {
				//http url=request.baseURL&"qry.cfm" result="res";
				uri=createURI("LDEV0757/qry.cfm");
				local.res=_InternalRequest(uri);
				echo("%%%%"&serialize(res));
				arrayAppend(request.results757,res.filecontent.trim());
			}
		}
		thread action="join" name="#names#";
		assertEquals("30",request.results757.len());
		loop array=request.results757 item="local.result" {
			assertEquals(":ok:",left(result,4));
		}

	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}

} 
</cfscript>