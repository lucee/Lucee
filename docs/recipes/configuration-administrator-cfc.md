<!--
{
  "title": "Configure Lucee within your Application",
  "id": "cookbook-configuration-administrator-cfc",
  "description": "How to configure Lucee within your application using Administrator.cfc and cfadmin tag.",
  "keywords": [
    "Administrator.cfc",
    "cfadmin",
    "Configuration",
    "Lucee",
    "Web context",
    "Server configuration"
  ]
}
-->
## Configure Lucee within your application

Lucee provides a web frontend to configure the server and each web context, but you can also do this configuration from within your application.
(For per request settings, please check out the "Application.cfc" section in the [Cookbook](/guides/cookbooks.html)).

### Administrator.cfc

Lucee provides the component "Administrator.cfc" in the package "org.lucee.cfml", a package auto imported in any template, so you can simply use that component as follows:

```cfs
admin = new Administrator("web", "myPassword"); // first argument is the admin type you want to load (web|server), second is the password for the Administrator
dump(admin); // show me the doc for the component
admin.updateCharset(resourceCharset: "UTF-8"); // set the resource charset
```

### cfadmin Tag

The component "Administrator" is far from being feature complete, so if you miss a functionality, best consult the unofficial tag "cfadmin" (undocumented) and check out how this tag is used inside the [Lucee Administrator](https://github.com/lucee/Lucee/blob/5.2/core/src/main/java/resource/component/org/lucee/cfml/Administrator.cfc).
Of course, it would be great if you could contribute your addition to the "Administrator" component.