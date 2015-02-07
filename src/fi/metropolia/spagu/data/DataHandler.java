package fi.metropolia.spagu.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.obsearch.exception.IllegalIdException;
import net.obsearch.exception.OBException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import fi.metropolia.spagu.web.ResponseParser;
import fi.metropolia.spagu.web.WlanSimilarity;

public class DataHandler {

	private WlanSimilarity sim; 
	private ResponseParser responseParser = new ResponseParser();
	
	public DataHandler() {		
		sim = new WlanSimilarity();
		sim.doIndex();
		
	}
	

	public String convertStreamToString(InputStream is) throws IOException {
		/*
		* To convert the InputStream to String we use the
		* Reader.read(char[] buffer) method. We iterate until the
		* Reader return -1 which means there's no more data to
		* read. We use the StringWriter class to produce the string.
		*/
		
		StringWriter writer = new StringWriter();
		
		if (is != null) {			 
			char[] buffer = new char[1024];
			
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
				
				int n;				
				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n);
					writer.toString();
				}
			} finally {
				is.close();
			}
			return writer.toString();
		} else {    
			System.out.println("IS is null.");
			return "";
		}
	}	
	
	public StringBuilder responseContent(String input) {		
		StringBuilder responseContent = new StringBuilder();	
		StringBuilder repeatedRooms = new StringBuilder();	
		
		List<String> roomsList;
	//	return responseContent += input;
		try {
			roomsList = sim.getRooms(input);
		
	/**		int i = 1;
			for(String room: roomsList) {
				responseContent.append(i + " : " + room + "/");
				i++;
			}	*/
			
			List<String> mostReapetedNames = responseParser.getMostReapetedNames(roomsList);
			
			for(String name : mostReapetedNames) {
				repeatedRooms.append(name + ", ");
			}
			
		//	responseContent.append(roomsList.get(0)+ " / " +repeatedRooms+ " / " +dateToString(new Date()));
			String firstRoomResult = roomsList.get(0);
			int prefix = firstRoomResult.indexOf(',');
			responseContent.append(firstRoomResult.substring(0, prefix));
			
			System.out.println("First Room: " + firstRoomResult.substring(0, prefix));
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IllegalIdException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (OBException e) {
			e.printStackTrace();
		}		
		return responseContent;
	}	
	
	public void writeDataToLog(String visitor, String room,
			String start) {
		// Write data to data/log
		try {
			File file = new File("/usr/local/WLAN-log.txt");
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
	
	public Date stringToDate(String strDate) {

		Date date = null;
		try {
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			date = (Date) dateFormat.parse(strDate);

		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
	}

	public String dateToString(Date date) {
		String dateFormat = "yyyy-MM-dd HH:mm:ss";
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
		return sdf.format(date).toString();

	}
}
