<cfscript>
	fn = ((x, y) => {
		return x * y;
	}(2, 3));

	writeOutput(fn);
</cfscript>