<!--
{
  "title": "Event Gateways",
  "id": "event-gateways-overview",
  "categories": [
    "gateways"
  ],
  "description": "Overview of how Event Gateways work in Lucee.",
  "keywords": [
    "Event Gateway",
    "Directory Watcher",
    "Mail Watcher",
    "Lucee",
    "cfadmin",
    "File Changes"
  ]
}
-->
## How does an Event Gateway work?

An event gateway is a background process that continuously runs.

While running, it is doing the following: `<cfsleep>` for a specific time (the "interval"), then doing what it is designed for (checking changes in a directory, polling a mailserver, etc.), and after that, it goes to `<cfsleep>` again.

This looping and sleeping does not count for all types of event gateways. For example, a socket gateway just instantiates a Java socket server.

This "doing what it is designed for" will be explained in more detail underneath.

## Which gateways are available?

Lucee comes with 2 gateways: a Directory watcher and a Mail watcher.

### Directory watcher

This event gateway checks a given directory for file changes. These changes (events) can be:

* new files
* changed files
* removed files

When this gateway starts, it first takes a snapshot of the current state of the directory. This is the starting point from where changes are calculated.

Please note that the files in this first snapshot are NOT seen as changes!

So if you already have some files in the directory you want to watch, these files are not seen as "new file" when the gateway starts. Also, when Lucee (or your whole server) restarts, any changes which happened within this time are not seen and will not be picked up when the Directory watcher starts up again.

### Filters

You can apply filters for what you exactly want to watch changes for:

* **Watch subdirectories**: same as the "Recurse" option in `<cfdirectory>` and `directoryList()`
* **Extensions**: an optional list of comma-delimited file extensions. The default is "*", which obviously means "all files".

Note: the Extensions setting might be changed in the near future, due to an enhancement request.

### Mail watcher

This gateway checks a given POP mailbox for new mail. Since it only checks for new mail, it is rather limited in what it can do, but this is what makes it fast. The Mail watcher will read the inbox, and then check all the emails found.

### Logs

Make sure you regularly check the logs, because when anything goes wrong, Lucee will report this in its logs.

Lucee logs can be found here:

* `{Lucee-server}/context/logs/`
* `{Lucee-web}/lucee/logs/`

You can also view the logs in your web/server admin by installing the Log Analyzer plugin.

Also, make sure that you wrap your Listener function code inside try-catch blocks and do something within the catch block. For example:

```lucee
<cffunction name="onAdd" access="public" returntype="void" output="no">
    <cfargument name="fileDetails" type="struct" required="yes" />
    <cftry>
        <!--- do your file handling here, for example copy it: --->
        <cffile action="copy" source="#fileDetails.directory##server.separator.file##fileDetails.name#"
                destination="C:/backupfiles/" />
        <cfcatch>
            <cflog log="DirectoryWatcher-errors" type="error" text="Function onChange: #cfcatch.message# #cfcatch.detail#" />
        </cfcatch>
    </cftry>
</cffunction>
```

If you do not add these try-catch blocks, and anything goes wrong, it will be much harder to find out if anything went wrong!

For example, the above code would crash if a file with the same name already exists in the directory "C:/backupfiles/".

It might be even wiser to just email the complete error dump, so you will be semi-instantly notified of any errors.

### Using cfadmin with Event gateways

Instead of using the server/web admin, you can also use Lucee's `<cfadmin>` tag.

Add or update a gateway instance:

```lucee
<cfadmin action="updateGatewayEntry" type="server" password="server-admin-password"
    startupMode="automatic"
    id="copyIncomingFiles"
    class=""
    cfcpath="lucee.extension.gateway.DirectoryWatcher"
    listenerCfcPath="backupFilesGateway.BackupFilesListener"
    custom="# {
        directory="/ftp-root/incoming/",
        recurse=true,
        interval=10000,
        extensions="*",
        changeFunction="onAdd",
        addFunction="onAdd",
        deleteFunction=""
    } #"
    readOnly=false
/>
```

### Remove a gateway instance

```lucee
<cfadmin action="removeGatewayEntry" type="server" password="server-admin-password"
    id="copyIncomingFiles" />
```