<cfscript>
transaction isolation='read_uncommitted' {
	arr = entityLoad("Code", {code = "a"});
}


if(arr.len() eq 0) {
	query {
		echo("insert into Code (ID, code)
		values (1, 'a')");
	}
}
arr = entityLoad("Code", {code = "a"});
ormExecuteQuery("FROM Ref WHERE code IN (:codes)", {codes = arr});
</cfscript>




