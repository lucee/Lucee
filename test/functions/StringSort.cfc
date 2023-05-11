component extends="org.lucee.cfml.test.LuceeTestCase"{
    function run( testResults, testBox ){
        describe( "testcase for stringSort", function(){
            it(title="Check with stringSort()", body=function( currentSpec ){
                assertEquals(stringSort("teststring"), "eginrssttt");
                assertEquals(stringSort("ABcdEFgh"), "ABEFcdgh");
                assertEquals(stringSort("testSTRING123"), "123GINRSTestt");
                assertEquals(stringSort("StringSort!@$%{}?"), "!$%?@SSginorrtt{}");
                assertEquals(stringSort("test.string.sort"), "..eginorrssstttt");
                assertEquals(stringSort("https://download.lucee.org/"), "..///:acddeeghllnoooprsttuw");
            });
            it(title="Check with stringSort() member functions", body=function( currentSpec ){
                assertEquals("teststring".Sort(), "eginrssttt");
                assertEquals("ABcdEFgh".Sort(), "ABEFcdgh");
                assertEquals("testSTRING123".Sort(), "123GINRSTestt");
                assertEquals("StringSort!@$%{}?".Sort(), "!$%?@SSginorrtt{}");
                assertEquals("test.string.sort".Sort(), "..eginorrssstttt");
                assertEquals("https://download.lucee.org/".Sort(), "..///:acddeeghllnoooprsttuw");
            })
        });
    }
}