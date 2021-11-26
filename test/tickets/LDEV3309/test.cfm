<cfparam name="Form.scene" default="">
<cfparam name="url.cfset" default="">
<cf_cusTag>
<cfscript>
    if( form.scene == 1 ) writeoutput(url.cfset);
    else if( form.scene == 2) writeoutput(url.param);
    else if( form.scene == 3) writeoutput(url.setVariable);
</cfscript>