<cfif (thisTag.executionMode == "end" || !thisTag.hasEndTag)>
	<cfscript>

		param name="attributes.navigation"    default="";
		param name="attributes.onload"		  default="";
		param name="attributes.title"         default="";
		param name="attributes.content"       default="";
		param name="attributes.right"         default="";
		param name="attributes.width"         default="780";

		param name="session.lucee_admin_lang" default="en";
		param name="session.debugEnabled"     default="false";

		if (structKeyExists(url, "debug"))
			session.debugEnabled = url.debug;

		setting showDebugOutput=session.debugEnabled;

		// make sure that any unavaliable language falls back to English
		variables.stText = ( application.stText[ session.lucee_admin_lang ] ) ?: application.stText.en;

		ad = request.adminType;
		hasNavigation = len(attributes.navigation) GT 0;
		request.mode = "full";
		resNameAppendix = hash(server.lucee.version & server.lucee["release-date"], "quick");
	</cfscript>
<cfcontent reset="yes"><!DOCTYPE html>
<cfoutput>
<html>
<head>
	<title>#attributes.title# - Lucee #ucFirst(request.adminType)# Administrator</title>
	<script>
	if (localStorage.getItem('darkMode') === 'enabled') {
		document.documentElement.classList.add('dark-mode');
	}
	</script>
	<link rel="stylesheet" href="../res/css/admin.css.cfm" type="text/css">
	<link rel="stylesheet" href="../res/css/darkmode.css.cfm" type="text/css">
	


	<meta name="robots" content="noindex,nofollow">
	<cfhtmlhead action="flush">
</head>

