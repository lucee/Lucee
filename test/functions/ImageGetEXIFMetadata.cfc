component extends="org.lucee.cfml.test.LuceeTestCase" {
	
	function run( testResults, testBox ) {
		describe("Testcase for imageGetEXIFMetadata()", function() {
			it( title="checking imageGetEXIFMetadata()", body=function( currentSpec ) {
				var img = imageRead(GetDirectoryFromPath(GetCurrentTemplatePath())&"images/BigBen.jpg");
				expect(imageGetEXIFMetadata(img)).toBeStruct();
				expect(imageGetEXIFMetadata(img).ColorSpace).toBe("1");
				expect(imageGetEXIFMetadata(img).ExifOffset).toBe("204");
				expect(imageGetEXIFMetadata(img)).toHaveKey("colormodel");
				expect(imageGetEXIFMetadata(img)).toHaveKey("metadata");
				expect(imageGetEXIFMetadata(img)).toHaveKey("exif");
				expect(imageGetEXIFMetadata(img).metadata.Compression.CompressionTypeName).toBe("JPEG");
				expect(imageGetEXIFMetadata(img).compression).toBe("6");
				expect(imageGetEXIFMetadata(img).exif.ColorSpace).toBe("1");
				expect(imageGetEXIFMetadata(img).exif.ExifOffset).toBe("204");
			});
		});
	}
}