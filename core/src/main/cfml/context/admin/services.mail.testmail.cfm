<!--- sends the admin test mail; kept in its own file so the cfmail tag (mail extension) is only needed when a test mail is actually sent --->
<cfmail from="#form.fromMail#" to="#form.toMail#" subject="Test email from Lucee" server="#data.hostname#" username="#data.username#" password="#data.password#" port="#data.port#" usetls="#data.tls#" usessl="#data.ssl#" async="false">
Hi this is a test email from your lucee server instance.
</cfmail>
