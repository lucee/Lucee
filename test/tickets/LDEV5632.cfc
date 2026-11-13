component extends="org.lucee.cfml.test.LuceeTestCase" labels="javasettings"  {

	function beforeAll() {
		variables.loadPaths=getTempDirectory("LDEV5632");
		// create lib dir
		if (!directoryExists(variables.loadPaths)) {
			directoryCreate(variables.loadPaths);
		}
		
		var trg=variables.loadPaths&"dd-java-agent.jar";
		if(!fileExists(trg)){
			// https://dtdg.co/latest-java-tracer
			http url="https://repo1.maven.org/maven2/com/datadoghq/dd-java-agent/1.9.0/dd-java-agent-1.9.0.jar" file="dd-java-agent.jar" path=variables.loadPaths throwOnError=true redirect="true" timeout=10;
		}

		var trg=variables.loadPaths&"opentelemetry-api.jar";
		if(!fileExists(trg)){
			http url="https://repo1.maven.org/maven2/io/opentelemetry/opentelemetry-api/1.50.0/opentelemetry-api-1.50.0.jar" file="opentelemetry-api.jar" path=variables.loadPaths throwOnError=true redirect="true" timeout=10;
		}

		var trg=variables.loadPaths&"opentelemetry-context.jar";
		if(!fileExists(trg)){
			http url="https://repo1.maven.org/maven2/io/opentelemetry/opentelemetry-context/1.50.0/opentelemetry-context-1.50.0.jar" file="opentelemetry-context.jar" path=variables.loadPaths throwOnError=true redirect="true" timeout=10;
		}
	}

	function afterAll() {
		// remove lib dir
		if (isWindows()) return; // jars are locked on windows
		if (directoryExists(variables.loadPaths)) {
			directoryDelete(variables.loadPaths,true);
		}
	}


	function run( testResults , testBox ) {
		describe( title="test java settings set directly with createObject", body=function() {

			it(title="validate environment", body = function( currentSpec ) {
				expect( directoryExists(variables.loadPaths) ).toBeTrue();
				expect( directoryExists(contractPath(variables.loadPaths)) ).toBeTrue();
			});


			it(title="load object from [loadPaths]", body = function( currentSpec ) {
				var obj=createObject(
					type:"java", 
					class:"io.opentelemetry.api.trace.Span",
					context : { loadPaths = [contractPath(variables.loadPaths)] }
				).current();
				expect( getMetadata(obj).getName() ).toBe( "io.opentelemetry.api.trace.PropagatedSpan" );
			});
	
		});
	}

	private function isWindows(){
		return (server.os.name contains "windows");
	}

	
}