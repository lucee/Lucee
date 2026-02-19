<cfscript>
// Test 1: Pure script mode - works
systemOutput( "=== TEST 1: Script mode ===", true );
try {
	code1 = 'x = "test <!--- comment";';
	ast1 = astFromString( code1, "script" );
	systemOutput( "SUCCESS: " & serializeJSON( ast1 ), true );
} catch( any e ) {
	systemOutput( "ERROR: " & e.message, true );
}

// Test 2: Component without second param - broken
systemOutput( "", true );
systemOutput( "=== TEST 2: Component without script mode ===", true );
try {
	code2 = 'component { function test() { x = "^<!---.*--->$"; } }';
	ast2 = astFromString( code2 );
	systemOutput( "Result body length: " & arrayLen( ast2.body ), true );
	systemOutput( "Full AST: " & serializeJSON( ast2 ), true );
} catch( any e ) {
	systemOutput( "ERROR: " & e.message, true );
}

// Test 3: Component WITH script mode
systemOutput( "", true );
systemOutput( "=== TEST 3: Component WITH script mode ===", true );
try {
	code3 = 'component { function test() { x = "^<!---.*--->$"; } }';
	ast3 = astFromString( code3, "script" );
	systemOutput( "Result body length: " & arrayLen( ast3.body ), true );
	systemOutput( "First body type: " & ast3.body[1].type, true );
} catch( any e ) {
	systemOutput( "ERROR: " & e.message, true );
}

// Test 4: What if we manually wrap in cfscript?
systemOutput( "", true );
systemOutput( "=== TEST 4: Manually wrapped in cfscript (tag mode) ===", true );
try {
	code4 = '<cfscript>component { function test() { x = "^<!---.*--->$"; } }</cfscript>';
	ast4 = astFromString( code4 );
	systemOutput( "Result body length: " & arrayLen( ast4.body ), true );
	systemOutput( "Full AST: " & serializeJSON( ast4 ), true );
} catch( any e ) {
	systemOutput( "ERROR: " & e.message, true );
}
</cfscript>
