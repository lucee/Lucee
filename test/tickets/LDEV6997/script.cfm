<cfscript>
result = "";
try {
	result &= "t";
}
catch ( any e ) {
	result &= "c";
}
finally {
	try {
		result &= "f";
	}
	catch ( any e2 ) {
		result &= "x";
	}
}
</cfscript>
