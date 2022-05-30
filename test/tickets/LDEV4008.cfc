component extends="org.lucee.cfml.test.LuceeTestCase" labels="output" skip=true{

	function run( testResults, testBox ) {
		describe("Testcase for LDEV-4008", function() {

			it( title="Checking cfoutput encodeFor attribute with tag attribute in cfapplication", body=function( currentSpec ) {
				expect(testTagAttr()).toBe("brad &lt;br&gt; wood");
			});

			it( title="Checking cfoutput encodeFor attribute with tag attribute in Application.cfc", body=function( currentSpec ) {
				var res = _InternalRequest(
					template = "#createURI("LDEV4008")#/LDEV4008.cfm"
				);
				expect(res.fileContent.trim()).toBe("brad &lt;br&gt; wood");
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}


	```
		<cffunction name="testTagAttr" access="private">
			<cfapplication tag=#{output:{encodefor:"html"}}#>

			<cfsavecontent variable="result">
				<cfset var name="brad <br> wood">
				<cfoutput>#name#</cfoutput>
			</cfsavecontent>
			
			<cfreturn result.trim()>
		</cffunction>
	```
}