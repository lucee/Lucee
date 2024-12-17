component extends = "org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll(){
		variables.uri = createURI("LDEV5206");
		disableExecutionLog();
	};

	function afterAll(){
		disableExecutionLog();
	};

	function run( testResults, testBox ){
		describe( "LDEV-5206 execution logs", function(){

			it( "test ConsoleExecutionLog", function(){
				enableExecutionLog("lucee.runtime.engine.ConsoleExecutionLog",{
					"stream-type": "out",
					"unit": "milli",
					"min-time": 100,
					"snippet": true
				});
				systemOutput("", true);
				systemOutput("Logging executionLog to console", true);
				local.result = _InternalRequest(
					template : "#uri#/ldev5206.cfm"
				);
				systemOutput("Logging debugEntries to console", true);
				systemOutput(getDebugEntry(), true);
				systemOutput("finished", true);
			});

		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrentTemplatePath()),"\/")#/";
		return baseURI&""&calledName;
	}

	private function enableExecutionLog(class, args){
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

	private function getDebugEntry(){
		var logs = [];
		admin action="getDebugEntry" type="server" password="#request.SERVERADMINPASSWORD#" returnVariable="logs";
		return logs;
	}
	
}
