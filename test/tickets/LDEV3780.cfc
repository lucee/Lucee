component extends="org.lucee.cfml.test.LuceeTestCase" skip=true{
	function beforeAll() {
		variables.path = getDirectoryFromPath(getCurrentTemplatePath())&"LDEV3780\";
		variables.file = path&"test.cfc";
		if(!directoryExists(path)) directoryCreate(path);
			fileWrite( file=file, data="component {
				static {
					sleep(500);
					TYPE_ENCRYPTION_FILE='result';
				}
			}");
	}

	function run( testResults, testBox ) {
		describe("Testcase for LDEV-3780", function() {
			it( title="", body=function( currentSpec ){
				assertEquals("result,result,", test());
			});
		});
	}

	function afterAll() {
		if(fileExists(file)) fileDelete(file);
	}

	private function test() {
		var appName="test"&createUniqueID();
		var names="";
		loop times=2 {
			var name=appName&createUniqueID();
			names=listAppend(names,name);	
			thread name=name {
				thread.test = LDEV3780.test::TYPE_ENCRYPTION_FILE;
			}
		}
		thread action="join" name=names;
		var results="";
		loop struct=cfthread index="local.name" item="local.threadsct" {
			if(listFind(names, threadsct.name)==0) continue;
			if(threadsct.keyExists("test")) results &= threadsct.test;
			else if(threadsct.keyExists("error")) results &= threadsct.error["message"];
			results &= ",";
		}
		return results;
	}
}