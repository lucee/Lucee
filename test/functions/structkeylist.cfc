component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run() {
		describe( title="Test suite for structKeyList", body=function() {
			student = {
				student1:{
					id:1,
					name:"joe"
				},
				student2:{
					id:2,
					name:"root"
				},
				student3:{
					id:3,
					name:"jack"
				}
			}
			it( title='Test case for structKeyList function  ',body=function( currentSpec ) {
				assertEquals('"STUDENT1,STUDENT2,STUDENT3"',serialize(structKeyList(student)));
				structClear(student)
				assertEquals('',structKeyList(student));
			});
		});

		describe( title="Test suite for structKeyList", body=function() {
			studentnew = {
				student1:{
					id:1,
					name:"joe"
				},
				student2:{
					id:2,
					name:"root"
				},
				student3:{
					id:3,
					name:"jack"
				}
			}
			it( title='Test case for structKeyList member function',body=function( currentSpec ) {
				assertEquals('"STUDENT1,STUDENT2,STUDENT3"',serialize(studentnew.keyList()));
				structClear(studentnew)
				assertEquals('',studentnew.keyList());
			});
		});

	}
}