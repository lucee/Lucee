component extends = "org.lucee.cfml.test.LuceeTestCase" labels="image" {

	function beforeAll(){
		variables.path = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/" &"ImageDrawBeveledRect/";
		if(!directoryExists(path)){
			directorycreate(path);
		}
	}

	function run( testResults, testBox ) {
		describe( "test case for ImageDrawBeveledRect", function() {

			it(title = "Checking with ImageDrawBeveledRect", body = function( currentSpec ){
				var imgDraw = imagenew("",150,150,"rgb","149c82");
				imageDrawBeveledRect(imgDraw,30,30,40,30,"yes");
				cfimage(action = "write", source = imgDraw, destination = path&'imgDrawimg.jpg', overwrite = "yes");
				expect(fileexists(path&'imgDrawimg.jpg')).tobe("true");
			});

			it(title = "Checking with image.drawBeveledRect()", body = function( currentSpec ){
				var img = imageNew("", 400, 400);
				img.DrawBeveledRect(100,40,50,30,"yes","yes");
				cfimage(action = "write", source = img, destination = path&'objDrawbevelrect.jpg', overwrite = "yes");
				expect(fileexists(path&'objDrawbevelrect.jpg')).tobe("true");
			});

		});
	}

	function afterAll(){
		if(directoryExists(path)){
			directoryDelete(path,true);
		}
	}
}