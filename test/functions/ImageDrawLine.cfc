component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.path = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/" &"ImageDrawLine/";
		if(!directoryExists(path)){
			directorycreate(path);
		}
	}
	function run( testResults, testBox ) {
		describe( "test case for ImageDrawLine", function() {
			it(title = "Checking with ImageDrawLine", body = function( currentSpec ){
				img = imageRead("https://dev.lucee.org/uploads/default/original/2X/1/140e7bb0f8069e4f7f073b6d01f55c496bbd42e3.png");
				imageDrawline(img,20,20,100,150);
				cfimage(action = "write", source = img, destination = path&'imgDrawline.jpg', overwrite="yes");
				expect(fileExists(path&'imgDrawline.jpg')).tobe("true");
			});

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

			it(title = "Checking with image.DrawLine()", body = function( currentSpec ){
				objImg = imageRead("https://pbs.twimg.com/profile_images/1037639083135250433/fREb9ZhM_400x400.jpg");
				objImg.Drawline(35,40,100,150);
				cfimage(action = "write", source = objImg, destination = path&'imgDrawlineobj.jpg', overwrite="yes");
				expect(fileExists(path&'imgDrawlineobj.jpg')).tobe("true");
			});
		});
	}
	function afterAll(){
		if(directoryExists(path)){
			directoryDelete(path,true);
		}
	}
}