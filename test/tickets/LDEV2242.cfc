component extends="org.lucee.cfml.test.LuceeTestCase" {
    function run( testResults , testBox ) {
        describe( "test case for LDEV-2242", function() {
            it( title="implict getters properties returnType with getMetadata()", body=function( currentSpec ) {
                var comp = new LDEV2242.testComponent();
                var compMetaData = getMetadata(comp);

                var callback = function(e1,e2) { return compare(e1.name, e2.name); }
                var propsArr = compMetaData.properties.sort(callback);
                var functionsArr = compMetaData.functions.sort(callback);
                
                // checking component metadata properties type
                expect(propsArr[1].type).toBe("array");
                expect(propsArr[2].type).toBe("string");
                expect(propsArr[3].type).toBe("struct");
                expect(propsArr[4].type).toBe("testcfc");

                // checking component metadata functions returnType
                expect(functionsArr[1].returntype).toBe("array");
                expect(functionsArr[2].returntype).toBe("string");
                expect(functionsArr[3].returntype).toBe("struct");
                expect(functionsArr[4].returntype).toBe("testcfc");
                
                // checking implict getters properties functions returnType
                expect(getMetadata(comp["getStringProp"]).returntype).toBe("string");
                expect(getMetadata(comp["getArrayProp"]).returntype).toBe("array");
                expect(getMetadata(comp["getStructProp"]).returntype).toBe("struct");
                expect(getMetadata(comp["gettestcfcProp"]).returntype).toBe("testcfc");
            });
        });
    }
}