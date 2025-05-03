component extends="org.lucee.cfml.test.LuceeTestCase" labels="date" {

	function run( testResults , testBox ) {

		describe( "Test case for LDEV-4774", function() {
			beforeEach( function(){
				variables.startingTZ=getTimeZone();
				setTimeZone("UTC");
            });
			afterEach( function(){
                setTimeZone(variables.startingTZ?:"UTC");
            });
			it( title="string format 'dd/mm/yyyy' should be treated as a date(set the date to the variable)", body=function( currentSpec ) {
				expect(function() {
					var fromDate = "01/01/2024";
					var toDate = "01/10/2024";
					loop from="#fromDate#" to="#toDate#" index="local.i" step="#CreateTimeSpan(1,0,0,0)#" {
						// writeOutput(dateformat(i, "dd/mm/yyyy"));
					}
				}).notToThrow();
			});
			it( title="string format 'dd/mm/yyyy' should be treated as a date(Set the date directly in the loop)", body=function( currentSpec ) {
				expect(function() {
					loop from="01/01/2024" to="01/10/2024" index="local.i" step="#CreateTimeSpan(1,0,0,0)#" {
						// writeOutput(dateformat(i, "dd/mm/yyyy"));
					}
				}).notToThrow();
			});
			it( title="string format 'dd/mm/yyyy' should be treated as a date", body=function( currentSpec ) {
				expect(function() {
					loop from="#parseDateTime("01/01/2024","dd/mm/yyyy")#" to="#parseDateTime("10/01/2024","dd/mm/yyyy")#" index="local.i" step="#CreateTimeSpan(1,0,0,0)#" {
						// writeOutput(dateformat(i, "dd/mm/yyyy"));
					}
				}).notToThrow();
			});
			it( title="string format 'dd/mm/yyyy' should be treated as a date", body=function( currentSpec ) {
				expect( isDate("01/01/2024") ).toBeTrue();
			});

			it( title="string '5 6' should NOT be treated as a date", body=function( currentSpec ) {
				var failed=false;
				try {
					dateAdd("d", 0, "5 6");
				}
				catch(e) {
					failed=true;
				}
				
				expect( failed ).toBeTrue();
			});
		});

	}

}
