component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults, textbox ) {
		describe("testcase for isImage()", function() {
			it(title="checking isImage() function", body=function( currentSpec ) {
				var img = imageNew("",100,100,"rgb","pink");
				var string = "pink";
				expect(isImage(img)).toBeTrue();
				expect(isImage(string)).toBeFalse();
			});
		});
	}
}