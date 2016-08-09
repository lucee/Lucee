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

	cacheName="testClusterStorage"&createUniqueId();
	times=[2000,100,2500];


	public void function testFormOrder(){
		forms=structNew('linked');
		forms['a']='1';
		forms['b']='2';
		forms['e']='3';
		forms['d']='4';
		forms['f']='5';

		uri=createURI("Scope/form.cfm");
		local.res=_InternalRequest(
			template:uri,
			forms:forms
		);
		assertEquals("a:1;b:2;e:3;d:4;f:5;",res.filecontent.trim());
	}

	public void function testScopeStorage(){

		try {
			createRAMCache();
			request.data=[];
			local.names="";
			uri=createURI("ScopeStorage/call.cfm");
			loop from=1 to=len(times) index="local.i" {
				names=listAppend(names,"tghfjg"&i);
				sleep(5);

				thread name="tghfjg#i#" index="#i#" time=times[i] uri=uri cacheName=cacheName {
					request.data[index]=_InternalRequest(
						addToken:true,
						template:uri,
						urls:{
							cacheName:cacheName,
							name:"a#index#",
							value:index,
							time:time
						}
					);
				}
			}
			thread action="join" names=names;
			sleep(1000); // TODO why is this necessary
			uri=createURI("ScopeStorage/dump.cfm");
			res=_InternalRequest(
				addToken:true,
				template:uri,
				urls:{
					cacheName:cacheName
				}
			);

			

			local.sess=evaluate(trim(res.filecontent));
			
			/*throw "count:"&structCount(cfthread)&";cfid:"&getPageContext().getCFID()&">"&listSort(structKeyList(sess),'textnocase')&"
			xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
			"&serialize(res)&"
			========================================
			"&serialize(request.data)&"
			----------------------------------------
			"&serialize(cfthread)&"
			----------------------------------------
			";
			assertEquals('',listSort(structKeyList(sess),'textnocase'));*/
			assertEquals(1,sess['a-a1']);
			assertEquals(2,sess['a-a2']);
			assertEquals(3,sess['a-a3']);
			/*assertEquals(1,sess['b-a1']);
			assertEquals(2,sess['b-a2']);
			assertEquals(3,sess['b-a3']);*/

		}
		finally {
			deleteCache();
		}		
			//assertEquals(first,second);
			//assertEquals(first,third);
	}

	// ScopeStorage

	private function createRAMCache(){
		admin 
				action="updateCacheConnection"
				type="web"
				password="#request.webadminpassword#"
				
				
				name="#cacheName#" 
				class="lucee.runtime.cache.ram.RamCache" 
				storage="true"
				default="object" 
				custom="#{timeToLiveSeconds:86400
					,timeToIdleSeconds:86400}#";
	}
	private function deleteCache(){
		admin 
			action="removeCacheConnection"
			type="web"
			password="#request.webadminpassword#"
			name="#cacheName#";
						
	}
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}

} 
</cfscript>