component extends="org.lucee.cfml.test.LuceeTestCase" labels="query" {

	public function beforeTests(){
		defineDatasource();
	}

	public void function testArrayQueryParamsInts() {
		testArrayQueryParam( [ 1, 2, 3 ] );
	}

	public void function testArrayQueryParamsStrings() {
		testArrayQueryParam( [ "1", "2", "3" ] );
	}


	private string function defineDatasource(){
		application action="update"
			datasource="#server.getDatasource( "h2", server._getTempDir( "ldev-3893" ) )#";
	}

	private function createTable(name) {
		//dropTable(name);
		query  {
			echo("CREATE TABLE "&name&" (");
			echo("id int NOT NULL,");
			echo("i int,");
			echo("dec DECIMAL");
			echo(") ");
		}
	}

	private function dropTable(name) {
		try{
			query {
				echo("drop TABLE "&name);
			}
		}
		catch(local.e){}
	}

	private void function testArrayQueryParam( arr ) localmode=true{
		var tbl = "qp_array";
		var qry = "";
		createTable( tbl );
		try {
			query name="qry" {
				echo("insert into #tbl# ( id, i, dec) values( 1, 1, 1.0 )");
			}

			var params = {
				id: { value: arguments.arr, type: "integer", list: false }
			};
			expect( function(){
				query name="qry" params=#params# {
					echo(" SELECT id FROM #tbl# WHERE id = :id ");
				}
				```
				<cfquery name="qry">
					SELECT id FROM #tbl# WHERE id = <cfqueryparam value=#params.id.value# sqltype=#params.id.type# list=#params.id.list#>
				</cfquery>
				```
			}).toThrow(); // Array explicitly not allowed

			expect( function(){
				query name="qry" params=#params# {
					echo(" SELECT id FROM #tbl# WHERE id IN ( :id ) ");
				}
				```
				<cfquery name="qry">
					SELECT id FROM #tbl# WHERE id in ( <cfqueryparam value=#params.id.value# sqltype=#params.id.type# list=#params.id.list#> )
				</cfquery>
				```
			}).toThrow(); // Array explicitly not allowed, despite IN clause

			params.id.list = true;
			expect( function(){
				query name="qry" params=#params# {
					echo(" SELECT id FROM #tbl# WHERE id = :id ");
				}
				```
				<cfquery name="qry">
					SELECT id FROM #tbl# WHERE id = <cfqueryparam value=#params.id.value# sqltype=#params.id.type# list=#params.id.list#>
				</cfquery>
				```
			}).toThrow(); // Array throws an error, as it doesn't produce valid sql (not an IN clause, ID = ?,?,? )

			query name="qry" params=#params# {
				echo(" SELECT id FROM #tbl# WHERE id IN ( :id ) ");
			}
			expect( qry.recordcount ).toBe( 1 ); // Array allowed, works inside an IN clause

			```
			<cfquery name="qry">
				SELECT id FROM #tbl# WHERE id in ( <cfqueryparam value=#params.id.value# sqltype=#params.id.type# list=#params.id.list#> )
			</cfquery>
			```
			expect( qry.recordcount ).toBe( 1 ); // Array allowed, works inside an IN clause

			structDelete( params.id, "list" );
			query name="qry" params=#params# {
				echo(" SELECT id FROM #tbl# WHERE id IN ( :id ) ");
			}
			expect( qry.recordcount ).toBe( 1 ); // Array allowed, inside an IN clause (default behavior)

			```
			<cfquery name="qry">
				SELECT id FROM #tbl# WHERE id in ( <cfqueryparam value=#params.id.value# sqltype=#params.id.type#> )
			</cfquery>
			```
			expect( qry.recordcount ).toBe( 1 ); // Array allowed, inside an IN clause (default behavior)
		}
		finally {
			dropTable( tbl );
		}
	}
}
