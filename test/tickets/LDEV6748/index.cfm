<cfscript>
	consumer = new Consumer();
	writeOutput( "exists:#consumer.hasResponseFormat()#;null:#consumer.isResponseFormatNull()#" );
</cfscript>
