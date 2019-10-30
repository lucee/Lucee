component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.path = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/" &"ImageDrawBeveledRect/";
		if(!directoryExists(path)){
			directorycreate(path);
		}
	}
	function run( testResults, testBox ) {
		describe( "test case for ImageDrawBeveledRect", function() {
			it(title = "Checking with ImageDrawBeveledRect", body = function( currentSpec ){
				img = imageRead("https://dev.lucee.org/uploads/default/original/2X/1/140e7bb0f8069e4f7f073b6d01f55c496bbd42e3.png");
				imageDrawBeveledRect(img,80,40,30,30,"yes","yes");
				cfimage(action = "write", source = img,destination = path&'imgDrawbevelrect.jpg',overwrite="yes");
				expect(fileexists(path&'imgDrawbevelrect.jpg')).tobe("true");
			});

			it(title = "Checking with ImageDrawBeveledRect", body = function( currentSpec ){
				imgDraw = imagenew("",150,150,"rgb","149c82");
				imageDrawBeveledRect(imgDraw,30,30,40,30,"yes");
				cfimage(action = "write", source = imgDraw, destination = path&'imgDrawimg.jpg', overwrite = "yes");
				expect(fileexists(path&'imgDrawimg.jpg')).tobe("true");
			});

			it(title = "Checking with image.drawBeveledRect()", body = function( currentSpec ){
				img = imageRead("https://pbs.twimg.com/profile_images/1037639083135250433/fREb9ZhM_400x400.jpg");
				img.DrawBeveledRect(100,40,50,30,"yes","yes");
				cfimage(action = "write", source = img,destination = path&'objDrawbevelrect.jpg', overwrite="yes");
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