package lucee.runtime.text.xml.html.jaxp;

import java.io.File;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParserFactory;

import org.w3c.dom.Document;

/**
 * Trivial non-robust test class, to show that TagSoup can be accessed using JAXP interface.
 */
public class JAXPTest {
	public static void main(String[] args) throws Exception {
		new JAXPTest().test(args);
	}

	private void test(String[] args) throws Exception {
		if (args.length != 1) {
			System.err.println("Usage: java " + getClass() + " [input-file]");
			System.exit(1);
		}
		File f = new File(args[0]);
		// System.setProperty("javax.xml.parsers.SAXParserFactory", SAXFactoryImpl.class.toString());
		System.setProperty("javax.xml.parsers.SAXParserFactory", "lucee.runtime.text.xml.html.jaxp.SAXFactoryImpl");

		SAXParserFactory spf = SAXParserFactory.newInstance();
		System.out.println("Ok, SAX factory JAXP creates is: " + spf);
		System.out.println("Let's parse...");
		spf.newSAXParser().parse(f, new org.xml.sax.helpers.DefaultHandler());
		System.out.println("Done. And then DOM build:");

		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(f);

		System.out.println("Succesfully built DOM tree from '" + f + "', -> " + doc);
	}
}
