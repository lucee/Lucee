<cfparam name="FORM.scene" default="">
<cfif form.scene == 1>
    <cfhtmlhead>Body-content without text attribute</cfhtmlhead>

<cfelseif form.scene == 2>
    <cfhtmlhead text="Text without body-content"></cfhtmlhead>

<cfelseif form.scene == 3>
    <cfhtmlhead text="Text-content,"> Body-content</cfhtmlhead>
</cfif>

<cfif form.scene == 4>
    <cfhtmlbody>Body-content without text attribute</cfhtmlbody>

<cfelseif form.scene == 5>
    <cfhtmlbody text="Text without body-content"></cfhtmlbody>

<cfelseif form.scene == 6>
    <cfhtmlbody text="Text-content,"> Body-content</cfhtmlbody>
</cfif>