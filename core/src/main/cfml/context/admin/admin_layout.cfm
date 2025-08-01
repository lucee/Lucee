<cfif (thisTag.executionMode == "end" || !thisTag.hasEndTag)>
	<cfscript>

		param name="session.lucee_admin_lang" default="en";
		param name="attributes.navigation"    default="";
		param name="attributes.title"         default="";
		param name="attributes.content"       default="";
		param name="attributes.right"         default="";
		param name="attributes.width"         default="780";

		// make sure that any unavaliable language falls back to English
		variables.stText = ( application.stText[ session.lucee_admin_lang ] )?:application.stText.en;

		ad=request.adminType;
		hasNavigation = len(attributes.navigation) GT 0;
		request.mode = "full";
		resNameAppendix = hash(server.lucee.version & server.lucee["release-date"], "quick");
		
		// load darkmode css
		if(!structKeyExists(application,"darkmodeCSS") or session.alwaysNew?:false) {
			application.darkmodeCSS=fileRead("resources/css/darkmode.css");
		}
	</cfscript>
<cfcontent reset="yes"><!DOCTYPE html>
<cfoutput>
<html>
<head>
	<title>#attributes.title# - Lucee #ucFirst(request.adminType)# Administrator</title>
	<link rel="stylesheet" href="../res/css/admin.css.cfm" type="text/css">
	<meta name="robots" content="noindex,nofollow">
	<cfhtmlhead action="flush">
</head>

<cfparam name="attributes.onload" default="">
<body id="body" class="admin-single single full" onload="#attributes.onload#">
	<div id="<cfif !hasNavigation>login<cfelse>layout</cfif>">
		<table id="layouttbl">
			<tbody>
				<tr id="tr-header">	<!--- TODO: not sure where height of 275px is coming from? forcing here 113px/63px !--->
					<td colspan="2">
						<div id="header">
								<a id="logo" <!--- class="scale-up" ---> href="index.cfm"></a>
								
						</div>	<!--- #header !---><div class="version-number">#server.lucee.version#</div>
					</td>
				</tr>
				<tr>
				<cfif hasNavigation>
					<td id="navtd" class="lotd">
						
						<div id="nav">
								<!---<form method="get" action="#cgi.SCRIPT_NAME#">
									<input type="hidden" name="action" value="admin.search">
									<input type="text" name="q" size="15"  class="navSearch" id="lucee-admin-search-input" placeholder="#stText.buttons.search.ucase()#">
									<button type="submit" class="sprite  btn-search"><!--- <span>#stText.buttons.search# ---></span></button>
									<!--- btn-mini title="#stText.buttons.search#" --->
								</form>--->

								#attributes.navigation#
						</div>
					</td>
				</cfif>
					<td id="<cfif !hasNavigation>logintd<cfelse>contenttd</cfif>" class="lotd">
						<div id="content">
							 <div id="maintitle">
								<cfif hasNavigation && application.adminfunctions.canAccessContext()>
									<div id="logouts">
										<a class="sprite tooltipMe logout" href="#request.self#?action=logout" title="Logout"></a>
									</div> 
									<!--- Favorites --->
									<cfparam name="url.action" default="">
									<cfset pageIsFavorite = application.adminfunctions.isFavorite(url.action)>
								</cfif>
									<h1><cfif structKeyExists(request,'title')>#request.title#<cfelse>#attributes.title#</cfif><cfif structKeyExists(request,'subTitle')> - #request.subTitle#</cfif></h1>
								</div>
							<div id="innercontent" <cfif !hasNavigation>align="center"</cfif>>
								#thistag.generatedContent#
							</div>
						</div>
					</td>
				</tr>
				<tr>
					<td class="lotd" id="copyrighttd" colspan="#hasNavigation?2:1#">
						<div id="copyright" class="copy">
							&copy; #year(Now())#
							<a href="https://www.lucee.org" target="_blank">Lucee Association Switzerland</a>.
							All Rights Reserved
						</div>
					</td>
				</tr>
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
		$(function(){

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
		});
	
document.addEventListener('DOMContentLoaded', function() {
  // Check if dark mode is enabled in the browser settings
  const isDarkMode = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;
  // First, inject our dark mode CSS
  const darkModeStyle = document.createElement('style');
  darkModeStyle.id = 'dark-mode-styles';
  
  // CSS content will be inserted here from the CSS artifact
  darkModeStyle.textContent = `#application.darkmodeCSS#`;
  
  document.head.appendChild(darkModeStyle);
  
  // Create toggle button
  const toggleButton = document.createElement('button');
  toggleButton.classList.add('dark-mode-toggle');
  toggleButton.textContent = '☽';
  document.body.appendChild(toggleButton);
  
  // Check for saved preference
  const darkModeEnabled = localStorage.getItem('darkMode') === 'enabled';
  
  // Apply dark mode if previously enabled
  if (darkModeEnabled || isDarkMode) {
    document.body.classList.add('dark-mode');
    toggleButton.textContent = '☀';
    
    // Force reload charts if they exist
    refreshCharts();
  }
  
  // Add click event
  toggleButton.addEventListener('click', function() {
    // Toggle dark mode class on body
    document.body.classList.toggle('dark-mode');
    
    // Save preference to localStorage
    if (document.body.classList.contains('dark-mode')) {
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
          const isDarkMode = document.body.classList.contains('dark-mode');
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
  }
});



	</script>

	<cfhtmlbody action="flush">




</body>
</html>
</cfoutput>
	<cfset thistag.generatedcontent="">
</cfif>
<cfparam name="session.debugEnabled" default="false">
<cfif structKeyExists (url, "debug")>
	<cfset session.debugEnabled = url.debug>
</cfif>
<cfsetting showdebugoutput="#session.debugEnabled#">
