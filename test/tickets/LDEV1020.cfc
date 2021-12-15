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

	public void function testImplicit(){
		
		local.http = new http(); 
		local.http.setMethod('put'); 
		local.http.setURL('https://update.lucee.org/rest/update/provider/echoPut'); 
		local.http.addParam(type="formfield",name='email',value='test@test.com'); 
		local.httpSendResult = local.http.send(); 
		local.httpResult = httpSendResult.getPrefix(); 
		local.filecontent=httpResult.filecontent;
		local.data=evaluate(filecontent);

		assertEquals('email',data.form.fieldNames);
		assertEquals('test@test.com',data.form.email);
	}
} 
</cfscript>