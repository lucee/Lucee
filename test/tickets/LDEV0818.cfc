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

	public void function testSingleArg(){
		var q=query(a:[1]);
		queryAddColumn(q,"b");
		q.addColumn("c");
		assertEquals('query("a":[1],"b":[""],"c":[""])',serialize(q));
	}

	public void function testTwoArgType(){
		var q=query(a:[1]);
		queryAddColumn(q,"b",'varchar');
		q.addColumn("c",'varchar');
		assertEquals('query("a":[1],"b":[""],"c":[""])',serialize(q));
	}

	public void function testTwoArgArray(){
		var q=query(a:[1]);
		queryAddColumn(q,"b",[2]);
		q.addColumn("c",[3]);
		assertEquals('query("a":[1],"b":[2],"c":[3])',serialize(q));
	}

	public void function testThreeArg(){
		var q=query(a:[1]);
		queryAddColumn(q,"b","varchar",[2]);
		q.addColumn("c","varchar",[3]);
		assertEquals('query("a":[1],"b":[2],"c":[3])',serialize(q));
	}


} 
</cfscript>