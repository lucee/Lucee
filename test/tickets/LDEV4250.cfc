component extends="org.lucee.cfml.test.LuceeTestCase" labels="syntax" skip="true" {
	function run( testResults , testBox ) {
		describe( "Testcase for LDEV-4250", function() {
			it( title="break without a semicolon inside the script", body=function() {
				var num = 0;
				switch(1) {
					case 1:
						num += 1;
						break
					case 2:
						num += 2;
				}
				expect(num).toBe(1);
			});
		});
	}
}