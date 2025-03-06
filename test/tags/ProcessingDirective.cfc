component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run() {
		describe("Tests for cfprocessingdirective", function() {

			it("should correctly set page encoding", function() {
				var result = _InternalRequest(template="#createURI('processingDirective')#/cfprocessingdirective_encoding.cfm");
				debug(result);
				expect(result.fileContent.trim()).toBe("Encoding set to UTF-8");
			});

			// ACF 2023 doesn't seem to strip whitespace at all?
			it("should suppress whitespace when specified", function() {
				var result = _InternalRequest(template="#createURI('processingDirective')#/cfprocessingdirective_whitespace.cfm");
				var text = replace(
						replace(
							replace(result.fileContent,chr(10),"LF","all"),
						chr(13),"CR","all"),
					" ",".","all"
				);
				// test file is LF
				expect( text ).toBe( "LFLFLine1LFLine2LFLine3LF" );
			});
		});

		// disabled hangs https://luceeserver.atlassian.net/browse/LDEV-5378

		xdescribe("Tests for cfprocessingdirective - preservecase", function() {

			it("should preserve case when enabled", function() {
				var result = _InternalRequest(
					template="#createURI('processingDirective')#/cfprocessingdirective_preservecase.cfm",
					url: {
						preserve: true
					}
				);
				expect(result.fileContent).toBeWithCase('{"camelCase":true}');
			});

			it("shouldn't preserve case when disabled", function() {
				var result = _InternalRequest(
					template="#createURI('processingDirective')#/cfprocessingdirective_preservecase.cfm",
					url: {
						preserve: false
					}
				);
				expect(result.fileContent).toBeWithCase('{"CAMELCASE":false}');
			});

			it("shouldn't preserve case by default, no processingdirective", function() {
				var result = _InternalRequest(
					template="#createURI('processingDirective')#/cfprocessingdirective_preservecase.cfm",
					url: {
						default: true
					}
				);
				expect(result.fileContent).toBeWithCase('{"CAMELCASE":"default"}');
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrentTemplatePath()),"\/")#/";
		return baseURI&""&calledName;
	}

	private function isWindows(){
		return (server.os.name contains "windows");
	}

}
