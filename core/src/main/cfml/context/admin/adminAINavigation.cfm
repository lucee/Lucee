<cfscript>
	function adminAINavToBool(required struct sct, required string key) {
		if (!structKeyExists(arguments.sct, arguments.key)) {
			return false;
		}
		return arguments.sct[arguments.key];
	}

	function adminAINavSanitizeText(required string text) {
		var t = trim(arguments.text ?: "");
		t = replace(t, "--", "-", "all");
		t = reReplace(t, "<[^>]+>", " ", "all");
		t = reReplace(t, "[ \t]+", " ", "all");
		return trim(t);
	}

	function adminAINavGetDescription(required string action, required struct stText, required string adminType) {
		var t = arguments.stText;
		var desc = "";

		switch (arguments.action) {
			case "overview":
				return adminAINavSanitizeText(t.Overview.introdesc[arguments.adminType] ?: "");
			case "info.bundle":
				return adminAINavSanitizeText(structKeyExists(t, "bundles") ? (t.bundles.introText ?: "") : "");
			case "server.cache":
				return adminAINavSanitizeText(t.setting.cachedesc ?: "");
			case "server.compiler":
				return adminAINavSanitizeText(t.setting.compiler ?: "");
			case "server.security":
				return adminAINavSanitizeText(t.security.desc ?: "");
			case "server.regional":
				return adminAINavSanitizeText(t.regional[arguments.adminType == "server" ? "server" : "web"] ?: "");
			case "server.charset":
				return adminAINavSanitizeText(t.charset[arguments.adminType == "server" ? "server" : "web"] ?: "");
			case "server.scope":
				return adminAINavSanitizeText(t.scopes[arguments.adminType == "server" ? "server" : "web"] ?: "");
			case "server.request":
				return adminAINavSanitizeText(t.request.description ?: "");
			case "server.output":
				if (structKeyExists(t.setting, arguments.adminType)) {
					return adminAINavSanitizeText(t.setting[arguments.adminType]);
				}
				return adminAINavSanitizeText(t.setting.allowCompressionDescription ?: "");
			case "server.error":
				return adminAINavSanitizeText(t.err.descr ?: "");
			case "server.logging":
				return adminAINavSanitizeText(structKeyExists(t.Settings, "logging") ? (t.Settings.logging.desc ?: "") : "");
			case "server.regex":
				return adminAINavSanitizeText(t.regex.descr ?: "");
			case "server.export":
				return adminAINavSanitizeText(t.setting.exportAppCFCDesc ?: "");
			case "services.cache":
				return adminAINavSanitizeText(structKeyExists(t.Settings, "cache") ? (t.Settings.cache.description ?: "") : "");
			case "services.datasource":
				return adminAINavSanitizeText(t.setting.datasourceDesc ?: "");
			case "services.orm":
				return adminAINavSanitizeText(t.Settings.orm.desc ?: "");
			case "services.search":
				return adminAINavSanitizeText(t.Search.Description ?: "");
			case "services.gateway":
				return adminAINavSanitizeText(t.setting.gateway.descexisting ?: "");
			case "services.tasks":
				return adminAINavSanitizeText(t.remote.ot.overviewDesc ?: "");
			case "services.schedule":
				return adminAINavSanitizeText(t.schedule.description ?: "");
			case "services.update":
				return adminAINavSanitizeText(t.services.update.desc ?: "");
			case "services.certificates":
				return adminAINavSanitizeText(t.services.certificate.desc ?: "");
			case "ext.applications":
				return adminAINavSanitizeText(t.ext.installeddesc ?: "");
			case "ext.providers":
				if (structKeyExists(t.ext, "provext") && structKeyExists(t.ext.provext, "list")) {
					return adminAINavSanitizeText(t.ext.provext.list);
				}
				return adminAINavSanitizeText(t.ext.prov.introtext ?: "");
			case "remote.securityKey":
			case "remote.clients":
				return adminAINavSanitizeText(t.remote.desc ?: "");
			case "resources.mappings":
				return adminAINavSanitizeText(t.Mappings.IntroText ?: "");
			case "resources.rest":
				return adminAINavSanitizeText(t.rest.desc ?: "");
			case "resources.component":
				return adminAINavSanitizeText(t.setting.general.component ?: "");
			case "resources.customtags":
				return adminAINavSanitizeText(t.setting.general.customtag ?: "");
			case "resources.cfx_tags":
				return adminAINavSanitizeText(t.security.cfxdescription ?: "");
			case "debugging.settings":
				return adminAINavSanitizeText(t.debug.enabledescription ?: "");
			case "debugging.templates":
				return adminAINavSanitizeText(t.debug.list.createDesc ?: "");
			case "debugging.logs":
				return adminAINavSanitizeText(structKeyExists(t.Settings, "logging") ? (t.Settings.logging.desc ?: "") : "");
			case "debugging.output":
				return adminAINavSanitizeText(t.debug.filterTitle ?: "");
			case "security.access":
				return adminAINavSanitizeText(t.security.desc ?: "");
			case "security.password":
				return adminAINavSanitizeText(t.login.changepassworddescription ?: "");
		}

		return desc;
	}

	function adminAIBuildNavigationIndex(
		required array navigation,
		required struct stText,
		required struct request,
		boolean isRestricted = false,
		boolean hasScheduler = true,
		boolean hasRemoteClientUsage = false,
		boolean luceneInstalled = true
	) {
		var index = [];
		var stNavi = {};
		var stCld = {};
		var fullAction = "";
		var navTitle = "";
		var i = 0;
		var iCld = 0;

		for (i = 1; i <= arrayLen(arguments.navigation); i++) {
			stNavi = arguments.navigation[i];

			if (!structKeyExists(stNavi, "children")) {
				if (adminAINavToBool(stNavi, "display")) {
					fullAction = stNavi.action;
					arrayAppend(index, {
						"action": fullAction,
						"title": stNavi.label,
						"description": adminAINavGetDescription(fullAction, arguments.stText, arguments.request.adminType),
						"url": arguments.request.self & "?action=" & fullAction
					});
				}
				continue;
			}

			for (iCld = 1; iCld <= arrayLen(stNavi.children); iCld++) {
				stCld = stNavi.children[iCld];

				if (adminAINavToBool(stCld, "hidden")) {
					continue;
				}
				if (arguments.isRestricted && !adminAINavToBool(stCld, "display")) {
					continue;
				}
				if (arguments.request.adminType == "web" && stCld.action == "search" && !arguments.luceneInstalled) {
					continue;
				}

				if (structKeyExists(stCld, "_action")) {
					fullAction = stCld._action;
				} else if (len(trim(stCld.action))) {
					fullAction = stNavi.action & "." & stCld.action;
				} else {
					fullAction = stNavi.action;
				}
				if (!arguments.hasScheduler && fullAction == "services.schedule") {
					continue;
				}
				if (listFindNoCase("remote.securityKey,remote.clients", fullAction) && !arguments.hasRemoteClientUsage) {
					continue;
				}

				navTitle = len(trim(stCld.action)) ? (stCld.label & " - " & stNavi.label) : stNavi.label;

				arrayAppend(index, {
					"action": fullAction,
					"title": navTitle,
					"description": adminAINavGetDescription(fullAction, arguments.stText, arguments.request.adminType),
					"url": arguments.request.self & "?action=" & fullAction
				});
			}
		}

		return index;
	}

	function adminAIFormatNavigationIndex(required array navIndex) {
		if (!isArray(arguments.navIndex) || !arrayLen(arguments.navIndex)) {
			return "";
		}
		return serializeJSON(arguments.navIndex);
	}
</cfscript>
