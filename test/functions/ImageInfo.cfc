component extends="org.lucee.cfml.test.LuceeTestCase" labels="image" {
	function run( testResults , testBox ) {
		describe( "testcase for imageInfo()", function() {
			it(title = "Checking with imageInfo()", body = function( currentSpec ) {
				var img = imageNew("", 200, 200, "rgb", "red");
				expect(imageInfo(img)).toHaveKey("colormodel");
				expect(imageInfo(img).colormodel.colorspace).toBe("Any of the family of RGB color spaces");
				expect(imageInfo(img).width).toBe("200");
				expect(img.info().width).toBe("200");
			});
		});
	}
}	