<cfscript>
application action="update" sessionmanagement="true";


	url.test="url-581";
	form.test="form-581";
	cookie.test="cookie-581";
	session.test="session-581";
	setting showdebugoutput="false";
	header name="test" value="header-581";

echo("{");
	// URL
	echo("url:");
	echo(serialize(url));
	echo(",");

	// Form
	echo("form:");
	echo(serialize(form));
	echo(",");

	// Cookie
	echo("cookie:");
	echo(serialize(cookie));
	echo(",");

	// HTTPRequestData
	echo("HTTPRequestData:");
	echo(serialize(getHTTPRequestData()));


echo("}");
</cfscript>