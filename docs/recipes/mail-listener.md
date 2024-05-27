<!--
{
  "title": "Mail Listeners",
  "id": "mail-listeners",
  "since": "6.0",
  "description": "Learn how to define mail listeners in Lucee. This guide demonstrates how to set up global mail listeners in the Application.cfc file to listen to or manipulate every mail executed. Examples include defining listeners directly in Application.cfc and using a component as a mail listener.",
  "keywords": [
    "mail",
    "listener",
    "Application.cfc",
    "component"
  ]
}
-->
# Mail Listeners

Since Lucee 6.0, you can define a listener in the Application.cfc to listen to or manipulate every mail executed.

## Global Listeners

This example shows how to define a global mail listener in the Application.cfc:

```lucee
this.mail.listener = {
    before: function (caller, nextExecution, created, detail, closed, advanced, tries, id, type, remainingtries) {
        detail.from &= ".de";
        return arguments.detail;
    },
    after: function (caller, created, detail, closed, lastExecution, advanced, tries, id, type, passed, remainingtries) {
        systemOutput(arguments.keyList(), 1, 1);
    }
}
```

The listener can also be a component:

```lucee
this.mail.listener = new MailListener();
```

The component would look like this:

```lucee
component {
    function before(caller, nextExecution, created, detail, closed, advanced, tries, id, type, remainingtries) {
        detail.from &= ".de";
        return arguments.detail;
    }

    function after(caller, created, detail, closed, lastExecution, advanced, tries, id, type, passed, remainingtries) {
        systemOutput(arguments.keyList(), 1, 1);
    }
}
```