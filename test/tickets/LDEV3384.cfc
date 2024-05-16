component extends="org.lucee.cfml.test.LuceeTestCase" labels="datetime" {
    function run( testResults, testBox ){
        describe( "Test case for LDEV-3384", function() {
            it(title="dateDiff function", body=function( currentSpec ){
                expect(dateDiff("s",now(),now())).toBe(0);
                expect(dateDiff("s",createDateTime(2010,10,10,10,10,10),createDateTime(2010,10,10,11,10,10))).toBe(3600);
                expect(dateDiff("s",dateTimeFormat(now()),dateTimeFormat(now()))).toBe(0);
                expect(dateDiff("s","10-Oct-2010 10:10:10","10-Oct-2010 11:10:10")).toBe(3600);
            });
            it(title="dateDiff member function", body=function( currentSpec ){
                try{
                    hasError = false;
                    expect(now().diff("s",now())).toBe(0);
                    expect(createDateTime(2010,10,10,10,10,10).diff("s",createDateTime(2010,10,10,11,10,10))).toBe(-3600);
                    expect(dateTimeFormat(now()).diff("s",dateTimeFormat(now()))).toBe(0);
                    expect("10-Oct-2010 10:10:10".diff("s","10-Oct-2010 11:10:10")).toBe(-3600);
                }   
                catch(any e){
                    hasError = e.message;
                }
                expect(hasError).toBe(false);
            });
            
            // there is two compare() member functions in lucee String.compare(), dateTime.compare()
            // So made tests to confirm dateTimeFomartedString.compare() should invokes String.compare()
            it(title="String.compare() to invokes String.compare()", body=function( currentSpec ){
                expect("lucee".compare("lucee")).toBe("0");
                expect("lucee".compare("CFML")).toBe("1");
                expect("CFML".compare("coldfusion")).toBe("-1");
            });
            it(title="dateTimeFomartedString.compare() to invokes String.compare()", body=function( currentSpec ){
                expect("10/10/2020 10:10:10".compare("test")).toBe("-1");
                expect("01/01/2020".compare("test")).toBe("-1");
            });
            it(title="dateTime.compare() to invokes DateCompare()", body=function( currentSpec ){
                expect(createDateTime(2010,10,10,0,0,0).compare("2010/10/09")).toBe("1");
                expect(createDateTime(2010,10,10,0,0,0).compare("2010/10/10")).toBe("0");
                expect(createDateTime(2010,10,10,0,0,0).compare("2010/10/11")).toBe("-1");
            });
        });
    }
}