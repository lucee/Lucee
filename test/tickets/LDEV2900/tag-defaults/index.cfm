<cfif structKeyExists(url, "tagSameSite")>
    <cfcookie name="VALUE" value="LDEV2900" samesite="#url.tagSameSite#">
<cfelse>
    <cfcookie name="VALUE" value="LDEV2900">
</cfif>
