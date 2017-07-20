package edu.uri.cs.gwt.plat.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.io.PDBFileReader;

/**
 * The servlet that accesses the library to obtain RCSB PDB chains.
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
public class PdbChainFetcher extends HttpServlet {
	
	// pdb files are stored in a local directory automatically maintained by PDBFileReader
	private String pathToPDBdir = "/plat/plat_jmol/pdb/";
	private PDBFileReader pdbreader = new PDBFileReader();
	
	private Structure structure = null;
	private List<Chain> chainList;
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException {

		pdbreader.setPath(pathToPDBdir);
		pdbreader.setAutoFetch(true);
		
		// get the structure
		String pdbFile = req.getParameter("pdbFileName");
		System.out.println("pdbFileName = " + pdbFile);
		try {
			structure = pdbreader.getStructureById(pdbFile);
		} catch (IOException e){
			e.printStackTrace();
		}
		// debug logging System.out.println("pdbFile requested = " + pdbFile);
		chainList = structure.getChains();
		Iterator<Chain> chainItr = chainList.iterator();
		
		// the response to return will be written to out
		PrintWriter out = resp.getWriter();
		out.println('[');
		while(chainItr.hasNext()) {
			String chainId = chainItr.next().getChainID();
			// debug logging out.println(chainId);
			out.println("  {");
			out.print("    \"chainId\": \"");
			out.println(chainId + "\"");
			out.println("  },");
		}
		out.println(']'); // end the response
		out.flush();
		
	}
}
