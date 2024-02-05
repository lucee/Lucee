component extends="org.lucee.cfml.test.LuceeTestCase" labels="date" skip=true {

	function run( testResults , testBox ) {

		describe( "Test case for LDEV-4774", function() {
			it( title="string format 'dd/mm/yyyy' should be treated as a date", body=function( currentSpec ) {
				expect(function() {
					loop from="#dateFormat("01/01/2024","dd/mm/yyyy")#" to="#dateFormat("10/01/2024","dd/mm/yyyy")#" index="i" step="#CreateTimeSpan(1,0,0,0)#" {
						// writeOutput(dateformat(i, "dd/mm/yyyy"));
					}
				}).notToThrow();
			});
			it( title="string format 'dd/mm/yyyy' should be treated as a date", body=function( currentSpec ) {
				expect( isDate("01/01/2024") ).toBeTrue();
			});
		});

	}

}
