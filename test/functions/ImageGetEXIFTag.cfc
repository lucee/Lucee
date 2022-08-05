component extends="org.lucee.cfml.test.LuceeTestCase" labels="image" {
	function run( testResults , testBox ) {
		describe( "test case for ImageGetEXIFTag", function() {
			it(title = "Checking with ImageGetEXIFTag", body = function( currentSpec ) {
				img=imageRead(GetDirectoryFromPath(GetCurrentTemplatePath())&"images/BigBen.jpg");
				assertEquals("1631 1223 1795 1077" , "#ImageGetEXIFTag(img,'Subject Location')#");
				assertEquals("JPEG (old-style)" , "#ImageGetEXIFTag(img,'Thumbnail Compression')#");
			});
		});	
	}
}