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

	public function setUp(){
		variables.qry=queryNew("str,nbr,dat","varchar,integer,date");
		queryAddrow(variables.qry);
	}

	public void function testImplicidQueryAccess(){
		if(hasFullNullSupport()) throw "test need full null support disabled";
		
		assertEquals(false,isNull(qry.str));
		assertEquals(false,isNull(qry.nbr));
		assertEquals(false,isNull(qry.dat));
		
		// call without the query name
		loop query="#qry#" {
			assertEquals(false,isNull(qry.str));
			assertEquals(false,isNull(qry.nbr));
			assertEquals(false,isNull(qry.dat));
			
			assertEquals(false,isNull(str));
			assertEquals(false,isNull(nbr));
			assertEquals(false,isNull(dat));
		}
		
		s=qry.str;
		assertEquals('java.lang.String',s.getClass().getName());
		s=qry.nbr;
		assertEquals('java.lang.String',s.getClass().getName());
		s=qry.dat;
		assertEquals('java.lang.String',s.getClass().getName());
		
		// same with evaluate
		assertEquals(false,isNull(evaluate('qry.str')));
		assertEquals(false,isNull(evaluate('qry.nbr')));
		assertEquals(false,isNull(evaluate('qry.dat')));
		
		// call without the query name
		loop query="#qry#" {
			assertEquals(false,isNull(evaluate('str')));
			assertEquals(false,isNull(evaluate('nbr')));
			assertEquals(false,isNull(evaluate('dat')));
			
			assertEquals(false,isNull(evaluate('qry.str')));
			assertEquals(false,isNull(evaluate('qry.nbr')));
			assertEquals(false,isNull(evaluate('qry.dat')));
		}
		
		s=evaluate('qry.str');
		assertEquals('java.lang.String',s.getClass().getName());
		s=evaluate('qry.nbr');
		assertEquals('java.lang.String',s.getClass().getName());
		s=evaluate('qry.dat');
		assertEquals('java.lang.String',s.getClass().getName());
	
	}
	
	function testForEach() {

		var q = queryNew( "id,value", "integer,varchar" );

		queryAddRow( q, { id: 1, value: "Abc" } );
		queryAddRow( q, { id: 2, value: "Def" } );
		queryAddRow( q, { id: 3, value: nullValue() } );
		queryAddRow( q, { id: 4, value: "Ghi" } );

		for( var row in q )
			local.x = row.value;
	}
	
	
	private void function hasFullNullSupport(){
		return server.ColdFusion.ProductName EQ "Lucee" && getPageContext().getConfig().getFullNullSupport();
	}
} 

</cfscript>