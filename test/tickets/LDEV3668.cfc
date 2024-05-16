component extends="org.lucee.cfml.test.LuceeTestCase" skip=true{
    function run( testResults , testBox ) {
        describe( "Testcase for LDEV-3668", function() {
            variables.arr = ["direct"];
            it( title="passby=value for tag argument", body=function( currentSpec ){
                expect(serializeJSON(byTagValue(arr))).toBe('["direct","by tag value"]');
                expect(serializeJSON(arr)).toBe('["direct"]');
            });
            it( title="default passby for script argument", body=function( currentSpec ){
                expect(serializeJSON(byRef(arr))).toBe('["direct","by reference"]');
                expect(serializeJSON(arr)).toBe('["direct","by reference"]');
            });
            it( title="passby=value for script argument", body=function( currentSpec ){
                expect(serializeJSON(byValue(arr))).toBe('["direct","by reference","by value"]');
                expect(serializeJSON(arr)).toBe('["direct","by reference"]');
            });
        });
    }
    
    ```
        <cffunction name="byTagValue">
            <cfargument name="arr" passby="value">
            <cfset arguments.arr.append("by tag value")>
            <cfreturn arguments.arr>
        </cffunction>
    ```

    function byRef (required array arr ){
        arguments.arr.append("by reference");
        return arguments.arr;
    }
    
    // argument with passBy=value
    function byValue (required array arr passby="value"){
        arguments.arr.append("by value");
        return arguments.arr;
    }
}