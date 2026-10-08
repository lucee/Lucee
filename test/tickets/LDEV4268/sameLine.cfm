<cfscript>
	count = 0;
	for (x = 1; x <= 10; x++) {
		if (x EQ 5) { continue }
		if (x EQ 8) { break }
		count++;
	}
	writeOutput(count);
</cfscript>
