component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {

		describe( "GetSystemPropOrEnvVarInfo() — returns a struct of all supported system settings", function() {

			it( "returns a struct", function() {
				expect( isStruct( GetSystemPropOrEnvVarInfo() ) ).toBeTrue();
			} );

			it( "has at least one entry", function() {
				expect( structCount( GetSystemPropOrEnvVarInfo() ) ).toBeGT( 0 );
			} );

			it( "is keyed by config setting name", function() {
				expect( GetSystemPropOrEnvVarInfo() ).toHaveKey( "loginCaptcha" );
			} );

			it( "each entry exposes type, description, systemProperties and environmentVariables", function() {
				var info = GetSystemPropOrEnvVarInfo();
				var entry = info[ listFirst( structKeyList( info ) ) ];
				// note: a key whose value is null (e.g. an absent description) still appears in
				// the key list, so assert on the key list rather than structKeyExists/toHaveKey
				var entryKeys = structKeyList( entry );
				expect( listFindNoCase( entryKeys, "type" ) ).toBeGT( 0 );
				expect( listFindNoCase( entryKeys, "description" ) ).toBeGT( 0 );
				expect( listFindNoCase( entryKeys, "systemProperties" ) ).toBeGT( 0 );
				expect( listFindNoCase( entryKeys, "environmentVariables" ) ).toBeGT( 0 );
				expect( entry.systemProperties ).toBeArray();
				expect( entry.environmentVariables ).toBeArray();
			} );

		} );
	}

}
