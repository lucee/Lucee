component extends="org.lucee.cfml.test.LuceeTestCase" labels="json" {

	function beforeAll() {
		variables.originalNullSupport = getApplicationSettings().nullSupport;
	}

	function afterAll() {
		application action="update" nullSupport=variables.originalNullSupport;
	}

	function run( testResults, testBox ) {
		describe( title="LDEV-6185 JSON null round trip", body=function() {

			// a JSON null must survive deserializeJSON() -> serializeJSON() regardless of the null support setting
			// (LDEV-6185 was closed as Won't Fix: null keys are not omitted)
			for ( var nullSupport in [ false, true ] ) {
				var ns = nullSupport;

				it( title="deserializeJSON() null keeps its key in serializeJSON(), nullSupport=#ns#", data={ ns: ns }, body=function( data ) {
					application action="update" nullSupport=data.ns;
					var json = serializeJSON( deserializeJSON( '{"a":null,"b":1}' ) );
					assertRoundTrip( json );
				});

				it( title="nullValue() struct key is serialized as null, nullSupport=#ns#", data={ ns: ns }, body=function( data ) {
					application action="update" nullSupport=data.ns;
					var sct = {};
					sct[ "a" ] = nullValue();
					sct[ "b" ] = 1;
					assertRoundTrip( serializeJSON( sct ) );
				});
			}

		});
	}

	private function assertRoundTrip( required string json ) {
		// compare the structure, not key order or case
		expect( reFindNoCase( '"a"\s*:\s*null', arguments.json ) ).toBeGT( 0, arguments.json );
		var result = deserializeJSON( arguments.json );
		expect( listSort( structKeyList( result ), "textNoCase" ) ).toBe( "a,b", arguments.json );
		expect( isNull( result.a ) ).toBeTrue( arguments.json );
		expect( result.b ).toBe( 1 );
	}

}
