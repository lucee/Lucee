component extends="org.lucee.cfml.test.LuceeTestCase" {

    function run( testResults , testBox ) {
        describe( title = "Testcase for HtmlParse() function", body = function() {
            it( title = "Checking HtmlParse() function", body = function( currentSpec ) {
                test = htmlParse("HI <body><p>Lucee</p></body> !!!");
                expect(IsXML(test)).toBeTrue();
                expect(isstruct(test)).toBeTrue();
                expect(IsXmlElem(test.html)).toBeTrue();
            });
        });
    }
}