component extends="org.lucee.cfml.test.LuceeTestCase" skip=true{
	function run( testResults , testBox ) {
		describe( title="Test suite for arrayFunctions accepts struct values", body=function() {
			it(title = "arraysplice struct with number keys", body = function( currentSpec ) {
                struct = {"1":"one","2":"two","3":"three"}
                try {
                    res = arraysplice(struct,1,1);
                }
                catch(any e) {
                    res = "success";
                }
                expect(res).toBe("success");
            });

            it(title = "arrayappend struct with number keys", body = function( currentSpec ) {
                struct = {"1":"one","2":"two","3":"three"}
                try {
                    res = arrayappend(struct,1);
                }
                catch(any e) {
                    res = "success";
                }
                expect(res).toBe("success");
            });

            it(title = "arrayprepend struct with number keys", body = function( currentSpec ) {
                struct = {"1":"one","2":"two","3":"three"}
                try {
                    res = arrayprepend(struct,1);
                }
                catch(any e) {
                    res = "success";
                }
                expect(res).toBe("success");
            });
                
            it(title = "arrayFind struct with number keys", body = function( currentSpec ) {
                struct = {"1":"one","2":"two","3":"three"}
                try {
                    res = arrayFind(struct,"two");
                }
                catch(any e) {
                    res = "success";
                }
                expect(res).toBe("success");
            });

            it(title = "arrayFindNoCase struct with number keys", body = function( currentSpec ) {
                struct = {"1":"one","2":"two","3":"three"}
                try {
                    res = arrayFindNoCase(struct,"two");
                }
                catch(any e) {
                    res = "success";
                }
                expect(res).toBe("success");
            });

            it(title = "arrayFindAll struct with number keys", body = function( currentSpec ) {
                struct = {"1":"one","2":"two","3":"three"}
                try {
                    res = arrayFindAll(struct,"two");
                }
                catch(any e) {
                    res = "success";
                }
                expect(res).toBe("success");
            });
                
            it(title = "arrayFindAllNoCase struct with number keys", body = function( currentSpec ) {
                struct = {"1":"one","2":"two","3":"three"}
                try {
                    res = arrayFindAllNoCase(struct,"two");
                }
                catch(any e) {
                    res = "success";
                }
                expect(res).toBe("success");
            });

            it(title = "arrayFindAllNoCase struct with number keys", body = function( currentSpec ) {
                struct = {"1":"one","2":"two","3":"three"}
                try {
                    res = arrayToStruct(struct);
                }
                catch(any e) {
                    res = "success";
                }
                expect(res).toBe("success");
            });
            
            it(title = "arrayToList struct with number keys", body = function( currentSpec ) {
                struct = {"1":"one","2":"two","3":"three"}
                try {
                    res = arrayToList(struct);
                }
                catch(any e) {
                    res = "success";
                }
                expect(res).toBe("success");
            });

            it(title = "arrayContains struct with number keys", body = function( currentSpec ) {
                struct = {"1":"one","2":"two","3":"three"}
                try {
                    res = arrayContains(struct,"two");
                }
                catch(any e) {
                    res = "success";
                }
                expect(res).toBe("success");
            });
            
            it(title = "arrayContainsNoCase struct with number keys", body = function( currentSpec ) {
                struct = {"1":"one","2":"two","3":"three"}
                try {
                    res = arrayContainsNoCase(struct,"two");
                }
                catch(any e) {
                    res = "success";
                }
                expect(res).toBe("success");
            });

            it(title = "arrayClear struct with number keys", body = function( currentSpec ) {
                struct = {"1":"one","2":"two","3":"three"}
                try {
                    res = arrayClear(struct);
                }
                catch(any e) {
                    res = "success";
                }
                expect(res).toBe("success");
            });
		});
	}
}