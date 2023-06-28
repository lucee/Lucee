<cfscript>
	d = expandPath( "/logs" );  // should be a sub dir from this folder via a mapping
	systemOutput( d, true );
	systemOutput( getApplicationSettings().mappings, true );
	if ( !directoryExists( d ) )
		directoryCreate( d )
	echo( d );
</cfscript>