component extends="org.lucee.cfml.test.LuceeTestCase" labels="image" {

	function run( testResults , testBox ) {
		describe( "test suite for LDEV3024", function() {
			it(title = "imageAddBorder with other bordertypes", body = function( currentSpec ) {
				var img = imageNew("",200,200);
				try {
					imageAddBorder(img,10,"red","wrap");
					res = (structkeyexists(img,"component 1") && structkeyexists(img,"component 2") && structkeyexists(img,"component 3") OR structkeyexists(img,"width")); 
				}
				catch (any e) {
					res = e.message;
				}
				expect(res).toBe("true");

				var img = imageNew("",200,200);
				try {
					imageAddBorder(img,10,"red","reflect");
					res = (structkeyexists(img,"width")); 
				}
				catch (any e) {
					res = e.message;
				}
				expect(res).toBe("true");

			});
				
			it(title = "imageAddBorder with bordertype 'copy'", body = function( currentSpec ) {

				var img = imageNew("",200,200,"rgb","blue");
				try {
					imageAddBorder(img,10,"red","copy");
					res = (structkeyexists(img,"colormodel") && structkeyexists(img,"width")); 
				}
				catch (any e) {
					res = e.message;
				}
				expect(res).toBe("true");
				
				var img = imageNew("",200,200,"rgb","blue");
				try {
					imageAddBorder(img,10,"red","copy");
					res = (structkeyexists(img,"component 1") && structkeyexists(img,"component 2") && structkeyexists(img,"component 3") && structkeyexists(img,"width")); 
				}
				catch (any e) {
					res = e.message;
				}
				expect(res).toBe("true");

				var img = imageNew("",200,200,"rgb","blue");
				try {
					imageAddBorder(img,10,"red","copy");
					res = (structkeyexists(img,"component 1") && structkeyexists(img,"component 2") && structkeyexists(img,"component 3") && structkeyexists(img,"width")); 
				}
				catch (any e) {
					res = e.message;
				}
				expect(res).toBe("true");

			});

		});
	}
}