<cfif (thisTag.executionMode EQ "end") OR !thisTag.hasEndTag>
<cfinclude template="adminAIStore.cfm">
<cfinclude template="adminAINavigation.cfm">
<cfscript>
	param name="attributes.systemMessage" default="";
	param name="attributes.pageAction" default="#structKeyExists(url, 'action') ? url.action : ''#";
	param name="attributes.pageTitle" default="#structKeyExists(request, 'title') ? request.title : ''#";
	param name="attributes.allowModify" default="false";
	param name="attributes.formName" default="";
	param name="attributes.prefixContent" default="";

	aiLocationMarker = "{AI-location}";
	rawContent = trim(attributes.systemMessage);
	if (thisTag.hasEndTag && len(trim(thistag.generatedContent))) {
		rawContent = thistag.generatedContent;
	}

	prefixContent = trim(attributes.prefixContent);
	if (!len(prefixContent) && structKeyExists(variables, "caller") && structKeyExists(caller, "adminAIPrefix")) {
		prefixContent = trim(caller.adminAIPrefix);
	}
	if (len(prefixContent)) {
		rawContent = prefixContent & rawContent;
	}

	adminAIAvailable = false;
	try {
		adminAIAvailable = AIHas("default:administrator");
	}
	catch (any e) {
		adminAIAvailable = false;
	}

	contentBefore = "";
	contentAfter = rawContent;
	showBar = false;

	function adminAIFindPageIntroEnd(required string raw) {
		var searchFrom = 1;
		while (true) {
			var classPos = findNoCase("pageintro", arguments.raw, searchFrom);
			if (!classPos) {
				return 0;
			}

			var divStart = 0;
			var probePos = findNoCase("<div", arguments.raw, max(1, classPos - 200));
			while (probePos && probePos <= classPos) {
				divStart = probePos;
				probePos = findNoCase("<div", arguments.raw, probePos + 1);
			}

			if (divStart) {
				var tagEnd = find(">", arguments.raw, classPos);
				if (tagEnd) {
					var closePos = findNoCase("</div>", arguments.raw, tagEnd);
					if (closePos) {
						return closePos + len("</div>");
					}
				}
			}

			searchFrom = classPos + 1;
		}
	}

	function adminAISplitContent(required string raw, required string marker) {
		var markerPos = find(arguments.marker, arguments.raw);
		if (markerPos) {
			return {
				"before": mid(arguments.raw, 1, markerPos - 1),
				"after": mid(arguments.raw, markerPos + len(arguments.marker))
			};
		}

		var introEnd = adminAIFindPageIntroEnd(arguments.raw);
		if (introEnd) {
			return {
				"before": left(arguments.raw, introEnd),
				"after": mid(arguments.raw, introEnd + 1)
			};
		}

		return {
			"before": "",
			"after": arguments.raw
		};
	}

	function adminAIStripTagBlocks(required string raw, required string tagName) {
		var tag = lCase(arguments.tagName);
		var pattern = "(?is)<" & tag & "[^>]*>.*?</" & tag & ">";
		return reReplace(arguments.raw, pattern, " ", "all");
	}

	function adminAICleanPageContext(required string raw) {
		var t = trim(arguments.raw);
		if (!len(t)) return "";
		t = adminAIStripTagBlocks(t, "script");
		t = adminAIStripTagBlocks(t, "style");
		t = reReplace(t, "<[^>]+>", " ", "all");
		t = replace(t, "&nbsp;", " ", "all");
		t = replace(t, "&amp;", "&", "all");
		t = replace(t, "&lt;", "<", "all");
		t = replace(t, "&gt;", ">", "all");
		t = replace(t, "&quot;", '"', "all");
		t = reReplace(t, "[ \t]+", " ", "all");
		t = reReplace(t, "(\r?\n\s*){2,}", chr(10), "all");
		return trim(t);
	}

	function adminAIExtractNavigationIndex(required string raw) {
		var marker = "<!-- admin-ai-navigation";
		var startPos = findNoCase(marker, arguments.raw);
		if (!startPos) {
			return [];
		}
		var jsonStart = find(">", arguments.raw, startPos);
		if (!jsonStart) {
			return [];
		}
		jsonStart++;
		var endPos = find("-->", arguments.raw, jsonStart);
		if (!endPos) {
			return [];
		}
		var jsonText = trim(mid(arguments.raw, jsonStart, endPos - jsonStart));
		if (!len(jsonText)) {
			return [];
		}
		try {
			var data = deserializeJSON(jsonText);
			return isArray(data) ? data : [];
		}
		catch (any e) {
			return [];
		}
	}

	function adminAIRemoveNavigationBlock(required string raw) {
		return trim(reReplace(arguments.raw, "(?is)<!--\s*admin-ai-navigation[^>]*>.*?-->", "", "all"));
	}

	function adminAIBuildSystemMessage(
		required string pageAction,
		required string pageContext,
		required boolean allowModify,
		required string adminType,
		string pageTitle="",
		string luceeVersion="",
		array navIndex=[]
	) {
		var hasNavIndex = isArray(arguments.navIndex) && arrayLen(arguments.navIndex) > 0;
		var msg = "You are Lucy Miller, the Lucee #ucFirst(arguments.adminType)# Administrator assistant. "
			& "You may refer to yourself as Lucy. ";

		if (hasNavIndex) {
			msg &= "You are on the administrator overview page. This page is a dashboard only — configuration is done on sub-pages listed below. "
				& "When the user asks how to change a setting, always direct them to the matching sub-page with an HTML link using the url and title from the index. "
				& "Never say a setting is unavailable or not on this page. Never say there is no region, locale, or country setting. "
				& "Do not suggest changing fields on this overview page. Do not use the Web contexts label table for locale, timezone, or regional settings. "
				& "For locale, timezone, regional, or country settings (including Germany): always link to Regional - Settings (action server.regional). "
				& "Do not invent admin pages that are not listed in the navigation index. ";
		} else {
			msg &= "You help operators understand and configure the administrator page described below. "
				& "Only discuss settings, fields, and actions that appear on this page. "
				& "Do not invent options or fields. ";
		}

		msg &= "Answer clearly in plain HTML only. Never use markdown syntax for headings, bold, or lists. "
			& "Keep answers short and precise. Use h3 as the largest heading. "
			& "Use <code> for inline values and <code class=""lucee-ml""> for multi-line examples. "
			& "Never ask for or repeat passwords, API keys, or other secrets.";

		if (arguments.allowModify && !hasNavIndex) {
			msg &= " The user may ask you to suggest configuration changes. "
				& "Explain briefly in HTML, then append a machine-readable block exactly in this form: "
				& "<!-- susi-actions --> then a JSON object with form field names as keys and recommended values, "
				& "then <!-- /susi-actions -->. "
				& "Example: <!-- susi-actions -->{""locale"":""de_DE"",""timezone"":""Europe/Berlin""}<!-- /susi-actions -->. "
				& "Use exact HTML form field names and select option values from the page, not display labels. "
				& "Only include fields you recommend changing. Never include password or secret fields. "
				& "Never include the name field in susi-actions; connection names are fixed on edit pages and must not contain URL hash values. "
				& "If current form values are provided with the question, use them as the starting point. "
				& "The user must click Apply on your answer and then Update on the page to save changes. "
				& "The passthroughJson textarea holds extra AI provider API parameters as a JSON object; it is separate from the named driver fields above it. "
				& "Lucee Skills (https://docs.lucee.org/lucee.skill) are documentation for external AI assistants and are not configured through administrator forms.";
		}

		msg &= chr(10) & chr(10)
			& "Admin action: #arguments.pageAction#";

		if (len(trim(arguments.luceeVersion))) {
			msg &= chr(10) & "Lucee version: #trim(arguments.luceeVersion)#";
		}

		if (len(trim(arguments.pageTitle))) {
			msg &= chr(10) & "Page title: #trim(arguments.pageTitle)#";
		}

		if (hasNavIndex) {
			msg &= chr(10) & chr(10) & "Common routes (use these first):";
			msg &= chr(10) & "- Locale, timezone, regional, country (e.g. Germany): Regional - Settings | server.regional";
			msg &= chr(10) & "- Charset: Charset - Settings | server.charset";
			msg &= chr(10) & "- Scope, session, application: Scope - Settings | server.scope";
			msg &= chr(10) & "- Datasource: Datasource - Services | services.datasource";
			msg &= chr(10) & chr(10) & "Full administrator navigation index:";
			for (var navItem in arguments.navIndex) {
				msg &= chr(10) & "- #navItem.title#"
					& " | action: #navItem.action#"
					& " | url: #navItem.url#";
				if (len(trim(navItem.description ?: ""))) {
					msg &= chr(10) & "  #trim(navItem.description)#";
				}
			}
			msg &= chr(10) & chr(10) & "Overview page summary:" & chr(10) & arguments.pageContext;
		} else {
			msg &= chr(10) & chr(10) & "Page description:" & chr(10) & arguments.pageContext;
		}
		return msg;
	}

	function adminAIExtractOverviewContext(required string raw) {
		var introEnd = adminAIFindPageIntroEnd(arguments.raw);
		if (introEnd) {
			return adminAICleanPageContext(left(arguments.raw, introEnd));
		}
		return adminAICleanPageContext(arguments.raw);
	}

	function adminAIGetHistory(required string stored) {
		try {
			var data = isStruct(arguments.stored) ? arguments.stored : deserializeJSON(arguments.stored);
			if (structKeyExists(data, "history") && isArray(data.history)) {
				var history = duplicate(data.history);
				for (var i = 1; i <= arrayLen(history); i++) {
					if (structKeyExists(history[i], "question")) {
						history[i].question = adminAIFormatQuestionForDisplay(history[i].question);
					}
					if (structKeyExists(history[i], "answer")) {
						history[i].answer = adminAIFormatAnswerForDisplay(history[i].answer);
					}
				}
				return history;
			}
		}
		catch (any e) {
		}
		return [];
	}

	if (adminAIAvailable) {
	splitContent = adminAISplitContent(rawContent, aiLocationMarker);
	contentBefore = splitContent.before;
	contentAfter = splitContent.after;

	navIndex = [];
	if (trim(attributes.pageAction) == "overview" && structKeyExists(request, "adminAINavIndex") && isArray(request.adminAINavIndex)) {
		navIndex = request.adminAINavIndex;
	}
	if (!arrayLen(navIndex)) {
		navIndex = adminAIExtractNavigationIndex(rawContent);
	}
	rawContent = adminAIRemoveNavigationBlock(rawContent);

	if (arrayLen(navIndex)) {
		pageContext = adminAIExtractOverviewContext(replace(rawContent, aiLocationMarker, " ", "all"));
	} else {
		pageContext = adminAICleanPageContext(replace(rawContent, aiLocationMarker, " ", "all"));
	}

	if (!structKeyExists(request, "adminAI")) {
		request.adminAI = {};
	}

	request.adminAI.pageAction = trim(attributes.pageAction);
	request.adminAI.pageTitle = trim(attributes.pageTitle);
	request.adminAI.allowModify = attributes.allowModify;
	request.adminAI.formName = trim(attributes.formName);
	request.adminAI.pageContext = pageContext;
	request.adminAI.systemMessage = adminAIBuildSystemMessage(
		request.adminAI.pageAction,
		pageContext,
		request.adminAI.allowModify,
		request.adminType,
		request.adminAI.pageTitle,
		server.lucee.version,
		navIndex
	);
	request.adminAI.pageHash = hash(request.adminType & "|" & cgi.script_name & "|" & cgi.query_string, "SHA-256");

	request.adminAI.enabled = false;
	if (len(pageContext) || arrayLen(navIndex)) {
		try {
			request.adminAI.enabled = AIHas("default:administrator");
		}
		catch (any e) {
			request.adminAI.enabled = false;
		}
	}

	if (request.adminAI.enabled) {
		pageStore = adminAIStoreGet(request.adminAI.pageHash);
		stored = structKeyExists(pageStore, "serialized") ? trim(pageStore.serialized ?: "") : "";

		try {
			sessionLoad = adminAILoadSession(stored, request.adminAI.systemMessage);
			ais = sessionLoad.ais;
			stored = sessionLoad.serialized;
		}
		catch (any e) {
			stored = "";
			adminAIStoreDelete(request.adminAI.pageHash);
			ais = createAISession("default:administrator", request.adminAI.systemMessage, 8, 0.3);
			stored = SerializeAISession(ais);
		}

		label = structKeyExists(pageStore, "label") ? (pageStore.label ?: "") : "";
		if (!len(label)) {
			try {
				meta = AIGetMetaData(ais);
				label = (meta.label ?: "") & " (" & (meta.model ?: "") & ")";
			}
			catch (any e) {
				label = "";
			}
		}

		adminAIStoreSet(request.adminAI.pageHash, {
			"serialized": stored,
			"label": label,
			"systemMessage": request.adminAI.systemMessage
		});
		request.adminAI.chatHistory = adminAIGetHistory(stored);
	}

	showBar = request.adminAI.enabled && !structKeyExists(request.adminAI, "rendered");
	if (showBar) {
		request.adminAI.rendered = true;
		request.adminAI.chatHistoryJson = serializeJSON(request.adminAI.chatHistory ?: []);
		request.adminAI.chatHistoryJson = replace(request.adminAI.chatHistoryJson, "</", "<\/", "all");
	}
	} else {
		if (!structKeyExists(request, "adminAI")) {
			request.adminAI = {};
		}
		request.adminAI.enabled = false;
	}

	if (thisTag.hasEndTag) {
		thisTag.generatedContent = "";
	}
