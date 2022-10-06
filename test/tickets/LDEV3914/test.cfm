<cfscript>
	runner1 = () => { 
		thread name="LDEV3914" {}
		return "success";
	}
	writeOutput(runner1())
</cfscript>
