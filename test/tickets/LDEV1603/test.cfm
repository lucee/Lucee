<cflock type="exclusive" timeout="10" name="test">
</cflock>
<cfoutput>#IsDefined("cflock")#</cfoutput>
