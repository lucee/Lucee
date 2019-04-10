component	{
	variables=x=1;
	function init(tbl) {
		variables.tbl=arguments.tbl;
	}
	function getTableName() {
		return variables.tbl;
	}

	function before(caller,args) {
		args.sql="insert into QueryTestAsync (id,i,dec) values('1',1,1.0)"; // change SQL
        //args.sql="select 1 as one"; // change SQL
        return arguments;
	}

	function after(caller,args,result,meta) {
		//args.sql="insert into TQuery(id,i,dec) values('3',1,1.0)"; // change SQL
		return arguments;
	}

}