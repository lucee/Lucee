component extends="org.lucee.cfml.test.LuceeTestCase" labels="query" {

	function run( testResults, testBox ) {
		describe( "LDEV-6233 QueryImpl: currentRow state survives serialisation, causes stale row reads", function() {

			it( title="a deserialised query starts at row 1 when the original was on another row", body=function( currentSpec ) {
				var original = getSource();
				original.go( 3, getPageContext().getId() );
				expect( original.name ).toBe( "row3" );

				var restored = objectLoad( objectSave( original ) );
				expect( restored.name ).toBe( "row1" );
				expect( restored.currentRow ).toBe( 1 );
			});

			it( title="the original query keeps its own row after being serialised", body=function( currentSpec ) {
				var original = getSource();
				original.go( 3, getPageContext().getId() );
				objectLoad( objectSave( original ) );
				expect( original.name ).toBe( "row3" );
				expect( original.currentRow ).toBe( 3 );
			});

			it( title="a deserialised query keeps all its data", body=function( currentSpec ) {
				var original = getSource();
				original.go( 2, getPageContext().getId() );

				var restored = objectLoad( objectSave( original ) );
				expect( restored.recordCount ).toBe( 3 );
				expect( listSort( restored.columnList, "textNoCase" ) ).toBe( "ID,NAME" );
				expect( restored.id[ 1 ] ).toBe( 1 );
				expect( restored.name[ 2 ] ).toBe( "row2" );
				expect( restored.name[ 3 ] ).toBe( "row3" );
			});

			it( title="a deserialised query that never had a row position starts at row 1", body=function( currentSpec ) {
				var restored = objectLoad( objectSave( getSource() ) );
				expect( restored.name ).toBe( "row1" );
				expect( restored.currentRow ).toBe( 1 );
			});

			it( title="looping over a deserialised query starts at row 1 and visits every row", body=function( currentSpec ) {
				var original = getSource();
				original.go( 3, getPageContext().getId() );

				var restored = objectLoad( objectSave( original ) );
				var visitedRows = [];
				loop query="restored" {
					arrayAppend( visitedRows, restored.currentRow & ":" & restored.name );
				}
				expect( visitedRows ).toBe( [ "1:row1", "2:row2", "3:row3" ] );
			});

			it( title="a row kept for a request that is not the creator is not carried over to the deserialised query", body=function( currentSpec ) {
				// another thread moves the query first and becomes its creator, so this request's row is kept in the per-request map
				variables.sharedQuery = getSource();
				var threadName = "LDEV6233_" & createUUID();
				thread name=threadName {
					variables.sharedQuery.go( 1, getPageContext().getId() );
				}
				thread action="join" name=threadName;
				expect( cfthread[ threadName ].status ).toBe( "COMPLETED" );

				variables.sharedQuery.go( 3, getPageContext().getId() );
				expect( variables.sharedQuery.name ).toBe( "row3" );

				var restored = objectLoad( objectSave( variables.sharedQuery ) );
				expect( restored.name ).toBe( "row1" );
				expect( restored.currentRow ).toBe( 1 );
			});

			it( title="repeated serialisation keeps starting at row 1 and keeps the data", body=function( currentSpec ) {
				var restored = getSource();
				loop from=1 to=3 index="local.cycle" {
					restored.go( 3, getPageContext().getId() );
					restored = objectLoad( objectSave( restored ) );
					expect( restored.name ).toBe( "row1" );
					expect( restored.recordCount ).toBe( 3 );
				}
			});

		});
	}

	private query function getSource() {
		return queryNew( "id,name", "integer,varchar", [ [ 1, "row1" ], [ 2, "row2" ], [ 3, "row3" ] ] );
	}

}
