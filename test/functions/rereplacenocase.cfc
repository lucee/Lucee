component extends="org.lucee.cfml.test.LuceeTestCase"{

    function run( testResults , testBox ) {
        describe( "test case for reReplaceNocase", function() {

            var myQry=QueryNew("id,name","Integer,VarChar",[[1,'Lucee'],[2,'I love Lucee']]);

            it(title = "Test case for reReplaceNocase function", body = function( currentSpec ) {
                assertEquals("xxdefxxabcxx",reReplaceNocase("xxabcxxabcxx","ABC","def"));
                assertEquals("xxdefxxdefxx",reReplaceNocase("xxabcxxabcxx","ABC","def","all"));
                assertEquals("GAGARET",reReplaceNocase("CABARET","C|B","G","ALL"));
                assertEquals("GABARET",reReplaceNocase("CABARET","C|B","G"));
            });

            it(title = "Test case for reReplaceNocase member function", body = function( currentSpec ) {
                assertEquals("xxdefxxabcxx","xxabcxxabcxx".reReplaceNocase("ABC","def"));
                assertEquals("xxdefxxdefxx","xxabcxxabcxx".reReplaceNocase("ABC","def","all"));
                assertEquals("GAGARET","CABARET".reReplaceNocase("C|B","G","ALL"));
                assertEquals("GABARET","CABARET".reReplaceNocase("C|B","G"));
            });

        }); 
    }
    
}