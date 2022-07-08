component extends="org.lucee.cfml.test.LuceeTestCase" labels="orm" skip=true{
	function beforeAll() {
		variables.uri = createURI("LDEV4067");
	}

	function run( testResults, testBox ) {
		describe("Testcase for LDEV-4067", function() {
			it( title="checking this scope calling from closure without ORM entity",  body=function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#\LDEV4067.cfm",
					forms = {scene:1}
				);
				expect(trim(result.filecontent)).toBe("Michael");
			});
			it( title="checking variables scope calling from closure without ORM entity",  body=function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#\LDEV4067.cfm",
					forms = {scene:2}
				);
				expect(trim(result.filecontent)).toBe("Michael");
			});
			it( title="checking this scope calling from lambda without ORM entity",  body=function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#\LDEV4067.cfm",
					forms = {scene:3}
				);
				expect(trim(result.filecontent)).toBe("Michael");
			});
			it( title="checking variables scope calling from lambda without ORM entity",  body=function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#\LDEV4067.cfm",
					forms = {scene:4}
				);
				expect(trim(result.filecontent)).toBe("Michael");
			});
			it( title="checking this scope calling from closure with ORM entity",  body=function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#\LDEV4067.cfm",
					forms = {scene:5}
				);
				expect(trim(result.filecontent)).toBe("Michael");
			});
			it( title="checking variables scope calling from closure with ORM entity",  body=function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#\LDEV4067.cfm",
					forms = {scene:6}
				);
				expect(trim(result.filecontent)).toBe("Michael");
			});
			it( title="checking this scope calling from lambda with ORM entity",  body=function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#\LDEV4067.cfm",
					forms = {scene:7}
				);
				expect(trim(result.filecontent)).toBe("Michael");
			});
			it( title="checking variables scope calling from lambda with ORM entity",  body=function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#\LDEV4067.cfm",
					forms = {scene:8}
				);
				expect(trim(result.filecontent)).toBe("Michael");
			});
		});
	}
	
	private string function createURI(string calledName) {
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}

	function afterAll() {
		var list = DirectoryList(
			path=getDirectoryFromPath(getCurrentTemplatePath()), 
			recurse=true, filter="*.db");

		var javaIoFile=createObject("java","java.io.File");
		loop array=list item="local.path"  {
			fileDeleteOnExit(javaIoFile,path);
		}
	}

	private function fileDeleteOnExit(required javaIoFile, required string path) {
		var file=javaIoFile.init(arguments.path);
		if(!file.isFile())file=javaIoFile.init(expandPath(arguments.path));
		if(file.isFile()) file.deleteOnExit();
	}
	
}
