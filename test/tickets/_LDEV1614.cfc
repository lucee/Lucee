component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-1614", body=function() {
			it( title='Checking createobject() with try catch block inside the cfsaveContent',body=function( currentSpec ) {
				cfsaveContent(variable = 'test'){
					try{
						var result = "";
						testComp = CreateObject('component','LDEV1614.testComp');
					} catch (any e ){
						result = e.message;
					}
					writeOutput(result);
				}
				expect(test).toBe("invalid component definition, can't find component [foo.bar]")
			});
		});
	}
}