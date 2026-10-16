<cfparam name="url.mode" default="test">
<cfparam name="url.count" default="5">
<cfscript>
// Line 4: variable assignment
var result = [];
// Line 6: variable assignment
var mode = url.mode;

// Line 9: loop
for ( var i = 1; i <= url.count; i++ ) {
	// Line 11: array append
	arrayAppend( result, "item #i#" );
}

// Line 15: output
systemOutput( "Mode: #mode#", true );
// Line 17: output
systemOutput( "Count: #arrayLen( result )#", true );
</cfscript>

<cfoutput>
<h1>Results</h1>
<ul>
<cfloop array="#result#" item="item">
	<li>#item#</li>
</cfloop>
</ul>
</cfoutput>
