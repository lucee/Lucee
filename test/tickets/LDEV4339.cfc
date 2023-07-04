component extends="org.lucee.cfml.test.LuceeTestCase" labels="orm" skip="true" {
	function beforeAll(){
		variables.uri = createURI("LDEV4339");
	}

	function run( testResults, testBox ) {
		describe("Testcase for LDEV-3907", function() {

			it( title="check ormSessions are closed after a thread ends (autoManageSession & flushAtRequestEnd )", body=function( currentSpec ) {
				try {
					local.result = _InternalRequest(
						template : "#uri#/ormSessionCheck.cfm",
						url: {
							autoManageSession: true,
							flushAtRequestEnd: true
						}
					).filecontent;  // should be 0
				}
				catch(any e) {
					result = e.stacktrace;
				}
				expect(trim(result)).toBe("0:6", "open vs closed");
			});

			it( title="check ormSessions are closed after a thread ends", body=function( currentSpec ) {
				try {
					local.result = _InternalRequest(
						template : "#uri#/ormSessionCheck.cfm",
						url: {
							autoManageSession: false,
							flushAtRequestEnd: false
						}
					).filecontent;  // should be 0
				}
				catch(any e) {
					result = e.stacktrace;
				}
				expect(trim(result)).toBe("0:6", "open vs closed");
			});

		});
	}

	private string function createURI(string calledName) {
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}
