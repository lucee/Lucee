<cfscript>
// Test if astFromPath works with files outside mappings
testFile = getTempDirectory() & "ast-test-outside-mapping.cfm";
fileWrite( testFile, '<cfset x = 1>' );

try {
    result = astFromPath( testFile );
    writeOutput( "SUCCESS: astFromPath worked for file outside mapping" );
    writeDump( result );
} catch( any e ) {
    writeOutput( "FAILED: " & e.message );
}
</cfscript>
