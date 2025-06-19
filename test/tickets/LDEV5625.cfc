component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults , testBox ) {

		describe( title="Test case LDEV-5625", body=function() {

			it(title="pdfbox pddocument", body = function( currentSpec ) {
				var pd = new component
						javasettings = '{
							"maven": [
								"org.apache.pdfbox:pdfbox:3.0.3",
								"org.apache.logging.log4j:log4j-api:2.20.0",
								"org.apache.logging.log4j:log4j-core:2.20.0"
							]
						}'
				{
					import java.io.FileOutputStream;
					import org.apache.pdfbox.pdmodel.PDDocument;

					function getPDDocument() {
						
						return new PDDocument();
					}
				};

				pd.getPDDocument();  // throws could not find component or class  with name [PDDocument]
			});

		});
	}

}
