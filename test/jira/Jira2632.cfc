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
	
	variables.serverAdminPassword=request.SERVERADMINPASSWORD;
	variables.webAdminPassword=request.WEBADMINPASSWORD;
	
	
	public function beforeTests(){
		// store existing api-key
		admin type="web" password="#variables.webAdminPassword#" action="getAPIKey" returnvariable="variables.oldWebAPIKey";
		admin type="server" password="#variables.serverAdminPassword#" action="getAPIKey" returnvariable="variables.oldServerAPIKey";
		
	}
	
	public function afterTests(){
		
		// reset old api-key
		if(isNull(variables.oldServerAPIKey))
			admin type="server" password="#variables.serverAdminPassword#" action="removeAPIKey";
		else 
			admin type="server" password="#variables.serverAdminPassword#" action="updateAPIKey" key="#variables.oldServerAPIKey#";
			
		if(isNull(variables.oldWebAPIKey))
			admin type="web" password="#variables.webAdminPassword#" action="removeAPIKey";
		else 
			admin type="web" password="#variables.webAdminPassword#" action="updateAPIKey" key="#variables.oldWebAPIKey#";
	}
	
	//public function setUp(){}

	public void function testGetAPIKey(){
		local.serverKey=createGUid();
		local.webKey=createGUid();
		
		// set and read
		admin type="server" password="#variables.serverAdminPassword#" action="updateAPIKey" key="#serverKey#";
		admin type="server" password="#variables.serverAdminPassword#" action="getAPIKey" returnvariable="local.k";
		assertEquals(serverKey,k);
		
		admin type="web" password="#variables.webAdminPassword#" action="updateAPIKey" key="#webKey#";
		admin type="web" password="#variables.webAdminPassword#" action="getAPIKey" returnvariable="local.k";
		assertEquals(webKey,k);
		
		local.serverKey=createGUid();
		local.webKey=createGUid();
		
		// reset and read
		admin type="server" password="#variables.serverAdminPassword#" action="updateAPIKey" key="#serverKey#";
		admin type="server" password="#variables.serverAdminPassword#" action="getAPIKey" returnvariable="local.k";
		assertEquals(serverKey,k);
		
		admin type="web" password="#variables.webAdminPassword#" action="updateAPIKey" key="#webKey#";
		admin type="web" password="#variables.webAdminPassword#" action="getAPIKey" returnvariable="local.k";
		assertEquals(webKey,k);
		
		
		
		if(!getApplicationSettings().singleContext) {
			// when no web api key is defined server api key is used
			admin type="web" password="#variables.webAdminPassword#" action="removeAPIKey";
			admin type="web" password="#variables.webAdminPassword#" action="getAPIKey" returnvariable="local.kk";
			assertEquals(serverKey,kk);
		}
		
		// when no api key exists null is returned
		admin type="server" password="#variables.serverAdminPassword#" action="removeAPIKey";
		admin type="server" password="#variables.serverAdminPassword#" action="getAPIKey" returnvariable="local.k";
		assertEquals(true,isNull(k));
		admin type="web" password="#variables.webAdminPassword#" action="getAPIKey" returnvariable="local.k";
		assertEquals(true,isNull(k));
	}
} 
</cfscript>