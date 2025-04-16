component extends="org.lucee.cfml.test.LuceeTestCase" {

	variables.bd = createObject("java", "java.math.BigDecimal").init("2.00")

	function run( testResults , testBox ) {
		describe( title="Testcase for LDEV-4500", body=function() {

			it(title="Checking BigDecimal 2.00 casting in switch 2", body = function( currentSpec ) {
				var result = "";
				switch ( bd ){
					case 2:
						result="MATCHED";
						break;
					default:
						result="NOT MATCHED";
				}

				expect( result ).tobe( "MATCHED" );
				expect( bd ).tobe( 2 );
			});

			it(title="Checking BigDecimal 2.00 casting in switch '2'", body = function( currentSpec ) {
				var result = "";
				switch ( bd ){
					case "2":
						result="MATCHED";
						break;
					default:
						result="NOT MATCHED";
				}

				expect( result ).tobe( "MATCHED" );
				expect( bd ).tobe( 2 );
			});

			it(title="Checking BigDecimal 2.00 casting in cfswitch 2 ", skip=false, body = function( currentSpec ) {
				```
				<cfswitch expression="#bd#">
					<cfcase value=2>
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
			
			it(title="Checking BigDecimal 2.00 casting in cfswitch '2' ", skip=false, body = function( currentSpec ) {
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