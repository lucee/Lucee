/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.*
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
 */
component extends="org.lucee.cfml.test.LuceeTestCase"	{

	public function setUp(){
		variables.data=_internalRequest(
			template:"/test/tickets/LDEV0581/test.cfm",
			urls:{'url1':'url1:value'},
			forms:{'form1':'form1:value'},
			cookies:{'cookie1':'cookie1:value'},
			headers:{'header1':'header1:value'}
		);
		variables.data.content=evaluate(data.filecontent);

	}
	public void function test(){
		// Cookie
		assertEquals("cfid,cftoken,cookie1,test",data.content.cookie.keyList().listSort("textNoCase"));
		assertEquals("cookie1:value",data.content.cookie.cookie1);
		assertEquals("cookie-581",data.content.cookie.test);
		// Form
		assertEquals("fieldnames,form1,test",data.content.form.keyList().listSort("textNoCase"));
		assertEquals("form1:value",data.content.form.form1);
		assertEquals("form-581",data.content.form.test);
		// URL
		assertEquals("test,url1",data.content.url.keyList().listSort("textNoCase"));
		assertEquals("url1:value",data.content.url.url1);
		assertEquals("url-581",data.content.url.test);
		// Header
		assertEquals("header1",data.content.HTTPRequestData.headers.keyList().listSort("textNoCase"));
		assertEquals("header1:value",data.content.HTTPRequestData.headers.header1);
		// Response
		assertEquals("cookie-581",data.cookies.test);
		assertEquals("header-581",data.headers.test);
		assertEquals("session-581",data.session.test);
	}
}