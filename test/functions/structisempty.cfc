component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run() {

		describe( title="Test suite for structIsEmpty", body=function() {
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
			it( title='Test case for structIsEmpty function  ',body=function( currentSpec ) {
				assertEquals('FALSE',structIsEmpty(student));
				structClear(student)
				assertEquals('TRUE',structIsEmpty(student));

			});

		});

		describe( title="Test suite for structIsEmpty", body=function() {
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
			it( title='Test case for structIsEmpty member function',body=function( currentSpec ) {
				assertEquals('FALSE',studentnew.isEmpty());
				structClear(studentnew)
				assertEquals('TRUE',studentnew.isEmpty());

			});
		});
	}
}