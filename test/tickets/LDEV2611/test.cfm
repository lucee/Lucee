<cfsavecontent variable="mytext">.... any content</cfsavecontent>

<cfoutput>#myText#</cfoutput>
  
  <cfoutput><cfset myText="<html><body>"&mytext&'</body></html>'>
  <cfdocument format="PDF" filename="outputfile.pdf" orientation="portrait" overwrite="yes" pageType="A4" unit="cm" marginleft="1" marginright="1">
  #myText#
  </cfdocument>

  <cfif fileExists("outputfile.pdf")><cfset fileDelete("outputfile.pdf")><cfelse>file not exists!</cfif>
</cfoutput>