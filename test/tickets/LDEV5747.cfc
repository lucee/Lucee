component extends = "org.lucee.cfml.test.LuceeTestCase" {
	
	function beforeAll() {
		variables.n = 100;
		variables.urlPath = "http://"&cgi.SERVER_NAME & createURI("LDEV5747/index.cfm");
	}

	function run( testResults, textbox ) {
		describe(title="testcase for LDEV-5747", body=function(){
			it(title = "update a scheduled tasks", body = function ( currentSpec ){
				cfschedule(
					action="update",
					url="#urlPath#",
					task="LDEV-5747",
					interval="daily",
					paused="true",
					startdate="#dateformat(now())#",
					starttime="#timeFormat(now()+1)#"
				);
			});
			it(title = "Concurrent updating of Scheduled tasks", body = function ( currentSpec ){

				cfschedule( action="list", returnvariable="local.tasks_before");

				var arr = [];
				arraySet(arr, 1, n, "");
				arrayEach(arr, function(el, idx){
					systemOutput("LDEV-5747 updating scheduled tasks #idx#", true);
					cfschedule(
						action="update",
						url="#urlPath#",
						task="LDEV-5747-#idx#",
						interval="daily",
						paused="true",
						startdate="#dateformat(now())#",
						starttime="#timeFormat(now()+1)#"
					);
				}, true);

				cfschedule( action="list", returnvariable="local.tasks_after");
				expect( local.tasks_before.recordcount+ n ).toBe( local.tasks_after.recordcount );
			});
		});
	}

	function afterAll() {
		cfschedule( action="delete", task="LDEV-5747");
		for (var t= 1; t <= variables.n; t++) {
			cfschedule( action="delete", task="LDEV-5747-#t#");
		}
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI & "" & calledName;
	}
}