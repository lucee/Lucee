component extends="org.lucee.cfml.test.LuceeTestCase"{
	public function run( testResults , testBox ) {
		describe( "Testcase for LDEV-3197", function() {
			it( title="switch with different case", body=function( currentSpec ){
				
				a = "a";
				switch(a){
					case "a":
						res = "a";
						break;
					case "b":
						res = "b";
						break;
					case "c":
						res = "c";
						break;
				}

				expect(res).toBe("a");
			});
			it( title="switch with same case", body=function( currentSpec ){

				try{
					b = "b";
					switch(b){
						case "a":
							res = "a";
							break;
						case "b":
							res = "b";
							break;
						case "b":
							res = "c";
							break;
					}
				}
				catch(any e){
					res = e.message;
				}

				expect(res).toBe("switch has a duplicate case for value B");
			});
			it( title="switch tag with same case", body=function( currentSpec ){
                ```
				<cftry>
					<cfset b = "b">
					<cfswitch expression="#b#">
						<cfcase value="a">
							<cfset res = "a">
						</cfcase>	
						<cfcase value="b">
							<cfset res = "b">
						</cfcase>	
						<cfcase value="b">
							<cfset res = "c">
						</cfcase>
					</cfswitch>
					<cfcatch>
						<cfset res = cfcatch.message>
					</cfcatch>
                </cftry>
                ```			
				expect(res).toBe("cfswitch has a duplicate case for value B");
			});
		});	
	}
}