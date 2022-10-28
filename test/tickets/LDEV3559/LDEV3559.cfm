<cfquery name="result">
    SELECT CAST(1 AS DECIMAL(9, 1)) AS Height FROM LDEV3559
</cfquery>
<cfoutput>#arrayToList(queryColumnData(result,"Height"))#</cfoutput>
