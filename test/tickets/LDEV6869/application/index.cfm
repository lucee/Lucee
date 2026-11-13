<cfscript>
	result = {
		sessionType: getApplicationSettings().sessionType,
		previous: session.ldev6869 ?: ""
	};
	session.ldev6869 = "application";
	result.value = session.ldev6869;

	content type="application/json";
	echo( serializeJSON( result ) );
</cfscript>
