<cflogin applicationtoken="secret" cookiedomain="lucee.org"> <!--- idleTimeout only for loginStorage=cookie --->
	<cfloginuser name="cfloginTestUser" password="dummy" roles="test">
</cflogin>
<cfscript>
	session.add_content_to_create=true;
	session.else_no_session=true;
	echo( getAuthUser() );
	//echo( session.toJson() );
</cfscript>