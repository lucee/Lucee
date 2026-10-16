<cfscript>
// LDEV-6078: Test file with control structures
// Each statement should have a unique line number in compiled bytecode

function testIfElse( required numeric value ) {
	// Line 6: variable declaration
	var result = 0;
	// Line 8: if condition (separate line from body)
	if ( arguments.value > 10 ) {
		// Line 10: assignment inside if
		result = arguments.value * 2;
	}
	// Line 13: return statement
	return result;
}

function testTryCatch() {
	// Line 18: variable declaration
	var data = "";
	// Line 20: try block start
	try {
		// Line 22: assignment inside try
		data = "success";
	// Line 24: catch block
	} catch ( any e ) {
		// Line 26: assignment inside catch
		data = "error";
	}
	// Line 29: return statement
	return data;
}

function testForLoop() {
	// Line 34: variable declaration
	var total = 0;
	// Line 36: for loop
	for ( var i = 1; i <= 5; i++ ) {
		// Line 38: assignment inside loop
		total += i;
	}
	// Line 41: return statement
	return total;
}

// Test calls
systemOutput( testIfElse( 15 ), true );
systemOutput( testTryCatch(), true );
systemOutput( testForLoop(), true );
</cfscript>
