<cftry>
  <cfquery name="test" datasource="test_dsn_new">
    select * from test_table
  </cfquery>
  <cfoutput>#test.name#</cfoutput>
  <cfcatch type="any">
    <cfoutput>#cfcatch.message#</cfoutput>
  </cfcatch>
</cftry>