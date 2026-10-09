<cfscript>
	count = 0;
	for (x = 1; x <= 10; x++) {
		// the statement on the next line is not the label of continue
		if (x EQ 5) continue
		count++;
	}
	writeOutput(count);
</cfscript>
