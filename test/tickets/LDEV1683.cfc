component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.uri = createURI("LDEV1683");
		if(!directoryExists(variables.uri)){
			directoryCreate(variables.uri);
			fileWrite("#variables.uri#/test1.cfc",'component {
					this.name = "test1";
					public any function test() {
						return this;
					}
				}'
			);
			fileWrite("#variables.uri#/test2.cfc",'component {}');
			fileWrite("#variables.uri#/test3.cfc",'component {
				this.name = "test2";
				public any function init() {
					return this;
				}
			}');
		}
	}

	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1683", function() {
			it( title='Passing component as argument to constructor of another component', body=function( currentSpec ) {
				var test1 = new LDEV1683.test1();
				var result = new LDEV1683.test2(test1);
			});

			it( title='Passing struct as argument to constructor of another component', body=function( currentSpec ) {
				var result = new LDEV1683.test2({a:1});
			});


			it( title='Passing component ( having init function as argument to constructor of another component', body=function( currentSpec ) {
				var test1 = new LDEV1683.test1();
				try {
					var result = new LDEV1683.test3(test1);
				} catch ( any e ){
					var result = e.message;
				}
				expect(isObject(result)).toBe(true);
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}