<cfscript>
	count = 0;
	for (x = 1; x <= 10; x++) {
		// leave the loop at row 5
		if (x EQ 5) {
			break
		}
		count++;
	}
	writeOutput(count);
</cfscript>
