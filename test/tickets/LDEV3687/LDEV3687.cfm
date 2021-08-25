<cfparam name="form.scene" default="1">
<cfif form.scene eq 1>

	<cftry>
		<cfmail from="aaa@bb.com" to="xxx@yyy.com" subject="sample" server="localhost" cc="cc:81@gmail.com,cc:81@gmail.com,">dummy email</cfmail>
		<cfoutput>success</cfoutput>
		<cfcatch>
			<cfoutput>#cfcatch.message#</cfoutput>
		</cfcatch>
	</cftry>
<cfelseif form.scene eq 2>
	<cftry>
		<cfmail from="test, mail<aaa@bb.com>" to="xxx@yyy.com" subject="sample" server="localhost" cc="cc:81@gmail.com,bcc:81@gmail.com">dummy email</cfmail>
		<cfoutput>success</cfoutput>
		<cfcatch>
			<cfoutput>#cfcatch.message#</cfoutput>
		</cfcatch>
	</cftry>
<cfelseif form.scene eq 3>
	<cftry>
		<cfmail from="mail<aaa@bb.com>" to="xxx@yyy.com,xxx@yyy.com," subject="sample" server="localhost" cc="cc:81@gmail.com,bcc:81@gmail.com">dummy email</cfmail>
		<cfoutput>success</cfoutput>
		<cfcatch>
			<cfoutput>#cfcatch.message#</cfoutput>
		</cfcatch>
	</cftry>
<cfelseif form.scene eq 4>
	<cftry>
		<cfmail from="mail<aaa@bb.com>" to="xxx@yyy.com,xxx@yyy.com" subject="sample" server="localhost" bcc="cc:81@gmail.com,bcc:81@gmail.com,">dummy email</cfmail>
		<cfoutput>success</cfoutput>
		<cfcatch>
			<cfoutput>#cfcatch.message#</cfoutput>
		</cfcatch>
	</cftry>
</cfif>

