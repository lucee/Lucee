component extends="org.lucee.cfml.test.LuceeTestCase" labels="qoq"{

    function beforeAll() {
        variables.sentences = queryNew("id,sentence", "integer,varchar", [
            [1, "Is this a question?"],
            [2, "This is not a question."]
        ]);   
    }

    function run() {
        describe("Testcase for LDEV-3361", function(){
            it("checking ? as queryparams", function(){
                try {
                    questions = queryExecute("SELECT * FROM sentences WHERE id=? AND sentence = ?"
                        , ["1","Is this a question?"]
                        , {dbtype="query"}
                    );
                    res = questions.sentence;
                }
                catch (any e) {
                    res = e.message;
                }
                expect(res).toBe("Is this a question?");
            });

            it("checking : as queryparams", function(){
                try {
                    questions = queryExecute("SELECT * FROM sentences WHERE id=:id AND sentence = :sentence"
                        , {'id'="1",'sentence' = "Is this a question?"}
                        , {dbtype="query"}
                    );
                    res = questions.sentence;
                }
                catch (any e) {
                    res = e.message;
                }
                expect(res).toBe("Is this a question?");
            });

            it("checking ? inside the sql string", function(){
                try {
                    questions = queryExecute("SELECT 'id?' AS xyz FROM sentences"
                        , [1]
                        , {dbtype="query"}
                    );
                    haserror = true;
                }
                catch (any e) {
                    haserror = false;
                }
                expect(haserror).toBeFalse();
            });

            it("checking column that has ? in values", function(){
                try {
                    questions = queryExecute("SELECT * FROM sentences WHERE sentence LIKE '%?' AND sentence NOT LIKE ?"
                        , ["%."]
                        , {dbtype="query"}
                    );
                    res = questions.sentence;
                }
                catch (any e) {
                    res = e.message;
                }
                expect(res).toBe("Is this a question?");
            });
        });
    }
}