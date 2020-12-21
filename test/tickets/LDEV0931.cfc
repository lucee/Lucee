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
		
	public void function testImplicit(){
		var data=chr(228)&chr(246)&chr(252); // äöü
		data="{aaa:'#data#'}";
		http url="https://update.lucee.org/rest/update/provider/echoPut" result="local.res" method="put" throwonerror="no" charset="utf-8"{
			httpparam type="body" value=data;
		}
		res=evaluate(res.filecontent);
		assertEquals(data,res.httpRequestData.content);
	}

	public void function testExplicit(){
		var data=chr(228)&chr(246)&chr(252); // äöü
		data="{aaa:'#data#'}";
		http url="https://update.lucee.org/rest/update/provider/echoPut" result="local.res" method="put" throwonerror="no" charset="utf-8"{
			httpparam type="body" mimetype="text/plain; charset=UTF-8" value=data;
		}
		res=evaluate(res.filecontent);
		assertEquals(data,res.httpRequestData.content);
	}
} 
</cfscript>