component extends = "org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll() {
		variables.path = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/" &"imageClearRect/";
		if(!directoryExists(path)){
			directorycreate(path);
		}
	}

	function run( testResults , testBox ) {
		describe( title = "Test suite for imageClearRect", body = function() {

			it( title = 'Checking with imageClearRect()',body = function( currentSpec ) {
				img = imageread("https://dev.lucee.org/uploads/default/original/2X/1/140e7bb0f8069e4f7f073b6d01f55c496bbd42e3.png");
				ImageClearRect(img,100,100,100,100);
				cfimage(action = "write", source = img, destination = path&".\rect.png", overwrite = "yes");
				assertEquals(fileexists(path&".\rect.png"),"true");
			});

			it( title = 'Checking with image.ClearRect()', body = function( currentSpec ) {
				img1 = imageread("https://dev.lucee.org/uploads/default/original/2X/1/140e7bb0f8069e4f7f073b6d01f55c496bbd42e3.png");
				img1.ClearRect(100,100,100,100);
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