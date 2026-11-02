<cfscript>
	cfcookie( name="p", value="slash1", path="/" );
	cfcookie( name="p", value="foo1", path="/foo" );
	cfcookie( name="p", value="slash2", path="/" );
	cfcookie( name="p", value="foo2", path="/foo" );

	cookie.d = { value: "host1", domain: "example.com" };
	cookie.d = { value: "other", domain: "other.example" };
	cookie.d = { value: "host2", domain: ".Example.com" };
	cookie.d = { value: "nodomain", path: "/" };
</cfscript>
