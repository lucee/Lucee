<cfscript>
// LDEV-6078: Test file with sequential statements
// Each statement should have a unique line number in compiled bytecode

// Line 6: variable declaration
var a = 1;
// Line 8: variable declaration
var b = 2;
// Line 10: variable declaration with expression
var c = a + b;

// Line 13: function call
systemOutput( "a=#a#", true );
// Line 15: function call
systemOutput( "b=#b#", true );
// Line 17: function call
systemOutput( "c=#c#", true );
</cfscript>
