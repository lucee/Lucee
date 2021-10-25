<cfscript>
	function foo(x) { return arguments.x; }
	z = invoke(variables, "foo", { x: "y" });
	writeOutput(z);
</cfscript>