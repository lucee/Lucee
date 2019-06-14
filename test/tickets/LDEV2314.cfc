component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll() {
		variables.path = "#GetDirectoryFromPath(getCurrentTemplatePath())#LDEV2314";
		if(!directoryexists(path)) {
			directorycreate(path);
		}
		filewrite(path&"\test.cfc","component {#chr(10)##chr(9)#public function someFunction() {}#chr(10)#}");
	}

	function run( testResults, testBox ) {
		describe( title="Test suite for LDEV2314", body=function() {
			it( title='If structKeyExists() works on a component method, so should .keyExists() using function',body=function( currentSpec ) {
				var obj = createobject("component","LDEV2314.test");
				assertEquals("true",structkeyexists(obj,"someFunction"));
			});

			it( title="If structKeyExists() works on a component method, so should .keyExists() using object",body=function( currentSpec ) {
				var obj = createobject("component","LDEV2314.test");
				assertEquals("true",obj.keyexists("someFunction"));
			});
		});
	}

	function afterAll() {
		if(directoryexists(path)) {
			directorydelete(path,true);
		}
	}
}