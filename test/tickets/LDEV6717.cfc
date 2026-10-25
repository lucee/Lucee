component extends="org.lucee.cfml.test.LuceeTestCase" labels="serialization" {

	function beforeAll() {
		variables.value = createValue();
		variables.bytes = objectSave( variables.value );
		variables.expected = serializeJSON( variables.value );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6717 objectLoad() with concurrent callers", function() {

			it( "objectLoad() returns the same value as the source", function() {
				var loaded = objectLoad( variables.bytes );
				assertValue( loaded );
			});

			it( "objectLoad() returns correct values when called from many threads at once", function() {
				var threadCount = 32;
				var iterations = 50;
				var prefix = "ldev6717_" & createUUID();
				var names = [];
				var bytes = variables.bytes;
				var expected = variables.expected;

				loop from=1 to=threadCount index="local.t" {
					var threadName = prefix & "_" & t;
					arrayAppend( names, threadName );
					thread name=threadName action="run" bytes=bytes expected=expected iterations=iterations {
						thread.loaded = 0;
						loop from=1 to=attributes.iterations index="local.i" {
							var loaded = objectLoad( attributes.bytes );
							if ( !isQuery( loaded.qry ) || loaded.qry.recordCount != 40 ) {
								throw( type="LDEV6717", message="query not restored correctly in iteration #i#" );
							}
							if ( serializeJSON( loaded ) != attributes.expected ) {
								throw( type="LDEV6717", message="loaded value differs from the source in iteration #i#" );
							}
							thread.loaded++;
						}
					}
				}
				threadJoin( arrayToList( names ) );

				loop array=names item="local.threadName" {
					var t = cfthread[ threadName ];
					if ( structKeyExists( t, "error" ) ) {
						fail( "thread [#threadName#] failed: " & t.error.message & " " & ( t.error.detail ?: "" ) );
					}
					expect( t.status ).toBe( "COMPLETED" );
					expect( t.loaded ).toBe( iterations );
				}
			});

			it( "objectLoad() resolves classes repeatedly within the same stream and across calls", function() {
				var arr = [];
				loop from=1 to=20 index="local.i" {
					arrayAppend( arr, createValue() );
				}
				var bytes = objectSave( arr );
				loop from=1 to=3 index="local.n" {
					var loaded = objectLoad( bytes );
					expect( loaded ).toHaveLength( 20 );
					loop array=loaded item="local.v" {
						assertValue( v );
					}
				}
			});
		});
	}

	private struct function createValue() {
		var items = [];
		loop from=1 to=40 index="local.i" {
			arrayAppend( items, [
				"id": i,
				"name": "item #i#",
				"price": i * 1.5,
				"active": ( i mod 2 == 0 ),
				"tags": [ "a#i#", "b#i#" ]
			]);
		}
		var qry = queryNew( "id,title,amount,created", "integer,varchar,double,timestamp" );
		loop from=1 to=40 index="local.i" {
			queryAddRow( qry, [ i, "row #i#", i * 2.25, createDateTime( 2026, 1, 1, 12, 0, 0 ) ] );
		}
		var javaList = createObject( "java", "java.util.ArrayList" ).init();
		javaList.add( "x" );
		javaList.add( createObject( "java", "java.math.BigDecimal" ).init( "12.34" ) );
		return [
			"items": items,
			"qry": qry,
			"created": createDateTime( 2026, 10, 25, 10, 30, 0 ),
			"nested": [ "a": [ "b": [ "c": "deep" ] ] ],
			"javaList": javaList
		];
	}

	private void function assertValue( required struct loaded ) {
		expect( serializeJSON( arguments.loaded ) ).toBe( variables.expected );
		expect( isQuery( arguments.loaded.qry ) ).toBeTrue();
		expect( arguments.loaded.qry.recordCount ).toBe( 40 );
		expect( arguments.loaded.items ).toHaveLength( 40 );
		expect( isDate( arguments.loaded.created ) ).toBeTrue();
		expect( arguments.loaded.nested.a.b.c ).toBe( "deep" );
		expect( arguments.loaded.javaList.size() ).toBe( 2 );
	}
}
