
package MuDamage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;


public abstract class Parser {
	
	public String getPage(String urlString){
		//System.out.println("Parsing: " + urlString);
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
