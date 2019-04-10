<cfset catchCount = 0 >
<cftry>
	<cfthrow message="Here's an error thrown once" />
	<cfcatch>
		<cfset catchCount++>
		<cftry>
			<cftry>
				<cftry>
					<cftry>
						<cfcatch>
							<cfset catchCount++>
						</cfcatch>
					</cftry>
					<cfcatch>
						<cfset catchCount++>
					</cfcatch>
				</cftry>
				<cfcatch>
					<cfset catchCount++>
				</cfcatch>
			</cftry>
			<cfcatch>
				<cfset catchCount++>
			</cfcatch>
		</cftry>
	</cfcatch>
</cftry>
<cfadmin action="getDebugData" returnvariable="res"/>
<cfoutput>#arrayLen(res.exceptions)#</cfoutput>