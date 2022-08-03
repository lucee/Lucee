<cfparam name="FORM.scene" default="">
<cfscript>
	function foo(x) { return arguments.x; }

	if (FORM.scene == 1) {
		z = invoke(variables, "foo", { x: "variables scope" });
		writeOutput(z);
	}
	else if (FORM.scene == 2) {
		z = invoke("", "foo", { x: "empty string in cfm page" });
		writeOutput(z);
	}
</cfscript>