<cffunction name="searchLang" returntype="array" output="no">
	<cfargument name="q" type="string" />
	<cfargument name="data" type="any" required="yes" />
	<cfargument name="prefix" type="string" required="no" default="" />
	<cfargument name="ret" type="array" required="no" default="#[]#" />

	<cfset var key = "" />
	<cfloop collection="#data#" item="key">
		<cfif not isSimpleValue(data[key])>
			<cfset searchLang(q, data[key], listappend(prefix, key, '.'), ret) />
		<cfelseif findNoCase(q, data[key])>
			<cfset arrayAppend(ret, prefix & "." & key) />
		</cfif>
	</cfloop>
	<cfreturn ret />
</cffunction>
<cfif structKeyExists(url, 'q')>
	<cfset url.q= replacelist(url.q,"<,>,"",","")>
</cfif>
<cfset luceeArchivePath = expandPath("{lucee-web}/context/lucee-admin.lar") />
<cfset luceeArchiveZipPath = "zip://" & luceeArchivePath & "!" />
<cfset dataDir = expandPath("{lucee-server}/searchdata") & server.separator.file />

<cfset current.label = stText.admin.search.label />
<cfoutput>
	<h2>#stText.admin.search.desc#</h2>
	
	
	<form method="get" action="#cgi.SCRIPT_NAME#">
		<input type="hidden" name="action" value="admin.search" />
		<input type="text" name="q" class="medium" size="50"<cfif structKeyExists(url, 'q')> value="#url.q#"</cfif> placeholder="#stText.buttons.search#" />
		<input type="submit" class="button submit" value="#stText.buttons.search#" />
	</form>
</cfoutput>
<cfif structKeyExists(url, 'q') and len(url.q)>
	<cfset variables.indexFile = '#dataDir#searchindex.cfm' />

	<!--- do initial or new indexing when a new Lucee version is detected --->
	<cfif not fileExists(variables.indexFile)
	or structKeyExists(url, "reindex")
	or fileRead('#dataDir#indexed-lucee-version.cfm') neq server.lucee.version & server.lucee['release-date']>
		<cfinclude template="admin.search.index.cfm" />
	</cfif>

	<cfset foundpages = {} />
	
	<!--- get the translations2actions data--->
	<cfset keys2action = evaluate(fileread(variables.indexFile)) />
	
	<!--- loop through translations--->
	<cfset data = application.stText[session.lucee_admin_lang] />
	<cfset foundkeys = searchLang(url.q, data) />
	
	<cfloop array="#foundkeys#" index="key">
		<cfif structKeyExists(keys2action, key)>
			<cfset pages = keys2action[key] />
			<cfloop collection="#pages#" item="page">
				<cfif not structKeyExists(foundpages, page)>
					<cfset foundpages[page] = pages[page] />
				<cfelse>
					<cfset foundpages[page] += pages[page] />
				</cfif>
			</cfloop>
		</cfif>
	</cfloop>
	
	<cfset q = queryNew("page,occ", "varchar,integer") />
	<cfloop collection="#foundpages#" item="page">
		<cfset queryAddRow(q) />
		<cfset querysetcell(q, "page", page) />
		<cfset querysetcell(q, "occ", foundpages[page]) />
	</cfloop>
	<cfquery name="q" dbtype="query">
		select page, occ
		FROM q
		ORDER BY occ DESC
	</cfquery>
	
	<cfset showchars = 300 />

	<cfoutput>
		<h2>#q.recordcount# result<cfif q.recordcount neq 1>s</cfif> found for "#HTMLEditFormat(url.q)#"</h2>
	</cfoutput>
	<cfoutput query="q">
		<cfset action = rereplace(q.page, '\.cfm$', '') />
		<!--- try to create friendly name for current page --->
		<cfif isDefined("variables.data.menu.#listfirst(action, '.')#.label") and isDefined("variables.data.menu.#action#")>
			<cfset pagename = variables.data.menu[listfirst(action, '.')].label & " - " & evaluate("variables.data.menu.#action#") />
		<cfelse>
			<cfset pagename = rereplace(replace(action, ".", " - "), '.', '\U\0') />
		</cfif>
		<h3><a href="#cgi.SCRIPT_NAME#?action=#action#">#pagename#</a></h3>
		<cfset tmp = fileRead('#dataDir##action#.#session.lucee_admin_lang#.txt', 'utf-8') />
		<cfset pos = find(url.q, tmp) />
		<cfset startpos = max(1, pos-showchars/2) />
		<cfif startpos gt 1>
			<cfset prevSpace = find(' ', reverse(left(tmp, startpos))) />
			<cfset startpos = startpos - prevSpace + 1 />
		</cfif>
		<div><em><cfif startpos gt 1>...</cfif>#replaceNoCase(rereplace(mid(tmp, startpos, showchars), '[a-zA-Z0-9]+$', ''), url.q, '<b>#url.q#</b>', 'all')#</em></div>
	</cfoutput>
	<div class="warning nofocus">
		This feature is currently in Beta State.
		If you have any problems while using this Implementation,
		please post the bugs and errors in our
		<a href="https://jira.jboss.org/jira/browse/Lucee" target="_blank">bugtracking system</a>. 
	</div>
</cfif>