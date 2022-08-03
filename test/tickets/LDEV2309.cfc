component extends="org.lucee.cfml.test.LuceeTestCase" labels="schedule" {
	function run( testResults , testBox ) {
		describe( "Testcase for LDEV-2309", function() {
			it( title="checking schedule task url http with standard port", body=function( currentSpec ) {
				var testURL = "http://testLucee#createUniqueID()#.com";
				var taskName = "schedule_url_test1";

				schedule task = "#taskName#"
					action="update"
					operation = "HTTPRequest"
					url = "#testURL#:80/test.cfm"
					startDate = now()
					startTime = "0:00"
					interval = "daily";

				var resultURL = getURL(taskName);

				expect("#testURL#/test.cfm").toBe(resultURL);
			});

			it( title="checking schedule task url http with non-standard port", body=function( currentSpec ) {
				var testURL = "http://testLucee#createUniqueID()#.com";
				var taskName = "schedule_url_test2";

				schedule task = "#taskName#"
					action="update"
					operation = "HTTPRequest"
					url = "#testURL#:8888/test.cfm"
					startDate = now()
					startTime = "0:00"
					interval = "daily";

				var resultURL = getURL(taskName);

				expect("#testURL#:8888/test.cfm").toBe(resultURL);
			});

			it( title="checking schedule task url https with standard port", body=function( currentSpec ) {
				var testURL = "https://testLucee#createUniqueID()#.com";
				var taskName = "schedule_url_test3";

				schedule task = "#taskName#"
					action="update"
					operation = "HTTPRequest"
					url = "#testURL#:443/test.cfm"
					startDate = now()
					startTime = "0:00"
					interval = "daily";

				var resultURL = getURL(taskName);

				expect("#testURL#/test.cfm").toBe(resultURL);
			});

			it( title="checking schedule task url https with non-standard port", body=function( currentSpec ) {
				var testURL = "https://testLucee#createUniqueID()#.com";
				var taskName = "schedule_url_test4";

				schedule task = "#taskName#"
					action="update"
					operation = "HTTPRequest"
					url = "#testURL#:8080/test.cfm"
					startDate = now()
					startTime = "0:00"
					interval = "daily";

				var resultURL = getURL(taskName);

				expect("#testURL#:8080/test.cfm").toBe(resultURL);
			});
		});
	}

	private function getURL(required string taskName) {
		var str = "";
		schedule action="list" result="local.tasks";

		queryEach(tasks, 
			(r) => {
				if (arguments.r.task == taskName) {
					str = arguments.r.url;
				}
			}
		);

		schedule action="delete" task=arguments.taskName; // delete the task
		return str;
	}
}