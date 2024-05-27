<!--
{
  "title": "Query Listeners",
  "id": "query-listeners",
  "since": "6.0",
  "description": "Learn how to define query listeners in Lucee. This guide demonstrates how to set up global query listeners in the Application.cfc file to listen to or manipulate every query executed. Examples include defining listeners directly in Application.cfc and using a component as a query listener.",
  "keywords": [
    "query",
    "listener",
    "Lucee",
    "Application.cfc",
    "component"
  ]
}
-->
# Query Listeners

Since Lucee 6.0, you can define a listener in the Application.cfc to listen to or manipulate every query executed.

## Global Listeners

This example shows how to define a global query listener in the Application.cfc:

```lucee
this.query.listener = {
    before: function (caller, args) {
        dump(label: "before", var: arguments);
    },
    after: function (caller, args, result, meta) {
        dump(label: "after", var: arguments);
    }
};
```

The listener can also be a component:

```lucee
this.query.listener = new QueryListener();
```

The component would look like this:

```lucee
component {
    function before(caller, args) {
        args.sql = "SELECT TABLE_NAME as abc FROM INFORMATION_SCHEMA.TABLES";
        args.maxrows = 2;
        return arguments;
    }

    function after(caller, args, result, meta) {
        var row = queryAddRow(result);
        result.setCell("abc", "123", row);
        return arguments;
    }

    function error(args, caller, meta, exception) {
        // Handle exception
    }
}
```