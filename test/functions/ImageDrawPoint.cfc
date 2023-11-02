component extends="org.lucee.cfml.test.LuceeTestCase"{

	function beforeAll(){
		variables.path = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/" &"ImageDrawPoint/";
		writeDump(path);
		if(!directoryExists(path)){
			directorycreate(path);
		}
	}

	function run( testResults, testBox ){
		describe( "test case for ImageDrawPoint", function() {

			it(title = "Checking with ImageDrawOval", body = function( currentSpec ){
				imgOne = imageNew("",200,200,"rgb","red");
				imageDrawPoint(imgOne,90,90);
				cfimage(action = "write", source = imgOne, destination = path&'imgDrawovalOne.jpg',overwrite = "yes");
				expect(fileexists(path&'imgDrawovalOne.jpg')).tobe("true");
			});

			it(title = "Checking with image.drawOval()", body = function( currentSpec ){
				imgTwo = imageNew("",200,200,"rgb","blue");
				imgTwo.DrawPoint(100,130);
				cfimage(action = "write", source = imgTwo, destination = path&'drawovalObjone.jpg',overwrite = "yes");
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