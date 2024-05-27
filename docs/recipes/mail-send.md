<!--
{
  "title": "Sending Emails",
  "id": "cookbook-mail-send",
  "related": [
    "tag-imap",
    "tag-mail",
    "tag-mailparam",
    "tag-mailpart"
  ],
  "description": "How to send an email using Lucee.",
  "keywords": [
    "Email",
    "Send mail",
    "cfmail",
    "Mail server",
    "Mail script",
    "Lucee"
  ]
}
-->
## How to send a Mail

The following example shows you how you can send a mail. Before you can use this functionality, you have to define a Mail server in the Lucee Administrator.

### Tags

```coldfusion
<cfmail subject="Your Order" from="whatever@lucee.org" to="whatever@gmail.com">
  Hi there,
  This mail is sent to confirm that we have received your order.
</cfmail>
```

### Script

```cfs
mail subject="Your Order" from="whatever@lucee.org" to="whatever@gmail.com" {
  writeOutput('Hi there,');
  writeOutput('This mail is sent to confirm that we have received your order.');
};
```

That is all you need to do to send a mail.

## Advanced

TODO