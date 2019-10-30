component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.path = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/" &"imageDrawArc/";
		if(!directoryExists(path)){
			directorycreate(path);
		}
	}
	function run( testResults, testBox ){
		describe( "test case for imageDrawArc", function() {
			it(title = "Checking with imageDrawArc", body = function( currentSpec ){
				img = imageRead("https://dev.lucee.org/uploads/default/original/2X/1/140e7bb0f8069e4f7f073b6d01f55c496bbd42e3.png");
				imageDrawArc(img,25,30,30,50,90,180,"true");
				cfimage(action = "write", source = img, destination = path&'imgDrawarc.jpg',overwrite = "yes");
				expect(fileexists(path&'imgDrawarc.jpg')).tobe("true");
			});

			it(title = "Checking with image.DrawArc()", body = function( currentSpec ){
				arcImg = imagenew("",150,150,"rgb","149c82");
				arcImg.DrawArc(50,50,50,100,360,90,"yes");
				cfimage(action = "write", source = arcImg, destination = path&'imgDrawarcobj.jpg',overwrite = "yes");
				expect(fileexists(path&'imgDrawarcobj.jpg')).tobe("true");
			});
		});
	}
	function afterAll(){
		if(directoryExists(path)){
			directoryDelete(path,true);
		}
	}
}