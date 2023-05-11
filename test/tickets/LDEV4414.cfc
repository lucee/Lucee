component extends="org.lucee.cfml.test.LuceeTestCase" skip="true" {
	function run( testResults, testBox ) {
		describe("Testcase for LDEV-4414", function() {
			it( title="checking ObjectEquals()", body=function( currentSpec ) {
				expect(ObjectEquals(
					[["PHONE", "EMAIL"], ["PHONE"], ["PHONE", "EMAIL"]],
					[["PHONE", "EMAIL"], ["PHONE"], ["PHONE"]]))
				.toBeFalse(); 
				expect(ObjectEquals( 
					{ id: 1, name: 'Lucee' }, 
					{ id: 1, name: 'Lucee' }))
				.toBeTrue()
				expect(ObjectEquals( 
					{ id: 1, name: 'Lucee' }, 
					{ id: 1, name: 'Lucee', type: "language" }))
				.toBeFalse();
			});
		});
	}
}