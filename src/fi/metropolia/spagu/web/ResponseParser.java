package fi.metropolia.spagu.web;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ResponseParser {
	
	

//	static String[] key = { "B22", "Triangle", "Triangle", "Triangle", "B343",
//			"B22", "B22", "B22", "Triangle" };

	static String[] key;
	static List<String> dummyUnparsedRooms = new ArrayList<String>();
	
	
	/**
	public static void main(String[] args) {

		dummyUnparsedRooms.add("library-newspaper, distance: 126.57408897558773");
		dummyUnparsedRooms.add("library-newspaper_2, distance: 124.683599563054");
		dummyUnparsedRooms.add("library-silent-reading, distance: 128.0234353546256");
		dummyUnparsedRooms.add("library-computer-room_3, distance: 129.15107432770353");
		dummyUnparsedRooms.add("2floor-bn-triangle&b213, distance: 124.80384609458156");
		dummyUnparsedRooms.add("library-bookshelf_2, distance: 130.25359879865124");
		dummyUnparsedRooms.add("b205_4, distance: 144.84129245487972");
		dummyUnparsedRooms.add("library-silent-reading_3, distance: 146.6117321362789");
		dummyUnparsedRooms.add("library-silent-reading_2, distance: 146.66969693839283");
		dummyUnparsedRooms.add("b242, distance: 156.1121391820636");
		
		List<String> mostReapetedNames = ResponseParser.getMostReapetedNames(dummyUnparsedRooms);
		
		for (String list : mostReapetedNames) {
			System.out.println(list);
		}
		

	}*/

	public List<String> getMostReapetedNames(List<String> unparsedRooms) {
		
		List<String> mostRepeatedRooms = new ArrayList<String>();
		HashMap<String, Integer> roomNameOccurrence = new HashMap<String, Integer>();
		List<Integer> mapValues = new ArrayList<Integer>();
		List<String> parsedRooms = new ArrayList<String>();
		
		for (String unparsedRoom : unparsedRooms) {
			int suffix = unparsedRoom.indexOf('_');
			int secondSuffix = unparsedRoom.indexOf(',');
			String room = "";
			
			if(suffix != -1) {
				room = unparsedRoom.substring(0, suffix);
				parsedRooms.add(room);
			} else {
				room =  unparsedRoom.substring(0, secondSuffix);
				parsedRooms.add(room); //remove .txt
			}
		}

		int initialValue = 1;

		for (String parsedRoom : parsedRooms) {

			if (roomNameOccurrence.containsKey(parsedRoom)) {

				roomNameOccurrence.put(parsedRoom, roomNameOccurrence.get(parsedRoom) + 1);

			} else {
				roomNameOccurrence.put(parsedRoom, initialValue);
			}

		}

		System.out.println(roomNameOccurrence);

		Collection<Integer> aggrigateValue = roomNameOccurrence.values();
		Object[] objectResult = aggrigateValue.toArray();

		for (int i = 0; i < objectResult.length; i++) {
			mapValues.add((Integer) objectResult[i]);
		}

		int value = getBiggerMapValue(mapValues);

		System.out.println(value);
		
		mostRepeatedRooms = getKeysFromValue(roomNameOccurrence, value);

		return mostRepeatedRooms;
	}
	
	@SuppressWarnings("rawtypes")
	public static List<String> getKeysFromValue(Map hm, Object value) {
	    Set ref = hm.keySet();
	    Iterator it = ref.iterator();
	    List<String> list = new ArrayList<String>();

	    while (it.hasNext()) {
	      String roomName = (String) it.next(); 
	      if(hm.get(roomName).equals(value)) { 
	        list.add(roomName); 
	      } 
	    } 
	    return list;
	}

	/**
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static List getKeysFromValue(Map hm, Object value) {
		Set ref = hm.keySet();
		Iterator it = ref.iterator();
		List list = new ArrayList();

		while (it.hasNext()) {
			Object o = it.next();
			if (hm.get(o).equals(value)) {
				list.add(o);
			}
		}
		return list;
	} */
	
	public static int getBiggerMapValue(List<Integer> values) {
		if (values.size() == 0) {
			return new Integer(0);
		}

		int most = values.get(0);
		int length = values.size();

		for (int i = 1; i < length; i++) {
			int value = values.get(i);
			most = Math.max(most, value);
		}
		
		return most;
	}

}
