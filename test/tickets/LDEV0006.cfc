component extends="org.lucee.cfml.test.LuceeTestCase"{
	
	public void function test() {
		var img = "LDEV0006/exif_original.jpg";
		var imgObj = imageRead(img);
		var imgMeta = imageGetEXIFMetadata(imgObj);
		expect(124).toBeLT(structCount(imgMeta));
	}
}