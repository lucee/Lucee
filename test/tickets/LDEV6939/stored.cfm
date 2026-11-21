<cfscript>
	// read the copy that was written to the cache, not the live session
	key = "lucee-storage:session:#session.cfid#:#application.applicationName#".uCase();
	sv = cacheGet( key, true, "ldev6939cache" );
	IKStorageValue = createObject( "java", "lucee.runtime.type.scope.storage.IKStorageValue" );
	stored = IKStorageValue.deserialize( IKStorageValue.toByteRepresentation( sv )[ 1 ] );
	echo( stored.get( createObject( "java", "lucee.runtime.type.KeyImpl" ).getInstance( "user" ) ).getValue().getUsername() );
</cfscript>
