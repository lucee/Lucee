component extends = "org.lucee.cfml.test.LuceeTestCase" labels="struct" {
	variables.str=" ## MessageFormat pattern
 s1=Die Platte ""{1}"" enthält {0}.

 ## location of {0} in pattern
 s2=1

 ## sample disk name
 s3=Meine Platte

 ## first ChoiceFormat choice
 s4=keine Dateien

 ## second ChoiceFormat choice
 s5=eine Datei

 ## third ChoiceFormat choice
 s6={0,number} Dateien

 ## sample date
 s7=3. März 1996";


    function run( testResults, testBox ) {
        describe( "Testcase for LDEV-4938", function() {
            it( title="testing dynamic innvocation", body=function( currentSpec ) {
				var path=expandPath("{temp-directory}test.prop");
				fileWrite(path,variables.str);

				var fis          = CreateObject("java","java.io.FileInputStream").init( arguments.propertiesFile );
				var fir          = CreateObject("java","java.io.InputStreamReader").init( fis, "UTF-8" );
				var prb          = CreateObject("java","java.util.PropertyResourceBundle").init( fir );
				var keys         = prb.getKeys();
				var key          = "";
				var returnStruct = {};

				try {
					while( keys.hasMoreElements() ){
						key                 = keys.nextElement();
						returnStruct[ key ] = prb.handleGetObject( key );
					}
				}
				catch( any e ) {
					fis.close();
					rethrow;
				}

				fis.close();
			});
		});
    }
}
