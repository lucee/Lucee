<cfscript>
	param name="url.token";
	lib = new component javaSettings='{
			"maven": [ "org.slf4j:slf4j-api:2.0.17" ]
		}' {

		function getFactoryClass() {
			return createObject( "java", "org.slf4j.LoggerFactory" ).getILoggerFactory().getClass().getName();
		}

		function logError( required string msg ) {
			createObject( "java", "org.slf4j.LoggerFactory" ).getLogger( "ldev6664" ).error( arguments.msg );
		}
	};
	lib.logError( "ldev6664 " & url.token );
	echo( lib.getFactoryClass() );
</cfscript>
