<cfscript>
	consumer = new Consumer();
	writeOutput( "exists:#consumer.hasResponseFormatArgument()#;null:#consumer.isResponseFormatNull()#" );
</cfscript>
