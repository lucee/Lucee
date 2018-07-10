/*
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
 */
component extends="org.lucee.cfml.test.LuceeTestCase"	{
		variables.numbers=[3,1,2,4,5];
	    variables.numbers2=[2,5,3,1,4];

	private query function createQuery() localmode="true" {
		var qry=queryNew('a,b');
	    loop array=numbers item="local.nbr" {
	        var row=queryAddRow(qry);
	        querySetCell(qry,'a',"a"&nbr,row);
	        querySetCell(qry,'b',"b"&numbers2[row],row);
	    }
	    return qry;
	}
	    
	public void function testSortUDF() localmode="true" {
		var qry=createQuery();

	    assertEquals('a3,a1,a2,a4,a5',qry.valueList('a'));
    	assertEquals('b2,b5,b3,b1,b4',qry.valueList('b'));

    	querySort(qry,function (left,right){
	        return compare(left.a,right.a);
	    });

	    assertEquals('a1,a2,a3,a4,a5',qry.valueList('a'));
    	assertEquals('b5,b3,b2,b1,b4',qry.valueList('b'));

	    
	    querySort(qry,function (left,right){
	        return compare(left.b,right.b);
	    });

	    assertEquals('a4,a3,a2,a5,a1',qry.valueList('a'));
    	assertEquals('b1,b2,b3,b4,b5',qry.valueList('b'));
	    
	    querySort(qry,function (left,right){
	        return compare(right.b,left.b);
	    });

	    assertEquals('a1,a5,a2,a3,a4',qry.valueList('a'));
    	assertEquals('b5,b4,b3,b2,b1',qry.valueList('b'));
	}

	public void function testSortString() localmode="true" {
		var qry=createQuery();

		querySort(qry,"a");
	    assertEquals('a1,a2,a3,a4,a5',qry.valueList('a'));
    	assertEquals('b5,b3,b2,b1,b4',qry.valueList('b'));

		querySort(qry,"b");
	    assertEquals('a4,a3,a2,a5,a1',qry.valueList('a'));
    	assertEquals('b1,b2,b3,b4,b5',qry.valueList('b'));

		querySort(qry,"b","desc");
	    assertEquals('a1,a5,a2,a3,a4',qry.valueList('a'));
    	assertEquals('b5,b4,b3,b2,b1',qry.valueList('b'));

	}

	public void function testSortList() localmode="true" {
		var qry=createQuery();

		querySort(qry,"a,b");
	    assertEquals('a1,a2,a3,a4,a5',qry.valueList('a'));
    	assertEquals('b5,b3,b2,b1,b4',qry.valueList('b'));

	}


} 