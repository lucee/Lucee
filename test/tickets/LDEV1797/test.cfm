<cfscript>
arr = [];
hasError = false;
try {
	[1, 2, 3].each( function(i){
		arr.append("testThread#i#");
		thread name="testThread#i#"{
			sleep(300);
		}
	});
} catch ( any e ){
	hasError = true;
}
writeOutput(hasError);
</cfscript>