</cfscript>
<cfoutput>
<cfif showBar>
<script type="application/json" id="admin-ai-history-#request.adminAI.pageHash#">#request.adminAI.chatHistoryJson#</script>
<div class="admin-ai-wrap">
<div class="admin-ai-box" id="admin-ai-box"
	data-session-key="#encodeForHTMLAttribute(request.adminAI.pageHash)#"
	data-history-id="admin-ai-history-#encodeForHTMLAttribute(request.adminAI.pageHash)#"
	data-allow-modify="#request.adminAI.allowModify ? 'true' : 'false'#"
	data-form-name="#encodeForHTMLAttribute(request.adminAI.formName)#"
	data-ask-url="#encodeForHTMLAttribute(request.self)#?action=admin.ai"
	tabindex="0"
	aria-label="AI assistant">
	<div class="admin-ai-box-summary" aria-hidden="true">
		<span class="admin-ai-stars" aria-hidden="true">
			<span class="admin-ai-star admin-ai-star-1">✦</span>
			<span class="admin-ai-star admin-ai-star-2">✦</span>
			<span class="admin-ai-star admin-ai-star-3">✦</span>
		</span>
		<span class="admin-ai-label">AI</span>
	</div>
	<div class="admin-ai-box-panel">
		<div class="admin-ai-transcript"></div>
		<form class="admin-ai-form" onsubmit="return false;">
			<input type="text" class="admin-ai-input" name="question" autocomplete="off"
				placeholder="Ask about this page or suggest changes…" />
			<input type="button" class="admin-ai-submit button" value="Ask" />
			<input type="button" class="admin-ai-clear button" value="Clear" title="Clear chat" />
		</form>
	</div>
</div>
</div>
</cfif>
<cfif thisTag.hasEndTag>#contentBefore##contentAfter#</cfif>
</cfoutput>
</cfif>
