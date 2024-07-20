component extends="org.lucee.cfml.test.LuceeTestCase" {

	variables.rounds = 10;
	variables.mysql = server.getDatasource("mysql");

	function run( testResults , testBox ) {
		describe( title="Test suite for CreateULID()", body=function() {
			it(title="checking CreateULID() function", body = function( currentSpec ) {
				// systemOutput( "", true );
				// systemOutput( "sample output", true );
				loop times=10 {
					// systemOutput( createULID(), true );
					createULID();
				}
			});

			it(title="checking CreateULID('Monotonic') function", body = function( currentSpec ) {
				// systemOutput( "", true );
				// systemOutput( "sample Monotonic output", true );
				loop times=10 {
					//systemOutput( createULID("Monotonic"), true );
                    createULID("Monotonic");
				}
			});

			it(title="checking CreateULID('hash', number, string ) function", body = function( currentSpec ) {
				var once = createULID( "hash", 1, "b" );
				var again = createULID( "hash", 1, "b" );

				expect( once ).toBe( again );
			});

			it(title="checking CreateULID() function perf with #variables.rounds# rows", body = function( currentSpec ) {

				var tbl = createTable( "default" );
				if ( isEmpty( tbl ) ) return;
				timer unit="milli" variable="local.timer" {
					//transaction {
						loop times=#variables.rounds# {
							populateTable( tbl, createULID() );
						}
					//}
				}
				// systemOutput( "" , true );
				// systemOutput( "inserting #variables.rounds# rows with CreateULID() took " & numberFormat(timer) & "ms", true);

				var r = testJoin( tbl );
				expect( r ).toBe( variables.rounds );
			});

			it(title="checking CreateUUID() function perf with #variables.rounds# rows", body = function( currentSpec ) {

				var tbl = createTable( "uuid" );
				if ( isEmpty( tbl ) ) return;

				timer unit="milli" variable="local.timer" {
					//transaction {
						loop times=#variables.rounds# {
							populateTable( tbl, createUUID() );
						}
					//}
				}

				// systemOutput( "" , true );
				// systemOutput( "inserting #variables.rounds# rows with CreateUUID() took " & numberFormat(timer) & "ms", true);

				var r = testJoin( tbl );
				expect( r ).toBe( variables.rounds );
			});

			it(title="checking CreateUUID() function perf with #variables.rounds# rows (pre cooked)", body = function( currentSpec ) {

				var tbl = createTable( "uuid_precooked" );
				if ( isEmpty( tbl ) ) return;
				var src = [];
				loop times=#variables.rounds# {
					arrayAppend(src, CreateUUID() );
				}

				timer unit="milli" variable="local.timer" {
					//transaction {
						loop from=1 to=#variables.rounds# index="local.i" {
							populateTable( tbl, src[i] );
						}
					//}
				}

				// systemOutput( "" , true );
				// systemOutput( "inserting #variables.rounds# rows with CreateUUID() (pre cooked) took " & numberFormat(timer) & "ms", true);
			});

			it(title="checking CreateULID('Monotonic') function perf with #variables.rounds# rows", body = function( currentSpec ) {

				var tbl = createTable( "Monotonic" );
				if ( isEmpty( tbl ) ) return;

				timer unit="milli" variable="local.timer" {
					//transaction {
						loop times=#variables.rounds# {
							populateTable( tbl, CreateULID("Monotonic") );
						}
					//}
				}

				// systemOutput( "" , true );
				// systemOutput( "inserting #variables.rounds# rows with CreateULID('Monotonic') took " & numberFormat(timer) & "ms", true);

				var r = testJoin( tbl );
				expect( r ).toBe( variables.rounds );
			});

			
			it(title="checking CreateULID('Monotonic') function perf with #variables.rounds# rows (pre cooked)", body = function( currentSpec ) {

				var tbl = createTable( "Monotonic_precooked" );
				if ( isEmpty( tbl ) ) return;
				var src = [];
				loop times=#variables.rounds# {
					arrayAppend(src, CreateULID("Monotonic") );
				}

				timer unit="milli" variable="local.timer" {
					//transaction {
						loop from=1 to=#variables.rounds# index="local.i" {
							populateTable( tbl, src[i] );
						}
					//}
				}

				// systemOutput( "" , true );
				// systemOutput( "inserting #variables.rounds# rows with CreateULID('Monotonic') (pre cooked) took " & numberFormat(timer) & "ms", true);
			});

		});
	}

	private function createTable( prefix ){
		if ( isEmpty( variables.mysql ) ) return "";

		var tbl = "test_ulid_" & prefix;

		query datasource=#variables.mysql# {
			echo("DROP TABLE IF EXISTS #tbl#");
		}
		query datasource=#variables.mysql# {
			echo("CREATE TABLE #tbl# ( id varchar(36) NOT NULL PRIMARY KEY ) ");
		}
		sleep(1000);
		return tbl;
	}

	private function populateTable (tbl, id){
		query datasource=#variables.mysql# params={ id: arguments.id, type="varchar" } {
			echo("INSERT into #arguments.tbl# (id) VALUES (:id) "); 
		}
	}

	private function testJoin(tbl){
		timer unit="milli" variable="local.timer" {
			query name="local.q" datasource=#variables.mysql# {
				echo("select t1.id from #tbl# t1, #tbl# t2 where t1.id=t2.id "); 
			}	
		}

		// systemOutput( "join with #tbl# took " & numberFormat(timer) & "ms", true);
		return q.recordcount;
	}
}