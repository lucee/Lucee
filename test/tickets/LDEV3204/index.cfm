<cfscript>
	err = "";
	try {
		mail to="a@lucee.org" from="b@lucee.org" subject="LDEV3204-#url.id#" spoolEnable=false { echo( "x" ); }
	}
	catch ( any e ) { err = e.message; }
	echo( serializeJSON( { "err": err } ) );
</cfscript>
