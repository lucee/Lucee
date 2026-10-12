<cfapplication name="ldev2454-#hash( getCurrentTemplatePath() )#" blockedExtForFileUpload="*">
<cfscript>
	res = getPageContext().getConfig().getResource( getTempDirectory() & url.name );
	try {
		createObject( "java", "lucee.runtime.tag.FileTag" ).checkExtension( res, javaCast( "null", "" ), javaCast( "null", "" ), getPageContext().getApplicationContext() );
		echo( "allowed" );
	}
	catch ( any e ) {
		echo( "blocked" );
	}
</cfscript>
