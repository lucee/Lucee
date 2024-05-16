component extends="org.lucee.cfml.test.LuceeTestCase" {
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
	function run( testResults , testBox ) {
		describe( title = "Test suite for structfindkey", body = function() {

			it( title = 'Test case for structfindkey function',body = function( currentSpec ) {
				assertEquals('[{"path":".STUDENT1.ID","owner":{"NAME":"joe","ID":1},"value":1},{"path":".STUDENT2.ID","owner":{"NAME":"root","ID":2},"value":2},{"path":".STUDENT3.ID","owner":{"NAME":"jack","ID":3},"value":3}]',serialize(structfindkey(student,"id","all")));
				assertEquals('[{"path":".STUDENT1.ID","owner":{"NAME":"joe","ID":1},"value":1}]',serialize(structfindkey(student,"id")));
			});

			it( title = 'Test case for structfindkey member function',body = function( currentSpec ) {
				assertEquals('[{"path":".STUDENT1.ID","owner":{"NAME":"joe","ID":1},"value":1},{"path":".STUDENT2.ID","owner":{"NAME":"root","ID":2},"value":2},{"path":".STUDENT3.ID","owner":{"NAME":"jack","ID":3},"value":3}]',serialize(student.findkey("id","all")));
				assertEquals('[{"path":".STUDENT1.ID","owner":{"NAME":"joe","ID":1},"value":1}]',serialize(student.findkey("id")));
			});
		});

	}
}