<cfscript>
try{
	include template="test.cfm" runonce="true";
} catch ( any e ){
	result = e.message;
}
</cfscript>
