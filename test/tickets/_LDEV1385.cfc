component extends="org.lucee.cfml.test.LuceeTestCase"  labels="pdf"{
	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-1385", body=function() {
			it( title='cfpdf action="addwatermark"', body=function( currentSpec ) {
				local.source = expandPath('./LDEV1385/source.pdf');
				local.destination = expandPath('./LDEV1385/dest.pdf');
				local.image = expandPath('./LDEV1385/watermark.png');
				local.hasError = false;
				local.errorMsg = "";
				try {
					pdf action="addwatermark" source="#local.source#" image="#local.image#" destination="#local.destination#" overwrite="yes";
				}
				catch(any e) {
					local.hasError = true;
					local.errorMsg = e.message;
				}
				expect(local.errorMsg).toBe("");
				expect(local.hasError).toBeFalse();
			});
		});
	}
}