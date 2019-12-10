<cfparam name="form.scene" default="1">
<cfif form.scene eq 1>
	<cfquery name="update" datasource="LDEV2604_MSSQL">
		SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED
	</cfquery>
	<cfquery name="get" datasource="LDEV2604_MSSQL">
		SELECT session_id, CASE transaction_isolation_level
		WHEN 0 THEN 'Unspecified'
		WHEN 1 THEN 'ReadUncommitted'
		WHEN 2 THEN 'ReadCommitted'
		WHEN 3 THEN 'Repeatable'
		WHEN 4 THEN 'Serializable'
		WHEN 5 THEN 'Snapshot' END AS TRANSACTION_ISOLATION_LEVEL
		FROM sys.dm_exec_sessions
		WHERE session_id = @@SPID;
	</cfquery>
	<cfquery name="revert" datasource="LDEV2604_MSSQL">
		SET TRANSACTION ISOLATION LEVEL READ COMMITTED;
	</cfquery>
	<cfoutput>#get.TRANSACTION_ISOLATION_LEVEL#</cfoutput>

<cfelseif form.scene eq 2>
	<cftransaction action="begin" isolation="read_uncommitted">
		<cfquery name="update" datasource="LDEV2604_MSSQL">
			SELECT 1
		</cfquery>
	</cftransaction>
	<cfquery name="get" datasource="LDEV2604_MSSQL">
		SELECT session_id, CASE transaction_isolation_level
		WHEN 0 THEN 'Unspecified'
		WHEN 1 THEN 'ReadUncommitted'
		WHEN 2 THEN 'ReadCommitted'
		WHEN 3 THEN 'Repeatable'
		WHEN 4 THEN 'Serializable'
		WHEN 5 THEN 'Snapshot' END AS TRANSACTION_ISOLATION_LEVEL
		FROM sys.dm_exec_sessions
		WHERE session_id = @@SPID;
	</cfquery>
	<cfquery name="revert" datasource="LDEV2604_MSSQL">
		SET TRANSACTION ISOLATION LEVEL READ COMMITTED;
	</cfquery>
	<cfoutput>#get.TRANSACTION_ISOLATION_LEVEL#</cfoutput>

<cfelseif form.scene eq 3>
	<cfquery name="update" datasource="LDEV2604_MYSQL">
		set transaction isolation level READ UNCOMMITTED
	</cfquery>
	<cfquery name="get_session" datasource="LDEV2604_MYSQL">
		SELECT @@tx_isolation AS isolation;
	</cfquery>
	<cfquery name="get_global" datasource="LDEV2604_MYSQL">
		SELECT @@global.tx_isolation AS isolation;
	</cfquery>
	<cfquery name="revert_session" datasource="LDEV2604_MYSQL">
		set SESSION transaction isolation level READ COMMITTED;
	</cfquery>
	<cfquery name="revert_global" datasource="LDEV2604_MYSQL">
		set GLOBAL transaction isolation level READ COMMITTED;
	</cfquery>
	<cfoutput>#get_session.isolation NEQ 'READ-UNCOMMITTED' AND get_global.isolation NEQ 'READ-UNCOMMITTED'#</cfoutput>

<cfelseif form.scene eq 4>
	<cfquery name="update" datasource="LDEV2604_MYSQL">
		set SESSION transaction isolation level READ UNCOMMITTED
	</cfquery>
	<cfquery name="get_session" datasource="LDEV2604_MYSQL">
		SELECT @@tx_isolation AS isolation;
	</cfquery>
	<cfquery name="get_global" datasource="LDEV2604_MYSQL">
		SELECT @@global.tx_isolation AS isolation;
	</cfquery>
	<cfquery name="revert_session" datasource="LDEV2604_MYSQL">
		set SESSION transaction isolation level READ COMMITTED;
	</cfquery>
	<cfquery name="revert_global" datasource="LDEV2604_MYSQL">
		set GLOBAL transaction isolation level READ COMMITTED;
	</cfquery>
	<cfoutput>#get_session.isolation EQ 'READ-UNCOMMITTED' AND get_global.isolation NEQ 'READ-UNCOMMITTED'#</cfoutput>

<cfelseif form.scene eq 5>
	<cfquery name="update" datasource="LDEV2604_MYSQL">
		set GLOBAL transaction isolation level READ UNCOMMITTED
	</cfquery>
	<cfquery name="get_session" datasource="LDEV2604_MYSQL">
		SELECT @@tx_isolation AS isolation;
	</cfquery>
	<cfquery name="get_global" datasource="LDEV2604_MYSQL">
		SELECT @@global.tx_isolation AS isolation;
	</cfquery>
	<cfquery name="revert_session" datasource="LDEV2604_MYSQL">
		set SESSION transaction isolation level READ COMMITTED;
	</cfquery>
	<cfquery name="revert_global" datasource="LDEV2604_MYSQL">
		set GLOBAL transaction isolation level READ COMMITTED;
	</cfquery>
	<cfoutput>#get_session.isolation NEQ 'READ-UNCOMMITTED' AND get_global.isolation EQ 'READ-UNCOMMITTED'#</cfoutput>
</cfif>