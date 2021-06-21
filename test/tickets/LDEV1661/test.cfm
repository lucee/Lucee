<cfoutput>
	<cfif FORM.Scene EQ 1>
		<!--- update timestamp using createDateTime() --->
		<cfquery name="updateRecord">
			UPDATE users
			SET myTimestamp = #createDateTime(2018, 08, 01, 12, 00, 00)#
			WHERE sNo = 345
		</cfquery>
		<cfquery name="selectRecord">
			SELECT * FROM users
			WHERE sNo = 345
		</cfquery>
		<!--- <cfquery name="chkConfig">
			SELECT @@global.time_zone, @@session.time_zone
		</cfquery>
		#systemOutput(chkConfig, true)# --->
		#selectRecord.myTimestamp#
	<cfelseif FORM.Scene EQ 2>
		<!--- update timestamp using createOdbcDateTime() --->
		<cfquery name="updateRecord">
			UPDATE users
			SET myTimestamp = #createOdbcDateTime(createDateTime(2018, 08, 01, 12, 00, 00))#
			WHERE sNo = 345
		</cfquery>
		<cfquery name="selectRecord">
			SELECT * FROM users
			WHERE sNo = 345
		</cfquery>
		#selectRecord.myTimestamp#
	<cfelseif FORM.Scene EQ 3>
		<!--- update timestamp using a string --->
		<cfquery name="updateRecord">
			UPDATE users
			SET myTimestamp = '2018-08-01 12:00:00'
			WHERE sNo = 345
		</cfquery>
		<cfquery name="selectRecord">
			SELECT * FROM users
			WHERE sNo = 345
		</cfquery>
		#selectRecord.myTimestamp#
	<cfelseif FORM.Scene EQ 4>
		<!--- update timestamp using cfqueryparam --->
		<cfquery name="updateRecord">
			UPDATE users
			SET myTimestamp = <cfqueryparam cfsqltype="CF_SQL_TIMESTAMP" value="#createDateTime(2018, 08, 01, 12, 00, 00)#" />
			WHERE sNo = 345
		</cfquery>
		<cfquery name="selectRecord">
			SELECT * FROM users
			WHERE sNo = 345
		</cfquery>
		#selectRecord.myTimestamp#
	</cfif>
</cfoutput>
