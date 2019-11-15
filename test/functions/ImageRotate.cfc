component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.path = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/" &"ImageRotate/";
		if(!directoryExists(path)){
			directorycreate(path);
		}
	}
	function run( testResults , testBox ) {
		describe( "test case for ImageRotate", function() {
			it(title = "Checking with ImageRotate Function", body = function( currentSpec ) {
				img = imageNew("",150,150,"RGB","00BFFF");
				imageRotate(img,10,10,50);
				cfimage(action = "write", source = img, destination = path&"rotateImg.jpg");
			  	expect(fileExists(path&"rotateImg.jpg")).tobe("true");
			});
			it(title = "Checking with ImageRotate with interpolation", body = function( currentSpec ) {
				imgOne = imageNew("",150,150,"RGB","FFD700");
				imageRotate(imgOne,80,'bilinear');
				cfimage(action="write", source=imgOne , destination=path&"rotateImgone.jpg");
			  	expect(fileExists(path&"rotateImgone.jpg")).tobe("true");
			});
			it(title = "Checking with ImageRotate with all attributes", body = function( currentSpec ) {
				imgTwo = imageNew("",150,150,"RGB","red");
				imgTwo.rotate(5,5,60,'bicubic');
				cfimage(action="write", source=imgTwo, destination=path&"rotateImgtwo.jpg");
			  	expect(fileExists(path&"rotateImgtwo.jpg")).tobe("true");
			});
			it(title = "Checking with ImageRotate with angle only", body = function( currentSpec ) {
				imgThree = imageNew("",150,150,"RGB","A0522D");
				imgThree.rotate(80);
				cfimage(action="write", source=imgThree, destination=path&"rotateImgthree.jpg");
			  	expect(fileExists(path&"rotateImgthree.jpg")).tobe("true");
			});
		});
	};
	function afterAll(){
		if(directoryExists(path)){
			directoryDelete(path, true);
		}
	}
}
		