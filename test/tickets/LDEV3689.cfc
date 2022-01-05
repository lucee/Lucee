component extends = "org.lucee.cfml.test.LuceeTestCase" skip = "true"{

	function beforeAll(){
		variables.uri=createURI("LDEV3689");
		variables.file = GetDirectoryFromPath(getcurrentTemplatepath())&'LDEV3689/result.txt';
		if(fileExists(file)) fileDelete(file);
	}
	
	function run( testResults, textbox ) {
		application action="update" customtagPaths=["#getDirectoryFromPath(getCurrentTemplatePath())#LDEV3689"];
		describe("testcase for LDEV-3689 ", function() {

			afterEach( function( currentSpec ){
				if(fileExists(file)) fileDelete(file);
			});

			it(title="Checking custom tag inside thread", body=function( currentSpec ) {
				expect(trim(test(1))).toBe("$55.00");
			});
			it(title="Checking include page inside thread", body=function( currentSpec ) {
				expect(trim(test(2))).toBe("Page included");
			});
			it(title="Checking custom tag inside long running thread", body=function( currentSpec ) { 
				_InternalRequest(
					template : "#uri#/LDEV3689.cfm",
					forms:{Scene=1}
				);
				sleep(100);
				expect(trim(fileread(file))).tobe("$55.00");
			});
			it(title="Checking include page inside long running thread", body=function( currentSpec ) {
				_InternalRequest(
					template : "#uri#/LDEV3689.cfm",
					forms:{Scene=2}
				);
				sleep(100);
				expect(trim(fileread(file))).tobe("Page included");
			});
		});
	}
	
	private function test(required string scene) {
		var name="thread_#createUniqueID()#";
		thread action = "run" name="#name#" scene="#arguments.scene#"{
			try {
				if (scene == 1) {
					var y = 55;
					savecontent variable="x"{
						cf_dollar(value=y);
					}
					thread.result = x;
				}
				else if (scene == 2) {
					include "./LDEV3689/test.cfm";
				}
			}
			catch(any e) {
				thread.result = e.message;
				systemoutput(e.message);
			}
		}
		thread action = "join" name="#name#";
		return cfthread["#name#"].result;
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI & "" & calledName;
	}

}