component extends = "org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll(){
		variables.uri = createURI("LDEV5206");
		disableExecutionLog();
	};

	function afterAll(){
		disableExecutionLog();
	};

	function run( testResults, testBox ){
		describe( "LDEV-5206 ConsoleExecutionLog", function(){
			it( "test ConsoleExecutionLog", function(){
				enableExecutionLog("lucee.runtime.engine.ConsoleExecutionLog",{
					"stream-type": "out",
					"unit": "milli",
					//"min-time": 100,
					"snippet": true
				});
				systemOutput("", true);
				systemOutput("Logging executionLog to console", true);
				local.result = _InternalRequest(
					template : "#uri#/ldev5206.cfm"
				);
			});
		});

		describe( "LDEV-5206 DebugExecutionLog", function(){
			it( "test DebugExecutionLog - cfm ", function(){
				var logs = getDebugLogs();
				expect( len( logs ) ).toBe( 1 );
				var log = logs[ 1 ];
				expect( log ).toHaveKey( "pageParts" );
				var pageParts = log.pageParts;
				expect( pageParts ).toBeQuery();
				//systemOutput( pageParts.toString(), true );
				pageParts = _toPartsStruct( pageParts );
				//systemOutput( structKeyList(pageParts), true );
				//systemOutput( pageParts.toString(), true );
				
				var key = "ldev5206.cfm:3:3";
				expect( pageParts ).toHaveKey( key );
				expect( pageParts[ key ].snippet ).toBe( "sleep(5)" );

				key = "ldev5206.cfm:5:5";
				expect( pageParts ).toHaveKey( key );
				expect( pageParts[ key ].snippet ).toBe( "cfc = new ldev5206()" );
			});

			xit( "LDEV-5212 test DebugExecutionLog - cfm parts member", function(){
				var logs = getDebugLogs();
				expect( len( logs ) ).toBe( 1 );
				var log = logs[ 1 ];
				expect( log ).toHaveKey( "pageParts" );
				var pageParts = log.pageParts;
				expect( pageParts ).toBeQuery();
				pageParts = _toPartsStruct( pageParts );

				key = "ldev5206.cfm:6:6";
				expect( pageParts ).toHaveKey( key );
				expect( pageParts[ key ].snippet ).toBe( "cfc.doSleep()" ); // LDEV-5207 only returns "cfc"

				key = "ldev5206.cfm:9:9";
				expect( pageParts ).toHaveKey( key );
				expect( pageParts[ key  ].snippet ).toBe( "cfc.doSleep()" ); // LDEV-5207 only returns "cfc"
			});

			it( "test DebugExecutionLog - cfc parts ", function(){
				var logs = getDebugLogs();
				expect( len( logs ) ).toBe( 1 );
				var log = logs[ 1 ];
				expect( log ).toHaveKey( "pageParts" );
				var pageParts = log.pageParts;
				expect( pageParts ).toBeQuery();
				pageParts = _toPartsStruct( pageParts );

				key = "ldev5206_tag.cfc:6:6";
				expect( pageParts ).toHaveKey( key );
				expect( pageParts[ key ].snippet ).toBe( "sleep(5)" );

				key = "ldev5206.cfc:5:5";
				expect( pageParts ).toHaveKey( key );
				expect( pageParts[ key  ].snippet ).toBe( "sleep(5)" ); // LDEV-5207

			});
		});

	}

	private string function createURI( string calledName ){
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & "" & calledName;
	}

	private function enableExecutionLog( string class, struct args ){
		admin action="UpdateExecutionLog" type="server" password="#request.SERVERADMINPASSWORD#"
			class="#arguments.class#" enabled= true
			arguments=arguments.args;
		admin action="updateDebug" type="server" password="#request.SERVERADMINPASSWORD#" debug="true" template="true"; // template needs to be enabled to produce debug logs
	}
	private function disableExecutionLog(class="lucee.runtime.engine.ConsoleExecutionLog"){
		admin action="updateDebug" type="server" password="#request.SERVERADMINPASSWORD#" debug="false";

		admin action="UpdateExecutionLog" type="server" password="#request.SERVERADMINPASSWORD#" arguments={}
			class="#arguments.class#" enabled=false;
		admin action="PurgeDebugPool" type="server" password="#request.SERVERADMINPASSWORD#";
	}

	private function getLoggedDebugData(){
		var logs = [];
		admin action="getLoggedDebugData" type="server" password="#request.SERVERADMINPASSWORD#" returnVariable="logs";
		return logs;
	}

	private function _toPartsStruct( query pageParts ){
		var parts = duplicate( pageParts );
		var dir = getDirectoryFromPath( getCurrentTemplatePath() ) & "/ldev5206/";
		queryAddColumn( parts, "key" );
		var r = 0;
		loop query=parts {
			var r = parts.currentrow;
			querySetCell( parts, "path", mid( parts.path[ r ], len( dir ) ), r ); // less verbose
			querySetCell( parts, "key", parts.path[ r ] & ":" & parts.startLine[ r ] & ":" & parts.endLine[ r ], r );
		}
		var st = QueryToStruct(parts, "key");
		return st;
	}

	function getDebugLogs() cachedwithin="request" {
		disableExecutionLog();
		enableExecutionLog( "lucee.runtime.engine.DebugExecutionLog",{
			"unit": "milli"
			//"min-time": 100
		});
		local.result = _InternalRequest(
			template : "#uri#/ldev5206.cfm",
			url: "pagePoolClear=true" // TODO cfcs aren't recompiled like cfm templates?
		);
		return getLoggedDebugData();
	}
}
