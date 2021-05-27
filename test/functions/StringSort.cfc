component extends="org.lucee.cfml.test.LuceeTestCase"{
    function run( testResults, testBox ){
        describe( "testcase for stringSort", function(){
            it(title="Check with stringSort()", body=function( currentSpec ){
                assertEquals("eginrssttt", stringSort("teststring"));
                assertEquals("ABEFcdgh", stringSort("ABcdEFgh"));
                assertEquals("123GINRSTestt", stringSort("testSTRING123"));
                assertEquals("!$%?@SSginorrtt{}", stringSort("StringSort!@$%{}?"));
                assertEquals("..eginorrssstttt", stringSort("test.string.sort"));
                assertEquals("..///:acddeeghllnoooprsttuw", stringSort("https://download.lucee.org/"));
            });
            it(title="Check with stringSort() member functions", body=function( currentSpec ){
                assertEquals("eginrssttt", "teststring".Sort());
                assertEquals("ABEFcdgh", "ABcdEFgh".Sort());
                assertEquals("123GINRSTestt", "testSTRING123".Sort());
                assertEquals("!$%?@SSginorrtt{}", "StringSort!@$%{}?".Sort());
                assertEquals("..eginorrssstttt", "test.string.sort".Sort());
                assertEquals("..///:acddeeghllnoooprsttuw", "https://download.lucee.org/".Sort());
            })
        });
    }
}