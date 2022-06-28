<!--- Set Email to be sent to an POP3 account --->
<!--- This is a very similar email to the ones receive from Yahoo, just with addressed anonymized and some custom headers removed --->
<cftry>
    <cfset creds = { "smtp" : server.getTestService("smtp"), "pop" : server.getTestService("pop") }>
    <cfset sBoundary = "E1DDE315-7C6C-4864-B47E-7AA62810F684--">

<cfmail to = "luceeldev3548pop@localhost"
    from = "luceeldev3548@localhost"
    subject = "Sample inline message/rfc822 POP3 testcase" 
    server="#creds.smtp.server#"
    password="#creds.smtp.password#"
    username="luceeldev3548@localhost"
    port="3025"
    useTls="true"
    usessl="false"
    async="false">

<cfmailparam name="Content-Type" value="multipart/report; report-type=feedback-report;  boundary=#chr(34)##sBoundary##chr(34)#">--#sBoundary#
Content-Type: message/rfc822
Content-Disposition: inline

X-HmXmrOriginalRecipient: <anonymized@hotmail.com>
X-MS-Exchange-EOPDirect: true
Received: from CO1NAM11HT005.eop-nam11.prod.protection.outlook.com
 (2603:10a6:600:152::12) by LO2P265MB4335.GBRP265.PROD.OUTLOOK.COM with HTTPS
 via LO4P123CA0043.GBRP123.PROD.OUTLOOK.COM; Thu, 3 Jun 2021 02:25:16 +0000
Received: from CO1NAM11FT011.eop-nam11.prod.protection.outlook.com
 (2a01:111:e400:3861::4c) by
 CO1NAM11HT005.eop-nam11.prod.protection.outlook.com (2a01:111:e400:3861::395)
 with Microsoft SMTP Server (version=TLS1_2,
 cipher=TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384) id 15.20.4150.30; Thu, 3 Jun
 2021 02:25:15 +0000
Authentication-Results: spf=pass (sender IP is 6x.xxx.xxx.xxx)
 smtp.mailfrom=somedomain.com; hotmail.com; dkim=pass (signature
 was verified) header.d=somedomain.com;hotmail.com;
 dmarc=bestguesspass action=none
 header.from=somedomain.com;compauth=pass reason=109
Received-SPF: Pass (protection.outlook.com: domain of
 somedomain.com designates 6x.xxx.xxx.xxx as permitted sender)
 receiver=protection.outlook.com; client-ip=6x.xxx.xxx.xxx;
 helo=smtp.somedomain.com;
Received: from smtp.somedomain.com (6x.xxx.xxx.xxx) by
 CO1NAM11FT011.mail.protection.outlook.com (10.13.175.186) with Microsoft SMTP
 Server (version=TLS1_2, cipher=TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384) id
 15.20.4150.30 via Frontend Transport; Thu, 3 Jun 2021 02:25:13 +0000
Received: from smtp.somedomain.com (smtp.somedomain.com [127.0.0.1])
 by smtp.somedomain.com with ESMTP ; Thu, 3 Jun 2021 04:25:12
 +0200
Date: Thu, 3 Jun 2021 04:25:12 +0200 (CEST)
From: "SomeName" <noreply@somedomain.com>
Reply-To: noreply@somedomain.com
To: anonymized@hotmail.com
Message-ID: <1017629883.42316.1622687112077@smtp.somedomain.com>
Subject: Some email attached
Content-Type: text/html; charset=UTF-8
Content-Transfer-Encoding: 7bit
X-IncomingHeaderCount: 14
Return-Path: noreply@somedomain.com
X-MS-Exchange-Organization-ExpirationStartTime: 03 Jun 2021 02:25:13.7907
 (UTC)
X-MS-Exchange-Organization-ExpirationStartTimeReason: OriginalSubmit
X-MS-Exchange-Organization-ExpirationInterval: 1:00:00:00.0000000
X-MS-Exchange-Organization-ExpirationIntervalReason: OriginalSubmit
X-MS-Exchange-Organization-Network-Message-Id:
 ed41441e-980c-40cb-31f8-08d92636d1b3
X-EOPAttributedMessage: 0
X-EOPTenantAttributedMessage: 84df9e7f-e9f6-40af-b435-aaaaaaaaaaaa:0
X-MS-Exchange-Organization-MessageDirectionality: Incoming
X-MS-PublicTrafficType: Email
X-MS-Exchange-Organization-AuthSource:
 CO1NAM11FT011.eop-nam11.prod.protection.outlook.com
X-MS-Exchange-Organization-AuthAs: Anonymous
X-MS-UserLastLogonTime: 6/3/2021 12:54:46 AM
X-MS-Office365-Filtering-Correlation-Id: ed41441e-980c-40cb-31f8-08d92636d1b3
X-MS-TrafficTypeDiagnostic: CO1NAM11HT005:
X-MS-Exchange-EOPDirect: true
X-Sender-IP: 6x.xxx.xxx.xxx
X-SID-PRA: NOREPLY@somedomain.com
X-SID-Result: PASS
X-MS-Exchange-Organization-PCL: 2
X-MS-Exchange-Organization-SCL: 0
X-Microsoft-Antispam: BCL:0;
X-OriginatorOrg: outlook.com
X-MS-Exchange-CrossTenant-OriginalArrivalTime: 03 Jun 2021 02:25:13.1930
 (UTC)
X-MS-Exchange-CrossTenant-Network-Message-Id: ed41441e-980c-40cb-31f8-08d92636d1b3
X-MS-Exchange-CrossTenant-Id: 84df9e7f-e9f6-40af-b435-aaaaaaaaaaaa
X-MS-Exchange-CrossTenant-AuthSource:
 CO1NAM11FT011.eop-nam11.prod.protection.outlook.com
X-MS-Exchange-CrossTenant-AuthAs: Anonymous
X-MS-Exchange-CrossTenant-FromEntityHeader: Internet
X-MS-Exchange-CrossTenant-RMS-PersistedConsumerOrg:
 00000000-0000-0000-0000-000000000000
X-MS-Exchange-Transport-CrossTenantHeadersStamped: CO1NAM11HT005
X-MS-Exchange-Transport-EndToEndLatency: 00:00:03.1907929
X-MS-Exchange-Processed-By-BccFoldering: 15.20.4195.023
MIME-Version: 1.0

<html><body><p>This is the body cfpop testcase LDEV-3548 to retrieve</p></body></html>
--_002_LO2P265MB4335150B0BCFB553B52DBEF9CA3C9LO2P265MB4335GBRP_--
</cfmail>
    <cfcatch>
        <cfoutput>sending multipart mail failed with: #cfcatch.message#</cfoutput>
    </cfcatch>
    <cfoutput>Done!!!</cfoutput>
</cftry>