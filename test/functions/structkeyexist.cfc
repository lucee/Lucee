component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run() {

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
		describe( title="Test suite for structkeyexists", body=function() {
			it( title='Test case for structkeyexists function  ',body=function( currentSpec ) {
				assertEquals('true',structkeyexists(student,"student1"));
				assertEquals('true',structkeyexists(student,"student2"));
				assertEquals('true',structkeyexists(student,"student3"));
				assertEquals('false',structkeyexists(student,"student4"));

			});

			it( title='Test case for structkeyexists member function',body=function( currentSpec ) {
				assertEquals('true',student.keyexists("student1"));
				assertEquals('true',student.keyexists("student2"));
				assertEquals('true',student.keyexists("student3"));
				assertEquals('false',student.keyexists("student4"));

			});
		});
	}
}