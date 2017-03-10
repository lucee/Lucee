/*
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
 */
component extends="org.lucee.cfml.test.LuceeTestCase"	{
	variables.qry = queryNew(
        "id, name",
        "cf_sql_integer, cf_sql_varchar",
        [
            [ 1, "Tricia" ],
            [ 2, "Sarah" ],
            [ 3, "Joanna" ]
        ]
    );

	public void function testFunction() localmode="true" {

		request.testQueryEach='';
		queryEach(qry,function(struct row,numeric rowNumber,query query){
			request.testQueryEach&=row.id&":"&row.name&":"&rowNumber&';';
		});

		assertEquals(
			'1:Tricia:1;2:Sarah:2;3:Joanna:3;'
			,request.testQueryEach
		);
	} 

	public void function testMemberFunction() localmode="true" {

		request.testQueryEach='';
		qry.each(function(struct row,numeric rowNumber,query query){
			request.testQueryEach&=row.id&":"&row.name&":"&rowNumber&';';
		});

		assertEquals(
			'1:Tricia:1;2:Sarah:2;3:Joanna:3;'
			,request.testQueryEach
		);
	}


} 