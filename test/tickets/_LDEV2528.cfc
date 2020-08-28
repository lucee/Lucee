component extends="org.lucee.cfml.test.LuceeTestCase"{

	function run( testResults , testBox ) {
		describe( "test case for LDEV-2528", function() {
			it(title = "cfdocument with mixed-orientation default orientation landscape", body = function( currentSpec ) {
				document pagetype = "letter" orientation = "landscape" filename = "test-orientation.pdf" overwrite = "true" name = "output" {
					documentsection { echo("I am from landscape"); }
				}
				cfpdf(action = "read", source = "test-orientation.pdf", name = "out");
				expect(out.pagesize[1].height).toBe("612");
				expect(out.pagesize[1].width).toBe("792");
			});

			it(title = "cfdocument with mixed-orientation default orientation portrait", body = function( currentSpec ) {
				document pagetype = "letter" orientation = "portrait" filename = "test-orientation.pdf" overwrite = "true" name = "output" {
					documentsection { echo("I am from portrait"); }
				}
				cfpdf(action = "read", source = "test-orientation.pdf", name = "out");
				expect(out.pagesize[1].height).toBe("792");
				expect(out.pagesize[1].width).toBe("612");
			});

			it(title = "cfdocument with mixed-orientation documentsection orientation portrait", body = function( currentSpec ) {
				document pagetype = "letter" orientation = "landscape" filename = "test-orientation.pdf" overwrite = "true" name = "output" {
					documentsection orientation="portrait" { echo("I am from portrait"); }
				}
				cfpdf(action = "read", source = "test-orientation.pdf", name = "out");
				expect(out.pagesize[1].height).toBe("792");
				expect(out.pagesize[1].width).toBe("612");
			});

			it(title = "cfdocument with mixed-orientation documentsection orientation landscape", body = function( currentSpec ) {
				document pagetype = "letter" orientation = "landscape" filename = "test-orientation.pdf" overwrite = "true" name = "output" {
					documentsection orientation="landscape" { echo("I am from landscape"); }
				}
				cfpdf(action = "read", source = "test-orientation.pdf", name = "out");
				expect(out.pagesize[1].height).toBe("612");
				expect(out.pagesize[1].width).toBe("792");
			});
		});
	}

	function afterAll(){
		filedelete("test-orientation.pdf")
	}

}