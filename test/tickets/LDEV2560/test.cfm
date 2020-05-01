<cfparam name="form.scene" default="">
<cfif form.scene eq 1>
    <cfspreadsheet 
        action="read"  
        src = "#expandPath('./spreadsheet.xlsx')#" 
        excludeHeaderRow = "true"
        query = "myQuery"
        headerrow="2"
    >
    <cfoutput>#myquery.columnlist#</cfoutput>
</cfif>
<cfif form.scene eq 2>
    <cfspreadsheet
        action="read"
        src = "#expandPath('./spreadsheet.xlsx')#" 
        excludeHeaderRow = "true"
        query = "myQueryone"
        headerrow="1"
    >
    <cfoutput>#myQueryone.columnlist#</cfoutput>
</cfif>
