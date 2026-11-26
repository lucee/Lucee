<cfscript>
result = "";
try {
	try {
		result &= "t";
		throw "outer";
	}
	catch ( any e ) {
		result &= "c";
		rethrow;
	}
	finally {
		try {
			result &= "f";
			throw "inner";
		}
		catch ( any e2 ) {
			result &= "i";
		}
	}
}
catch ( any e3 ) {
	result &= "-" & e3.message;
}
</cfscript>
