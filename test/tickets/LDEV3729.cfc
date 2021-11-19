component extends = "org.lucee.cfml.test.LuceeTestCase" skip=true{
    function run( testResults, textbox ) {
        describe("testcase for LDEV-3729", function(){
            it(title="Checking precision with 16 digit number", body=function( currentSpec ){
                num = 1363431448919149; // 16 digit number
                expect(toString(num)).toBe("1363431448919149");
                expect(toString(javaCast("long", num))).toBe("1363431448919149");
            });
            it(title="Checking precision with 17 digit number", body=function( currentSpec ){
                num = 13634301448919149; // 17 digit number
                // dump(toString(num)); In lucee 6, dump result shows incorrect value (13634301448919148) but tests are passed
                // dump(toString(javaCast("long", num)));
                // dump(toString(javaCast("long", num)) == "13634301448919149");
                expect(toString(num)).toBe("13634301448919149");
                expect(toString(javaCast("long", num))).toBe("13634301448919149");
            });
            it(title="Checking precision with 18 digit number", body=function( currentSpec ){
                num = 136343001448919149; // 18 digit number
                expect(toString(num)).toBe("136343001448919149");
                expect(toString(javaCast("long", num))).toBe("136343001448919149");
            });
        });
    }
}