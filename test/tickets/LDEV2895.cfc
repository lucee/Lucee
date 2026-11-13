component extends="org.lucee.cfml.test.LuceeTestCase" labels="query,cache,hsqldb" {

	function run( testResults, testBox ) {
		describe( "LDEV-2895 query cache key includes the datasource identity", function() {

			it( title="cachedWithin with two dynamic datasource structs does not leak results", body=function() {
				var suffix = replace( createUUID(), "-", "", "all" );
				var ds1 = hsqlDs( "ldev2895_a_#suffix#" );
				var ds2 = hsqlDs( "ldev2895_b_#suffix#" );
				seedCustomers( ds1, "db1" );
				seedCustomers( ds2, "db2" );

				var q1 = queryExecute(
					  "select name, src from customers where name = 'Joe'"
					, {}
					, { datasource: ds1, cachedWithin: createTimespan( 0, 0, 0, 60 ), result: "r1" }
				);
				var q2 = queryExecute(
					  "select name, src from customers where name = 'Joe'"
					, {}
					, { datasource: ds2, cachedWithin: createTimespan( 0, 0, 0, 60 ), result: "r2" }
				);

				expect( q1.src ).toBe( "db1" );
				expect( q2.src ).toBe( "db2" );
				expect( r1.cached ).toBeFalse();
				expect( r2.cached ).toBeFalse();
				expect( r1.cachedWithinId ).notToBe( r2.cachedWithinId );
			});

			it( title="the same dynamic datasource still gets a cache hit", body=function() {
				var suffix = replace( createUUID(), "-", "", "all" );
				var ds = hsqlDs( "ldev2895_same_#suffix#" );
				seedCustomers( ds, "db1" );

				var q1 = queryExecute(
					  "select name, src from customers where name = 'Joe'"
					, {}
					, { datasource: ds, cachedWithin: createTimespan( 0, 0, 0, 60 ), result: "r1" }
				);
				var q2 = queryExecute(
					  "select name, src from customers where name = 'Joe'"
					, {}
					, { datasource: ds, cachedWithin: createTimespan( 0, 0, 0, 60 ), result: "r2" }
				);

				expect( q1.src ).toBe( "db1" );
				expect( q2.src ).toBe( "db1" );
				expect( r1.cached ).toBeFalse();
				expect( r2.cached ).toBeTrue();
				expect( r1.cachedWithinId ).toBe( r2.cachedWithinId );
			});

			it( title="the same datasource name with a different connection string does not leak results", body=function() {
				var suffix = replace( createUUID(), "-", "", "all" );
				var name = "ldev2895_shared_#suffix#";

				application action="update" datasources={
					"#name#": hsqlDs( "ldev2895_n1_#suffix#" )
				};
				seedCustomers( name, "db1" );
				var q1 = queryExecute(
					  "select name, src from customers where name = 'Joe'"
					, {}
					, { datasource: name, cachedWithin: createTimespan( 0, 0, 0, 60 ), result: "r1" }
				);

				application action="update" datasources={
					"#name#": hsqlDs( "ldev2895_n2_#suffix#" )
				};
				seedCustomers( name, "db2" );
				var q2 = queryExecute(
					  "select name, src from customers where name = 'Joe'"
					, {}
					, { datasource: name, cachedWithin: createTimespan( 0, 0, 0, 60 ), result: "r2" }
				);

				expect( q1.src ).toBe( "db1" );
				expect( q2.src ).toBe( "db2" );
				expect( r2.cached ).toBeFalse();
				expect( r1.cachedWithinId ).notToBe( r2.cachedWithinId );
			});

		});
	}

	private struct function hsqlDs( required string memName ) {
		return {
			class: "org.hsqldb.jdbcDriver",
			bundleName: "org.lucee.hsqldb",
			connectionString: "jdbc:hsqldb:mem:#arguments.memName#",
			username: "sa",
			password: ""
		};
	}

	private void function seedCustomers( required any datasource, required string src ) {
		queryExecute(
			  "create table customers (name varchar(50), src varchar(50))"
			, {}
			, { datasource: arguments.datasource }
		);
		queryExecute(
			  "insert into customers values ('Joe', ?)"
			, [ arguments.src ]
			, { datasource: arguments.datasource }
		);
	}

}
