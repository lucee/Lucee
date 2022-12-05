<cfscript>
	thread name="LDEV4157" {
		thread.test = "thread";
	}

	```
		<cfset res = "tag-island after the thread statement works">
	```

	thread action="join" name="LDEV4157";

	writeoutput(cfthread.LDEV4157.test & " and " & res);

</cfscript>