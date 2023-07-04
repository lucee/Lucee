<cfquery>
	INSERT INTO LDEV4337 (amount) VALUES(<cfqueryparam cfsqltype="CF_SQL_DECIMAL" value="27.178" scale="2">);
</cfquery>

<cfquery name="result">
	SELECT * FROM LDEV4337
</cfquery>

<cfoutput>#arrayToList(queryColumnData(result,"amount"))#</cfoutput>