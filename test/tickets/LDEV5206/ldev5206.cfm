<cfscript>
	// test DebugExecutionLog refers to this code, update if changed
	sleep(5);
	echo("back from sleep");
	cfc = new ldev5206();
	cfc.doSleep();

	cfc = new ldev5206_tag();
	cfc.doSleep();
</cfscript>
