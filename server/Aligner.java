package edu.uri.cs.gwt.plat.server;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.biojava.bio.structure.Atom;
import org.biojava.bio.structure.Calc;
import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.ChainImpl;
import org.biojava.bio.structure.Group;
import org.biojava.bio.structure.HetatomImpl;
import org.biojava.bio.structure.SVDSuperimposer;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureException;
import org.biojava.bio.structure.StructureImpl;
import org.biojava.bio.structure.io.FileParsingParameters;
import org.biojava.bio.structure.io.PDBFileReader;
import org.biojava.bio.structure.jama.Matrix;

/**
 * The servlet that accesses the library to perform the alignment and creates 
 * the output file.
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
public class Aligner extends HttpServlet {
	
	// TODO need better paths
	private String workingDir = "";
	private String outputFile = "";
	//private String outputFile = "/Library/apache-tomcat-5.5.33_plat/webapps/plat/rot_trans5.pdb";
	// pdb files are stored in a local directory automatically maintained by PDBFileReader
	private String pathToPDBdir = "/plat/plat_jmol/pdb/";
	private PDBFileReader pdbReader1 = new PDBFileReader();
	private PDBFileReader pdbReader2 = new PDBFileReader();
	
	private FileParsingParameters fileParsingParameters = new FileParsingParameters();
	
	private String pdbReader1SelectMode = "";
	private String pdbReader2SelectMode = "";
	
	private Structure structure1 = null;
	private Structure structure2 = null;
	
	private Chain chain1, chain2, chain3;
	
	private List<Group> aminoGroupList1, aminoGroupList2, aminoGroupList3;
	private List<Atom> atomList;
	
	private TreeMap<Integer, Atom> atomMap1 = new TreeMap();
	private TreeMap<Integer, Atom> atomMap2 = new TreeMap();
	private TreeMap<Integer, Atom> atomMap3 = new TreeMap();
	
	private Matrix rotMatrix = null;


	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException {

		String platDir = this.getServletContext().getRealPath("/PLAT.html");
		workingDir = platDir.replace("PLAT.html", "");
		outputFile = workingDir + "rot_trans5.pdb";
		System.out.println("outputFile = " + outputFile);
		
		pdbReader1.setPath(pathToPDBdir);
		pdbReader1.setAutoFetch(true);
		pdbReader2.setPath(pathToPDBdir);
		pdbReader2.setAutoFetch(true);
		// TODO consider setPdbDirectorySplit(true);
		

		
		String pdbFile1 = req.getParameter("p1");
		try {
			structure1 = pdbReader1.getStructureById(pdbFile1);
		} catch (IOException e){
			e.printStackTrace();
		}


		String pdbFile2 = req.getParameter("p2");
		try {
			structure2 = pdbReader2.getStructureById(pdbFile2);
		} catch (IOException e){
			e.printStackTrace();
		}

		String selectMode1 = req.getParameter("m1");
		String selectMode2 = req.getParameter("m2");
		
		System.out.println("select mode 1 = " + selectMode1);
		System.out.println("select mode 2 = " + selectMode2);
		
		String chainId1 = req.getParameter("c1");
		String chainId2 = req.getParameter("c2");
		
		String[] selects1 = req.getParameter("s1").split(" ");
		String[] selects2 = req.getParameter("s2").split(" ");

		Atom[] atoms1 = new Atom[selects1.length];
		Atom[] atoms2 = new Atom[selects2.length];
		Atom[] atoms2Rmsd = new Atom[selects2.length]; // used for RMSD atoms translation and rotation
		                                

		// prepare atom arrays for svd
		if (selectMode1.equals("residues")) {
			System.out.println("handling residues in 1");
			for (int i = 0; i < selects1.length; i++) {
				try {
					int residueNum = Integer.parseInt(selects1[i]);
					Group gr = (Group)structure1.getChainByPDB(chainId1).getAtomGroup(residueNum - 1).clone();
					atoms1[i] = gr.getAtom(" CA ");
				}
				catch (Exception e) {
					// TODO
				}
			} // end for
			// output coords for RMS POC diagnostic
			System.out.println("atoms1 initial coordinates");
			System.out.println("X, Y, Z");
			for (Atom a : atoms1) {
				System.out.println(a.getX() + ", " + a.getY() + ", " + a.getZ());
			}
			
		} // end if residues
		
		else if (selectMode1.equals("atoms")) {
			System.out.println("handling atoms in 1");
			// first build a model of the chain, its groups, and their atoms
			pdbReader1.setPath(pathToPDBdir);
			pdbReader1.setAutoFetch(true);
			// parse author designated secondary structure
			fileParsingParameters.setParseSecStruc(true);
			pdbReader1.setFileParsingParameters(fileParsingParameters);

			// get the structure
			try {
				structure1 = pdbReader1.getStructureById(pdbFile1);
			} catch (IOException e){
				e.printStackTrace();
			}
			
			// retrieve the chain
			try {
				chain1 = structure1.getChainByPDB(chainId1);
			} catch (StructureException e) {
				e.printStackTrace();
			}

			// retrieve the groups, aka amino acids, within the chain
			aminoGroupList1 = chain1.getAtomGroups("amino");
			
			if (atomMap1 != null) atomMap1.clear();
			Iterator<Group> aminoGroupItr = aminoGroupList1.iterator();
			while (aminoGroupItr.hasNext()) {
				Group group = aminoGroupItr.next();
				List<Atom> atomList = group.getAtoms();
				Iterator<Atom> atomListItr = atomList.iterator();
				int atomListSize = atomList.size();
				while (atomListItr.hasNext()) {
					Atom atom = atomListItr.next();
					Integer pdbSerial = new Integer(atom.getPDBserial());
					atomMap1.put(pdbSerial, atom);
				} // end while iteration over atoms
				
				
			} // end while iteration over groups aka amino acids; map is complete			
			System.out.println("Atom map 1 is size " + atomMap1.size());
			
			for (int i = 0; i < selects1.length; i++) {
				Integer pdbSerialSelected = new Integer(Integer.parseInt(selects1[i]));
				atoms1[i] = atomMap1.get(pdbSerialSelected);
				String atomName = atoms1[i].getName();
				String pdbSerial = Integer.toString(atoms1[i].getPDBserial());
				System.out.println("atom name, serial number = " + atomName + ", " + pdbSerial);
			}
			
		} // end if atoms

		if (selectMode2.equals("residues")) {
			System.out.println("handling residues in 2");
			for (int i = 0; i < selects2.length; i++) {
				try {
					int residueNum = Integer.parseInt(selects2[i]);
					Group gr = (Group)structure2.getChainByPDB(chainId2).getAtomGroup(residueNum - 1).clone();
					atoms2[i] = gr.getAtom(" CA ");
				}
				catch (Exception e) {
					// TODO
				}
			} // end for
			
			// output coords for RMS POC diagnostic
			System.out.println("atoms2 initial coordinates");
			System.out.println("X, Y, Z");
			for (Atom a : atoms2) {
				System.out.println(a.getX() + ", " + a.getY() + ", " + a.getZ());
			}

			
		} // end if residues
		else if (selectMode2.equals("atoms")) {
			System.out.println("handling atoms in 2");
			// first build a model of the chain, its groups, and their atoms
			pdbReader2.setPath(pathToPDBdir);
			pdbReader2.setAutoFetch(true);
			// parse author designated secondary structure
			fileParsingParameters.setParseSecStruc(true);
			pdbReader2.setFileParsingParameters(fileParsingParameters);

			// get the structure
			try {
				structure2 = pdbReader2.getStructureById(pdbFile2);
			} catch (IOException e){
				e.printStackTrace();
			}



			// retrieve the chain
			try {
				chain2 = structure2.getChainByPDB(chainId2);
			} catch (StructureException e) {
				e.printStackTrace();
			}

			// retrieve the groups, amino acids, within the chain
			aminoGroupList2 = chain2.getAtomGroups("amino");

			if (atomMap2 != null) atomMap2.clear();
			Iterator<Group> aminoGroupItr = aminoGroupList2.iterator();
			while (aminoGroupItr.hasNext()) {
				Group group = aminoGroupItr.next();
				List<Atom> atomList = group.getAtoms();
				Iterator<Atom> atomListItr = atomList.iterator();
				int atomListSize = atomList.size();
				while (atomListItr.hasNext()) {
					Atom atom = atomListItr.next();
					Integer pdbSerial = new Integer(atom.getPDBserial());
					atomMap2.put(pdbSerial, atom);
				} // end while iteration over atoms


			} // end while iteration over groups aka amino acids; map is complete			
			System.out.println("Atom map 2 is size " + atomMap2.size());

			for (int i = 0; i < selects2.length; i++) {
				Integer pdbSerialSelected = new Integer(Integer.parseInt(selects2[i]));
				atoms2[i] = atomMap2.get(pdbSerialSelected);
				String atomName = atoms2[i].getName();
				String pdbSerial = Integer.toString(atoms2[i].getPDBserial());
				System.out.println("atom name, serial number = " + atomName + ", " + pdbSerial);
			}

		} // end if atoms
		
		// copy atoms2 into atoms2Rmsd for purposes of calculating RMSD for the selected acids
		for (int i = 0; i < atoms2.length; i++) {
			atoms2Rmsd[i] = (Atom) atoms2[i].clone();
		}
		
		
		// the response to return will be written to out
		PrintWriter out = resp.getWriter();
		out.println('[');
		try {
			// set up the svd superimposer object, determine rotation and translation
			SVDSuperimposer svdi = new SVDSuperimposer(atoms1,atoms2);
			Matrix rotMatrix = svdi.getRotation();
			Atom tranMatrix = svdi.getTranslation();  // getTranslation returns an atom object
			
			for (Atom a : atoms2Rmsd) {
				Calc.rotate(a, rotMatrix);
				Calc.shift(a, tranMatrix);
			}

			
			// output coords for RMS POC diagnostic
			System.out.println("atoms2 coordinates post rotation and translation");
			System.out.println("X, Y, Z");
			for (Atom a : atoms2Rmsd) {
				System.out.println(a.getX() + ", " + a.getY() + ", " + a.getZ());
			}

			
			
			double rmsdSelectedAtoms = SVDSuperimposer.getRMS(atoms1, atoms2Rmsd);
			System.out.println("RMSD = " + rmsdSelectedAtoms);

			System.out.println("rotMatrix : " + rotMatrix);
			System.out.println("tranMatrix : " + tranMatrix.getX() + " " + tranMatrix.getY() + " " + tranMatrix.getZ());

			out.println("  {");
			out.print("    \"tranMatrixX\": \"");
			out.println(tranMatrix.getX() + "\",");  // Atom get method

			out.print("    \"tranMatrixY\": \"");
			out.println(tranMatrix.getY() + "\",");  // Atom get method

			out.print("    \"tranMatrixZ\": \"");
			out.println(tranMatrix.getZ() + "\",");  // Atom get method

			out.print("    \"rotMatrix00\": \"");
			out.println(rotMatrix.get(0,0) + "\",");

			out.print("    \"rotMatrix01\": \"");
			out.println(rotMatrix.get(0,1) + "\",");

			out.print("    \"rotMatrix02\": \"");
			out.println(rotMatrix.get(0,2) + "\",");

			out.print("    \"rotMatrix10\": \"");
			out.println(rotMatrix.get(1,0) + "\",");

			out.print("    \"rotMatrix11\": \"");
			out.println(rotMatrix.get(1,1) + "\",");

			out.print("    \"rotMatrix12\": \"");
			out.println(rotMatrix.get(1,2) + "\",");

			out.print("    \"rotMatrix20\": \"");
			out.println(rotMatrix.get(2,0) + "\",");

			out.print("    \"rotMatrix21\": \"");
			out.println(rotMatrix.get(2,1) + "\",");
			
			out.print("    \"rotMatrix22\": \"");
			out.println(rotMatrix.get(2,2) + "\",");
			
			out.print("    \"rmsdSelectedAtoms\": \"");
			out.println(rmsdSelectedAtoms + "\"");
			
			out.println("  },");
			
			// apply rotation and translation to structure2
			Calc.rotate(structure2, rotMatrix);
			Calc.shift(structure2, tranMatrix);
			
			// write to a file for a viewer
			FileOutputStream outFile = new FileOutputStream(outputFile);
			PrintStream p = new PrintStream(outFile);
			
			// implement a PDB structure for file output
			Structure pdbOutStruct = new StructureImpl();
			// declare that structure has been solved by NMR
			pdbOutStruct.setNmr(true); 
			
			// add the appropriate chain from structure 1 to output
			// @TODO factor c1
			Chain c1 = structure1.getChainByPDB(chainId1);
			c1.setChainID("A");
			List l1 = new ArrayList();
			l1.add(c1);
			pdbOutStruct.addModel(l1);
			
			// add the rotated and translated chain from structure 2 to output
			// @TODO factor c2
			Chain c2 = structure2.getChainByPDB(chainId2);
			c2.setChainID("B");
			List l2 = new ArrayList();
			l2.add(c2);
			pdbOutStruct.addModel(l2);

			// write to the print stream and close
			p.println(pdbOutStruct.toPDB());
			p.close();

			System.out.println("wrote to file " + outputFile);
		}
		catch (Exception e) {
			// TODO
		}
		
		
		
		out.println(']');
		out.flush();
		
	}
}
