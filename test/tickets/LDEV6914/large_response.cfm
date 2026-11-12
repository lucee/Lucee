<cfscript>
// Outputs a large string to exercise the writer pool's byte buffer.
// Called by LDEV6914.cfc via _internalRequest.
param name="url.size" default="200000";
writeOutput( repeatString( "x", val( url.size ) ) );
</cfscript>
