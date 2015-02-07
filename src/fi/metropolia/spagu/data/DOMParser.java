package fi.metropolia.spagu.data;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class DOMParser {

	private static String tagID = "RAZR";
	private static String instantRoom = "b341";


	public static void main(String[] args) {


		
//		createNewDocument(tagID, currentRoom, dateToString(new Date()));	
//		
//		if (isDocExists) {
//			Element element = getAndModifyDOMElement(tagID, currentRoom, dateToString(new Date()));
//			
//			if (element != null) {
//				String tagIDValue = getTagValue("tagID", element);
//			    String roomValue = getTagValue("room", element);
//			    String startValue = getTagValue("start", element);
//	
//			    System.out.println("Old vistor's Tag ID: " + tagIDValue + "\nPrevious room name: " + roomValue 
//					  		+ "\nStart time: " + startValue + "\nEnd time: " + dateToString(new Date()) + "\n");
//			    
//				 writeDataToLog(tagIDValue, roomValue, startValue, dateToString(new Date()));
//		
//			}
//		}
//		
		createNewDocument(tagID, instantRoom, dateToString(new Date()));
		
//		 writeDataToLog(tagID, instantRoom, dateToString(new Date()));
		    

	}

		
	public static void createNewDocument(String tagID, String roomName,
			String startTime) {
		
		ArrayList<String> tagIDLists = new ArrayList<String>();
		try {
			File fXmlFile = new File("data/instant-rooms.xml");			
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();	 
			Document doc = docBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();
			
/**			// root elements
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("location");
			doc.appendChild(rootElement);		*/
			
			// root elements
			Element rootElement = doc.getDocumentElement();
			
			NodeList nList = rootElement.getChildNodes();
			
			for (int temp = 0; temp < nList.getLength(); temp++) {
				 
				   Node nNode = nList.item(temp);				   
				   if (nNode.getNodeType() == Node.ELEMENT_NODE) {
		 
				      Element eElement = (Element) nNode;
				      String tagIDValue = getTagValue("tagID", eElement);
				      tagIDLists.add(tagIDValue);
				      			     
				   }
			}
			
			if (!tagIDLists.contains(tagID)) {

				// visitor elements
				Element visitor = doc.createElement("visitor");
				rootElement.appendChild(visitor);
				 
				// tagID elements
				Element tagIDElement = doc.createElement("tagID");
				tagIDElement.appendChild(doc.createTextNode(tagID));
				visitor.appendChild(tagIDElement);

				// room elements
				Element roomNameElement = doc.createElement("room");
				roomNameElement.appendChild(doc.createTextNode(roomName));
				visitor.appendChild(roomNameElement);

				// start elements
				Element startTimeElement = doc.createElement("start");
				startTimeElement.appendChild(doc.createTextNode(startTime));
				visitor.appendChild(startTimeElement);
				 
				// write the content into xml file
				TransformerFactory transformerFactory = TransformerFactory.newInstance();
				Transformer transformer = transformerFactory.newTransformer();
				DOMSource source = new DOMSource(doc);
				StreamResult result = new StreamResult(fXmlFile);
				 
				// Output to console for testing
				// StreamResult result = new StreamResult(System.out);
				 
				transformer.transform(source, result);
				
				System.out.println("New Document created!");
			} else {
				System.out.println("TagID already exist in the Document.");
			    	
			}
		

		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException tfe) {
			tfe.printStackTrace();

		}
	}

	public static Element getAndModifyDOMElement(String tagID, String roomName,
			String startTime) {
		
		Element element = null;
		
		try {

			File fXmlFile = new File("data/instant-rooms.xml");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();			
			Document doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();
				
			NodeList nList = doc.getElementsByTagName("visitor");
			 
			for (int temp = 0; temp < nList.getLength(); temp++) {
	 
			   Node nNode = nList.item(temp);
			   
			   if (nNode.getNodeType() == Node.ELEMENT_NODE) {
	 
			      Element eElement = (Element) nNode;
			      
			      System.out.println("-----------------------");
			      
				  String tagIDValue = getTagValue("tagID", eElement);
			      String roomValue = getTagValue("room", eElement);
			      
			      if (tagIDValue.equals(tagID) && !roomValue.equals(roomName)) {
			    	  
			    	  element = (Element) eElement.cloneNode(true);
				    
					  // loop the visitor child node
					  NodeList list = eElement.getChildNodes();
				 
						for (int i = 0; i < list.getLength(); i++) {
							
							Node node = list.item(i);
				 
							// get the room element, and update the value
							if ("room".equals(node.getNodeName())) {
								node.setTextContent(roomName);
							}
				 
							// get the room element, and update the value
							if ("start".equals(node.getNodeName())) {
								node.setTextContent(startTime);
							}
						   
				/**         //remove firstname
						   	if ("firstname".equals(node.getNodeName())) {
								staff.removeChild(node);
						   	}		*/
				 
						}						
						// write the content into xml file
						TransformerFactory transformerFactory = TransformerFactory.newInstance();
						Transformer transformer = transformerFactory.newTransformer();
						DOMSource source = new DOMSource(doc);
						StreamResult result = new StreamResult(fXmlFile);
						transformer.transform(source, result);
				 
						System.out.println("Document value is modified.");
			      } else {
			    	  System.out.println("No change made to the Document.");
			    	  continue;
			      }
			   }
			}
	

		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException tfe) {
			tfe.printStackTrace();

		}
		return element;
				
	}
	
	public static void writeDataToLog(String visitor, String room,
			String start) {
		// Write data to data/log
		try {
			File file = new File("data/log.txt");
			// file.createNewFile();
			FileWriter fileWriter = new FileWriter(file, true);
			BufferedWriter writer = new BufferedWriter(fileWriter);
			writer.append(visitor + "/" + room + "/" + start);
			writer.newLine();

			writer.flush();
			writer.close();
			fileWriter.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String getTagValue(String sTag, Element eElement) {
		
		NodeList nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();
	 
	    Node nValue = (Node) nlList.item(0);
	 
		return nValue.getNodeValue();
	}
	
	public static Date stringToDate(String strDate) {

		Date date = null;
		try {

			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			date = (Date) dateFormat.parse(strDate);

		} catch (ParseException e) {
			e.printStackTrace();
		}

		return date;

	}

	public static String dateToString(Date date) {
		String dateFormat = "yyyy-MM-dd HH:mm:ss";
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
		return sdf.format(date).toString();

	}
}
