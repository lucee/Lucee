component extends="org.lucee.cfml.test.LuceeTestCase" {

    function run(testResults, testBox) {
        describe("Testcase for LDEV-5857", function() {
            it("ACF compatibility: ReplaceNoCase with empty substring should return original string", function(currentSpec) {
                var mainString = "lucee";
                var subString = "";
                var replacement = "test";
                var uri = createURI("ldev5857");
                var result = _InternalRequest(
					template:"#uri#/ldev5857.cfm",
                    url: {
				    mainString: mainString,
                    subString:  subString,
                    replacement:replacement
			    }

				);
                expect(trim(result.filecontent)).toBe("lucee");
            });
        });
    }
    private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI & "" & calledName;
	}
}