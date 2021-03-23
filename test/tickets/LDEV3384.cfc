component extends="org.lucee.cfml.test.LuceeTestCase"{
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
                    expect(createDateTime(2010,10,10,10,10,10).diff("s",createDateTime(2010,10,10,11,10,10))).toBe(3600);
                    expect(dateTimeFormat(now()).diff("s",dateTimeFormat(now()))).toBe(0);
                    expect("10-Oct-2010 10:10:10".diff("s","10-Oct-2010 11:10:10")).toBe(3600);
                }   
                catch(any e){
                    hasError = e.message;
                }
                expect(hasError).toBe(false);
            });
        });
    }
}