component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults, testBox ){
		describe( "Test case for LDEV-3369", function() {
            it(title="Check expandPath() without mapping", body=function( currentSpec ){
                expect(expandPath("TEST.test")).toBeWithCase(expandPath("./")&'TEST.test');
                expect(expandPath("Test.teST")).toBeWithCase(expandPath("./")&'Test.teST');
                expect(expandPath("TesT.TEST")).toBeWithCase(expandPath("./")&'TesT.TEST');
            });
            it(title="Check expandPath() with mapping", body=function( currentSpec ){
                expect(expandPath("/mapping/TEST.test")).toBeWithCase(expandPath("/")&'mapping\TEST.test');
                expect(expandPath("/mapping/Test.teST")).toBeWithCase(expandPath("/")&'mapping\Test.teST');
                expect(expandPath("/mapping/TesT.TEST")).toBeWithCase(expandPath("/")&'mapping\TesT.TEST');
            })
        });
    }
}