component extends = "org.lucee.cfml.test.LuceeTestCase" {

    function run( testResults, testBox ){
        describe( "Testcase for LDEV-1018", function(){
            it( title="check for 'xml' returnFormat", body=function( currentSpec ) {
            	try{throw "abc";}
				catch (any local.e) {data=local.e}
				expect( find('try{throw "abc";}',data.tagContext[1].codePrintPlain)>0 ).toBeTrue();
            	expect( find('try{throw&nbsp;&quot;abc&quot;;}',data.tagContext[1].codePrintHTML)>0 ).toBeTrue();
            });
        });
    }
}