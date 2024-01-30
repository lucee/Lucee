<cfscript>
    count = 0;
    for (x = 1; x <= 10; x++) {
        // ignoring row 5 and continue the loop
        if (x EQ 5) {
            continue;
        }
        count++;
    }
    writeOutput(count);
</cfscript>