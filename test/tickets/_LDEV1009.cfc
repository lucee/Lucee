component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.uri = createURI("LDEV1009");
	}
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV1009", function() {
			it( title='The GetHttpRequestData() function with content type application/octet-stream should return binary instead of a string in the content variable', body=function( currentSpec ) {
			 	cfhttp( method="POST", url="#CGI.server_name##variables.uri#/test.cfm" ) {
					cfhttpparam(name="content-type", type="header", value="application/octet-stream");
					cfhttpparam(name="myfile", type="file", file=expandPath("./LDEV1009/lucee.jpg"));
				}
				expect(fileRead(#variables.uri#&"/a.txt")).tobe("boolean true");
			});
		});
	}
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}	
	function afterAll(){
		if(fileExists(uri&'/a.txt')){
			fileDelete(uri&'/a.txt');
		}
	}
}



