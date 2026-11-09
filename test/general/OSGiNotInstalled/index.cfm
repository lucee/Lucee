<cfscript>
try {
	cachePut( id: "k", value: "v", cacheName: "osgiguard" );
	echo( "no error" );
}
catch ( any e ) {
	echo( e.message );
}
</cfscript>
