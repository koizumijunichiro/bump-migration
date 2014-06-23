package bumpData;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


public class BumpUtils {
	static final String MUSIC_URL="http://www.adultswim.com/bumps/tools/music/";
//	private String xmlTestData = "<mashup>\n" +
//			"<track>\n" +
//			"<item id=\"5030\" time=\"5\" params=\"/bumps/tools/music/funk_4.mp3\"/>\n" +
//			"</track>\n" +
//			"<track>" +
//			" <item id=\"3002\" time=\"3\" params=\"Bump\"/>\n" +
//			" <item id=\"3002\" time=\"6\" params=\"Bump Bump\"/>\n" +
//			" <item id=\"3002\" time=\"9\" params=\"Bump Buuuuuump Bump\"/>\n" +
//			" <item id=\"3002\" time=\"12\" params=\"   \"/>\n" +
//			" <item id=\"3002\" time=\"15\" params=\"Bump\"/>\n" +
//			"</track>\n" +
//			"</mashup>";
	private String xmlTestData = "<mashup><track><item id='3002' time='0' params='http://www.adultswim.com/bumps/tools/music/blues_1.mp3' /></track><track><item id='3002' time='5' params='nothing to see here folks' /><item id='3002' time='10' params='just a bunch of characters ' /><item id='3002' time='15' params='!*'\"();:@&= $,/?%#[]' /></track></mashup>";
	//	private String xmlTestData = "<mashup>" +
	//			"<track>" +
	//			"    <item id=\"sound\" time=\"0\" params=\"http://www.adultswim.com/bumps/tools/music/electronica_5.mp3\" />" +
	//			"</track>" +
	//			"<track>" +
	//			"    <item id=\"3002\" time=\"15\" params=\"Hey look that's mine, mom look I made that, no not the tv the words, whatever you missed it.\" />" +
	//			"</track>" +
	//			"</mashup>";

	//trackList contains 2 meaningful ELEMENT_NODES
	//sometimes there are 2 additional TEXT_NODES which need to be skipped
	static List<Node> cleanTracks(NodeList trackList) {
		List<Node> cleanList = new ArrayList<Node>();
		int i=0, track1=0, track2=1;

		if (trackList.getLength() > 2) {
			track1=1;
			track2=3;
		}

		if (trackList.getLength() > 4) {
			System.out.println("**** BumpUtils - bump_data tracklist > 4 ****");
			Runtime.getRuntime().exit(0);
		}

		cleanList.add(0, trackList.item(track1));
		cleanList.add(1, trackList.item(track2));
		return cleanList;
	}

	static Node getFirstDataNode(NodeList nl) {
		int i = 0;
		while(nl.item(i).getNodeType() == Node.TEXT_NODE)
			i++;
		return nl.item(i);
	}

