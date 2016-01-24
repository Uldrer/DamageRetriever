package MuDamage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.stream.JsonReader;

public class MuParser extends Parser implements Runnable{
	
	private String memberUrl = "http://primera.e-sim.org/apiMilitaryUnitMembers.html?id=";
	private String baseMuIdUrl = "http://primera.e-sim.org/apiMilitaryUnitById.html?id=";
	private final String MEMBER_ERROR = "{\"error\":\"No such military unit in database\"}";
	private final String MU_ERROR = "{\"error\":\"No military unit with given id\"}";
	private HashMap<Integer,MilitaryUnit> mus;
	private Vector<MembersParsedListener> listeners;
	
	
	public MuParser(int countryId) {
		mus = new HashMap<Integer,MilitaryUnit>();
		ArrayList<Integer> countryIdList = new ArrayList<Integer>();
		countryIdList.add(countryId);
		
		mus = parseMusForCountry(mus,countryIdList);
	}
	
	public MuParser(ArrayList<Integer> countryIds, ArrayList<Integer> muIds) {
		mus = new HashMap<Integer,MilitaryUnit>();

		mus = parseMusForCountry(mus,countryIds, muIds);

	}
	
	public MuParser(int[] countryIds) {
		mus = new HashMap<Integer,MilitaryUnit>();
		
		ArrayList<Integer> countryIdList = new ArrayList<Integer>();
	    for (int index = 0; index < countryIds.length; index++)
	    {
	    	countryIdList.add(countryIds[index]);
	    }

		mus = parseMusForCountry(mus,countryIdList);

	}
	
	/*
	// special for sweden + some norway mus
	public MuParser(int countryId1, int countryId2) {
		mus = new HashMap<Integer,MilitaryUnit>();
			
		mus = parseMusForCountry(mus,countryId1);
		
		HashMap<Integer,MilitaryUnit> norwegianMus = new HashMap<Integer,MilitaryUnit>();
		norwegianMus = parseMusForCountry(mus,countryId2);
		
		// filter out the ones we want
		mus.put(551,norwegianMus.get(551)); // norwegian viking,  id = 551
	}
	*/
	
	public HashMap<Integer,MilitaryUnit> getMus() {
		return mus;
	}
	
	
	private HashMap<Integer,MilitaryUnit> parseMusForCountry(HashMap<Integer,MilitaryUnit> mus, ArrayList<Integer> countryIds) {
		
		// Loop over mus in api and take out all with correct country id
		fillCountryMus(mus, countryIds);
		
		return mus;
	}
	
private HashMap<Integer,MilitaryUnit> parseMusForCountry(HashMap<Integer,MilitaryUnit> mus, ArrayList<Integer> countryIds, ArrayList<Integer> muIds) {
		
		if(muIds.isEmpty())
		{
			fillCountryMus(mus, countryIds);
		}
		else
		{
			fillCountryMus(mus, countryIds, muIds);
		}
		
		return mus;
	}
	
	
	public void run() {
		
		boolean done = false;
		for (MilitaryUnit mu : mus.values()) {
			fillMuMembers(mu);
			done = true;
		}
		
		fireMembersParsedEvent(done);
	}
	
