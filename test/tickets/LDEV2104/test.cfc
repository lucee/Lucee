Component 
{
	remote any function testFun() returnformat="json"{
			users = queryNew( "firstname,lastname", "varchar,varchar", [{"firstname":"Lucee","lastname":"Server"},{"firstname":"Test ","lastname":"Case"}] );
			return users;
	}
}