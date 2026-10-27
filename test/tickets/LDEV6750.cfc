component extends="org.lucee.cfml.test.LuceeTestCase" labels="syntax" {

	function run( testResults, testBox ) {
		describe( "LDEV-6750: struct literal with the keys class, java, cfml and cfc", function() {

			it( "{ class: [ 1, 2 ] }", function() {
				include "LDEV6750/class.cfm";
				expect( serializeJSON( result.class ) ).toBe( "[1,2]" );
			});

			it( "{ java: [ 1, 2 ] }", function() {
				include "LDEV6750/java.cfm";
				expect( serializeJSON( result.java ) ).toBe( "[1,2]" );
			});

			it( "{ Class: [ 0, 0 ].slice( 1, 1 ) }", function() {
				include "LDEV6750/member.cfm";
				expect( serializeJSON( result.class ) ).toBe( "[0]" );
			});

			it( "{ cfml: [ 1 ], cfc: [ 2 ] }", function() {
				include "LDEV6750/cfmlcfc.cfm";
				expect( serializeJSON( result.cfml ) ).toBe( "[1]" );
				expect( serializeJSON( result.cfc ) ).toBe( "[2]" );
			});

			it( "{ class: ""a"", java: 1, cfml: { b: 2 }, cfc: [ 3 ] }", function() {
				include "LDEV6750/mixed.cfm";
				expect( result.class ).toBe( "a" );
				expect( result.java ).toBe( 1 );
				expect( result.cfml.b ).toBe( 2 );
				expect( serializeJSON( result.cfc ) ).toBe( "[3]" );
			});

			it( "static access with a java and cfml prefix still works", function() {
				expect( java:java.lang.Integer::MAX_VALUE ).toBe( createObject( "java", "java.lang.Integer" ).MAX_VALUE );
			});

		});
	}
}
