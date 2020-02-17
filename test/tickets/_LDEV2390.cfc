component extends="org.lucee.cfml.test.LuceeTestCase"{

	function run( testResults , testBox ) {
		describe( "test case for LDEV-2390", function() {
			it(title = "checking if functions get lost", body = function( currentSpec ) {
				
				local.q=query("foo":[""]);

				var objValidate = createObject('component','LDEV2390.Test');
				loop query="q" {
					objValidate.foo();
					objValidate.test();
				}



				//expect(local.result.filecontent.trim()).toBe("true");
			});
		});
	}

}

