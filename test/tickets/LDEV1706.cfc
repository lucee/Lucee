component extends="org.lucee.cfml.test.LuceeTestCase"{
	function afterALL(){
		if(fileExists(variables.testfile)){
			fileDelete(variables.testfile);
		}
	}

	function run(){
		describe( title="Test suite for LDEV-1706", body=function(){
			it(title="Checking invoke function with arguments scope ", body=function(){
				var dir="#GetDirectoryFromPath(GetCurrentTemplatePath())#LDEV1706\";
				if(!directoryExists(dir)){
					Directorycreate(dir);
				}
				variables.testfile = dir&"foo.cfc";
				fileWrite(variables.testfile,"
					component {
						static function bar() {
							return 'Lucee';
						}
					}");
				try {
					var result = invokeStatic("foo", "bar");
				} catch ( any e ) {
					var result = e.message;
				}
				assertEquals("lucee", LDEV1706.foo::bar());
				assertEquals("lucee", result);
			});
		});
	}
}