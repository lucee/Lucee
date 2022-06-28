<cfif form.scene eq 1> 
<cfsavecontent variable="spaceContent">
                
</cfsavecontent>
    
<cfelseif form.scene eq 2> 
<cfsavecontent variable="spaceContent">
                
</cfsavecontent>
    
<cfelseif form.scene eq 3> 
<cfsavecontent variable="spaceContent">
                
</cfsavecontent>
    
<cfelseif form.scene eq 4> 
<cfprocessingdirective suppressWhiteSpace = "true">
<cfsavecontent variable="spaceContent">
                
</cfsavecontent>
</cfprocessingdirective>
</cfif>
<cfoutput>#len(spaceContent)#</cfoutput>    