	private void fillCountryMus(HashMap<Integer,MilitaryUnit> mus, ArrayList<Integer> countryIds) {
		
		int counter = 1; // no id 0
		boolean done = false;
		while(!done)
		{
			String url = baseMuIdUrl + counter;
			String result = getPage(url);
			
			if(result.equals(MU_ERROR)) {
				System.out.println(" Reached end of mu list. ");
				done = true;
				continue;
			}
			
			InputStream in;
			MuInfo info = null;
			try {
				in = new ByteArrayInputStream(result.getBytes("UTF-8"));
				info = readMuJsonStream(in, counter);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println("first");
			} catch (com.google.gson.stream.MalformedJsonException e) {
				// In case primera server doesn't let us open to many pages
				try {
					Thread.sleep(2000, 0);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				//redo try
				continue;
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println("second");
				System.out.println("result: " + result);
			}
			if(info != null && countryIds.contains(info.getCountryId())) {
				MilitaryUnit mu = new MilitaryUnit(info.getId(),info.getTotalDamage(),info.getName());
				mus.put(info.getId(), mu);
			}
			
			// increase counter 
			counter++;
			
		}
	}
	
private void fillCountryMus(HashMap<Integer,MilitaryUnit> mus, ArrayList<Integer> countryIds,  ArrayList<Integer> muIds) {
		
	for(int i : muIds)
	{
		String url = baseMuIdUrl + i;
		String result = getPage(url);
		
		if(result.equals(MU_ERROR)) {
			System.out.println(" Invalid mu id. ");
			continue;
		}
		
		InputStream in;
		MuInfo info = null;
		try {
			in = new ByteArrayInputStream(result.getBytes("UTF-8"));
			info = readMuJsonStream(in, i);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("first");
		} catch (com.google.gson.stream.MalformedJsonException e) {
			// In case primera server doesn't let us open to many pages
			try {
				Thread.sleep(2000, 0);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			//redo try
			continue;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("second");
			System.out.println("result: " + result);
		}
		if(info != null && countryIds.contains(info.getCountryId())) {
			MilitaryUnit mu = new MilitaryUnit(info.getId(),info.getTotalDamage(),info.getName());
			mus.put(info.getId(), mu);
		}
		
	}
}
	
	
	private void fillMuMembers(MilitaryUnit mu) {
		String url = memberUrl + mu.getId();
		String result = getPage(url);
		
		if(result.equals(MEMBER_ERROR)) {
			System.out.println("Error: Did not find any mu members for " + mu.getName());
			return;
		}
		
		InputStream in;
		List<MemberInfo> infos = null;
		try {
			in = new ByteArrayInputStream(result.getBytes("UTF-8"));
			infos = readJsonStream(in);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("first");
		} catch (com.google.gson.stream.MalformedJsonException e) {
			// In case primera server doesn't let us open to many pages
			try {
				Thread.sleep(2000, 0);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			//redo try
			fillMuMembers(mu); // risk for infinite loop?
			return;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("second");
			System.out.println("result: " + result);
		}
		if(infos != null && infos.size() != 0) {
			for(MemberInfo info : infos) {
				mu.addMember(new Member(info.getId(),info.getName()));
			}
		}
		
		
	}
	
	private List<MemberInfo> readJsonStream(InputStream in) throws IOException {
	     JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
	     try {
	       return readMessagesArray(reader);
	     }finally {
	       reader.close();
	     }
	   }
	
	private MuInfo readMuJsonStream(InputStream in, int id) throws IOException {
	     JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
	     try {
	       return readMuMessageArray(reader, id);
	     }finally {
	       reader.close();
	     }
	   }

	private List<MemberInfo> readMessagesArray(JsonReader reader) throws IOException {
	     List<MemberInfo> messages = new ArrayList<MemberInfo>();

	     reader.beginArray();
	     while (reader.hasNext()) {
	       messages.add(readMessage(reader));
	     }
	     reader.endArray();
	     return messages;
	   }
	
	private MuInfo readMuMessageArray(JsonReader reader, int id) throws IOException {
		 MuInfo message = null;
		 message = readMuMessage(reader, id);
		 
	     return message;
	   }

	// only parse ids and names
	 private MemberInfo readMessage(JsonReader reader) throws IOException {
		 //init
		 int citizenId = 0;
		 String name ="";
		 
	     reader.beginObject();
	     while (reader.hasNext()) {
	       String info = reader.nextName();
	       if (info.equals("login")) {
	    	   name = reader.nextString();
	       } else if (info.equals("id")) {
	    	   citizenId = reader.nextInt();
	       } else {
	         reader.skipValue();
	       }
	     }
	     reader.endObject();
	     return new MemberInfo(citizenId, name);
	   }
	 
	// only parse ids and names
		 private MuInfo readMuMessage(JsonReader reader, int id) throws IOException {
			 //init
			 int countryId = 0;
			 String name ="";
			 double totalDamage = 0;
			 
		     reader.beginObject();
		     while (reader.hasNext()) {
		       String info = reader.nextName();
		       if (info.equals("name")) {
		    	   name = reader.nextString();
		       } else if (info.equals("countryId")) {
		    	   countryId = reader.nextInt();
		       } else if (info.equals("totalDamage")) {
		    	   totalDamage = reader.nextDouble();
		       }else {
		         reader.skipValue();
		       }
		     }
		     reader.endObject();
		     return new MuInfo(countryId, name, totalDamage, id);
		   }

	 public void addMembersParsedListener(MembersParsedListener listener) {
			if(listeners == null) {
				listeners = new Vector<MembersParsedListener>();
			}
			listeners.addElement(listener);
		}
		
	 public void removeMembersParsedListener(MembersParsedListener listener) {
			if(listeners == null) {
				listeners = new Vector<MembersParsedListener>();
			}
			listeners.removeElement(listener);
		}
		
	 protected void fireMembersParsedEvent(boolean done) {
			
			if(listeners != null && !listeners.isEmpty()) {
				MembersParsedEvent event = new MembersParsedEvent(this,done);
				

			    // walk through the listener list and
			    //   call the sunMoved method in each
			    Enumeration<MembersParsedListener> e = listeners.elements();
			    while (e.hasMoreElements()) {
			    	MembersParsedListener listener = (MembersParsedListener) e.nextElement();
			    	listener.membersParsed(event);
			    }
				
			}
			
		}

}
