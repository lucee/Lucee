component	{
	function before(caller,args) {
		args.sql="SELECT TABLE_NAME as abc FROM INFORMATION_SCHEMA.TABLES"; // change SQL
		args.maxrows=2;
		return arguments;
	}
	function after(caller,args,result,meta) {
		var row=queryAddRow(result);
		result.setCell("abc","123",row);
		return arguments;
	}
}