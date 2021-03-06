import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class helperTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		String url = "http://primera.e-sim.org/companyWorkResults.html?id=7760";
		
		String result = getPage(url);
		
		Document doc = Jsoup.parse(result, url);
		
		Element content = doc.getElementById("productivityTable");
		Elements links = content.getAllElements();
		for (Element link : links) {
		  String linkHref = link.attr("href");
		  String linkText = link.text();
		  System.out.println("href: " + linkHref + " text: " + linkText );
		}
		
		Elements links2 = content.getElementsByTag("tr");
		
		for (Element link : links2) {
			String linkHref = link.attr("href"); 
			Elements links3 = link.children();
			
			String linkText = link.text();  
			System.out.println("size: " + links3.size() +  " href2: " + linkHref + " text2: " + linkText );
		}	
		
		System.out.println(result);
		String name = "kukhue";
		int day = 608;
		double productivity = getProduction(name,day);
		System.out.println("name: " + name +  " day: " + day + " productivity: " + productivity);

	}
	
	static public double getProduction(String workerName, int workDay) {
		
		String url = "http://primera.e-sim.org/companyWorkResults.html?id=7760";
		
		String result = getPage(url);
		
		Document doc = Jsoup.parse(result, url);
		
		Element content = doc.getElementById("productivityTable");
		
		Elements table = content.getElementsByTag("tr");
		
		int index = getIndexForDay(table,workDay);
		System.out.println("work day index: " + index);
		if(index != -1) {
			for (Element link : table) {
				
				String name = link.child(0).text();
				
				if(name.equals(workerName)) {
					Element prod = link.child(index);
					Elements prods = prod.getElementsByTag("div");
					
					if(prods.size() > 0){
						String productivity = prods.get(0).text();
					
						return Double.parseDouble(productivity);
					}else{
						return 0;
					}
				}

			}
		}
		
		return 0;
	}
	
	static public int getIndexForDay(Elements table, int workDay) {
		Elements info = table.get(0).children();
		int indexCount = 0;
		for (Element dayInfo : info) {
			if(indexCount > 1) {
				int day = Integer.parseInt(dayInfo.text().substring(4,7));
				
				if(day == workDay) {
					return indexCount;
				}
			}
			indexCount++;
		}
		return -1;
	}
	
	static public String getPage(String urlString){
	    String result = "";
	    //Access the page
	    try {
	     // Create a URL for the desired page
	     URL url = new URL(urlString);
	     // Read all the text returned by the server
	     BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
	     String str;
	     while ((str = in.readLine()) != null) {
	         // str is one line of text; readLine() strips the newline character(s)
	         result += str;
	     }
	     in.close();             
	    } catch (MalformedURLException e) {
	    } catch (IOException e) {
	    }          
	    return result;
	}

}
