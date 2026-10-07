<cfscript>
	refURL="http://docs.lucee.org";
	wikiURL="https://docs.lucee.org/index.html";
	gitURL="https://github.com/lucee/Lucee";
	adminURL="#CGI.CONTEXT_PATH#/lucee/admin.cfm";
	webAdminURL="#CGI.CONTEXT_PATH#/lucee/admin/web.cfm";
	serverAdminURL="#CGI.CONTEXT_PATH#/lucee/admin/server.cfm";
	docURL="#CGI.CONTEXT_PATH#/lucee/doc.cfm";
	mailinglistURL="https://dev.lucee.org/";
	profURL="https://lucee.org/get-support/consulting.html";
	issueURL="https://luceeserver.atlassian.net/projects/LDEV/issues";
	newURL="http://docs.lucee.org/guides/lucee-5.html";
</cfscript><cfoutput><!DOCTYPE html>
<html>
	<head>
		<title>Rapid web development with Lucee!</title>
		<link rel="stylesheet" type="text/css" href="#cgi.context_path#/assets/css/lib/bootstrap.min.css">
		<link rel="stylesheet" type="text/css" href="http://fonts.googleapis.com/css?family=Open+Sans:400,300,600,700,800">
		<!--[if lte IE 8]><link rel="stylesheet" type="text/css" href="#cgi.context_path#/assets/css/lib/ie8.css"><![endif]-->
		<link rel="stylesheet" type="text/css" href="#cgi.context_path#/assets/css/core/_ed07b761.core.min.css">
		<!--[if lt IE 9]>
			<script src="http://html5shiv.googlecode.com/svn/trunk/html5.js"></script>
		<![endif]-->
	</head>
	<body class="sub-page">
		<div class="main-wrapper">
			<!-- <header id="masthead" class="branding" role="banner">
				<div class="container top-header">
		            <h1 class="site-logo">
		                <a href="/">
		                    <img src="#cgi.context_path#/assets/img/lucee-logo.png" alt="Lucee">
		                </a>
		            </h1>


			        Top Navigation
			   		<nav role="navigation" class="main-navigation">
					    <ul class="nav navbar-nav">
				    	    <li><a href="/">Home</a></li>
							<li class="active "><a href="http://lucee.dev1.pixl8-hosting.co.uk/supporters.html">Supporters</a></li>
							<li class=""><a href="http://lucee.dev1.pixl8-hosting.co.uk/about.html">About</a></li>
		    	    	</ul>
					</nav>
					<div class="clearfix"></div>
		        </div>
		 	</header> -->

						









	<section id="page-banner" class="page-banner">
		<div class="container">
			<div class="banner-content">
				<cfoutput>
				<img src="#cgi.context_path#/assets/img/lucee-logo.png" alt="Lucee"> 
				<h1>Welcome to your Lucee #ListFirst(server.lucee.version,'.')# Installation!</h1>
				<p class="lead-text">You are now successfully running Lucee #server.lucee.version# on your system!</p>
				</cfoutput>
			</div>
		</div>
	</section>
	


	<section id="contents">

		<div class="container full-width">
			<div class="row">

				<div class="col-md-8 main-content">

					<div class="content-wrap">

						
						

						<!--- <h1 class="blue">Important Notes</h1> --->


						<ul class="listing border-light">


							<cfoutput>
							
							<li class="listing-item thumb-large">
								<div class="listing-thumb">
									<a href="#refURL#">
										<img src="#cgi.context_path#/assets/img/img-new.png" alt="">
									</a>
								</div>
								

								<div class="listing-content">
									<h2 class="title">
										<a href="#newURL#">New in Lucee 5</a>
									</h2>

									<p>
										Lucee 5 is the first major release after forking from the Railo project. Lucee 5 is not about dazzling new features but about improving the core language and providing a complete architectural overhaul of the engine.
										This brings Lucee and CFML to a whole new level!
									</p>

								</div>
								
								<div class="clearfix"></div>
							</li>

							<li class="listing-item thumb-large">
								<div class="listing-thumb">
									<a href="#wikiURL#">
										<img src="#cgi.context_path#/assets/img/img-first-steps.png" alt="">
									</a>
								</div>
								

								<div class="listing-content">
									<h2 class="title">
										<a href="#wikiURL#">First steps</a>
									</h2>

									<p>If you are new to Lucee, please check our <a href="#wikiURL#" target="_blank">Docs</a> where you will find useful resources to get you started with Lucee including a Cookbook with a growing list of examples.</p>
							

								</div>
								
								<div class="clearfix"></div>
							</li>



							<li class="listing-item thumb-large">
								<div class="listing-thumb">
									<a href="#docURL#">
										<img src="#cgi.context_path#/assets/img/img-code.png" alt="">
									</a>
								</div>
								

								<div class="listing-content">
									<h2 class="title">
										<a href="#docURL#">Documentation</a>
									</h2>
									<p>
										If you have any questions about Lucee please take a look at the documentation in the first instance.
										<p><a href="http://docs.lucee.org" target="_blank">Online Documentation</a></p>
										<p><a href="#docURL#" target="_blank">Local Reference</a></p>
									</p>

								</div>
								
								<div class="clearfix"></div>
							</li>

							<li class="listing-item thumb-large">
								<div class="listing-thumb">
									<a href="#adminURL#">
										<img src="#cgi.context_path#/assets/img/img-exclamation-mark.png" alt="">
									</a>
								</div>
								

								<div class="listing-content">
									<h2 class="title">
										<a href="#adminURL#	">Secure Administrators</a>
									</h2>

									<p>Warning! If you have installed Lucee on a public server you need to secure the <a href="#serverAdminURL#	">Server</a> and <a href="#webAdminURL#	">Web</a> admins OF EVERY CONTEXT with appropriate passwords or other access restrictions. 

