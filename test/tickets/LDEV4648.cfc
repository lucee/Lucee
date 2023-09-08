component extends = "org.lucee.cfml.test.LuceeTestCase" {
    function run( testResults, textbox ) {
        describe("testcase for LDEV-4648", function(){
            it(title="Checking windows-31j encoding sequence with ascii range bytes gets decoded properly.", body=function( currentSpec ){
                // unicode       windows-31j
                // hex    dec    hex
                // u77e2  30690  96ee
                // u5b50  23376  8e71
                assertEquals(Chr(30690) & Chr(23376) & "ABC", "#URLDecode("%96%ee%8eqABC","windows-31j")#");
            });
        });
    }
}
