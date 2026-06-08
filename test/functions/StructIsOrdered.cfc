component extends="org.lucee.cfml.test.LuceeTestCase" {

    function beforeAll() {
        variables.orderedStruct = structNew(type = "ordered");
        variables.orderedStruct.a = 1;
        variables.orderedStruct.b = 2;
        variables.unorderedStruct = structNew();
        variables.unorderedStruct.a = 1;
        variables.unorderedStruct.b = 2;
        variables.orderedStructLiteral = [one=1, two=2, three=6, four=4];
        variables.unorderedStructLiteral = {one=1, two=2, three=6, four=4};
    }

    function run(testResults, testBox) {

        describe("Testcases for StructIsOrdered() BIF", function() {
            it(title = "should return true for ordered struct", skip = true, body = function(currentSpec) {
                expect(StructIsOrdered(variables.orderedStruct)).toBeTrue();
            });

            it(title = "should return false for unordered struct", skip = true, body = function(currentSpec) {
                expect(StructIsOrdered(variables.unorderedStruct)).toBeFalse();
            });

            it(title = "should return false for unordered struct (literal syntax)", skip = true, body = function(currentSpec) {
                expect(StructIsOrdered(variables.unorderedStructLiteral)).toBeFalse();
            });

            it(title = "should return true for ordered struct (literal syntax)", skip = true, body = function(currentSpec) {
                expect(StructIsOrdered(variables.orderedStructLiteral)).toBeTrue();
            });

        });

        describe("Testcases for isOrdered() - Member Function", function() {
            it(title = "should return true for ordered struct (member)", skip = true, body = function(currentSpec) {
                expect(variables.orderedStruct.isOrdered()).toBeTrue();
            });

            it(title = "should return false for unordered struct (member)", skip = true, body = function(currentSpec) {
                expect(variables.unorderedStruct.isOrdered()).toBeFalse();
            });

            it(title = "should return false for unordered struct (literal syntax) (member)", skip = true, body = function(currentSpec) {
                expect(variables.unorderedStructLiteral.isOrdered()).toBeFalse();
            });

            it(title = "should return true for ordered struct (literal syntax) (member)", skip = true, body = function(currentSpec) {
                expect(variables.orderedStructLiteral.isOrdered()).toBeTrue();
            });

        });

    }
}
