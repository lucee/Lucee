component extends="org.lucee.cfml.test.LuceeTestCase" {
    public function run( testResults, textbox ) {
        describe(title="Testcase for Cfwhile tag", body=function() {
            it(title="Checking the Cfwhile tag", body=function( currentSpec ) {
                cnt = 0;
                while(cnt LT 2) {
                    cnt = cnt + 1;
                }
                expect(cnt).toBe(2);
                expect(cnt).toBeNumeric();
            });
        });
    }
}
