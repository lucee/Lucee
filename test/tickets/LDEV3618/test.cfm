<cfparam name="FORM.scene" default="">
<cfif form.scene == 1>
    <cfhtmlhead>Body-content without text attribute</cfhtmlhead>

<cfelseif form.scene == 2>
    <cfhtmlhead text="Text without body-content"></cfhtmlhead>

<cfelseif form.scene == 3>
    <cfhtmlhead text="Text-content,"> Body-content</cfhtmlhead>
</cfif>