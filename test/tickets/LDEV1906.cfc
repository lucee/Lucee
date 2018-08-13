component extends="org.lucee.cfml.test.LuceeTestCase"{

	function run( testResults , testBox ) {
		describe( "test suite for LDEV-1906", function() {
			it(title = "test duplicate image", body = function( currentSpec ) {
				
				Local.ThisImage = imageNew('', 600,  600, 'argb', '000000');
				duplicate(Local.ThisImage);

			});
		});
	}
}