<cfcomponent displayname="Application" hint="Handle the application." output="no">
<cfsilent>
		<cfscript>
			request.appName = "App1";
			this.name = request.appName;
			this.applicationTimeout = createTimeSpan( 0, 0, 20, 0 );
			this.clientManagement = true;
			this.setClientCookies = false;
			this.sessionManagement = true;
			this.setDomainCookies = false;
			this.SessionCluster = false;
		</cfscript>
	</cfsilent>
<cfscript>

	public function onRequestStart() {
		setting requesttimeout=10;
	}
</cfscript>
</cfcomponent>