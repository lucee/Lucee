component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.uri = createURI("LDEV2012");
		if(!directoryExists(variables.uri)) directoryCreate(variables.uri);
		
		fileWrite("#variables.uri#/test.cfc",'component {
				public any function test() {
					return this;
				}
			}'
		);
	}

	function afterAll(){
		if(directoryExists(variables.uri)) directoryDelete(variables.uri,true);
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}


	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-2012", body=function() {
			it( title='checking ',body=function( currentSpec ) {
				var obj = new LDEV2012.test();
				assertEquals(true, isStruct(obj));
				assertEquals(1, ArrayLen(structKeyArray(obj)));
				assertEquals(1, ArrayLen(obj.keyArray()));
			});
		});
	}
}
