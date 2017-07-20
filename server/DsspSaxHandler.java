package edu.uri.cs.gwt.plat.server;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class DsspSaxHandler extends DefaultHandler {
	
	private final static String FEATURE_ELEMENT = "FEATURE";
	private final static String ID_ATTRIBUTE = "id";
	private final static String DSSP_START = "__dazzle__Secondary Structure_DSSP";
	private final static String START_ELEMENT = "START";
    private final static String END_ELEMENT = "END";
    private final static String NOTE_ELEMENT = "NOTE";
	private String featureId;
	List<PdbAminoAcidDasDsspFeature> featureList = new ArrayList<>();
	PdbAminoAcidDasDsspFeature feature = null;
	String xmlContent = null;
	boolean isDsspFeature = false;

	/**
	 * Process start element events.
	 */
	@Override
	//Triggered when the start of tag is found.
	public void startElement(String namespaceURI, String localName, String qName, Attributes attributes)
			throws SAXException {

		if (qName.equals(FEATURE_ELEMENT))
		{
			featureId = attributes.getValue (ID_ATTRIBUTE);
			if (featureId.startsWith(DSSP_START)) {
				System.out.println("we have a dssp feature: " + featureId);
				feature = new PdbAminoAcidDasDsspFeature(featureId);
				isDsspFeature = true;
				
			}	
			
		}
	} // end startElement

	/**
	 * Process end element events.
	 */	 
	@Override
	public void endElement(String uri, String localName, String qName) 
			               throws SAXException {
		
		if (isDsspFeature) {
			switch(qName){
			// Add the completed feature to the list 
			case FEATURE_ELEMENT:
				featureList.add(feature);
				System.out.println("adding feature" + featureId);
				isDsspFeature = false;
				break;
				// For all other end tags update the feature.
			case START_ELEMENT:
					feature.setStartPos(Integer.parseInt (xmlContent.toString()));
				break;
			case END_ELEMENT:
					feature.setEndPos(Integer.parseInt (xmlContent.toString()));
				break;
			case NOTE_ELEMENT:
				feature.setNote(xmlContent.toString());
			break;

			}

		} // end if

	} // end String qName) throws SAXException {

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		//xmlContent = String.copyValueOf(ch, start, length).trim();
		xmlContent = new String(ch, start, length);

		System.out.println("xmlContent = " + xmlContent);
		
	} // end characters
	
    /**
     * Gets Start base pair position.
     * @return start base pair position
     */
    public List<PdbAminoAcidDasDsspFeature> getFeatureList() {
    	return featureList;
    }


}
