<cflogin idletimeout="2" cookiedomain="lucee.org">
	<cfloginuser name="cfloginTestUser" password="dummy" roles="test">
</cflogin>
<cfscript>
	echo( getAuthUser() );
	//echo( session.toJson() );
</cfscript>