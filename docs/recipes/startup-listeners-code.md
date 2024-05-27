<!--
{
  "title": "Startup Listeners, server.cfc and web.cfc",
  "id": "startup-listeners-code",
  "related": [
    "function-configimport"
  ],
  "categories": [
    "server",
    "system"
  ],
  "description": "Lucee supports two types of Startup Listeners, server.cfc and web.cfc",
  "menuTitle": "Startup Listeners",
  "keywords": [
    "Startup Listeners",
    "server.cfc",
    "web.cfc",
    "configImport",
    "Lucee"
  ]
}
-->
## Startup Listeners Code ##

Lucee has two kinds of startup listeners.

- **Server.cfc** which runs when the Lucee Server starts up. It exists only once per Lucee instance.
- **Web.cfc** which can be used for each web context.

### Server.cfc ###

Create a `Server.cfc` file in `lucee-server\context\context` directory.

```lucee

// lucee-server\context\context\Server.cfc

component{
	public function onServerStart( reload ){
		if ( !arguments.reload ){
			systemOutput("-------Server Context started -----",true);
			// this is when the server is first started.
			// you can for example, use configImport() to import a  .cfConfig.json setting file
			var config_web = "/www/config/lucee_server_cfConfig.json";
			configImport(
				type: "server",
				data: deserializeJSON(fileRead(config_web)),
				password: "your lucee server admin password"
			);
		} else {
			// the server config is reloaded each time an extension is installed / or the config is updated
			systemOutput( "-------Server Context config reloaded -----", true );
		}
	}
}
```

- Here, Server.cfc has one function ``onServerStart()``
- Start Lucee Server. 
- The server console or `out.log` should show the above systemOutput's which means it has run the `Server.cfc`

### Web.cfc ###

Create a `Web.cfc` file in `webapps\ROOT\WEB-INF\lucee\context\` directory, or the context webroot.

```lucee

// webapps\ROOT\WEB-INF\lucee\context\Web.cfc

component {
	public function onWebStart( reload ){
		if ( !arguments.reload ){
			systemOutput("-------Web Context started -----",true);
			// this is when the web content is first started.
			// you can for example, use configImport() to import a .cfConfig.json setting file
			var config_web = "/www/config/lucee_web_cfConfig.json"
			configImport(
				type: "web",
				data: deserializeJSON( fileRead( config_web ) ),
				password: "your lucee web content admin password"
			);
		} else {
			// the web context is reloaded each time an extension is installed / or the config is updated
			systemOutput("-------Web Context config reloaded -----",true);
		}
	}
}
```

Here `Web.cfc` has one function ``onWebStart()`` and one argument ``reload`` that indicates if the web context is a new startup of the server. 

Here ``reload`` is used to reload the web context. We see the difference when setting reload to true or false.

- Start your Lucee server. 
- Here we see the server context output first, then the web context output next. So you can see that both listeners get triggered by Lucee.
- Next, change the **settings --> charset** for web charset "UTF-8" in web admin.
- After setting the charset in web admin, the web context only is reloaded and we do not have the server context. So this feature is used to stop/prevent any difficulties with the server context.

This is a simple way to stop the server context. It is never triggered because there is no event happening inside java.

### Footnotes ###

Here you can see above details in video

[Lucee Startup Listeners](https://youtu.be/b1MWLwkKdLE)