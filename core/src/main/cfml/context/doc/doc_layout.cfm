<cfsetting showDebugOutput=false>
<cfparam name="Attributes.prevLinkItem" default="">
<cfparam name="Attributes.nextLinkItem" default="">
<cfoutput>
<cfif thistag.executionmode EQ "start">
	<cfif !structKeyExists(url, "isAjaxRequest")> <!--- iAR1 start --->
		<cfset currPath = expandpath(getDirectoryfromPath(CGI.SCRIPT_NAME))>
		<!DOCTYPE html>
		<html>
			<head>
				<title>Lucee documentation :: #Attributes.Title#</title>
				<base href="">
				<meta content="Lucee Server Documentation" name="description">
				<meta content="initial-scale=1.0, width=device-width" name="viewport">
				<meta name="ROBOTS" content="NOINDEX,NOFOLLOW">
				<link href="assets/css/base.min.css.cfm" rel="stylesheet">
				<link href="assets/css/highlight.css.cfm" rel="stylesheet">
				<link rel="icon" type="image/png" href="assets/images/favicon.png.cfm">
			</head>

	</cfif> <!--- iAR1 end --->
	<link href="/lucee/doc/assets/css/base.min.css.cfm" rel="stylesheet" >
	<link href="/lucee/doc/assets/css/highlight.css.cfm" rel="stylesheet" >
	<cfif structKeyExists(url, "isAjaxRequest")>
		<cfif !structKeyExists(url, "fromAdmin") OR url.fromAdmin NEQ true>
			<style type="text/css">
				.modal{
					height: 98% !important;
					position: relative !important;
					top: 2% !important;
				}
			</style>
		<cfelse>
			<style type="text/css">
				.modal{
					height: 95% !important;
					position: fixed !important;
					top: 3%;
					left: 20%;
				}
			</style>
		</cfif>
	</cfif>

	<cfif !structKeyExists(url, "isAjaxRequest")> <!--- iAR2 start --->
		<link rel="icon" type="image/png" href="assets/images/favicon.png.cfm">

		<body class="homepage">
			<nav class="menu menu-left nav-drawer" id="menu">
				<div class="menu-scroll">
					<div class="menu-wrap">
						<div class="menu-content">
							<a class="nav-drawer-logo" href="index.cfm"><img class="Lucee" src="assets/images/lucee-logo-bw.png.cfm"></a>
							<ul class="nav">
								<li class=" ">
									<a href="tags.cfm">Tags</a>
								</li>
								<li class=" ">
									<a href="functions.cfm">Functions</a>
								</li>
								<li class=" ">
									<a href="objects.cfm">Objects</a>
								</li>
								<li class=" ">
									<a href="categories.cfm">Categories</a>
								</li>
								<li class=" ">
									<a href="components.cfm">Components</a>
									<span class="menu-collapse-toggle collapsed" data-target="##reference" data-toggle="collapse" aria-expanded="0">
										<i class="icon icon-close menu-collapse-toggle-close"></i>
										<i class="icon icon-add menu-collapse-toggle-default"></i>
									</span>
									<ul class="menu-collapse collapse" id="reference">
										<cfloop array="#request.componentDetails.cfcs#" index="currComp">
											<li><a href="components.cfm?item=#currComp#" title="#currComp#">#currComp#</a></li>
										</cfloop>
									</ul>
								</li>
							</ul>
						</div>
					</div>
				</div>
			</nav>

			<header class="header">
				<ul class="hidden-lg nav nav-list pull-left">
					<li>
						<a class="menu-toggle" href="##menu">
							<span class="access-hide">Menu</span>
							<span class="icon icon-menu icon-lg"></span>
							<span class="header-close icon icon-close icon-lg"></span>
						</a>
					</li>
				</ul>
				<a class="header-logo hidden-lg" href="index.cfm"><img alt="Lucee" src="assets/images/lucee-logo.png.cfm"></a>
				<ul class="nav nav-list pull-right">
					<cfif len(Attributes.prevLinkItem)>
						<li>
							<a href="#listLast(CGI.SCRIPT_NAME, "/")#?item=#Attributes.prevLinkItem#">
								<span class="access-hide">Previous page: &lt;#Attributes.prevLinkItem#&gt;</span>
								<span class="icon icon-arrow-back icon-lg"></span>
							</a>
						</li>
					</cfif>

					<cfif len(Attributes.nextLinkItem)>
						<li>
							<a href="#listLast(CGI.SCRIPT_NAME, "/")#?item=#Attributes.nextLinkItem#">
								<span class="access-hide">Next page: &lt;#Attributes.nextLinkItem#&gt;</span>
								<span class="icon icon-arrow-forward icon-lg"></span>
							</a>
						</li>
					</cfif>

					<cfif lCase(listLast(CGI.SCRIPT_NAME, "/") NEQ "index.cfm")>
						<li>
							<a class="menu-toggle" href="##search">
								<span class="access-hide">Search</span>
								<span class="icon icon-search icon-lg"></span>
								<span class="header-close icon icon-close icon-lg"></span>
							</a>
						</li>
					</cfif>
				</ul>
				<span class="header-fix-show header-logo pull-none text-overflow">#Attributes.Title#</span>
			</header>
			<div class="menu menu-right menu-search" id="search">
				<div class="menu-scroll">
					<div class="menu-wrap">
						<div class="menu-content">
							<div class="menu-content-inner">
								<label class="access-hide" for="lucee-docs-search-input">Search</label>
								<input class="form-control form-control-lg menu-search-focus" id="lucee-docs-search-input" placeholder="Search" type="search">
							</div>
						</div>
					</div>
				</div>
			</div>

			<div class="content">
				<div class="content-heading">
					<div class="container">
						<div class="row">
							<div class="col-lg-10 col-lg-push-1">
								<h1 class="heading">#Attributes.Title#</h1>
							</div>
						</div>
					</div>
				</div>

				<div class="content-inner">
					<div class="container">
						<div class="row">
							<div class="col-lg-10 col-lg-push-1 body">
	</cfif><!--- iAR2 end --->
</cfif>
<cfif !structKeyExists(url, "isAjaxRequest")> <!--- iAR3 start --->
<cfif thistag.executionmode EQ "end" or not thistag.hasendtag>
							</div>
						</div>
					</div>
				</div>
			</div>

			<footer class="footer">
				<div class="container">
					<p>The Lucee Documentation is developed and maintained by the Lucee Association Switzerland and is licensed under a
						<a rel="license" href="http://creativecommons.org/licenses/by-nc-sa/3.0/"><img alt="Creative Commons License" style="border-width:0" src="http://i.creativecommons.org/l/by-nc-sa/3.0/80x15.png"></a>
						<a rel="license" href="http://creativecommons.org/licenses/by-nc-sa/3.0/">Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License</a>.
					</p>
				</div>
			</footer>

			<cfif isDefined( "Request.htmlBody" )>#Request.htmlBody#</cfif>
			<script src="assets/js/base.min.js.cfm" type="text/javascript"></script>
		</body>
	</html>
</cfif> <!--- iAR3 end --->
</cfif>
</cfoutput>