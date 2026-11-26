<cfscript>
result = "";
n = 0;
try {
	result &= "t";
}
catch ( any e ) {
}
finally {
	for ( i = 1; i <= 3; i++ ) {
		try {
			if ( i == 1 ) continue;
			if ( i == 3 ) break;
			result &= i;
		}
		catch ( any e2 ) {
		}
		finally {
			result &= "f";
		}
	}
	try {
		n++;
		if ( n < 2 ) throw "retry";
		result &= "r";
	}
	catch ( any e3 ) {
		retry;
	}
}
</cfscript>
