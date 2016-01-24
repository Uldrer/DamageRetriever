package application;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class ConfigLoader {
	
	static final String ID = "ID";
	static final String MU = "MilitaryUnit"; // PLAYER
	static final String COUNTRY = "Country"; // REGION

	/**
	 * @param configFile
	 */
	public void readConfig(String configFile, ConfigInfo config)
	{
		
	    try {
	      // First, create a new XMLInputFactory
	      XMLInputFactory inputFactory = XMLInputFactory.newInstance();
	      // Setup a new eventReader
	      InputStream in = new FileInputStream(configFile);
	      XMLEventReader eventReader = inputFactory.createXMLEventReader(in);
	      // read the XML document
	      int item = -1;

	      while (eventReader.hasNext()) {
	        XMLEvent event = eventReader.nextEvent();

	        if (event.isStartElement()) {
	          StartElement startElement = event.asStartElement();
	          
	          // If we have an item element, we create a new item
	          if (startElement.getName().getLocalPart() == (MU) || 
	        		  startElement.getName().getLocalPart() == (COUNTRY)) {
	            // We read the attributes from this tag and add the date
	            // attribute to our object
	            Iterator<Attribute> attributes = startElement.getAttributes();
	            while (attributes.hasNext()) {
	              Attribute attribute = attributes.next();
	              if (attribute.getName().toString().equals(ID)) {
	            	item = Integer.parseInt(attribute.getValue());
	              }
	            }
	          }
	          
	        }
	        // If we reach the end of an item element, we add it to the list
	        if (event.isEndElement()) {
	          EndElement endElement = event.asEndElement();
	          if (endElement.getName().getLocalPart() == (MU)) {
	        	  config.muIds.add(item);
	          }
	          else if(endElement.getName().getLocalPart() == (COUNTRY))
	          {
	        	  config.countryIds.add(item);
	          }
	        }
	      }
	    }
	    catch (FileNotFoundException e) {
	      e.printStackTrace();
	    } 
	    catch (XMLStreamException e) {
	      e.printStackTrace();
	    }
	}
	
	

}
