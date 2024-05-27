<!--
{
  "title": "Basic Date",
  "id": "cookbook-basic-date",
  "description": "Learn how to output the current date in Lucee.",
  "keywords": [
    "Date",
    "Current date",
    "lsDateTimeFormat",
    "now",
    "setLocale",
    "Application.cfc",
    "Locale"
  ]
}
-->
# Output the current date

The following examples show you how to output the current date.

```run
<cfoutput>
   <p>The time is #lsdateTimeFormat(now())#</p>
</cfoutput>
```

The tag `<cfoutput>` defines for the compiler that everything within a `##` is a code expression and needs to be parsed accordingly. the function `now()` is a function call that returns a date object containing the current time. [function-lsDateTimeFormat] then converts that date to a string using locale-specific formatting rules (default en_US).

You can configure a different locale globally in the Lucee admin under "Settings/Regional".

You can then configure a different locale for the current request in the Application.cfc file (for example: `this.locale="de_CH"`) or with the help of the function [function-setLocale] or as an argument of the function call itself as follows:

```run
<cfoutput>
    <p>The time is #lsDateTimeFormat(date:now(),locale:'de_CH')#</p>
</cfoutput>
```