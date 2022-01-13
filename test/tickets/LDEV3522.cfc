component extends = "org.lucee.cfml.test.LuceeTestCase" {

    function run( testResults, textbox ) {

        describe("testcase for LDEV-3522", function(){

            it(title="Can cast as date", body=function( currentSpec ){
                qry = QueryNew('foo','integer',[[40]]);
                var actual = queryExecute(
                    "SELECT cast( foo as date ) as asDate,
											convert( foo, date ) as asDate2,
											convert( foo, 'date' ) as asDate3
                    FROM qry",
                    [],
                    {dbtype="query"} );
								expect( actual.asDate ).toBeDate();
								expect( actual.asDate ).toBeInstanceOf( 'java.util.Date' );
								expect( actual.asDate2 ).toBeDate();
								expect( actual.asDate2 ).toBeInstanceOf( 'java.util.Date' );
								expect( actual.asDate3 ).toBeDate();
								expect( actual.asDate3 ).toBeInstanceOf( 'java.util.Date' );
            });

            it(title="Can cast as string", body=function( currentSpec ){
                qry = QueryNew('foo','date',[[now()]]);
                var actual = queryExecute(
                    "SELECT foo,
											cast( foo as string ) as asString,
											convert( foo, string ) as asString2,
											convert( foo, 'string' ) as asString3
                    FROM qry",
                    [],
                    {dbtype="query"} );
								expect( actual.foo ).toBeDate();
								expect( actual.foo ).toBeInstanceOf( 'java.util.Date' );
								expect( actual.asString ).toBeString();
								expect( actual.asString ).toBeInstanceOf( 'java.lang.String' );
								expect( actual.asString2 ).toBeString();
								expect( actual.asString2 ).toBeInstanceOf( 'java.lang.String' );
								expect( actual.asString3 ).toBeString();
								expect( actual.asString3 ).toBeInstanceOf( 'java.lang.String' );
            });

            it(title="Can cast as number", body=function( currentSpec ){
                qry = QueryNew('foo','string',[['40']]);
                var actual = queryExecute(
                    "SELECT foo,
											cast( foo as number ) as asNumber,
											convert( foo, number ) as asNumber2,
											convert( foo, 'number' ) as asNumber3
                    FROM qry",
                    [],
                    {dbtype="query"} );
								expect( actual.foo ).toBeString();
								expect( actual.foo ).toBeInstanceOf( 'java.lang.String' );
								expect( actual.asNumber ).toBeNumeric();
								expect( actual.asNumber ).toBeInstanceOf( 'java.lang.Double' );
								expect( actual.asNumber2 ).toBeNumeric();
								expect( actual.asNumber2 ).toBeInstanceOf( 'java.lang.Double' );
								expect( actual.asNumber3 ).toBeNumeric();
								expect( actual.asNumber3 ).toBeInstanceOf( 'java.lang.Double' );
            });

            it(title="Can cast as boolean", body=function( currentSpec ){
                qry = QueryNew('foo','string',[['true']]);
                var actual = queryExecute(
                    "SELECT foo,
											cast( foo as boolean ) as asBoolean,
											convert( foo, boolean ) as asBoolean2,
											convert( foo, 'bool' ) as asBool3
                    FROM qry",
                    [],
                    {dbtype="query"} );
								expect( actual.foo ).toBeString();
								expect( actual.foo ).toBeInstanceOf( 'java.lang.String' );
								expect( actual.asBoolean ).toBeBoolean();
								expect( actual.asBoolean ).toBeInstanceOf( 'java.lang.Boolean' );
								expect( actual.asBoolean2 ).toBeBoolean();
								expect( actual.asBoolean2 ).toBeInstanceOf( 'java.lang.Boolean' );
								expect( actual.asBool3 ).toBeBoolean();
								expect( actual.asBool3 ).toBeInstanceOf( 'java.lang.Boolean' );
            });

            it(title="Can cast as xml", body=function( currentSpec ){
                qry = QueryNew('foo','string',[['<root brad="wood" />']]);
                var actual = queryExecute(
                    "SELECT foo,
											cast( foo as xml ) as asXML,
											convert( foo, xml ) as asXML2,
											convert( foo, 'xml' ) as asXML3
                    FROM qry",
                    [],
                    {dbtype="query"} );
								expect( isXML( actual.foo ) ).toBeTrue();
								expect( isXMLDoc( actual.foo ) ).toBeFalse();
								expect( isXML( actual.asXML ) ).toBeTrue();
								expect( isXMLDoc( actual.asXML ) ).toBeTrue();
								expect( isXML( actual.asXML2 ) ).toBeTrue();
								expect( isXMLDoc( actual.asXML2 ) ).toBeTrue();
								expect( isXML( actual.asXML3 ) ).toBeTrue();
								expect( isXMLDoc( actual.asXML3 ) ).toBeTrue();
            });
       });

    }

}