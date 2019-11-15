component extends = "org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.path = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/" &"ImageDrawText/";
		if(!directoryExists(path)){
			directorycreate(path);
		}
	}
	function run( testResults, testBox ){
		describe( "test case for ImageDrawText", function() {
			it(title = "Checking with ImageDrawText", body = function( currentSpec ){
				img = imageNew("",150,150,"RGB","149c82");
				ImageDrawText(img, "I Love Lucee",40,70);
				cfimage(action = "write", source = img, destination = path&'imgDrawtext.jpg',overwrite = "yes");
				expect(fileexists(path&'imgDrawtext.jpg')).tobe("true");
			});

			it(title = "Checking with ImageDrawText with imageURL", body = function( currentSpec ){
				imgOne = imageRead("https://pbs.twimg.com/profile_images/1037639083135250433/fREb9ZhM_400x400.jpg");
				design = { style="ITALIC", underline = "true", size = "30" };
				ImageDrawText(imgOne, "Save Tree!!",30,160,design);
				cfimage(action = "write", source = imgOne, destination = path&'imgDrawtextURL.jpg',overwrite = "yes");
				expect(fileExists(path&'imgDrawtextURL.jpg')).tobe("true");
			});

			it(title = "Checking with ImageDrawText with imageURL ere", body = function( currentSpec ){
				imgObj = imageNew("",200,200,"RGB","0000BB");
				aCollection = { style="BOLD", underline = "true", size = "23" };
				imgObj.DrawText("Save Tree!!!",40,30,aCollection);
				cfimage(action = "write", source = imgObj, destination = path&'drawtxtObj.jpg',overwrite = "yes");
				expect(fileExists(path&'drawtxtObj.jpg')).tobe("true");
			});

			it(title = "Checking with ImageDrawText with imageURL ere", body = function( currentSpec ){
				imgObj = imageNew("",200,200,"RGB","0000BB");
				aCollection = { style="BOLD", underline = "true", size = "23", font="Arial Black" };
				imgObj.DrawText("Save Tree!!!",40,30,aCollection);
				cfimage(action = "write", source = imgObj, destination = path&'drawtxtObj.jpg',overwrite = "yes");
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