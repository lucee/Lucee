component extends="org.lucee.cfml.test.LuceeTestCase"{
	function afterALL(){
		if(fileExists(variables.testfile)){
			fileDelete(variables.testfile);
		}
	}

	function run(){
		describe( title="Test suite for LDEV-1707", body=function(){
			it(title="Checking onMissingStaticMethod() in component", body=function(){
				var dir="#GetDirectoryFromPath(GetCurrentTemplatePath())#LDEV1707\";
				if(!directoryExists(dir)){
					Directorycreate(dir);
				}
				variables.testfile = dir&"foo.cfc";
				fileWrite(variables.testfile,"
					component {
						function onMissingStaticMethod(target){
							return test(target);
						}

						function test(trgt) {
							return 'Static ' &  arguments.trgt & ' method is missing in component message from onMissingStaticMethod()';
						}
					}"
				);
				try {
					var result = LDEV1707.foo :: bar();
				} catch ( any e ) {
					var result = e.message;
				}
				assertEquals("Static bar method is missing in component message from onMissingStaticMethod()", result);
			});
		});
	}
}