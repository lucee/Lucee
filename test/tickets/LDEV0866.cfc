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

	public function setUp(){
		variables.map = structNew("linked");
		variables.map['a']="1";
		variables.map['b']="2";
		variables.map['c']="3";
		variables.map['d']="4";
		variables.map['e']="5";
		variables.map['f']="6";
		variables.map['g']="7";
	}

	public void function testKeySet() {
		var it = map.keySet().iterator();
		var result="";
		while(it.hasNext()) {
			result&=it.next()&";";
		}
		assertEquals("a;b;c;d;e;f;g;",result);
	}

	public void function testEntrySet() {
		var it = map.entrySet().iterator();
		var result="";
		while(it.hasNext()) {
			var e=it.next();
			result&=e.getKey()&":"&e.getValue()&";";
		}
		assertEquals("a:1;b:2;c:3;d:4;e:5;f:6;g:7;",result);
	}

	public void function testValues() {
		var it = map.values().iterator();
		var result="";
		while(it.hasNext()) {
			result&=it.next()&";";
		}
		assertEquals("1;2;3;4;5;6;7;",result);
	}

} 
</cfscript>