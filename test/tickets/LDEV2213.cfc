component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.uri = createuri("LDEV2213");
		fileWrite(uri&'/Application.cfc',"component {#chr(10)##chr(9)#this.localmode = 'modern';#chr(10)#}");
	}
	function run( testResults , testBox ){
		describe("test case for LDEV-2213",function(){
			afterEach( function( currentSpec ){
				if(fileExists(#uri#&'/Application.cfc')){
					fileDelete(#uri#&'/Application.cfc');
				}
			});

			it("Cannot set calling scope vars from closures when localMode='modern'",function( currentSpec ){
				local.result = _InternalRequest(
					template:uri&'/test.cfm'
				);
				expect(local.result.filecontent).tobe("Before: []I Love Lucee!After: [testcase]");
			});
			it("Cannot set calling scope vars from closures without localMode",function( currentSpec ){
				local.result = _InternalRequest(
					template:uri&'/test.cfm'
				);
				expect(local.result.filecontent).tobe("Before: []I Love Lucee!After: [testcase]");
			});
		});
	}	
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}