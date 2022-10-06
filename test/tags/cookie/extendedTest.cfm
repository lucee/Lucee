<cfscript>
	include template="extendedData.cfm";

	loop array=#cookieTestData# item="c"{
		if ( !structKeyExists( c, "encodevalue" ) )
			cfcookie( name=c.name, value=c.value  );
		else
			cfcookie( name=c.name, value=c.value, encodevalue=c.encodeValue );
	}
</cfscript>