In addition you should set a default password in the Server admin for all web admins to be sure they are protected by default</p>


								</div>
								
								<div class="clearfix"></div>

							</li>
						</cfoutput>
						</ul>
					</div>
					

				</div>
				

				<div class="col-md-4 sidebar">

					<div class="sidebar-wrap">
						<cfoutput>
						<div class="widget widget-text">

							<h3 class="widget-title">Related Websites</h3>

							<!--- lucee.org --->
							<p class="file-link"><a href="http://www.lucee.org">Lucee Association Switzerland</a></p>
							<p>Non-profit custodians and maintainers of the Lucee Project</p>
							
							<!--- Bitbucket 
							<p class="file-link">Lucee Bitbucket</a></p>
							<p>Access the source code and builds</p> --->
							
							<!--- Mailinglist --->
							<p class="file-link"><a href="##">Get Involved</a></p>
							<p>
								Get involved in the Lucee Project!<br />
							- Engage with other Lucee community members via our <a href="#mailinglistURL#">forums/mailing list</a><br />
							- <a href="#issueURL#">Submitting</a> bugs and feature requests<br />
							- <a href="#gitURL#">Contribute</a> to the code<br />
							- <a href="https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=LKLC7KH4JRQ8J&">Support</a> the project<br />
							</p>
							

	

							<!--- Prof Services --->
							<p class="file-link"><a href="#profURL#">Professional Services</a></p>
							<p>Whether you need installation support or are looking for other professional services. Access our directory of providers</p>





						</div>
						</cfoutput>
					</div>
					
				</div>
				

			</div>
			

		</div>
		

	</section>
	



		    <footer id="subhead">


		        <div class="footer-bot">
		            <div class="container">
		                <div class="row">
		                    <div class="col-md-2 col-sm-4">
		                        <a href="/" class="footer-logo">
		                            <img src="#cgi.context_path#/assets/img/lucee-logo.png" alt="Lucee">
		                        </a>
		                        

		                    </div>
		                    

		                    <div class="col-md-5 col-sm-4">
		                        <p class="copyright-text">Copyright &copy; 2015 by the Lucee Association Switzerland</p>
		                    </div>
		                    



		                </div>
		                

		            </div>
		            

		        </div>
		        

		    </footer><!-- End of footer -->

        </div> <!-- End of .main-wrapper -->


		
	

	
		

<script src="#cgi.context_path#/assets/js/lib/jquery-1.10.1.min.js"></script>
<script src="#cgi.context_path#/assets/js/lib/bootstrap.min.js"></script>
<script src="#cgi.context_path#/assets/js/core/_38444bee.core.min.js"></script>
<script src="#cgi.context_path#/assets/js/lib/SmoothScroll.js"></script>

	</body>
	
</html></cfoutput>
