component extends = "org.lucee.cfml.test.LuceeTestCase" labels="image" {

	function beforeAll(){
		variables.path = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/" &"ImageDrawText/";
		if(!directoryExists(path)){
			directorycreate(path);
		}
	}

	function run( testResults, testBox ){
		describe( "test case for ImageDrawText()", function() {

			it(title = "Checking with ImageDrawText()", body = function( currentSpec ){
				img = imageNew("",150,150,"RGB","149c82");
				ImageDrawText(img, "I Love Lucee",40,70);
				cfimage(action = "write", source = img, destination = path&'imgDrawtext.jpg', overwrite = "yes");
				expect(fileexists(path&'imgDrawtext.jpg')).tobe("true");
			});

			it(title = "Checking with Image.DrawText()", body = function( currentSpec ){
				imgObj = imageNew("",200,200,"RGB","0000BB");
				aCollection = { style="BOLD", underline = "true", size = "23", font="Arial Black" };
				imgObj.DrawText("Save Tree!!!",40,30,aCollection);
				cfimage(action = "write", source = imgObj, destination = path&'drawtxtObj.jpg', overwrite = "yes");
				expect(fileExists(path&'drawtxtObj.jpg')).tobe("true");
			});

		});
	}

	function afterAll(){
		if(directoryExists(path)){
			directoryDelete(path,true);
		}
	}
}