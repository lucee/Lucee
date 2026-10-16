component extends="org.lucee.cfml.test.LuceeTestCase"{

    function run( testResults , testBox ) {
        describe( title = "Testcase for sanitizeHTML function", body = function() {
            it( title = "checking sanitizeHTML() function", body = function( currentSpec ) {
                var html = '<!DOCTYPE html><html><body><h2>HTML Forms</h2><form action="/action_page.cfm"><label for="fname">First name:</label><br><input type="text" id="fname" name="fname"value="Pothys"><br></body></html>';

                var result=SanitizeHtml(html);
                // bechause esapi and owasp sanitizeHtml functions differ in how they handle line breaks, we normalize the output by replacing all <br /> with <br> before comparing
                result=result.replace("<br />","<br>","all");
                expect(result).toBe('<h2>HTML Forms</h2>First name:<br><br>');
            });
        });
    }
}