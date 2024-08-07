component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.uri = createURI("LDEV2057");
		if(!directoryExists(variables.uri)) directoryCreate(variables.uri);
		
			fileWrite("#variables.uri#/test.cfc", 'component {
				function test(){
					try
			
					{ return; }
						catch( any e ) { 
					} finally { 
						try { 
						}catch(any e2){
						} 
					}
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
		describe( title="Test suite for LDEV-2057", body=function() {
			it( title='checking component with nested try catch block',body=function( currentSpec ) {
				var obj = new LDEV2057.test();
				assertEquals(true, isStruct(obj));
				var metaData = getComponentMetadata("LDEV2057.test");
				assertEquals(true, isStruct(metaData));
			});
		});
	}
}