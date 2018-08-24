component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.uri = createURI("LDEV1962");
	}
	function injectMixin( name, UDF ) {
		variables[ arguments.name ] = arguments.UDF;
		this[ arguments.name ] = arguments.UDF;
	}
	function run( testResults , testBox ) {
		objComponent1 = new LDEV1962.component1();
		objComponent2 = new LDEV1962.component2();
		objComponent2.injectMixin = injectMixin;
		objComponent2.injectMixin('getMessage', objComponent1.getMessage);
		objComponent2.injectMixin( 'getMessage', objComponent1.getMessage);

		describe( "test case for LDEV-1962", function() {
			it(title = "External Component call", body = function( currentSpec ) {
				local.result1=objComponent1.getMessage();
				local.result2=objComponent2.getMessage();
				expect(local.result1.trim()).toBe('This message from component1.cfc!');
				expect(local.result2.trim()).toBe('This message from component2.cfc!');
			});

			it(title = "Internal Component call", body = function( currentSpec ) {
				local.result1=objComponent1.testFun();
				local.result2=objComponent2.testFun();
				expect(local.result1.trim()).toBe('This message from component1.cfc!');
				expect(local.result2.trim()).toBe('This message from component2.cfc!');
			});

			it(title = "Accessing Component Path", body = function( currentSpec ) {
				expect(listLast(getMetaData(objComponent2.getMessage ).owner,"\")).toBe('component1.cfc');
			});
		});
	}
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}

}