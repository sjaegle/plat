package edu.uri.cs.gwt.plat.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.biojava.bio.structure.AminoAcid;
import org.biojava.bio.structure.Atom;
import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Group;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureException;
import org.biojava.bio.structure.io.FileParsingParameters;
import org.biojava.bio.structure.io.PDBFileReader;
import org.biojava.bio.structure.secstruc.SecStruc;
import org.biojava.bio.structure.secstruc.SecStrucGroup;
import org.pdb.webservices.PdbWebService;
import org.pdb.webservices.PdbWebServiceServiceLocator;

/**
 * The servlet that accesses the library to obtain RCSB PDB chains amino acid sequences
 * and secondary dssp structure.
 * 
 * Structure
 *         |
 *         Chain
 *             |
 *             Group
 *                 |
 *                 Atom
 * 
 * @author stephenjaegle
 *
 */
public class PdbChainSequenceDsspFetcher extends HttpServlet {
	
	// pdb files are stored in a local directory automatically maintained by PDBFileReader
	private String pathToPDBdir = "/plat/plat_jmol/pdb/";
	private String pathToXMLdir = "/plat/plat_jmol/das_xml";
	private PDBFileReader pdbreader = new PDBFileReader();
	private FileParsingParameters fileParsingParameters = new FileParsingParameters();
	private PdbChainFeaturesDasParser secFeatureParser;

	private Structure structure;
	private SecStruc secStructure;
	private Chain chain;
	private List<Group> aminoGroupList;
	private List<Atom> atomList;
	private ArrayList<String[]> atomAcidTuples = new ArrayList<String[]>();
	
	private String aminoAcidsStr = "";
	private String atomNamesListStr = "";
	
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException {
		pdbreader.setPath(pathToPDBdir);
		pdbreader.setAutoFetch(true);
		// parse author designated secondary structure
		fileParsingParameters.setParseSecStruc(true);
		pdbreader.setFileParsingParameters(fileParsingParameters);

		// get the structure
		String pdbFile = req.getParameter("pdbFileName");
		try {
			structure = pdbreader.getStructureById(pdbFile);
		} catch (IOException e){
			e.printStackTrace();
		}

		// get the chain
		String chainId = req.getParameter("chainId");
		try {
			chain = structure.getChainByPDB(chainId);
		} catch (StructureException e) {
			e.printStackTrace();
		}

		// Returns the sequence of amino acids as provided in the ATOM records, not a list of atoms.
		String aminoSequence = chain.getAtomSequence();
		// TODO do we want all atom groups, or specific ones? 
		aminoGroupList = chain.getAtomGroups("amino");
		System.out.println("aminoGroupList size = " + aminoGroupList.size());

		
		// first parse, then get the result
		try {
			secFeatureParser = new PdbChainFeaturesDasParser(pdbFile, chainId);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String secStructureSymbols = secFeatureParser.getDsspString();
		String secStructureSymbolsReturn = "";
		System.out.println("length of secStructureSymbols = " + secStructureSymbols.length());
		System.out.println("secStructureSymbols in fetcher = " + secStructureSymbols);
		String secStructureSymbol;
		int secStrucSymbolIndex = -1;
		atomAcidTuples.clear();
		Iterator<Group> aminoGroupItr = aminoGroupList.iterator();
		
		
		
		System.out.println("about to do while");
		while (aminoGroupItr.hasNext()) {
			secStrucSymbolIndex++;
			String seqPos = String.valueOf(secStrucSymbolIndex + 1);
			Group group = aminoGroupItr.next();
			String pdbName = group.getPDBName();
			AminoAcid aaGroup = (AminoAcid)group;
			// get the single letter amino acid symbol
			String aaSymbol = Character.toString(aaGroup.getAminoType());
			System.out.println("aaSymbol = " + aaSymbol);
			if (secStrucSymbolIndex+1 > secStructureSymbols.length())
				secStructureSymbol = " ";
			else
			    secStructureSymbol = secStructureSymbols.substring(secStrucSymbolIndex, secStrucSymbolIndex+1);

			
			atomList = group.getAtoms();
			Iterator<Atom> atomListItr = atomList.iterator();

			int atomListSize = atomList.size();
			for (int i = 0; i < atomListSize; i++) {
				Atom atom = atomList.get(i);
				String atomName = atom.getName();
				String pdbSerial = Integer.toString(atom.getPDBserial());
				String[] atomTuple = new String[6];
				String atomPos = Integer.toString(i + 1);  // base 1
				atomTuple[0] = secStructureSymbol;
				atomTuple[1] = seqPos;
				atomTuple[2] = atomPos;
				atomTuple[3] = aaSymbol;
				atomTuple[4] = atomName;
				atomTuple[5] = pdbSerial;
				atomAcidTuples.add(atomTuple);


			} // end for iteration over atoms

		} // end while iteration over amino groups
		System.out.println("aminoAcidsStr = " + aminoAcidsStr);
		System.out.println("atomNamesListStr = " + atomNamesListStr);
		
		// the response to return will be written to out
		resp.resetBuffer();
		PrintWriter out = resp.getWriter();
		out.println('[');
		// debug logging out.println(chainId);
		out.println("  {");
		out.print("    \"sequence\": \"");
		out.println(aminoSequence + "\",");
		out.print("    \"secStructure\": \"");
		
		if (secStructureSymbols.equals("")) {
			for (int i = 0; i < aminoSequence.length(); i++) {
				secStructureSymbolsReturn = secStructureSymbolsReturn + " ";
			}
		}
		else
			secStructureSymbolsReturn = secStructureSymbols;
			
		out.println(secStructureSymbolsReturn + "\",");
		out.println("    \"atomAcidTuples\": [");	// start tuples list
		// construct atom acid pairs
		for (int i = 0; i < atomAcidTuples.size(); i++) {
			// if it is not yet the last line, include a comma 
			if (i < atomAcidTuples.size() - 1) {
				out.println("      { \"secStructureSymbol\":\"" + atomAcidTuples.get(i)[0] + "\", \"seqPosition\":\"" + atomAcidTuples.get(i)[1] + "\", \"atomPosition\":\"" + atomAcidTuples.get(i)[2] + "\", \"aminoAcidName\":\"" +  atomAcidTuples.get(i)[3] + "\", \"atomName\":\"" + atomAcidTuples.get(i)[4] + "\", \"pdbSerial\":\"" + atomAcidTuples.get(i)[5] + "\" },");
			} else {
				out.println("      { \"secStructureSymbol\":\"" + atomAcidTuples.get(i)[0] + "\", \"seqPosition\":\"" + atomAcidTuples.get(i)[1] + "\", \"atomPosition\":\"" + atomAcidTuples.get(i)[2] + "\", \"aminoAcidName\":\"" +  atomAcidTuples.get(i)[3] + "\", \"atomName\":\"" + atomAcidTuples.get(i)[4] + "\", \"pdbSerial\":\"" + atomAcidTuples.get(i)[5] + "\" }");
			}
		}
        out.println("     ]"); // end tuples list
		out.println("  },");

		out.println(']'); // end the response
		out.flush();
		out.close();
		
	}
	@Override
	public void destroy() {
		System.out.println("destroy called");
	}
	
}
