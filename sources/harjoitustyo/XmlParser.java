package harjoitustyo;
import java.io.IOException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * 
 */

/**
 * @author tkt
 *
 */
public class XmlParser {
    private final String url = "http://smartpost.ee/fi_apt.xml";
    Document doc;

    public XmlParser() {};

    public void getDocument() {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            doc = db.parse(new URL(url).openStream());
            doc.getDocumentElement().normalize();

        }catch(ParserConfigurationException pce) {
            pce.printStackTrace();
        }catch(SAXException se) {
            se.printStackTrace();
        }catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }
    public NodeList parseData() {
        this.getDocument();
        Element docElement = doc.getDocumentElement();
        NodeList node = docElement.getElementsByTagName("place");
        return node;

    }
}
