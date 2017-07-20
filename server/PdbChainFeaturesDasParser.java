package edu.uri.cs.gwt.plat.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

public class PdbChainFeaturesDasParser {
	
	//DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	SAXParserFactory parserFactory;
	SAXParser parser;
	SchemaFactory schemafactory;
	Schema dasDsspSchema;
	XMLReader xmlReader;
	DsspSaxHandler dsspHandler;

	List<PdbAminoAcidDasDsspFeature> featureList;
	List<String> aminoDsspList;
	
	NodeList nodeList;
	String fileStoragePath = "/plat/das_xml/";
	private final String USER_AGENT = "Mozilla/5.0";
	
	String dsspString = "";  // initialize to prevent null
	ArrayList<String> dsspSymbols;
	
	String dasRetrievalBaseUrl = "http://pdb.rcsb.org/pdb/rest/das/pdbchainfeatures/features?segment=";
	//String dasDtdUrlString = "http://www.rcsb.org/pdb/rest/das/pdbchainfeatures/dasgff.dtd";
	
	// constructor 
	public PdbChainFeaturesDasParser(String file, String chain) throws Exception {

		schemafactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		dasDsspSchema = schemafactory.newSchema(new File("/plat/das_xml/dasgff.xsd"));
		
		parserFactory = SAXParserFactory.newInstance();
		parserFactory.setValidating(false);  // possible TODO
		//parserFactory.setSchema(dasDsspSchema);
		
		parser = parserFactory.newSAXParser();
		dsspHandler = new DsspSaxHandler();
		
		
		String dasUrl = dasRetrievalBaseUrl + file + "." + chain;
		System.out.println("The das URL is " + dasUrl);
		URL dasServiceUrl = new URL(dasUrl);
		System.out.println("The URL was constructed");

		
		parser.parse(new InputSource(dasServiceUrl.openStream()), dsspHandler);
		
		featureList = dsspHandler.getFeatureList();
		
		dsspSymbols = generateDsspSymbolsFromNotes(featureList);

		for (int i = 0; i < dsspSymbols.size(); i++) {
			dsspString += dsspSymbols.get(i);
		}
		

	} // end default constructor
	
	/**
	 * Convert feature note into DSSP symbols for sequence positions
	 * 
	 * 
	 */
	public ArrayList<String> generateDsspSymbolsFromNotes(List<PdbAminoAcidDasDsspFeature> dasDsspFeaturesList) {
		ArrayList<String> result = new ArrayList<String>();
		
		int startPos, endPos;
		String note;
		String dsspSymbol;
		
		for (PdbAminoAcidDasDsspFeature feature : dasDsspFeaturesList) {
			
			startPos = feature.getStartPos();
			endPos = feature.getEndPos();
			note = feature.getNote();
			
			if (note.startsWith("3/10-helix")) {
				dsspSymbol = "G";
			}
			else if (note.startsWith("alpha helix")) {
				dsspSymbol = "H";
			}
			else if (note.startsWith("pi helix")) {
				dsspSymbol = "I";
			}
			else if (note.startsWith("beta bridge")) {
				dsspSymbol = "B";
			}
			else if (note.startsWith("beta strand")) {
				dsspSymbol = "E";
			}
			else if (note.startsWith("turn")) {
				dsspSymbol = "T";
			}
			else if (note.startsWith("bend")) {
				dsspSymbol = "S";
			}
			else if (note.startsWith("no secondary structure assigned")) {
				dsspSymbol = " ";
			}
			else {
				dsspSymbol = " ";
			}
			
			for (int i = startPos; i < endPos + 1; i++) {
				result.add(dsspSymbol);
			}
			
		} // end iteration over features
		
		for (int i = 0; i < result.size(); i++) {
			System.out.println(result.get(i));
		}		 
		
		return result;
	}
	
	/**
	 * 
	 * 
	 * 
	 */
	public String getDsspString() {
		return dsspString;
	}

} // end class
