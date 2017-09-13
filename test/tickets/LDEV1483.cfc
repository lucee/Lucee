component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1483", function() {
			it( title='checking Immediate Invoke lambda expression, with single param', body=function( currentSpec ) {
				fn = ((x) => {
        			return x * 10;
    			}(1));
    			expect(fn).toBe(10);
			});

			it( title='checking Immediate Invoke lambda expression, with multipleParam param', body=function( currentSpec ) {
				var uri = createURI('LDEV1483');
				var result = _InternalRequest(
					template:"#uri#/test.cfm"
				);
				expect(result.filecontent.trim()).toBe(6);
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}