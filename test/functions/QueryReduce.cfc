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
	variables.people = QueryNew( "name,dob,age", "varchar,date,int", [
    [ "Susi", CreateDate( 1970, 1, 1 ), 0 ],
    [ "Urs" , CreateDate( 1995, 1, 1 ), 0 ],
    [ "Fred", CreateDate( 1960, 1, 1 ), 0 ],
    [ "Jim" , CreateDate( 1988, 1, 1 ), 0 ]
	]);

	public void function testMemberFunction() localmode="true" {
		totalAge = people.reduce( function(age=0, row, rowNumber, recordset ){
		    return age +  DateDiff( 'yyyy', recordset.dob, Now() );
		});
		assertEquals(151,totalAge);
	}

	public void function testFunction() localmode="true" {
		totalAge = QueryReduce(people, function(age=0, row, rowNumber, recordset ){
		    return age +  DateDiff( 'yyyy', recordset.dob, Now() );
		});
		assertEquals(151,totalAge);
	}




} 