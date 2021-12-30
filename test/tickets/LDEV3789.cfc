component extends="org.lucee.cfml.test.LuceeTestCase"{
    function run( testResults, testBox ) {
        describe("Testcase for LDEV-3789", function() {
            it( title="Checking member-functions with named arguments", body=function( currentSpec ){
                // dateTime functions
                dateValue = now(); 
                dateValue2 = ParseDateTime("01/01/2020"); 
                assertEquals(dateValue.compare(dateValue2, "d"), dateValue.compare(date2=dateValue2, datePart="d"));
                assertEquals(dateValue.diff("d", dateValue2), dateValue.diff(date1=dateValue2, datePart="d"));
                assertEquals(dateValue.add("d", 1), dateValue.add(number="1", datePart="d"));

                // string functions
                assertEquals("lucee".compare("Lucee"), "lucee".compare(String2="Lucee"));
                assertEquals("I love lucee".find("lucee"), "I love lucee".find(subString="lucee"));
                assertEquals("abcdef".replace("abc", "xxx"), "abcdef".replace(substring="abc", replacement="xxx"));
            });
        });
    }
}