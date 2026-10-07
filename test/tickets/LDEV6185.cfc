component extends="org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll() {
		variables.originalNS = getApplicationSettings().nullSupport;
	}

	function afterAll() {
		application action="update" nullSupport=variables.originalNS;
	}

	function run( testResults, testBox ) {

		describe( title="LDEV-6185 serializeJSON with nullValue(), null support disabled", body=function() {

			beforeEach( function( currentSpec, data ) {
				application action="update" nullSupport=false;
			});

			it( title="nullValue() keys are omitted", body=function() {
				var data = { name: "Pothys", middleName: nullValue() };
				var json = deserializeJSON( serializeJSON( data ) );
				expect( structKeyExists( data, "middleName" ) ).toBeFalse();
				expect( structKeyExists( json, "middleName" ) ).toBeFalse();
				expect( json.name ).toBe( "Pothys" );
				expect( structCount( json ) ).toBe( 1 );
			});

			it( title="nullValue() keys are omitted in nested structs", body=function() {
				var data = { outer: { inner: nullValue(), x: 1 } };
				var json = deserializeJSON( serializeJSON( data ) );
				expect( structKeyExists( json.outer, "inner" ) ).toBeFalse();
				expect( json.outer.x ).toBe( 1 );
			});

			it( title="a struct with only nullValue() keys serializes as an empty object", body=function() {
				var data = { a: nullValue() };
				expect( serializeJSON( data ) ).toBe( "{}" );
			});

			it( title="array elements keep their position", body=function() {
				var data = [ 1, nullValue(), 3 ];
				expect( serializeJSON( data ) ).toBe( "[1,null,3]" );
			});

		});

		describe( title="LDEV-6185 serializeJSON with nullValue(), null support enabled", body=function() {

			beforeEach( function( currentSpec, data ) {
				application action="update" nullSupport=true;
			});

			afterEach( function( currentSpec, data ) {
				application action="update" nullSupport=variables.originalNS;
			});

			it( title="null keys are serialized as JSON null", body=function() {
				var data = { name: "Pothys", middleName: nullValue() };
				var json = serializeJSON( data );
				expect( json ).toInclude( "null" );
				expect( json.reFindNoCase( '"middleName"\s*:\s*null' ) ).toBeGT( 0 );
			});

		});

	}

}
