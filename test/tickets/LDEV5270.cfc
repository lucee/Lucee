component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {
		describe( "LDEV-5270 validate Maven coordinate parts", function() {

			it( "valid coordinates are accepted", function() {
				expect( mavenExists( "org.apache.commons", "commons-lang3", "0.0.0-ldev5270-does-not-exist" ) ).toBeFalse();
				expect( mavenExists( "org.apache.commons:commons-lang3:0.0.0-ldev5270-does-not-exist" ) ).toBeFalse();
				expect( mavenExists( "ch.qos.logback", "logback-classic", "[1.2,2.0)" ) ).toBeFalse();
				expect( mavenExists( "com.example_1", "my-artifact.name_1", "1.0.0-SNAPSHOT" ) ).toBeFalse();
			});

			it( "mavenLoad still loads a valid coordinate", function() {
				var jars = mavenLoad( [ "commons-logging:commons-logging:1.2" ] );
				expect( len( jars ) ).toBe( 1, serializeJSON( jars ) );
				expect( mavenExists( "commons-logging", "commons-logging", "1.2" ) ).toBeTrue();
			});

			it( "mavenExists rejects unsafe coordinate parts", function() {
				arrayEach( getUnsafe(), function( data ) {
					expect( function() {
						mavenExists( data.g, data.a, data.v );
					}).toThrow( type="application", regex="Invalid Maven", message=serializeJSON( data ) );
				});
			});

			it( "mavenExists rejects unsafe parts in the gradle style form", function() {
				expect( function() {
					mavenExists( "org.example:..:1.0" );
				}).toThrow( type="application", regex="Invalid Maven" );
				expect( function() {
					mavenExists( "org.example:art:../../x" );
				}).toThrow( type="application", regex="Invalid Maven" );
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
