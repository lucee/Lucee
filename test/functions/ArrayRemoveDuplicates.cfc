component extends="org.lucee.cfml.test.LuceeTestCase"{
    
    function beforeAll() {
        variables.s = "a string";
        variables.i = 42;
        variables.pi = pi();
        variables.st1 = {key1="value1",key2="value2"};
        variables.st1bis = {key1="VAlue1",key2="value2"};
        variables.st1ref = st1;
        variables.st2 = {key2="value2"};
        variables.a1 = [1];
        variables.a1bis = [1];
        variables.a1ref = a1;
        variables.a2 = [2];
        variables.base = [s,i,pi,st1,st1bis,st1ref,st2,a1,a1bis,a1ref,a2,s,i,pi,st1,st1bis,st1ref,st2,a1,a1bis,a1ref,a2];
    }

        function run( testResults, testBox ) {
        describe("Test suite for ArrayRemoveDuplicates()", function() {
            it(title="Checking ArrayRemoveDuplicates() function", body=function( currentSpec ) {
                assertEquals('["a string",42,#variables.pi#,{"KEY1":"value1","KEY2":"value2"},{"KEY1":"VAlue1","KEY2":"value2"},{"KEY2":"value2"},[1],[2]]', serializeJSON(ArrayRemoveDuplicates(base)));
                assertEquals('["a string",42,#variables.pi#,{"KEY1":"value1","KEY2":"value2"},{"KEY2":"value2"},[1],[2]]', serializeJSON(ArrayRemoveDuplicates(base, true)));
                assertEquals([1], ArrayRemoveDuplicates([1,1,1,1,1]));
                assertEquals(['A','B','a','b','C'], ArrayRemoveDuplicates(['A','B','a','b','C','C','A']));
                assertEquals(['A','B','C'], ArrayRemoveDuplicates(['A','B','a','b','C','C','A'], true));
            });
        
            it(title="Checking ArrayRemoveDuplicates() member function", body=function( currentSpec ) {
                assertEquals('["a string",42,#variables.pi#,{"KEY1":"value1","KEY2":"value2"},{"KEY1":"VAlue1","KEY2":"value2"},{"KEY2":"value2"},[1],[2]]', serializeJSON(base.removeDuplicates()));
                assertEquals('["a string",42,#variables.pi#,{"KEY1":"value1","KEY2":"value2"},{"KEY2":"value2"},[1],[2]]', serializeJSON(base.removeDuplicates(true)));
                assertEquals([1], [1,1,1,1,1].removeDuplicates());
                assertEquals(['A','B','a','b','C'], ['A','B','a','b','C','C','A'].removeDuplicates());
                assertEquals(['A','B','C'], ['A','B','a','b','C','C','A'].removeDuplicates(true));
            });
        });
    }
}