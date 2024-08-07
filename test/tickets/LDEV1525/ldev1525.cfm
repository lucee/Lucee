<cfparam name="url.scene">
<cfparam name="url.orderby" default="false">
<cfset q=QueryNew("id,name","Integer,VarChar",[[8,'Micha'],[55,'lucee'],[55,'ACF']])>
<cftry>
	<cfif url.scene eq "native">
		<cfquery name="qoq" dbtype="query">
			SELECT 	count(*),id
			FROM 	q
			group 	by id
			<cfif url.orderby>
				order by name
			</cfif>
		</cfquery>
	<cfelse>
		<cfquery name="qoq" dbtype="query">
			SELECT 	count(*), q1.id
			FROM 	q q1, q q2
			WHERE   q1.id = q2.id
			group 	by q1.id
			<cfif url.orderby>
				order by q1.name
			</cfif>
		</cfquery>
	</cfif>
	<cfscript>
		echo(qoq.toJson());
	</cfscript>
	<cfcatch>
		<cfscript>
			echo(cfcatch.stacktrace);
		</cfscript>
	</cfcatch>
</cftry>
