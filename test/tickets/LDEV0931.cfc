<!--- 
 *
 * Copyright (c) 2016, Lucee Association Switzerland. All rights reserved.*
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
component extends="org.lucee.cfml.test.LuceeTestCase" labels="http" skip=true {

	variables.httpbin = server.getTestService("httpbin");
	variables.hasHttpbin = structCount(variables.httpbin) > 0;
	variables.httpbinUrl = variables.hasHttpbin ? "http://#variables.httpbin.server#:#variables.httpbin.port#" : "";
		
	public void function testImplicit(){
		if (!variables.hasHttpbin) return;
		var data=chr(228)&chr(246)&chr(252); // äöü
		data="{aaa:'#data#'}";
		http url="#variables.httpbinUrl#/anything" result="local.res" method="put" throwonerror="no" charset="utf-8"{
			httpparam type="body" value=data;
		}
		res=deserializeJSON(res.filecontent);
		assertEquals(data,res.data);
	}

	public void function testExplicit(){
		if (!variables.hasHttpbin) return;
		var data=chr(228)&chr(246)&chr(252); // äöü
		data="{aaa:'#data#'}";
		http url="#variables.httpbinUrl#/anything" result="local.res" method="put" throwonerror="no" charset="utf-8"{
			httpparam type="body" mimetype="text/plain; charset=UTF-8" value=data;
		}
		res=deserializeJSON(res.filecontent);
		assertEquals(data,res.data);
	}
} 
</cfscript>