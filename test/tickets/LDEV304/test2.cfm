<cfscript>
	q = new Query();
	q.setSQL("
	    DECLARE @tmp TABLE (
	        email nvarchar(50)
	    )
	    insert into @tmp (email) values ('test@test.com')
	    select 'test'
	");
	result = q.execute().getResult();

	try{
		writeOutput(result.COMPUTED_COLUMN_1);
	} catch(any e){
		writeOutput(e.message);
	}
</cfscript> 

