component extends="org.lucee.cfml.test.LuceeTestCase" {
	
	function run( testResults, testBox ) {
		describe("Testcase for imageGetEXIFMetadata()", function() {
			it( title="checking imageGetEXIFMetadata()", body=function( currentSpec ) {
				var img = imageRead(GetDirectoryFromPath(GetCurrentTemplatePath())&"images/BigBen.jpg");
				var meta=imageGetEXIFMetadata(img);
				expect(meta).toBeStruct();
				expect(meta.ColorSpace).toBe("1");
				expect(meta.ExifOffset).toBe("204");
				expect(meta).toHaveKey("colormodel");
				expect(meta).toHaveKey("metadata");
				expect(meta).toHaveKey("exif");
				expect(meta.metadata.Compression.CompressionTypeName).toBe("JPEG");
				expect(meta.compression).toBe("6");
				expect(meta.exif.ColorSpace).toBe("1");
				expect(meta.exif.ExifOffset).toBe("204");
				expect(meta["Subject Location"]).toBe("1631 1223 1795 1077");



				

			});
		});
	}
}