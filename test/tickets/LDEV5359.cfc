component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults , testBox ) {

		describe( title='ExpandPath regressions ("*.*")' , body=function(){

			it( title='ExpandPath fails with ("*.*")', skip=isWindows(), body=function() {
				// LDEV-5841: relative paths resolve from the current template directory, not the web root
				var starDotStar = ExpandPath("*.*");
				var dir = getDirectoryFromPath( getCurrentTemplatePath() );
				expect(starDotStar).toBe(dir & "*.*");
			});

			it( title='ExpandPath ("*")', skip=isWindows(), body=function() {
				var starDotStar = ExpandPath("*");
				var dir = getDirectoryFromPath( getCurrentTemplatePath() );
				expect(starDotStar).toBe(dir & "*");
			});

		});
	}

	private function isWindows(){
		return (server.os.name contains "windows");
	}
}