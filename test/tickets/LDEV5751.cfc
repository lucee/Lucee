component extends = "org.lucee.cfml.test.LuceeTestCase" skip=true {
	
	function beforeAll() {
		variables.n = 5;
		variables.urlPath = "http://"&cgi.SERVER_NAME & createURI("LDEV5751/index.cfm");
	}
 
	function run( testResults, textbox ) {
		describe(title="testcase for LDEV-5751", body=function(){
			it(title = "update a scheduled tasks", body = function ( currentSpec ){
				cfschedule(
					action="update",
					url="#urlPath#",
					task="LDEV-5751",
					interval="daily",
					paused="true",
					startdate="#dateformat(now())#",
					starttime="#timeFormat(now()+1)#"
				);
			});
			it(title = "Concurrent updating of Scheduled tasks", body = function ( currentSpec ){
				var arr = [];
				cfschedule( action="list", returnvariable="local.tasks_before");
				arraySet(arr, 1, n, "");
				arrayEach(arr, function(el, idx){
					systemOutput("LDEV-5751 updating scheduled tasks #idx#", true);
					cfschedule(
						action="update",
						url="#urlPath#",
						task="LDEV-5751-#idx#",
						interval="daily",
						paused="true",
						startdate="#dateformat(now())#",
						starttime="#timeFormat(now()+1)#"
					);
				}, true);

				cfschedule( action="list", returnvariable="local.tasks_after");
				systemOutput(QueryColumnData(local.tasks_after, "task"), true);
				expect( local.tasks_before.recordcount+ n ).toBe( local.tasks_after.recordcount );
			});
		});
	}

	function afterAll() {
		dumpList();
		cfschedule( action="delete", task="LDEV-5751");
		dumpList();
		systemOutput("", true);
		for (var t= 1; t <= variables.n; t++) {
			systemOutput(">>>>> #n# ", true);
			try {
				systemOutput("deleting LDEV-5751-#t#", true);
				dumpList();
				dumpCF();
				cfschedule( action="delete", task="LDEV-5751-#t#");
			} catch (e) {
				systemOutput(e.message, true);
			}
			systemOutput("", true);
		}
	}

	private function dumpList(){
		systemOutput("----------------cfschedule( action=list)-----", true);
		cfschedule( action="list", returnvariable="local.tasks_after");
		systemOutput(tasks_after.recordcount & " " & QueryColumnData(local.tasks_after, "task").toJson(), true);
	}

	private function dumpCF(){
		var cfg = expandPath("{lucee-config}") & "/.CFConfig.json";
		var config= fileRead(cfg);
		var _cfg = deserializeJSON(config);
		systemOutput("----------------scheduledTasks from CFConfig.json", true);
		for (var t in _cfg.scheduledTasks)
			systemOutput(" -- #t.name#", true);
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI & "" & calledName;
	}
}