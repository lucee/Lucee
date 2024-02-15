component extends="org.lucee.cfml.test.LuceeTestCase"{

    function run( testResults , testBox ) {
        describe( "test case for replaceNocase", function() {

            it(title = "test non ascii characters", body = function( currentSpec ) {
                var input = 'aaa bbb & İkra';
                var toReplace = 'aaa bbb & İkra';
                var newStr="aaa bbb;İkra";
                assertEquals(newStr,ReplaceNocase(input,toReplace,newStr));

            });

        }); 
    }
    
}