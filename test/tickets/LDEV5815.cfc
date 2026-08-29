component extends="org.lucee.cfml.test.LuceeTestCase" skip=true {

     function beforeAll(){

		var relPath = "LDEV5815_exists.log";
        var expectedDir = getDirectoryFromPath(getCurrentTemplatePath());
        var fullPath = expectedDir & relPath;
        // Create the file temporarily
        fileWrite(fullPath, "ldev5815 test");

	}
    function afterAll(){

        var relPath = "LDEV5815_exists.log";
        var expectedDir = getDirectoryFromPath(getCurrentTemplatePath());
        var fullPath = expectedDir & relPath;
        // Cleanup
		fileDelete(fullPath);

	}

    function run(testResults, textbox) {
        describe(title = "expandPath() absolute path behavior on Linux", body = function() {

            it(title = "should return the same absolute path for a non-existent file in an existing directory", skip="#isLinux()#", body = function(currentSpec) {
                // Test with a file that does NOT exist, but directory does
                var relPath = "LDEV5815_nonexistent.log";
                var expectedDir = getDirectoryFromPath(getCurrentTemplatePath());
                var expanded = expandPath(expectedDir & relPath); // NOTE: On Linux, this may incorrectly resolve to something like /opt/lucee/tomcat/webapps/ROOT/opt/lucee/tomcat/webapps/ROOT/LDEV5815_nonexistent.log
                expect( expanded.left( len(expectedDir) ) ).toBe( expectedDir );
            });

            it(title = "should return the same absolute path for an existing file in the current template directory", skip="#isLinux()#", body = function(currentSpec) {
                // Test with a file that DOES exist
                var relPath = "LDEV5815_exists.log";
                var expectedDir = getDirectoryFromPath(getCurrentTemplatePath());
                var expanded = expandPath(expectedDir & relPath);

                expect( expanded.left( len(expectedDir) ) ).toBe( expectedDir );
            });

        });
    }
    
    function isLinux() {
        return server.os.name != "Linux";
    }
}