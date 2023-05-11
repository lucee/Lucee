component extends = "org.lucee.cfml.test.LuceeTestCase" labels="image" {

	function beforeAll(){
		variables.path = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/" &"ImageDrawLine/";
		if(!directoryExists(path)){
			directorycreate(path);
		}
	}

	function run( testResults, testBox ) {
		describe( "test case for ImageDrawLine", function() {

			it(title = "Checking with ImageDrawLine", body = function( currentSpec ){
				newImg = imageNew("",150,150,"rgb","6a5acd");
				imageDrawline(newImg,40,50,120,120);
				cfimage(action = "write", source = newImg, destination = path&'Drawline.jpg', overwrite="yes");
				expect(fileExists(path&'Drawline.jpg')).tobe("true");
			});

			it(title = "Checking with image.drawLine()", body = function( currentSpec ){
				newImg = imageNew("",150,150,"rgb","149c82");
				newImg.Drawline(30,50,130,130);
				cfimage(action = "write", source = newImg, destination = path&'objDrawline.jpg', overwrite="yes");
				expect(fileExists(path&'Drawline.jpg')).tobe("true");
			});

		});
	}

	function afterAll(){
		if(directoryExists(path)){
			directoryDelete(path,true);
		}
	}
}