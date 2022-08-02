component extends="org.lucee.cfml.test.LuceeTestCase" labels="qoq"{
    function run( testResults, testBox ) {
        describe("Testcase for LDEV-3522", function() {
            variables.datas = queryNew("id,value","integer,double",[[0,19.22]]);
            it( title="QOQ cast function, cast to INT", body=function( currentSpec ){
                QOQInt = queryExecute( "SELECT CAST(datas.value AS INT) AS valueint FROM datas",  {}, { dbtype="query" } );
                expect( QOQInt.valueint ).toBe( 19 );
            });
            it( title="QOQ cast function, cast to BIT", body=function( currentSpec ){
                QOQBit = queryExecute( "SELECT CAST(datas.id AS BIT) AS valueBit, CAST(datas.value AS BIT) AS valueBit1 FROM datas", {}, { dbtype="query" } );
                expect( QOQBit.valueBit ).toBeTypeOf( "integer" );
                expect( QOQBit.valueBit1 ).toBeTypeOf( "integer" ); 
            });
            it( title="QOQ cast function, cast to DATE", body=function( currentSpec ){
                QOQDate = queryExecute( "SELECT CAST('2222222' AS DATE) AS valueDate FROM datas", {}, { dbtype="query" } );
                expect( isDate(QOQDate.valueDate) ).toBe(true);
            });
            it( title="QOQ convert function, convert to INT", body=function( currentSpec ){
                QOQConvInt = queryExecute( "SELECT Convert(datas.value, INT) AS valueint FROM datas",  {}, { dbtype="query" } );
                expect( QOQConvInt.valueint ).toBe( 19 );
            });
            it( title="QOQ convert function, convert to BIT", body=function( currentSpec ){
                QOQConvBit = queryExecute( "SELECT CONVERT(datas.id, BIT) AS valueBit, CONVERT(datas.value,  BIT) AS valueBit1 FROM datas", {}, { dbtype="query" } );
                expect( QOQConvBit.valueBit ).toBeTypeOf( "integer" );
                expect( QOQConvBit.valueBit1 ).toBeTypeOf( "integer" ); 
            });
            it( title="QOQ convert function, convert to DATE", body=function( currentSpec ){
                QOQConvDate = queryExecute( "SELECT CONVERT('2017-08-29', DATE) AS valueDate FROM datas", {}, { dbtype="query" } );
                expect(isDate(QOQConvDate.valueDate)).toBe(true);
            });
        });
    }
}