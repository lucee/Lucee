component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ){
		describe( "LDEV-5608 - can't find matching constructor with a nullValue", function(){

			it( "create a SimpleDoc with nullValue", function(){

				var p = new component{
					import javax.print.SimpleDoc;
					import javax.print.DocFlavor;
					import java.io.FileInputStream;
					import javax.print.attribute.HashDocAttributeSet;
					
					function test(){
						var fis = new FileInputStream(getCurrentTemplatePath());
						var flavor = createObject("java", "javax.print.DocFlavor$INPUT_STREAM").AUTOSENSE;
						var docAttr = new HashDocAttributeSet();
						// https://docs.oracle.com/en/java/javase/22/docs/api/java.desktop/javax/print/SimpleDoc.html
						var doc = new SimpleDoc( fis, flavor, nullValue() );
						return doc;
					}
				};

				var doc = p.test(); //  failed to load constructor for class [javax.print.SimpleDoc]
				expect( isObject(doc) ).toBeTrue();

			});

		} );
	}

}
