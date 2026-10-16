component extends="org.lucee.cfml.test.LuceeTestCase" labels="guard" {
    function run( testResults , testBox ) {
        describe( "test case for EncodeForJavascript", function() {
            it(title = "Checking with EncodeForJavascript", body = function( currentSpec ) {
                // Testing a single quote which both libraries escape as \x27
                var enc = EncodeForJavascript("'");
                assertEquals('\x27', enc);
            });
            it(title = "Checking with EncodeForJavascriptMember", body = function( currentSpec ) {
                var enc = "'".EncodeForJavascript();
                assertEquals('\x27', enc);
            });
        }); 
    }
}