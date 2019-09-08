package lucee.runtime.net.proxy;

// Proxy Auto Config
public class PAC {
	private static String str = "function FindProxyForURL(url, host)\n" + "\n" + "    {\n" + "\n" + "        if (shExpMatch(host, \"192.168.*\") ||\n" + "\n"
			+ "            shExpMatch(host, \"127.*\")     ||\n" + "\n" + "            shExpMatch(host, \"172.16.*\")  ||\n" + "\n"
			+ "            shExpMatch(host, \"172.17.*\")  ||\n" + "\n" + "            shExpMatch(host, \"172.18.*\")  ||\n" + "\n"
			+ "            shExpMatch(host, \"172.19.*\")  ||\n" + "\n" + "            shExpMatch(host, \"172.20.*\")  ||\n" + "\n"
			+ "            shExpMatch(host, \"172.21.*\")  ||\n" + "\n" + "            shExpMatch(host, \"172.22.*\")  ||\n" + "\n"
			+ "            shExpMatch(host, \"172.23.*\")  ||\n" + "\n" + "            shExpMatch(host, \"172.24.*\")  ||\n" + "\n"
			+ "            shExpMatch(host, \"172.25.*\")  ||\n" + "\n" + "            shExpMatch(host, \"172.26.*\")  ||\n" + "\n"
			+ "            shExpMatch(host, \"172.27.*\")  ||\n" + "\n" + "            shExpMatch(host, \"172.28.*\")  ||\n" + "\n"
			+ "            shExpMatch(host, \"172.29.*\")  ||\n" + "\n" + "            shExpMatch(host, \"172.30.*\")  ||\n" + "\n"
			+ "            shExpMatch(host, \"172.31.*\")  ||\n" + "\n" + "            shExpMatch(host, \"10.*\")      ||\n" + "\n"
			+ "            shExpMatch(host, \"*.ads.hel.kko.ch\")         ||\n" + "\n" + "            shExpMatch(host, \"*.hel.kko.ch\")             ||\n" + "\n"
			+ "            shExpMatch(host, \"*.ovan.ch\")                ||\n" + "\n" + "            shExpMatch(host, \"*.ncag.helsana.ch\")        ||\n" + "\n"
			+ "            shExpMatch(host, \"helsana.ncag.ch\")          ||\n" + "\n" + "            shExpMatch(host, \"helsanapod.ncag.ch\")       ||\n" + "\n"
			+ "            shExpMatch(host, \"printform.ncag.ch\")        ||\n" + "\n" + "            shExpMatch(host, \"k4webportal.ncag.ch\")              ||\n" + "\n"
			+ "            shExpMatch(host, \"k4webportal.mycontent.ch\")         ||\n" + "\n" + "            shExpMatch(host, \"printformtest.ncag.ch\")            ||\n" + "\n"
			+ "            shExpMatch(host, \"helsanapod-demo.ncag.ch\")          ||\n" + "\n" + "            shExpMatch(host, \"*.seczone.centrisag.ch\")           ||\n" + "\n"
			+ "            dnsDomainIs(host, \".kko.ch\")                         ||\n" + "\n" + "            isPlainHostName(host) )                      {\n" + "\n"
			+ "            return \"DIRECT\";  }\n" + "\n" + "        else if (shExpMatch(host, \"*helsana-test.ch\")            ||\n" + "\n"
			+ "                 shExpMatch(host, \"*helsana-entwicklung.ch\")     ||\n" + "\n" + "                 shExpMatch(host, \"*helsana-integration.ch\")     ||\n" + "\n"
			+ "                 shExpMatch(host, \"*helsana-preprod02.ch\")       ||\n" + "\n" + "                 shExpMatch(host, \"*helsana-preprod.ch\") )   {\n" + "\n"
			+ "                 return \"PROXY Client-Proxy-PreProd.hel.kko.ch:8080\"; }\n" + "\n" + "        else { return \"PROXY Client-Proxy.hel.kko.ch:8080\"; }\n" + "\n"
			+ "    }";

}
