<cfif (thisTag.executionMode EQ "end") OR !thisTag.hasEndTag>
<cfinclude template="adminAIStore.cfm">
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
				"before": left(arguments.raw, markerPos - 1),
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

	splitContent = adminAISplitContent(rawContent, aiLocationMarker);
	contentBefore = splitContent.before;
	contentAfter = splitContent.after;

	function adminAIStripTagBlocks(required string raw, required string tagName) {
		var t = arguments.raw;
		var openNeedle = "<" & lCase(arguments.tagName);
		var closeTag = "</" & lCase(arguments.tagName) & ">";
		var openPos = findNoCase(openNeedle, t);

		while (openPos) {
			var closePos = findNoCase(closeTag, t, openPos);
			if (!closePos) {
				break;
			}
			t = left(t, openPos - 1) & " " & mid(t, closePos + len(closeTag));
			openPos = findNoCase(openNeedle, t, openPos);
		}

		return t;
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

	function adminAIBuildSystemMessage(
		required string pageAction,
		required string pageContext,
		required boolean allowModify,
		required string adminType,
		string pageTitle="",
		string luceeVersion=""
	) {
		var msg = "You are Susi Sorglos, the Lucee #ucFirst(arguments.adminType)# Administrator assistant. "
			& "You may refer to yourself as Susi. "
			& "You help operators understand and configure the administrator page described below. "
			& "Answer clearly in plain HTML. Keep answers short and precise. Use h3 as the largest heading. "
			& "Use <code> for inline values and <code class=""lucee-ml""> for multi-line examples. "
			& "Only discuss settings, fields, and actions that appear on this page. "
			& "Do not invent options or fields. Never ask for or repeat passwords, API keys, or other secrets.";

		if (arguments.allowModify) {
			msg &= " The user may ask you to suggest configuration changes. "
				& "Explain briefly in HTML, then append a machine-readable block exactly in this form: "
				& "<!-- susi-actions --> then a JSON object with form field names as keys and recommended values, "
				& "then <!-- /susi-actions -->. "
				& "Example: <!-- susi-actions -->{""locale"":""de_DE"",""timezone"":""Europe/Berlin""}<!-- /susi-actions -->. "
				& "Use exact HTML form field names and select option values from the page, not display labels. "
				& "Only include fields you recommend changing. Never include password or secret fields. "
				& "If current form values are provided with the question, use them as the starting point. "
				& "The user must click Apply on your answer and then Update on the page to save changes.";
		}

		msg &= chr(10) & chr(10)
			& "Admin action: #arguments.pageAction#";

		if (len(trim(arguments.luceeVersion))) {
			msg &= chr(10) & "Lucee version: #trim(arguments.luceeVersion)#";
		}

		if (len(trim(arguments.pageTitle))) {
			msg &= chr(10) & "Page title: #trim(arguments.pageTitle)#";
		}

		msg &= chr(10) & chr(10) & "Page description:" & chr(10) & arguments.pageContext;
		return msg;
	}

	function adminAIGetHistory(required string stored) {
		try {
			var data = isStruct(arguments.stored) ? arguments.stored : deserializeJSON(arguments.stored);
			if (structKeyExists(data, "history") && isArray(data.history)) {
				return data.history;
			}
		}
		catch (any e) {
		}
		return [];
	}

	pageContext = adminAICleanPageContext(replace(rawContent, aiLocationMarker, " ", "all"));

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
		server.lucee.version
	);
	request.adminAI.pageHash = hash(request.adminType & "|" & cgi.script_name & "|" & cgi.query_string, "SHA-256");

	request.adminAI.enabled = false;
	if (len(pageContext)) {
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
		aisLoaded = false;

		if (len(stored)) {
			try {
				ais = LoadAISession("default:administrator", stored);
				aisLoaded = true;
			}
			catch (any e) {
				stored = "";
				adminAIStoreDelete(request.adminAI.pageHash);
			}
		}

		if (!aisLoaded) {
			ais = createAISession("default:administrator", request.adminAI.systemMessage, 8, 0.3);
			stored = SerializeAISession(ais);
		}

		label = structKeyExists(pageStore, "label") ? (pageStore.label ?: "") : "";
		if (!aisLoaded || !len(label)) {
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

	if (thisTag.hasEndTag) {
		thisTag.generatedContent = contentBefore;
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
	<div class="admin-ai-box-summary" aria-hidden="true">AI</div>
	<div class="admin-ai-box-panel">
		<button type="button" class="admin-ai-clear" title="Clear chat" aria-label="Clear chat">&times;</button>
		<div class="admin-ai-transcript"></div>
		<form class="admin-ai-form" onsubmit="return false;">
			<input type="text" class="admin-ai-input" name="question" autocomplete="off"
				placeholder="Ask about this page or suggest changes…" />
			<button type="button" class="admin-ai-submit button submit">Ask</button>
		</form>
	</div>
</div>
</div>
</cfif>
<cfif thisTag.hasEndTag>#contentAfter#</cfif>
</cfoutput>
</cfif>
