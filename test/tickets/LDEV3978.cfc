component extends="org.lucee.cfml.test.LuceeTestCase" labels="logs" skip=false {

	function beforeAll() {
		variables.filePath = "#expandPath("{lucee-config}")#/logs";
		variables.uuid = createUUID();
	}

	function run( testResults, testBox ) {
		describe("Testcase for LDEV-3978", function( currentSpec ) {
			it(title="Writing logs in files using cflog", body=function( currentSpec )  {
				_InternalRequest(
					template : "#createURI("LDEV3978")#\LDEV3978.cfm",
					urls : {"uuid": uuid}
				);
				var testLogFile_1 = fileRead("#filePath#/LDEV3978_1.log")
				var testLogFile_2 = fileRead("#filePath#/LDEV3978_2.log")
				var testLogFile_3 = fileRead("#filePath#/LDEV3978_3.log")

				expect(findNoCase("testone_first_#uuid#", testLogFile_1)).toBeGT(0);
				expect(findNoCase("testone_second_#uuid#", testLogFile_1)).toBeGT(0);
				expect(findNoCase("testtwo_first_#uuid#", testLogFile_2)).toBeGT(0);
				expect(findNoCase("testtwo_second_#uuid#", testLogFile_2)).toBeGT(0);
				expect(findNoCase("testthree_first_#uuid#", testLogFile_3)).toBeGT(0);
				expect(findNoCase("testthree_second_#uuid#", testLogFile_3)).toBeGT(0);
			});
			it(title="cflog without file attribute", body=function( currentSpec )  {
				var appLog = fileRead("#filePath#/application.log");
				
				expect(findNoCase("test_application_without_file_first_#uuid#", appLog)).toBeGT(0, "cflog without file attribute failed");
				expect(findNoCase("test_application_without_file_second_#uuid#", appLog)).toBeGT(0, "cflog without file attribute failed");
			});
		});
	}

	function afterAll() {
		var javaIoFile = createObject("java","java.io.File");
		var list = DirectoryList("#filePath#", true, true,function(path) {
			return findNoCase("LDEV3978",path)
		});
		loop array = list item="local.path" {
			fileDeleteOnExit(javaIoFile,path);
		}
	}

	private function fileDeleteOnExit(required javaIoFile, required string path) {
		var file = javaIoFile.init(arguments.path);
		if (!file.isFile()) file = javaIoFile.init(expandPath(arguments.path));
		if (file.isFile()) file.deleteOnExit();
	}

	private string function createURI(string calledName) {
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}