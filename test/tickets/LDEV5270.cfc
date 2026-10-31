component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {
		describe( "LDEV-5270 validate Maven coordinate parts", function() {

			it( "mavenLoad still loads a valid coordinate", function() {
				var jars = mavenLoad( [ "commons-logging:commons-logging:1.2" ] );
				expect( arrayFilter( jars, function( jar ) {
					return findNoCase( "commons-logging-1.2.jar", jar ) > 0;
				}) ).toHaveLength( 1, serializeJSON( jars ) );
			});

			it( "mavenLoad rejects unsafe coordinate parts", function() {
				arrayEach( getUnsafe(), function( data ) {
					expect( function() {
						mavenLoad( [ { groupId: data.g, artifactId: data.a, version: data.v } ] );
					}).toThrow( type="application", regex="Invalid Maven", message=serializeJSON( data ) );
				});
			});

			it( "mavenLoad rejects unsafe parts in the gradle style form", function() {
				expect( function() {
					mavenLoad( [ "org.example:art:../../x" ] );
				}).toThrow( type="application", regex="Invalid Maven" );
			});

		});
	}

	private array function getUnsafe() {
		return [
			{ g: "..", a: "art", v: "1.0" },
			{ g: "org..example", a: "art", v: "1.0" },
			{ g: ".org.example", a: "art", v: "1.0" },
			{ g: "org.example.", a: "art", v: "1.0" },
			{ g: "org/example", a: "art", v: "1.0" },
			{ g: "org\example", a: "art", v: "1.0" },
			{ g: "../../org", a: "art", v: "1.0" },
			{ g: "org.example", a: "..", v: "1.0" },
			{ g: "org.example", a: "../art", v: "1.0" },
			{ g: "org.example", a: "art/../../x", v: "1.0" },
			{ g: "org.example", a: "art\..\x", v: "1.0" },
			{ g: "org.example", a: "art", v: ".." },
			{ g: "org.example", a: "art", v: "../../x" },
			{ g: "org.example", a: "art", v: "1.0/../x" },
			{ g: "org.example", a: "art", v: "1.0\x" },
			{ g: "org.example", a: "art", v: "1.0?x=1" },
			{ g: "org.example", a: "art", v: "1.0##x" },
			{ g: "org.example", a: "art", v: "1.0%2e%2e" },
			{ g: "org.example", a: "art", v: "1.0" & chr( 1 ) & "x" },
			{ g: "org.example", a: "art", v: "1.0" & chr( 10 ) & "x" },
			{ g: "org.example", a: "art", v: "1 0" },
			{ g: "org.example", a: "a:b", v: "1.0" },
			{ g: "org.example", a: "art", v: "C:x" }
		];
	}
}
