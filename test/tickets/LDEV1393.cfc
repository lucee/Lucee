component extends="org.lucee.cfml.test.LuceeTestCase" labels="image" {
	function beforeAll(){
		variables.path ="#getDirectoryFromPath(getCurrenttemplatepath())#LDEV1393\";
		cffile(action = "read" , file = "#path#image_code.base64",  variable = "variables.base64");
	}

	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1393", function() {
			it(title="checking  tobinary() function, with base64 code", body = function( currentSpec ) {
				var binary = tobinary(base64);
				expect(isbinary(binary)).toBe(true);
			});

			//convert base64 code to image and then convert into binary.
			it(title="checking tobinary() function, with base64 code from the image", body = function( currentSpec ) {
				var imgPath="#path#originalImg.png";
				try {
					cfimage(
						source="#ImageReadBase64(variables.base64)#",  
						name="originalImg", 
						destination=imgPath, 
						action="resize", 
						width="50%", 
						height="80%", 
						overwrite="yes");

					var base64 = tobase64(originalImg);
					var binary = tobinary(base64);
					expect(isbinary(binary)).toBe(true);
				}
				finally {

					if(fileExists(imgPath)) fileDelete(imgPath);
				}
			});
		});
	}
}