component extends="org.lucee.cfml.test.LuceeTestCase" skip=true{
    function beforeAll(){
        afterAll();
        if( !directoryExists("./LDEV3391" )) directoryCreate("./LDEV3391");
        loop list="one,two", item="name"{
            document filename="./LDEV3391/#name#.pdf" overwrite=true { writeoutput("This is test pdf file #name#"); };
        }
    }
    function run( testResults, testBox ){
        describe("Test case for LDEV-3391", function(){
            it( title="cfpdf action=merge with different source and destination file", body=function( currentSpec ){
                cfpdf(action="merge", source="./LDEV3391/one.pdf,./LDEV3391/two.pdf", destination="./LDEV3391/three.pdf",overwrite=true);
                res = "Successfully merged";
                expect(res).toBe("Successfully merged");
            });
            it( title="cfpdf action=merge with same source and destination file", body=function( currentSpec ){
                try{
                    res = "Successfully merged";
                    cfpdf(action="merge", source="./LDEV3391/one.pdf,./LDEV3391/two.pdf", destination="./LDEV3391/one.pdf",overwrite=true);
                }
                catch(any e){
                    res = e.message;
                }
                expect(res).toBe("Successfully merged");
            });
        });
    }
    function afterAll(){
        if( directoryExists("./LDEV3391") ) directorydelete("./LDEV3391",true);
    }
}