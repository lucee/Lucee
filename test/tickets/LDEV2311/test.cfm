<cfset path = #replace(expandpath('./ex.txt'),"\","/","all")#>

<cfquery name = "fileLoad" datasource = "LDEV2311">
	LOAD DATA LOCAL INFILE '#path#'
	INTO TABLE sampleTble COLUMNS TERMINATED BY '\t';
</cfquery>
<cfquery name="selectData" datasource="lDEV2311">
	select * from sampleTble
</cfquery>
<cfoutput>#selectdata.recordcount#</cfoutput>

