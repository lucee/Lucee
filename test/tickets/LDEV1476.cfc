component extends="org.lucee.cfml.test.LuceeTestCase" labels="image" {
	function run( testResults , testBox ) {
		describe( "test case for LDEV-1476", function() {
			it(title = "checking image data from tobase64() function", body = function( currentSpec ) {
				arrSrc=["tomcat.gif", "lucee.png","test.jpg"];

				loop array=arrSrc item="src" {
					var result = "";
					try{
						var img = ImageRead("LDEV1476\#src#");
						var imgBase64=toBase64(img);
						var binimg = toBinary(imgBase64);
						var targetImg="img."&listLast(src,'.');
						file action="write" output="#binimg#" file="LDEV1476\#targetImg#";

						result = imageRead("LDEV1476\#targetImg#");
					} 
					finally {
						fileDelete("LDEV1476\#targetImg#");
					}
					assertEquals(isImage(result), true);
				}
			});

			it(title = "checking image data from filereadBinary() function", body = function( currentSpec ) {
				arrSrc=["tomcat.gif", "lucee.png","test.jpg"];

				loop array=arrSrc item="src" {
					var result = "";
					try{
						var img = ImageRead("LDEV1476\#src#");
						var bin = fileReadBinary("LDEV1476\#src#");
						var binBase64=toBase64(bin);
						var binbin = toBinary(binBase64);
						var targetBin="bin."&listLast(src,'.');
						file action="write" output="#binbin#" file="LDEV1476\#targetBin#";

						result = imageRead("LDEV1476\#targetBin#");
					} catch(any e){
						result = e.message;					}
					finally {
						fileDelete("LDEV1476\#targetBin#");
					}
					assertEquals(isImage(result), true);
				}
			});
		});
	}
}
