<cfoutput>
	<cftry>
		<cfpop server = "127.0.0.1" username = "test1@mail.local" password="password" 
		action = "getHeaderOnly"  port="110"  secure="true" name = "messageBody" timeout="60"> 
		<cfcatch type="any">
			#cfcatch.detail#
		</cfcatch>
	</cftry>
</cfoutput>
