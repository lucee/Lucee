component extends="org.lucee.cfml.test.LuceeTestCase" labels="image" {
	function run( testResults , testBox ) {
		describe( "test suite for LDEV-1906()", function() {
			it(title = "checking the duplicate() with image", body = function( currentSpec ) {
				Local.ThisImage = imageNew('', 600,  600, 'rgb', 'ffeaa7');
				//writeDump(Local.ThisImage);//uncomment this line, It'll working fine
				Local.duplicatedImage = duplicate(Local.ThisImage);
				expect(local.duplicatedImage).toBeTypeof('struct');
			});
		});
	}
}