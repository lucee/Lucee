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

	variables.suffix="QueryLazy";

	public function beforeTests(){
		defineDatasource();

		try{
			query {
				echo("drop TABLE T"&suffix);
			}
		}
		catch(local.e){}

		query {
			echo("CREATE TABLE T"&suffix&" (");
			echo("id int NOT NULL,")
			echo("n int NOT NULL")
			echo(") ");
		}

		loop from=1 to=8 index="local.i" {
			queryExecute("insert into t#suffix# ( id, n ) values (:id, :n )", {
				id: { value=i, type="integer" },
				n: { value=i*2, type="integer" }
			});
		}
	}

	private string function defineDatasource(){
		application action="update"
			datasource="#server.getDatasource( "h2", server._getTempDir( "queryExecute" ) )#";
	}

	public void function testLazyQuery(){
		var result = testLazy( {
			maxrows: 6,
			blockfactor: 2
		} );
		expect( ArrayLen( result ) ).toBe( 6 );
		expect( result.toJson() ).toBe('[{"COLUMNS":["ID","N"],"DATA":[[1,2],[2,4]]},"-",'
			& '{"COLUMNS":["ID","N"],"DATA":[[3,6],[4,8]]},"-",'
			& '{"COLUMNS":["ID","N"],"DATA":[[5,10],[6,12]]},"-"]');
	}

	public void function testLazyArray(){
		var result = testLazy( {
			maxrows: 6,
			blockfactor: 2,
			returnType: "array"
		} );
		expect( ArrayLen( result ) ).toBe( 6 );
		expect( result.toJson() ).toBe( '[[{"ID":1,"N":2},{"ID":2,"N":4}],"-",[{"ID":3,"N":6},{"ID":4,"N":8}],"-",[{"ID":5,"N":10},{"ID":6,"N":12}],"-"]' );
	}

	public void function testLazyStructByN(){
		var result = testLazy( {
			maxrows: 6,
			blockfactor: 2,
			returnType: "struct",
			columnKey: "n"
		} );
		expect( ArrayLen( result ) ).toBe( 6 );
		expect( result.toJson() ).toBe('[{"4":{"ID":2,"N":4},"2":{"ID":1,"N":2}},"-",{"6":{"ID":3,"N":6},"8":{"ID":4,"N":8}},"-",{"12":{"ID":6,"N":12},"10":{"ID":5,"N":10}},"-"]');
	}

	public void function testLazyStructById(){
		var result = testLazy( {
			maxrows: 6,
			blockfactor: 2,
			returnType: "struct",
			columnKey: "id"
		} );
		expect( ArrayLen( result ) ).toBe( 6 );
		expect( result.toJson() ).toBe( '[{"2":{"ID":2,"N":4},"1":{"ID":1,"N":2}},"-",{"3":{"ID":3,"N":6}},"-"]' );
	}

	public void function testLazyBlockFactor1(){
		var result = testLazy( {
			maxrows: 3,
			blockfactor: 1,
			returnType: "struct",
			columnKey: "id"
		} );
		expect( ArrayLen( result ) ).toBe( 6 );
		// with blockfactor 1, the result will always be a simple struct
		expect( result.toJson() ).toBe( '[{"ID":1,"N":2},"-",{"ID":2,"N":4},"-",{"ID":3,"N":6},"-"]' );
	}

	public void function testLazyStructById(){
		expect ( function(){
			testLazy( {
				maxrows: 3,
				blockfactor: 2,
				returnType: "struct",
				columnKey: "not_a_column"
			} );
		}).toThrow(); // "key [not_a_column] doesn't exist";
	}

	private array function testLazy( required struct options ) localmode="true" {
		var result = [];
		queryLazy(
			sql: "SELECT * FROM t#suffix# order by id"
			,listener: function( rows ){
				//systemOutput( rows, true );
				arrayAppend( result, arguments.rows );
				arrayAppend( result, "-" );
			}
			,options: arguments.options
		);
		/*
		systemOutput(arguments, true);
		systemOutput(result, true);
		systemOutput(result.toJson(), true);
		*/
		return result;

	};

}