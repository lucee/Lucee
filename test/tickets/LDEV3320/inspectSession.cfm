<cfscript>
	// LDEV-3320 the session must not keep a reference to the Application.cfc instance
	sess = getPageContext().sessionScope();
	cls = getPageContext().getClass().getClassLoader().loadClass( sessionClassName );
	if ( !cls.isInstance( sess ) ) {
		throw( message="session scope is not a [#sessionClassName#]" );
	}
	componentFields = [];
	while ( !isNull( cls ) ) {
		for ( field in cls.getDeclaredFields() ) {
			if ( field.getType().isPrimitive() ) continue;
			field.setAccessible( true );
			value = field.get( sess );
			if ( field.getType().getName() == "lucee.runtime.Component" || ( !isNull( value ) && isValid( "component", value ) ) ) {
				arrayAppend( componentFields, cls.getName() & "." & field.getName() );
			}
		}
		cls = cls.getSuperclass();
	}
	echo( serializeJSON( {
		"sessionId": session.sessionid,
		"applicationName": application.applicationName,
		"componentFields": componentFields
	} ) );
</cfscript>
