component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.uri = createURI("LDEV2162");
		fr="<";br=">";
		directoryCreate(variables.uri);
		fileWrite("#variables.uri#/test.cfm", '#fr#cfscript#br##chr(10)#function outerTest(){#chr(10)#  function innserTest(){}#chr(10)#}#chr(10)#outerTest(); #chr(10)#writeoutput(structkeylist(variables));#chr(10)##fr#/cfscript#br#');
	}

	function afterAll(){
		directoryDelete(variables.uri, true);
	}

	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-2162", body=function() {
			it( title='Incorrect scoping of nested functions" ',body=function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm");
				expect(trim(local.result.filecontent)).toBe('outerTest');
				expect(trim(listLen(local.result.filecontent))).toBe(1);
			});

			it( title='get scope of the functions without make a call" ',body=function( currentSpec ) {

				fileWrite("#variables.uri#/test.cfm", '#fr#cfscript#br##chr(10)#function outerTest(){#chr(10)#  function innserTest(){}#chr(10)#}#chr(10)##chr(10)#writeoutput(structkeylist(variables));#chr(10)##fr#/cfscript#br#');
				
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm");
				expect(trim(local.result.filecontent)).toBe('outerTest');
				expect(trim(listLen(local.result.filecontent))).toBe(1);
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
} 