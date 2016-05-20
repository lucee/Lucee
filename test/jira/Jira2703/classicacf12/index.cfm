<cfsetting showdebugoutput="no">
<cfscript>
variables.v="vv";
cookie.c="cv";
url.u="uv";
form.f="fv";

echo(getApplicationSettings().searchImplicitScopes);
echo("->");
echo(isDefined('v'));
echo(';');
echo(isDefined('u'));
echo(';');
echo(isDefined('f'));
echo(';');
echo(isDefined('c'));
echo(';');
echo(isDefined('script_name'));
echo(';');
</cfscript>