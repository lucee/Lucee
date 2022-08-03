component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		
	}
	function afterAll(){
		
	}
	function run( testResults , testBox ) {
		describe( "test suite for LDEV-1892", function() {
			it(title = "check if arrayAppend appends to a struct", body = function( currentSpec ) {
				ct={};
				sct['1']="first";
				arrayAppend(sct,"susi");
				arrayPrepend(sct,"urs");
				expect(arrayToList(sct)).toBe('urs,first,susi');
			});
		});
	}
}