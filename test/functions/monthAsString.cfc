component extends="org.lucee.cfml.test.LuceeTestCase" {
    function run( testResults , testBox ) {
        describe( title = "Testcase for monthAsString() function", body = function() {
            it( title = "Checking monthAsString() function", body = function( currentSpec ) {
                expect(monthAsString(1, "english (india)")).toBe('January');
                expect(monthAsString(2, "albanian")).toBe('shkurt');

                expect(monthAsString(monthNumber=3, locale="english (australia)")).toBe('March');
                expect(monthAsString(monthNumber=4, locale="english (united kingdom)")).toBe('April');
            });
        });
    }
}