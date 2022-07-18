<cfparam name="url.mode">
<cfif url.mode eq "named">
    <cfdocument format="pdf" name="pdf_test">
        <html style="height: 99%">
        <body style="font-family: Helvetica, Arial, sans-serif; height: 90%">
            <div>hello</div>
        </body>
        </html>
    </cfdocument>
    <cfcontent type="application/pdf" variable="#pdf_test#">
<cfelseif url.mode eq "direct">
    <cfdocument format="pdf">
        <html style="height: 99%">
        <body style="font-family: Helvetica, Arial, sans-serif; height: 90%">
            <div>hello</div>
        </body>
        </html>
    </cfdocument>
<cfelse>
    <cfthrow message="unknown mode: #url.mode#">
</cfif>