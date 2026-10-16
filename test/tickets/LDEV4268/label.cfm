<cfscript>
	count = 0;
	outer: for (x = 1; x <= 10; x++) {
		for (y = 1; y <= 2; y++) {
			if (x EQ 5) continue outer
			if (x EQ 8) break outer
			count++;
		}
	}
	writeOutput(count);
</cfscript>
