<cfoutput>
	<cfset myQry = queryNew( 'id,title,name','integer,varChar,varChar',
						[	{"id":1, "title":"sas", "name":"sample"},
							{"id":2, "title":"sas", "name":"lucee"},
							{"id":3, "title":"arg", "name":"test"},
							{"id":4, "title":"arg", "name":"case"},
							{"id":5, "title":"arg", "name":"result"} ]) />

	<cfset result = "">
	<cfloop query="#myQRY#" group="title" >
		<cfif myQRY.title eq 'sas'>
			<cfcontinue />
		</cfif>
		<cfset result = result & "#myQRY.title#" >
	</cfloop>
	#result#
</cfoutput>