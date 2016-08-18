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

	times=[10,200,100,300,200,100,2,100,1,100];


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


	public void function testScopeStorageCache(){
		testScopeStorage("memory-cache-");
	}

	public void function testScopeStorageNoCache(){
		testScopeStorage("no-in-memory-cache-");
	}

	private void function testScopeStorage(cacheName){
		local.cacheName=arguments.cacheName&createUniqueId();

		try {
			createRAMCache(cacheName);
			request.data=[];
			local.names="";
			uri=createURI("ScopeStorage/call.cfm");
			loop from=1 to=len(times) index="local.index" {
				names=listAppend(names,cacheName&index);
				sleep(1);

				if(index==1) {
					//http addToken=true url="http://localhost:8080"&uri&"?cacheName=#cacheName#&name=a#index#&value=#index#&time=#times[index]#";	
					//request.data[index]=cfhttp;
					request.data[index]=_InternalRequest(
							addToken:true,
							template:uri,
							urls:{
								cacheName:cacheName,
								name:"a#index#",
								value:index,
								time:times[index]
							}
						);
				}
				else {
					thread name="#cacheName##index#" index="#index#" time=times[index] uri=uri cacheName=cacheName {
						//http addToken=true url="http://localhost:8080"&uri&"?cacheName=#cacheName#&name=a#index#&value=#index#&time=#time#";	
						//request.data[index]=cfhttp;
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
			}
			thread action="join" names=names;
			
					

			uri=createURI("ScopeStorage/dump.cfm");
			res=_InternalRequest(
				addToken:true,
				template:uri,
				urls:{
					cacheName:cacheName
				}
			);
			
			// test if all call get the same cfid
			for(var i=1;i<request.data.len();i++) {
				assertEquals(
					trim(request.data[i].fileContent),
					trim(request.data[i+1].fileContent));
			}
			
			

			local.scopes=evaluate(trim(res.filecontent));
			for(var i=1;i<=times.len();i++) {
				assertEquals(i,scopes.session['sa-a'&i]);
				assertEquals(i,scopes.session['sb-a'&i]);
				assertEquals(i,scopes.client['ca-a'&i]);
				assertEquals(i,scopes.client['cb-a'&i]);
			}



		}
		finally {
			deleteCache(cacheName);
		}
	}

	// ScopeStorage

	private function createRAMCache(cacheName){
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
	private function deleteCache(cacheName){
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