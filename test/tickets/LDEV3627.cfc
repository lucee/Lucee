component extends = "org.lucee.cfml.test.LuceeTestCase" skip=true{

	function run ( testResults , testBox ) {
		describe("Testcase for LDEV-3627",function(){
			it(title="Calling CFC using root path in thread", body =function( currentSpec ){
				path = reReplace(createURI("LDEV3627.test"), "[/\\\\]", ".", "all");
				res = createObj(path);
				expect(res).toBe("success");
			});
			it(title="Calling CFC using relative path in thread", body =function( currentSpec ){
                		res = createObj("LDEV3627.test");
				expect(res).toBe("success");
			});
		});
	}

	private function createObj(string path) {
		thread name="#path#" action="run" path=path {
			thread.result = createObject("#path#").testFunc();
		}
		var res;
		sleep(20);
		if(cfthread[path].KeyExists("result")) res = cfthread[path].result;
		else if (cfthread[path].KeyExists("error")) res =  cfthread[path].error.Message;
		return res;
	}

	private string function createURI(string calledName){
        var baseURI = "test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
        return baseURI&""&calledName;
    }
}
