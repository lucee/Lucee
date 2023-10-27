component extends="org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll() {
		variables.dir = getDirectoryFromPath(getCurrentTemplatePath())&"#createGUID()#";
		DirectoryCreate(path=dir, ignoreExists= true);
	}

	function run() {
		describe( "Testcase for LDEV-4494", function() {
			it(title = "Checking FileDelete() function with empty directory", body = function( currentSpec ) {
				expect( function() {
					fileDelete(dir);
				} ).toThrow();
				expect( directoryExists(dir) ).toBeTrue();
			});
		});
	}
}