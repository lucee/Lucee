component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.path = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/" &"ImageDrawOval/";
		if(!directoryExists(path)){
			directorycreate(path);
		}
	}
	function run( testResults, testBox ){
		describe( "test case for ImageDrawOval", function() {
			it(title = "Checking with ImageDrawOval with imageURL", body = function( currentSpec ){
				img = imageRead("https://pbs.twimg.com/profile_images/1037639083135250433/fREb9ZhM_400x400.jpg");
				ImageDrawOval(img, 60,50,70,30,"yes");
				cfimage(action = "write", source = img, destination = path&'imgDrawoval.jpg',overwrite = "yes");
				expect(fileexists(path&'imgDrawoval.jpg')).tobe("true");
			});

			it(title = "Checking with ImageDrawOval", body = function( currentSpec ){
				imgOne = imageNew("",200,200,"rgb","red");
				ImageDrawOval(imgOne, 50,50,70,30,"no");
				cfimage(action = "write", source = imgOne, destination = path&'imgDrawovalOne.jpg',overwrite = "yes");
				expect(fileexists(path&'imgDrawovalOne.jpg')).tobe("true");
			});

			it(title = "Checking with image.drawOval() with imageURL", body = function( currentSpec ){
				imgTwo = imageRead("https://dev.lucee.org/uploads/default/original/2X/1/140e7bb0f8069e4f7f073b6d01f55c496bbd42e3.png");
				imgTwo.drawOval(80,100,70,20,"no");
				cfimage(action = "write", source = imgTwo, destination = path&'Drawovalobj.jpg',overwrite = "yes");
				expect(fileexists(path&'Drawovalobj.jpg')).tobe("true");
			});

			it(title = "Checking with image.drawOval()", body = function( currentSpec ){
				imgThree = imageNew("",200,200,"rgb","green");
				imgThree.drawOval(80,100,70,20,"yes");
				cfimage(action = "write", source = imgThree, destination = path&'drawovalObjone.jpg',overwrite = "yes");
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