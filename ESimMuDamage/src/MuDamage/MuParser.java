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
	
	private String baseCountryUrl;
	private String pageEndingUrl;
	private String muUnitUrl;
	private String memberUrl;
	private final String MEMBER_ERROR = "{\"error\":\"No such military unit in database\"}";
	private HashMap<Integer,MilitaryUnit> mus;
	private Vector<MembersParsedListener> listeners;
	
	
	public MuParser(int countryId) {
		baseCountryUrl = "http://primera.e-sim.org/militaryUnitStatistics.html?miltaryUnitStatisticsType=TOTAL_DAMAGE&countryId=";
		pageEndingUrl = "&page=";
		muUnitUrl = "militaryUnit.html?id=";
		memberUrl = "http://primera.e-sim.org/apiMilitaryUnitMembers.html?id=";
		mus = new HashMap<Integer,MilitaryUnit>();
		mus = parseMusForCountry(mus,countryId);
	}
	
	public MuParser(int[] countryIds) {
		baseCountryUrl = "http://primera.e-sim.org/militaryUnitStatistics.html?miltaryUnitStatisticsType=TOTAL_DAMAGE&countryId=";
		pageEndingUrl = "&page=";
		muUnitUrl = "militaryUnit.html?id=";
		memberUrl = "http://primera.e-sim.org/apiMilitaryUnitMembers.html?id=";
		mus = new HashMap<Integer,MilitaryUnit>();
		
		for(int i = 0; i < countryIds.length; i++)
		{
			mus = parseMusForCountry(mus,countryIds[i]);
		}
	}
	
	// special for sweden + some norway mus
	public MuParser(int countryId1, int countryId2) {
		baseCountryUrl = "http://primera.e-sim.org/militaryUnitStatistics.html?miltaryUnitStatisticsType=TOTAL_DAMAGE&countryId=";
		pageEndingUrl = "&page=";
		muUnitUrl = "militaryUnit.html?id=";
		memberUrl = "http://primera.e-sim.org/apiMilitaryUnitMembers.html?id=";
		mus = new HashMap<Integer,MilitaryUnit>();
			
		mus = parseMusForCountry(mus,countryId1);
		
		HashMap<Integer,MilitaryUnit> norwegianMus = new HashMap<Integer,MilitaryUnit>();
		norwegianMus = parseMusForCountry(mus,countryId2);
		
		// filter out the ones we want
		mus.put(551,norwegianMus.get(551)); // norwegian viking,  id = 551
	}
	
	public HashMap<Integer,MilitaryUnit> getMus() {
		return mus;
	}
	
	private HashMap<Integer,MilitaryUnit> parseMusForCountry(HashMap<Integer,MilitaryUnit> mus, int countryId) {
		
		String countryUrl = baseCountryUrl + countryId + pageEndingUrl;
		boolean done = false;
		int counter = 1;
		while(!done) {
			String url = countryUrl + counter;
			String result = getPage(url);
			
			if(result.equals("")) {
				done = true;
				break;
			}
			
			Document doc = Jsoup.parse(result, url);
			
			Elements tables = doc.getElementsByClass("dataTable");
			
			if(tables.size() == 0)
			{
				// Error
				System.out.println("Error when parsing MUs for country: " + countryId + ", page: " + counter + ", retrying...");
				 
				try {
					Thread.sleep(25, 0);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				continue;
			}
			
			Element table = tables.get(0);
			Elements tags = table.getElementsByTag("tr");
			
			for (Element link : tags) {
				
				if(link.child(0).text().equals("No military units")) {
					done = true;
					break;
				}
			
				Element info = link.child(1);
				String name = info.text();
				
				Elements id = info.children();
				String idInfo = id.attr("href");
				if(idInfo.contains(muUnitUrl)) {
					idInfo = idInfo.substring(muUnitUrl.length());
				}
				
				String dmg = link.child(4).text();
				dmg = dmg.replaceAll("\\D+", "");
				
				if(!name.equals("Name")) {
					MilitaryUnit mu = new MilitaryUnit(Integer.parseInt(idInfo),Double.parseDouble(dmg),name);
					// fillMuMembers(mu);
					mus.put(Integer.parseInt(idInfo),mu);
				}
				
			}
			counter++;
			try {
				Thread.sleep(10, 0);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
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

	private List<MemberInfo> readMessagesArray(JsonReader reader) throws IOException {
	     List<MemberInfo> messages = new ArrayList<MemberInfo>();

	     reader.beginArray();
	     while (reader.hasNext()) {
	       messages.add(readMessage(reader));
	     }
	     reader.endArray();
	     return messages;
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