<body id="body" class="admin-single single full" onload="#attributes.onload#">
	<div id="<cfif !hasNavigation>login<cfelse>layout</cfif>">
		<table id="layouttbl">
			<tbody>
				<cfif hasNavigation>
				<tr>
					<td id="navtd" class="lotd">
						<div id="nav-panel">
							<div id="header">
								<a id="logo" href="index.cfm"></a>
								<cfif 
									findNoCase("-SNAPSHOT", server.lucee.version) 
									or findNoCase("-RC", server.lucee.version)>
								    <div class="version-number warn">#server.lucee.version#</div>
								<cfelseif 
									findNoCase("-ALPHA", server.lucee.version) 
									or findNoCase("-BETA", server.lucee.version) >
								    <div class="version-number err">#server.lucee.version#</div>
								<cfelse>
								    <div class="version-number">#server.lucee.version#</div>
								</cfif>
							</div>
							<div id="nav">
								<!---<form method="get" action="#cgi.SCRIPT_NAME#">
									<input type="hidden" name="action" value="admin.search">
									<input type="text" name="q" size="15"  class="navSearch" id="lucee-admin-search-input" placeholder="#stText.buttons.search.ucase()#">
									<button type="submit" class="sprite  btn-search"><!--- <span>#stText.buttons.search# ---></span></button>
									<!--- btn-mini title="#stText.buttons.search#" --->
								</form>--->
								#attributes.navigation#
							</div>
							<div id="nav-footer">
								<div id="copyright" class="copy">
									&copy; #year(Now())#
									<a href="https://www.lucee.org" target="_blank">Lucee Association Switzerland</a>.
									All Rights Reserved
								</div>
							</div>
						</div>
					</td>
					<td id="contenttd" class="lotd">
						<div id="content">
							<div id="maintitle">
								<cfif application.adminfunctions.canAccessContext()>
									<div id="logouts">
										<a class="sprite tooltipMe logout" href="#request.self#?action=logout" title="Logout"></a>
									</div> 
									<!--- Favorites --->
									<cfparam name="url.action" default="">
									<cfset pageIsFavorite = application.adminfunctions.isFavorite(url.action)>
								</cfif>
							</div>
							<div id="innercontent">
								<h1><cfif structKeyExists(request,'title')>#request.title#<cfelse>#attributes.title#</cfif><cfif structKeyExists(request,'subTitle')> - #request.subTitle#</cfif></h1>
								#thistag.generatedContent#
							</div>
						</div>
					</td>
				</tr>
				<cfelse>
				<tr id="tr-header">
					<td>
						<div id="header">
							<a id="logo" href="index.cfm"></a>
						</div>
					</td>
				</tr>
				<tr>
					<td id="logintd" class="lotd">
						<div id="content">
							<div id="innercontent" align="center">
								#thistag.generatedContent#
							</div>
						</div>
					</td>
				</tr>
				<tr>
					<td class="lotd" id="copyrighttd">
						<div id="copyright" class="copy">
							&copy; #year(Now())#
							<a href="https://www.lucee.org" target="_blank">Lucee Association Switzerland</a>.
							All Rights Reserved
						</div>
					</td>
				</tr>
				</cfif>
			</tbody>
		</table>
	</div>

	<script src="../res/js/base.min.js.cfm" type="text/javascript"></script>
	<script src="../res/js/jquery.modal.min.js.cfm" type="text/javascript"></script>
	<script src="../res/js/jquery.blockUI.js.cfm" type="text/javascript"></script>
	<script src="../res/js/admin.js.cfm" type="text/javascript"></script>
	<script src="../res/js/util.min.js.cfm"></script>
	<cfinclude template="navigation.cfm">
	<script>
		$(function() {

			$(".collapsible-banner-box").on("mouseenter", function() {
				$(this).addClass("is-expanded");
			}).on("mouseleave", function() {
				$(this).removeClass("is-expanded");
			}).on("focusin", function() {
				$(this).addClass("is-expanded");
			}).on("focusout", function() {
				var $box = $(this);
				setTimeout(function() {
					if (!$box.is(":hover") && !$box.find(":focus").length) {
						$box.removeClass("is-expanded");
					}
				}, 0);
			});

			$(".coding-tip-trigger").click(
				function(){
					var $this = $(this);
					$this.next(".coding-tip").slideDown();
					$this.hide();
				}
			);

			$(".coding-tip .copy").on("click", function(evt){
				var $this = $(this);
				var textToCopy = $this.parents(".coding-tip").find("code").text();
				var textarea = document.createElement('textarea');
				
				textarea.value = textToCopy;
				document.body.appendChild(textarea);
				textarea.select();

				if(document.execCommand('copy')) {
					$this.text("copied!");
					document.body.removeChild(textarea);
					setTimeout(() => { $this.text("copy"); }, 3000);
				} else {
					console.log("error copying to clipboard")
				}
			});

			function adminAILockOpen($box) {
				$box.addClass("is-open is-expanded");
			}

			function adminAIFormatAnswer(answer) {
				if (answer == null) return "";
				if (typeof answer === "string") return answer;
				if (!Array.isArray(answer)) return String(answer);
				return answer.map(function(part) {
					if (!part) return "";
					if (part.type === "text" || part.type === "struct") return part.content || "";
					if (part.type === "binary") return "";
					return part.content || "";
				}).join("");
			}

			function adminAIFormatQuestion(question) {
				if (typeof question === "string") return question;
				return adminAIFormatAnswer(question);
			}

			function adminAIGetForm($box) {
				var formName = $box.data("formName");
				if (formName) {
					var $named = $("form[name='" + formName + "']");
					if ($named.length) {
						return $named;
					}
				}
				var $inner = $box.closest("##innercontent");
				if (!$inner.length) {
					$inner = $box.parents("##innercontent");
				}
				var $forms = $inner.find("form").not(".admin-ai-form").filter(function() {
					return !$(this).closest(".admin-ai-box").length;
				});
				return $forms.first();
			}

			function adminAINormalizeActions(raw) {
				if (!raw || typeof raw !== "object") {
					return null;
				}
				if (raw.actions && typeof raw.actions === "object" && !Array.isArray(raw.actions)) {
					return raw.actions;
				}
				if (raw.fields && typeof raw.fields === "object" && !Array.isArray(raw.fields)) {
					return raw.fields;
				}
				return raw;
			}

			function adminAIResolveSelectValue($select, val) {
				var strVal = String(val == null ? "" : val).trim();
				if (!strVal.length) {
					return null;
				}
				if ($select.find("option[value='" + strVal.replace(/'/g, "\\'") + "']").length) {
					return strVal;
				}
				var strLower = strVal.toLowerCase();
				var resolved = null;
				$select.find("option").each(function() {
					var $opt = $(this);
					var optVal = String($opt.attr("value") || "");
					var optText = $opt.text().trim();
					if (!optVal.length) {
						return;
					}
					if (optVal.toLowerCase() === strLower) {
						resolved = optVal;
						return false;
					}
					if (optText.toLowerCase() === strLower) {
						resolved = optVal;
						return false;
					}
					if (optText.toLowerCase().indexOf(strLower) >= 0 || strLower.indexOf(optText.toLowerCase()) >= 0) {
						resolved = optVal;
						return false;
					}
				});
				return resolved;
			}

			function adminAIParseSusiActions(html) {
				var displayHtml = html;
				var actions = null;
				var m = html.match(/<!--\s*susi-actions\s*-->\s*([\s\S]*?)\s*<!--\s*\/susi-actions\s*-->/i);
				if (m) {
					displayHtml = html.replace(m[0], "").trim();
					try {
						actions = adminAINormalizeActions(JSON.parse(m[1].trim()));
					} catch (e) {
						console.warn("adminAI susi-actions parse failed", e);
					}
				}
				if (!actions) {
					m = html.match(/<code[^>]*class="[^"]*susi-actions[^"]*"[^>]*>([\s\S]*?)<\/code>/i);
					if (m) {
						try {
							actions = adminAINormalizeActions(JSON.parse($("<textarea/>").html(m[1].trim()).text()));
							displayHtml = displayHtml.replace(m[0], "").trim();
						} catch (e) {
							console.warn("adminAI susi-actions code block parse failed", e);
						}
					}
				}
				return { displayHtml: displayHtml, actions: actions };
			}

			function adminAIApplyActions($box, actions) {
				if (!actions || typeof actions !== "object") {
					return 0;
				}
				var $form = adminAIGetForm($box);
				if (!$form.length) {
					return 0;
				}
				var count = 0;
				Object.keys(actions).forEach(function(name) {
					if (/password|secret|key/i.test(name)) {
						return;
					}
					if (name === "name") {
						if ($form.find("input[name='_name']").length) {
							return;
						}
						var nameVal = actions[name];
						if (typeof nameVal === "string" && /^.+,[0-9A-F]{32}$/.test(nameVal)) {
							return;
						}
					}
					var val = actions[name];
					var $els = $form.find("[name='" + name.replace(/'/g, "\\'") + "']");
					if (!$els.length) {
						return;
					}
					$els.each(function() {
						var $el = $(this);
						var type = ($el.attr("type") || "").toLowerCase();
						if (type === "checkbox") {
							var checked = val === true || val === "true" || val === "yes" || val === $el.val();
							$el.prop("checked", checked).trigger("change");
							$el.closest("tr, label, td").addClass("admin-ai-field-changed");
							count++;
						} else if (type === "radio") {
							if (String($el.val()) === String(val)) {
								$el.prop("checked", true).trigger("change");
								$el.closest("tr, label, td").addClass("admin-ai-field-changed");
								count++;
							}
						} else if ($el.is("select")) {
							var resolved = adminAIResolveSelectValue($el, val);
							if (resolved != null) {
								$el.val(resolved).trigger("change");
								$el.addClass("admin-ai-field-changed");
								count++;
							}
						} else if (type !== "password" && type !== "hidden") {
							$el.val(val).trigger("change");
							$el.addClass("admin-ai-field-changed");
							count++;
						}
					});
				});
				return count;
			}

			function adminAIDismissWaitOverlay() {
				if (typeof $.unblockUI === "function") {
					$.unblockUI();
				}
			}

			function adminAIFinishExchange($exchange, $box, html) {
				var parsed = adminAIParseSusiActions(html);
				var $answerEl = $exchange.find(".admin-ai-answer");
				$answerEl.html(parsed.displayHtml);

				var allowModify = $box.data("allowModify") === true || $box.data("allowModify") === "true";
				if (!allowModify || !parsed.actions || !Object.keys(parsed.actions).length) {
					return;
				}

				var $applyRow = $exchange.find(".admin-ai-apply-row");
				if (!$applyRow.length) {
					$applyRow = $("<div class=\"admin-ai-apply-row\"></div>");
					$applyRow.append($("<button type=\"button\" class=\"admin-ai-apply button\">Apply to form</button>"));
					$exchange.append($applyRow);
				}

				var $applyBtn = $applyRow.find(".admin-ai-apply");
				$applyBtn.prop("disabled", false).text("Apply to form");
				$applyBtn.data("susiActions", parsed.actions);
				$applyBtn.attr("data-susi-actions-json", JSON.stringify(parsed.actions));
				$applyRow.show();
			}

			function adminAIRestoreHistory($box) {
				var historyId = $box.attr("data-history-id");
				if (!historyId) return;
				var el = document.getElementById(historyId);
				if (!el) return;
				try {
					var history = JSON.parse(el.textContent || el.innerText || "[]");
					if (!Array.isArray(history) || !history.length) return;
					var $transcript = $box.find(".admin-ai-transcript");
					if ($transcript.children().length) return;
					history.forEach(function(entry) {
						var $exchange = $("<div class=\"admin-ai-exchange\"></div>");
						$exchange.append($("<div class=\"admin-ai-question\"></div>").text(adminAIFormatQuestion(entry.question)));
						$exchange.append($("<div class=\"admin-ai-answer\"></div>"));
						$transcript.append($exchange);
						adminAIFinishExchange($exchange, $box, adminAIFormatAnswer(entry.answer));
					});
					$transcript.scrollTop($transcript[0].scrollHeight);
				} catch (e) {
					console.warn("adminAI history restore failed", e);
				}
			}

			$(".admin-ai-box").each(function() {
				adminAIRestoreHistory($(this));
			});

			$(".admin-ai-box").on("mouseenter", function() {
				if (!$(this).hasClass("is-open")) {
					$(this).addClass("is-expanded");
				}
			}).on("mouseleave", function() {
				var $box = $(this);
				if ($box.hasClass("is-open")) {
					return;
				}
				if (!$box.find(":focus").length) {
					$box.removeClass("is-expanded");
				}
			}).on("focusin mousedown", function() {
				adminAILockOpen($(this));
			}).on("focusout", function() {
				var $box = $(this);
				if ($box.hasClass("is-open")) {
					return;
				}
				setTimeout(function() {
					if (!$box.is(":hover") && !$box.find(":focus").length) {
						$box.removeClass("is-expanded");
					}
				}, 0);
			});

			function adminAICollectFormState($box) {
				var $form = adminAIGetForm($box);
				if (!$form.length) return "";

				var state = {};
				$form.find("input, select, textarea").each(function() {
					var $el = $(this);
					var name = $el.attr("name");
					if (!name || name === "question") return;
					if (name === "mainAction" || name === "cancel" || name === "subAction") return;
					if (name === "class" || name === "_name" || name === "bundleName" || name === "bundleVersion") return;
					if (name === "name" && $form.find("input[name='_name']").length) return;
					if (/apikey|secretkey|secret|password/i.test(name)) return;
					var type = ($el.attr("type") || "").toLowerCase();
					if (type === "password" || (type === "hidden" && /password|secret|key/i.test(name))) return;
					if ($el.closest(".admin-ai-box").length) return;
					if (type === "checkbox" || type === "radio") {
						if ($el.is(":checked")) state[name] = $el.val();
					} else {
						state[name] = $el.val();
					}
				});
				return JSON.stringify(state);
			}

			function adminAIAsk($box) {
				adminAILockOpen($box);
				var $input = $box.find(".admin-ai-input");
				var question = $.trim($input.val());
				if (!question) return;

				$input.val("");

				var $transcript = $box.find(".admin-ai-transcript");
				var $exchange = $("<div class=\"admin-ai-exchange\"></div>");
				var $questionEl = $("<div class=\"admin-ai-question\"></div>").text(question);
				var $answerEl = $("<div class=\"admin-ai-answer\" aria-live=\"polite\"></div>");
				$exchange.append($questionEl).append($answerEl);
				$transcript.append($exchange);
				$transcript.scrollTop($transcript[0].scrollHeight);

				var askUrl = $box.data("askUrl");
				var formData = new FormData();
				formData.append("sessionKey", $box.data("sessionKey"));
				formData.append("question", question);
				if ($box.data("allowModify") === true || $box.data("allowModify") === "true") {
					formData.append("formState", adminAICollectFormState($box));
				}

				$box.addClass("is-loading");
				$answerEl.addClass("is-loading");

				fetch(askUrl, {
					method: "POST",
					body: formData,
					credentials: "same-origin"
				}).then(function(res) {
					if (!res.ok) {
						return res.text().then(function(t) { throw new Error(t || res.statusText); });
					}
					var reader = res.body.getReader();
					var decoder = new TextDecoder();
					var text = "";

					function readChunk() {
						return reader.read().then(function(result) {
							if (result.done) {
								$box.removeClass("is-loading");
								$answerEl.removeClass("is-loading");
								adminAIFinishExchange($exchange, $box, text);
								$transcript.scrollTop($transcript[0].scrollHeight);
								return;
							}
							text += decoder.decode(result.value, { stream: true });
							var partial = adminAIParseSusiActions(text);
							$answerEl.html(partial.displayHtml);
							$transcript.scrollTop($transcript[0].scrollHeight);
							return readChunk();
						});
					}
					return readChunk();
				}).catch(function(err) {
					$box.removeClass("is-loading");
					$answerEl.removeClass("is-loading");
					$answerEl.text(err.message || "AI request failed");
				}).finally(function() {
					adminAIDismissWaitOverlay();
				});
			}

			function adminAIFlush($box) {
				var askUrl = $box.attr("data-ask-url");
				var formData = new FormData();
				formData.append("sessionKey", $box.attr("data-session-key"));
				formData.append("flush", "true");

				$box.addClass("is-loading");

				fetch(askUrl, {
					method: "POST",
					body: formData,
					credentials: "same-origin"
				}).then(function(res) {
					if (!res.ok) {
						return res.text().then(function(t) { throw new Error(t || res.statusText); });
					}
					return res.json();
				}).then(function() {
					$box.find(".admin-ai-transcript").empty();
					var historyId = $box.attr("data-history-id");
					var el = historyId ? document.getElementById(historyId) : null;
					if (el) {
						el.textContent = "[]";
					}
				}).catch(function(err) {
					console.warn("adminAI flush failed", err);
				}).finally(function() {
					$box.removeClass("is-loading");
					adminAIDismissWaitOverlay();
				});
			}

			$(".admin-ai-transcript").on("click", ".admin-ai-apply", function(evt) {
				evt.preventDefault();
				evt.stopPropagation();
				var $btn = $(this);
				var $box = $btn.closest(".admin-ai-box");
				var actions = $btn.data("susiActions");
				if (!actions) {
					var raw = $btn.attr("data-susi-actions-json");
					if (raw) {
						try {
							actions = adminAINormalizeActions(JSON.parse(raw));
						} catch (e) {
							console.warn("adminAI apply actions parse failed", e);
						}
					}
				}
				var applied = adminAIApplyActions($box, actions);
				if (applied > 0) {
					$btn.prop("disabled", true).text("Applied — click Update to save");
				} else if (!adminAIGetForm($box).length) {
					$btn.text("Page form not found");
				} else {
					$btn.text("No matching fields — check values");
				}
			});

			$(".admin-ai-clear").on("click", function(evt) {
				evt.preventDefault();
				evt.stopPropagation();
				adminAIFlush($(this).closest(".admin-ai-box"));
			});

			$(".admin-ai-submit").on("click", function() {
				adminAIAsk($(this).closest(".admin-ai-box"));
			});

			$(".admin-ai-input").on("keydown", function(evt) {
				if (evt.key === "Enter") {
					evt.preventDefault();
					adminAIAsk($(this).closest(".admin-ai-box"));
				}
			});
		}); // jQuery ready
	
		document.addEventListener('DOMContentLoaded', function() {

			
			// Create toggle button
			const toggleButton = document.createElement('button');
			toggleButton.classList.add('dark-mode-toggle');
			toggleButton.textContent = '☽';
			document.body.appendChild(toggleButton);
			
			// Check for saved preference
			const darkModeEnabled = localStorage.getItem('darkMode') === 'enabled';
			
			// Apply dark mode if previously enabled
			if (darkModeEnabled) {
				document.documentElement.classList.add('dark-mode');
				toggleButton.textContent = '☀';
				
				// Force reload charts if they exist
				refreshCharts();
			}
			
			// Add click event
			toggleButton.addEventListener('click', function() {
				// Toggle dark mode class on body
				document.documentElement.classList.toggle('dark-mode');
				
				// Save preference to localStorage
				if (document.documentElement.classList.contains('dark-mode')) {
					localStorage.setItem('darkMode', 'enabled');
					toggleButton.textContent = '☀';
				} else {
					localStorage.setItem('darkMode', 'disabled');
					toggleButton.textContent = '☽';
				}
				
				// Force reload charts if they exist
				refreshCharts();
			});
			
			// Add keyboard shortcut (Alt+D)
			document.addEventListener('keydown', function(e) {
				if (e.altKey && e.key === 'd') {
					toggleButton.click();
				}
			});
			
			// Helper function to refresh charts when dark mode changes
			function refreshCharts() {
				// Check if echarts is available and charts exist
				if (typeof echarts !== 'undefined') {
					const charts = ['heap', 'nonheap', 'cpuSystem'];
					charts.forEach(chartId => {
						const chartElement = document.getElementById(chartId);
						if (chartElement && window[chartId]) {
							// Update chart background color
							const isDarkMode = document.documentElement.classList.contains('dark-mode');
							const chartInstance = window[chartId];
							
							// Get the chart options
							let chartOptions;
							if (chartId === 'cpuSystem') {
								chartOptions = cpuSystemChartOption;
							} else {
								chartOptions = window[chartId + 'Chart'];
							}
							
							// Update background color
							if (chartOptions) {
								chartOptions.backgroundColor = isDarkMode ? '##333' : '##ffffff';
								
								// For CPU chart, also update text colors
								if (chartId === 'cpuSystem') {
									if (chartOptions.legend) {
										chartOptions.legend.textStyle = {
											color: isDarkMode ? '##ddd' : '##333'
										};
									}
									
									if (chartOptions.xAxis && chartOptions.xAxis[0]) {
										chartOptions.xAxis[0].axisLabel = {
											textStyle: {
												color: isDarkMode ? '##aaa' : '##666'
											}
										};
									}
									
									if (chartOptions.yAxis && chartOptions.yAxis[0]) {
										chartOptions.yAxis[0].axisLabel = {
											textStyle: {
												color: isDarkMode ? '##aaa' : '##666'
											}
										};
									}
								}
								
								// Apply updated options
								chartInstance.setOption(chartOptions);
							}
						}
					});
				}
			} // function refreshCharts()
		}); // DOMContentLoaded

	</script>

	<cfhtmlbody action="flush">
</body>
</html>
</cfoutput>

<cfset thisTag.generatedContent = "">

</cfif>
