component extends="org.lucee.cfml.test.LuceeTestCase"{

	function beforeAll(){
		variables.path = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/" &"ImageDrawOval/";
		if(!directoryExists(path)){
			directorycreate(path);
		}
	}

	function run( testResults, testBox ){
		describe( "test case for ImageDrawOval", function() {

			it(title = "Checking with ImageDrawOval", body = function( currentSpec ){
				imgOne = imageNew("",200,200,"rgb","red");
				ImageDrawOval(imgOne, 50,50,70,30,"no");
				cfimage(action = "write", source = imgOne, destination = path&'imgDrawovalOne.jpg', overwrite = "yes");
				expect(fileexists(path&'imgDrawovalOne.jpg')).tobe("true");
			});

			it(title = "Checking with image.drawOval()", body = function( currentSpec ){
				imgTwo = imageNew("",200,200,"rgb","green");
				imgTwo.drawOval(80,100,70,20,"yes");
				cfimage(action = "write", source = imgTwo, destination = path&'drawovalObjone.jpg', overwrite = "yes");
				expect(fileexists(path&'drawovalObjone.jpg')).tobe("true");
			});

		});
	}
	function afterAll(){
		if(directoryExists(path)){
			directoryDelete(path,true);
		}
	}
}