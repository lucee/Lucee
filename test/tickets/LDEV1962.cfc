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
		objComponent2.injectMixin('getMessagex', objComponent1.getMessage);
		objComponent2.injectMixin('getMessage2', objComponent1.getMessage2);
		objComponent2.injectMixin('getMessage2x', objComponent1.getMessage2);
		//objComponent2.injectMixin('getMessage', objComponent1.getMessage);

		describe( "test case for LDEV-1962", function() {
			
			
			it(title = "Internal Component call PropertyUDF", body = function( currentSpec ) {
				expect(objComponent2.testFunx()).toBe('This message from component2.cfc!');
			});

			it(title = "External Component call PropertyUDF", body = function( currentSpec ) {
				expect(objComponent1.getMessage()).toBe('This message from component1.cfc!');
				expect(objComponent2.getMessage()).toBe('This message from component2.cfc!');
			});

			it(title = "External Component call UDF", body = function( currentSpec ) {
				expect(objComponent1.getMessage2()).toBe('This message from component1.cfc!');
				expect(objComponent2.getMessage2()).toBe('This message from component2.cfc!');
			});



			it(title = "Internal Component call PropertyUDF", body = function( currentSpec ) {
				expect(objComponent1.testFun()).toBe('This message from component1.cfc!');
				expect(objComponent2.testFun()).toBe('This message from component2.cfc!');
			});

			it(title = "Internal Component call UDF", body = function( currentSpec ) {
				expect(objComponent1.testFun2()).toBe('This message from component1.cfc!');
				expect(objComponent2.testFun2()).toBe('This message from component2.cfc!');
			});

			it(title = "Internal Component call UDF 2", body = function( currentSpec ) {
				expect(objComponent2.testFun2x()).toBe('This message from component2.cfc!');
			});

			it(title = "Accessing Component Path", body = function( currentSpec ) {
				expect(listLast(getMetaData(objComponent2.getMessage ).owner,"\/")).toBe('component1.cfc');
			});

		});
	}
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}

}