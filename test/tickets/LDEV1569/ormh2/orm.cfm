<cfscript>
	setting showdebugoutput=false;
	if ( url.scene == "entityNew" ) {
		item = entityNew( "Item1569" );
		item.setId( 2 );
		item.setName( nullValue() );
		entitySave( item );
		ormFlush();
		echo( serializeJSON( item ) );
	}
	else if ( url.scene == "entityLoad" ) {
		item = entityNew( "Item1569" );
		item.setId( 3 );
		item.setName( nullValue() );
		entitySave( item );
		ormFlush();
		ormClearSession();
		echo( serializeJSON( entityLoadByPK( "Item1569", 3 ) ) );
	}
	else if ( url.scene == "plain" ) {
		plain = new Plain1569();
		plain.setId( 4 );
		plain.setName( nullValue() );
		echo( serializeJSON( plain ) );
	}
</cfscript>
