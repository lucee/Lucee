<!--
{
  "title": "Check for changes",
  "id": "cookbook-check-for-changes",
  "description": "Automatically check for changes in your configuration file with Lucee.",
  "keywords": [
    "Configuration",
    "Check for changes",
    "Lucee",
    "Automatic update",
    "Server context",
    "Web context"
  ]
}
-->
# Check for changes in your configuration file automatically

Lucee can automatically check for changes in your configuration files from the complete server or a single web context.

This is useful if you are doing scripted deploys and/or synchronization from, for example, a master instance to many slave instances of Lucee.

## Check for Changes in ALL the contexts

To enable this for a whole Lucee server, find the Lucee server XML file in:

    <Lucee Install Dir>/lib/ext/lucee-server/context/lucee-server.xml

At the top of this file, you should see something along the lines of:

    <cfLuceeConfiguration hspw="xxx" salt="xx" version="4.2">

Now it's simple to add the following:

    <cfLuceeConfiguration hspw="xxx" salt="xx" check-for-changes="true" version="4.2">

Now that you have made the change, you can either restart Lucee server from the administrator at:

    http://localhost:8888/lucee/admin/server.cfm?action=services.restart

Or actually make any change in the Server Admin for the configuration to be picked up. This should now allow it to pick up any changes you have written to the lucee-server.xml file.

## Check for changes in an individual context

If you only want an individual context to check for changes, you can do the same configuration but you would have to go to:

    <Site Root>/WEB-INF/lucee/lucee-web.xml.cfm

And add the same changes from above:

    <cfLuceeConfiguration hspw="xxx" salt="xx" check-for-changes="true" version="4.2">

Lucee will now check for any changes in the Lucee configuration files every minute, and if there is a change, reload it and enable those changes.

A very handy little feature for those automated deployments!