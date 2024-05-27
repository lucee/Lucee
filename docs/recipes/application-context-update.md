<!--
{
  "title": "Update current Application Context",
  "id": "cookbook-application-context-update",
  "related": [
    "tag-application"
  ],
  "categories": [
    "application"
  ],
  "description": "How to update your Application settings, after they have been defined in Application.cfc.",
  "keywords": [
    "Application Context",
    "Update Application",
    "Mappings",
    "cfapplication",
    "getApplicationSettings",
    "Application.cfc"
  ]
}
-->
# Update Application Context

Lucee allows you to update the existing application context defined for example in [cookbook-application-context-basic].

For example, add a per-application mapping:

```lucee
<!--- creates a mapping with name "/test" that is pointing to the current directory --->
<cfapplication action="update" mappings="# {'/test': getDirectoryFromPath(getCurrentTemplatePath())} #">
```

This example doesn't extend the existing application mappings with this new one, it replaces them. So when you plan to add a mapping, it's best to first read the existing mappings with help of the function [function-getApplicationSettings] and update these mappings as follows:

```lucee
<!--- read the existing per-application mappings --->
<cfset mappings = getApplicationSettings().mappings>

<!--- add a mapping with name "/test" to the mappings struct --->
<cfset mappings['/test'] = getDirectoryFromPath(getCurrentTemplatePath())>

<!--- add all mappings --->
<cfapplication action="update" mappings="#mappings#">
```

Of course, it's not only mappings you can update. [tag-application] lets you update all the settings you can do in the Application.cfc!