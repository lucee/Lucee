<cfscript>
	getter = new Getter();
	none = new None();
	result = {
		"getter": getter.foo,
		"missing": getter.bar,
		"noHandler": ""
	};
	try {
		x = none.bar;
	}
	catch ( any e ) {
		result.noHandler = e.message;
	}
	writeOutput( serializeJSON( result ) );
</cfscript>
