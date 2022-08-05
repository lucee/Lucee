component extends = "org.lucee.cfml.test.LuceeTestCase" labels="image" {

	function beforeAll() {
		variables.path = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/" &"imageClearRect/";
		if(!directoryExists(path)){
			directorycreate(path);
		}
	}

	function run( testResults , testBox ) {
		describe( title = "Test suite for imageClearRect", body = function() {

			it( title = 'Checking with imageClearRect()',body = function( currentSpec ) {
				var img = imageNew("", 400, 400);
				ImageClearRect(img,100,100,100,100);
				cfimage(action = "write", source = img, destination = path&".\rect.png", overwrite = "yes");
				assertEquals(fileexists(path&".\rect.png"),"true");
			});

			it( title = 'Checking with image.ClearRect()', body = function( currentSpec ) {
				var img = imageNew("", 400, 400);
				img.ClearRect(100,100,100,100);
				cfimage(action = "write", source = img, destination = path&".\rect1.png", overwrite = "yes");
				assertEquals(fileexists(path&".\rect1.png"),"true");
			});

		});
	}

	function afterAll() {
		if(directoryexists(path)) {
			directorydelete(path,true);
		}
	}
}