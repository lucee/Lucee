component extends="org.lucee.cfml.test.LuceeTestCase"{
	
	public void function test() {
		var img = "LDEV0006/exif_original.jpg";
		var imgObj = imageRead(img);
		var imgMeta = imageGetEXIFMetadata(imgObj);
		assertEquals(124,structCount(imgMeta));
	}
}