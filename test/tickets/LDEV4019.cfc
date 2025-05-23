component extends="org.lucee.cfml.test.LuceeTestCase" skip="true" {

	public function beforeAll() {
		variables.ts=getTimeZone();
	}

	public function afterAll() {
		setTimezone(variables.ts);
	}

	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-4019", body=function() {
			it(title = "Checking getTimezone() as a struct", body = function( currentSpec ) {

				SetTimeZone("Asia/Calcutta");

				expect( toString(getTimeZone()) ).toBeString();
				expect( toString(getTimeZone()) ).toBe("Asia/Calcutta");
				//India currently observes India Standard Time (IST) all year. 
				expect( getTimeZone().shortNameDST ).toBe("IST");
				expect( function() {
					loop struct=getTimeZone() index="local.key" item="local.val" {
						// writeDump(label:k,var:v);
					}
				} ).notToThrow();
			});
		});
	}
}
