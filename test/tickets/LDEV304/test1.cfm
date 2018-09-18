<cfquery name="qryResult" result="tagResult">
	IF OBJECT_ID('tempdb..##temp3') IS NOT NULL
    DROP TABLE ##temp3
    
	CREATE TABLE ##temp3 (
		col1 uniqueidentifier default newid() primary key,
		col2 varchar(max) )

    INSERT [##temp3] (col2)
    VALUES ('')

    SELECT 'test'
</cfquery>	

<cftry>
	<cfoutput>#qryResult.COMPUTED_COLUMN_1#</cfoutput>
	<cfcatch>
		<cfoutput>#cfcatch.message#</cfoutput>
	</cfcatch>
</cftry>

