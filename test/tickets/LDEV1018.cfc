component extends = "org.lucee.cfml.test.LuceeTestCase" {

    function run( testResults, testBox ){
        describe( "Testcase for LDEV-1018", function(){
            it( title="check for 'xml' returnFormat", body=function( currentSpec ) {
            	try{throw "abc";}
				catch (any local.e) {data=local.e}
				
				expect( data.tagContext[1].codePrintPlain ).toBe('try{throw "abc";}');
            	expect( data.tagContext[1].codePrintHTML ).toBe('try{throw&nbsp;&quot;abc&quot;;}');
            });
        });
    }
}
