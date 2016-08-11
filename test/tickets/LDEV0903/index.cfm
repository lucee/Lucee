<cfscript>
	transaction isolation='read_uncommitted' {
		a = entityNew( 'RosterEmbargo' );
		a.setteamID( "E6983EDD-BBEB-43D3-BEC2-C648660142C7" ); // string
		a.setseasonID( 1 ); // int
		a.setseasonUID( "1" ); // string
		a.setembargoDate( now() ); // timestamp
		entitySave( a );
	}
	transaction isolation='read_uncommitted' {
		embargo = EntityLoad("RosterEmbargo",{teamID:'E6983EDD-BBEB-43D3-BEC2-C648660142C7'},true);
		EntitySave(embargo);
	}
</cfscript>