	public static String bumpDataToJson(String xmlData, String currentLine) {
		System.out.println("BumpUtils.0 - " + xmlData );
		System.out.println("BumpUtils.1 - " + currentLine );
		if ( xmlData.contains("'!*'\\\"();:@&= $,/?%#[]'") ) {
			System.out.println("***************************************************************");
			System.out.println( "xmlData contains :  !*'\"();:@&= $,/?%#[] ");
			xmlData=xmlData.replace("!*'\"();:@&= $,/?%#[]", "!*\"();:@= $,/?%#" );
			System.out.println("BumpUtils.2 - xmlData after replace - " + xmlData);
		}
		xmlData=xmlData.replace("&", "&amp;");
		xmlData=xmlData.replace("'", "&apos;");
		
		xmlData=xmlData.replace("<item id=&apos;", "<item id='");
		xmlData=xmlData.replace("&apos; time=&apos;", "' time='");
		xmlData=xmlData.replace("&apos; params=&apos;", "' params='");
		xmlData=xmlData.replace("&apos; />", "' />");
		xmlData=xmlData.replace("&apos;/>", "'/>");
		
		xmlData=xmlData.replace("let's", "let&apos;s");
		xmlData=xmlData.replace("#'s", " #&apos;s");
		xmlData=xmlData.replace("don't", "don&apos;t");
		xmlData=xmlData.replace("Let's", "Let&apos;s");
		xmlData=xmlData.replace("didn't", "didn&apos;t");
//		xmlData=xmlData.replace("'Murica","&apos;Murica");
//		xmlData=xmlData.replace("'Merca roolz","&apos;Merca roolz");
//		xmlData=xmlData.replace("'A lovers conversation...'", "&apos;A lovers conversation...&apos;");
//		xmlData=xmlData.replace("'Robin, get in the Batmobile!'", "&apos;Robin, get in the Batmobile!&apos;");
//		xmlData=xmlData.replace("'Every day we are together makes me feel stronger'", "&apos;Every day we are together makes me feel stronger&apos;");
//		xmlData=xmlData.replace("'Not like a weightlifter or something'", "&apos;Not like a weightlifter or something&apos;");
//		xmlData=xmlData.replace("params=\"'Cause", "params=\"Cause");
//		xmlData=xmlData.replace("You're", "You&apos;re");
		//xmlData=xmlData.replace("'Tis the season", "&apos;Tis the season");
		xmlData=xmlData.replace("''yes' $**&amp;@es'", "'yes $**&amp;@es'");
		xmlData=xmlData.replace("\"'", "\\\"'");
		
			System.out.println("**** xmlData after double quote single quote replace - " + xmlData);
		xmlData=xmlData.replace("'\"", "'\\\"");
			System.out.println("**** xmlData after single quote double quote replace - " + xmlData);
		System.out.println("BumpUtils after clean up.0 - " + xmlData );
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		String json="";
		try {
			xmlData = xmlData.replace("\n", "");
			json += "{\"mashup\": {\"track\": [";
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(new InputSource(new ByteArrayInputStream(xmlData.getBytes("utf-8"))));
			Element docElement = doc.getDocumentElement();
			docElement.normalize();
			Node root = doc.getFirstChild();
			NodeList mashupNode = root.getChildNodes();
			//track0==>sound data, track1==>bump data
			List<Node> trackList = cleanTracks( mashupNode );

			//Audio Attributes
			NodeList soundItems=trackList.get(0).getChildNodes();
			if (soundItems.getLength()==0)
				json+="{\"item\": [ { \"id\": [], \"time\": [], \"params\": [] }] },{\"item\": [";
			else {
				Node soundItem = getFirstDataNode(soundItems);
				NamedNodeMap soundAttributes = soundItem.getAttributes();
				String audioId = soundAttributes.getNamedItem("id").getNodeValue();
				if (audioId.equals("sound"))
					audioId = "\"" + audioId + "\"";
				String audioTime = soundAttributes.getNamedItem("time").getNodeValue();
				String audioParams = soundAttributes.getNamedItem("params").getNodeValue();
				audioParams = MUSIC_URL + audioParams.substring( audioParams.lastIndexOf('/')+1);
				json +="{\"item\": [ { \"id\": ["+ audioId +"], \"time\": ["+ audioTime +"], \"params\": [\""+ audioParams +"\"] }] },{\"item\": [";
			}
			//Bump data
			NodeList bumpList = trackList.get(1).getChildNodes();
			List<NamedNodeMap> itemAttributeList = new ArrayList<NamedNodeMap>();
			//pull out all of the bump attribute data
			for (int i=0; i < bumpList.getLength(); i++){
				if (bumpList.item(i).getNodeType() == Node.TEXT_NODE) continue;
				itemAttributeList.add(bumpList.item(i).getAttributes());
			}

			int attributeCount = itemAttributeList.size();
			for (NamedNodeMap itemAttributes: itemAttributeList) {
				String id = itemAttributes.getNamedItem("id").getNodeValue();
				String time = itemAttributes.getNamedItem("time").getNodeValue();
				Node paramsNode=itemAttributes.getNamedItem("params");
				String params = ((null==paramsNode)?"":paramsNode.getNodeValue());

				json += "{\"id\": [\""+ id +"\"], \"time\": [\""+ time +"\"], \"params\": [\""+ params +"\"]}";
				json += (--attributeCount == 0)? "": ",";
			}
			json += "]}]}}";
		} catch (ParserConfigurationException e) {
			System.out.println("06 BumpUtils - parser config exception:\n " + e.getMessage());
			System.exit(0);
		} catch (IOException ioe) {
			System.out.println("07 BumpUtils - ioe exception:\n " + ioe.getMessage());
			System.exit(0);
		} catch (SAXException e) {
			System.out.println("08.1 BumpUtils - sax exception:\n " + e.getMessage());
			System.out.println("08.2 BumpUtils - xmlData:\n " + xmlData);
			System.out.println("08.2 BumpUtils - currentLine:\n " + currentLine);
			System.exit(0);
		} catch (Exception e) {
			System.out.println("09 BumpUtils - exception:\n " + e.getMessage() );
			System.out.println("09 BumpUtils - exception:\n " + e.getStackTrace() );
			System.exit(0);
		}
		System.out.println("10.0 BumpUtils bump_data - " + json + "\n");
		return json; 
	}

	public static void main(String [] args) {
		BumpUtils xtj = new BumpUtils();
		//bumpDataToJson(xtj.xmlTestData, "empty test line");
		System.out.println( bumpDataToJson(xtj.xmlTestData, "foo") );
	}
}
