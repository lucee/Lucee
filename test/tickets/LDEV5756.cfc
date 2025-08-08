component extends = "org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, textbox ) {
		describe(title="testcase for LDEV-5756", body=function(){

			it(title = "check log appenders - pattern", body = function ( currentSpec ){
				var token = createGUID();
				var uri = createURI( "/LDEV5756" );
				var result =_InternalRequest(
					template: "#uri#/ldev5756.cfm",
					url: {
						token: token,
						log: "ldev5756-pattern"
					}
				);
				var logfile = expandPath("{lucee-config}/logs/ldev5756-pattern.log");
				var logs = fileRead( logFile );
				//systemOutput(logs, true);
				expect( logs ).toInclude( token );
				expect( logs ).toInclude( "[main] INFO" );
			});

			it(title = "check log appenders - classic", body = function ( currentSpec ){
				var token = createGUID();
				var uri = createURI( "/LDEV5756" );
				var result =_InternalRequest(
					template: "#uri#/ldev5756.cfm",
					url: {
						token: token,
						log: "ldev5756-classic"
					}
				);
				var logfile = expandPath("{lucee-config}/logs/ldev5756-classic.log");
				var logs = fileRead( logFile );
				//systemOutput(logs, true);
				expect( logs ).toInclude( token );
				expect( logs ).toInclude( '"INFO","main",' );
			});

			it(title = "ldev-4153 check log appenders - classic", body = function ( currentSpec ){
				var token = createGUID();
				var uri = createURI( "/LDEV5756" );
				var result =_InternalRequest(
					template: "#uri#/ldev5756.cfm",
					url: {
						token: token,
						log: "ldev4153-classic"
					}
				);
				var logfile = expandPath("{lucee-config}/logs/ldev4153-classic.log");
				var logs = fileRead( logFile );
				//systemOutput(logs, true);
				expect( logs ).toInclude( token );
				expect( logs ).toInclude( '"INFO","main",' );
			});

			

		});
	}
	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI & "" & calledName;
	}
}