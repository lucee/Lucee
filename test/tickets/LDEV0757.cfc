<!--- 
 *
 * Copyright (c) 2015, Lucee Association Switzerland. All rights reserved.
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

	function testConnections(){
		request.results757=[];
		local.names="";
		loop from=1 to=30 index="local.i" {
			names=listAppend(names,"t757_#i#")
			thread name="t757_#i#"  {
				//http url=request.baseURL&"qry.cfm" result="res";
				var uri=createURI("LDEV0757/qry.cfm");
				lock name='LDEV0757' timeout="5" type="exclusive" {
					try {
						local.res=_InternalRequest(uri);
						echo("%%%%"&serialize(res));
						arrayAppend( request.results757, res.filecontent.trim() );
					} catch (e){
						arrayAppend( request.results757, e.message.trim() );
					}
				}
			}
		}
		thread action="join" name="#names#";
		
		loop array=request.results757 item="local.result" {
			expect( left( result, 4 ) ).toBe(":ok:", result );
		}
		expect( request.results757.len() ).toBe( 30 );

	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}

} 
</cfscript>