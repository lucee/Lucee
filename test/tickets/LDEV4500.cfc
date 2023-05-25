component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults , testBox ) {
		describe( title="Testcase for LDEV-4500", body=function() {
			it(title="Checking BigDecimal casting in cfswitch", body = function( currentSpec ) {
				var bd=CreateObject("java","java.math.BigDecimal").valueOf(2.00);

				// TODO this doesn't repo the problem yet

				```
				<cfswitch expression="#bd#">
					<cfcase value="2">
						<cfset var result="MATCHED">
					</cfcase>
					<cfdefaultcase>
						<cfset var result="NOT MATCHED">
					</cfdefaultcase>
				</cfswitch>
				```
				expect( result ).tobe( "MATCHED" );
				expect( bd ).tobe( 2 );
			});

		});
	}
}