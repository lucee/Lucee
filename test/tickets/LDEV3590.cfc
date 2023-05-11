component extends = "org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll(){
		variables.uri = createURI("LDEV3590");
	}

	function run ( testResults , testBox ) {
		describe("This testcase for LDEV-3590",function(){
			
			it(title="test onApplicationStart",body =function( currentSpec ){
				var result=test("#variables.uri#/app/index.cfm");
				expect(trim(result)).toBe("test,test,test");
			});
			it(title="test onSessionStart",body =function( currentSpec ){
				var result=test("#variables.uri#/sess/index.cfm");
				expect(trim(result)).toBe("test,test,test");
			});
			it(title="test onApplicationStart and onSessionStart",body =function( currentSpec ){
				var result=test("#variables.uri#/appsess/index.cfm");
				expect(trim(result)).toBe("test;test,test;test,test;test");
			});

		});
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}


	private function test(required folder) {
		var appName="a"&createUniqueID();
		var names="";
		loop times=3 {
			var name=appName&createUniqueID();
			names=listAppend(names,name);	
			thread name=name appName=appName folder=folder {
				thread.result=_InternalRequest(
					template:folder
					,url:{"appName":appName}
				).filecontent;
			}
		}
		thread action="join" name=names;
		var results="";
		loop struct=cfthread index="local.name" item="local.thread" {
			if(listFind(names, thread.name)==0) continue;
			results=listAppend(results,thread.result);
		}
		return results;
	}
}