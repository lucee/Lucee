component extends="org.lucee.cfml.test.LuceeTestCase" {
    function run( testResults, testBox ) {
        describe("Testcase for LDEV-5349", function() {
            // JSON data
            var testJSON = '
                [
                    {
                        "id": "1",
                        "name": "pothys"
                    },
                    {
                        "id": "2",
                        "name": "lucee"
                    }
                ]';
            it(title='Check deserializeJSON with strictMapping as query type', skip=true, body=function(currentSpec) {
                var deserializedQuery = deserializeJSON(testJSON, "query");
                
                // Create the expected query object manually
                var expectedQuery = queryNew("id, name");
                queryAddRow(expectedQuery, [1, "pothys"]);
                queryAddRow(expectedQuery, [2, "lucee"]);
                
                // Compare the deserialized query to the expected query
                expect(deserializedQuery).toBe(expectedQuery);
            });

            it(title='Check deserializeJSON with strictMapping as true', skip=false, body=function(currentSpec) {
                var deserializedArray = deserializeJSON(testJSON, true);
                expect(arrayLen(deserializedArray)).toBe(2);
                expect(deserializedArray[1].id).toBe("1");
                expect(deserializedArray[2].name).toBe("lucee");
            });

            it(title='Check deserializeJSON with strictMapping as false', skip=false, body=function(currentSpec) {
                var deserializedArray = deserializeJSON(testJSON, false);
                expect(arrayLen(deserializedArray)).toBe(2);
                expect(deserializedArray[1].id).toBe("1");
                expect(deserializedArray[2].name).toBe("lucee");
            });

            it(title='Check deserializeJSON without strictMapping', skip=false, body=function(currentSpec) {
                var deserializedArray = deserializeJSON(testJSON);
                expect(arrayLen(deserializedArray)).toBe(2);
                expect(deserializedArray[1].id).toBe("1");
                expect(deserializedArray[2].name).toBe("lucee");
            });

        });
    }
}