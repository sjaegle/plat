package edu.uri.cs.gwt.plat.server;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class JmolDisplayBuilder {

	private String outputFilePath = "/Library/apache-tomcat-5.5.33_plat/webapps/plat/";
	private String outputFileName;
	private String outputFile;
	
	public JmolDisplayBuilder(String outputFileId) throws FileNotFoundException {
		
		outputFileName = "jmol_output_" + outputFileId + ".html";
		outputFile = outputFilePath + outputFileName;
		
		// write to a file for a viewer
		FileOutputStream outFile = new FileOutputStream(outputFile);
		PrintStream p = new PrintStream(outFile);
		
		p.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
		p.println("<html>");
		p.println("<head>");
		p.println("  <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">");
		p.println("  <title>JMOL output rendering PoC</title>");
		p.println("  <script type=\"text/javascript\" language=\"javascript\" src=\"plat/jmol/Jmol.js\"></script>");
		p.println("</head>");
		p.println("<body>");
		p.println("  <iframe src=\"javascript:''\" id=\"__gwt_historyFrame\" tabIndex='-1' style=\"position:absolute;width:0;height:0;border:0\"></iframe>");
		p.println("  <form>");
		p.println("    <script type=\"text/javascript\">");
		p.println("    jmolInitialize(\"jmol\", true);");
		p.println("    jmolSetAppletColor(\"white\"); // background color");
		p.println("    jmolApplet(580, \"load files '" + outputFileName + "'\");");
		p.println(" ");
		p.println("jmolBr();");
		p.println("jmolHtml(\"atoms \");");
		p.println(" ");
		p.println("// a radio group:");
		p.println("jmolRadioGroup([");
		p.println("[\"spacefill off\", \"off\"],");
		p.println("[\"spacefill 20%\", \"20%\", \"checked\"],");
		p.println("[\"spacefill 100%\", \"100%\"]");
		p.println("]);");
		p.println(" ");
		p.println("jmolBr();");
		p.println(" ");
		p.println("// a button:");
		p.println("//jmolButton(\"reset\", \"Reset orientation\");");
		p.println("jmolButton(\"hide null; select model=1; spacefill off; wireframe off; backbone 0.4;\" +");
		p.println("           \"cartoon off; \" +");
		p.println("           \"color backbone yellow;\" +");
		p.println("           \"select ligand; wireframe 0.16; spacefill 0.5; color cpk; \" +");
		p.println("           \"select *.FE; spacefill 0.7; color cpk;\" +");
		p.println("           \"select *.CU; spacefill 0.7; color cpk;\" +");
		p.println("           \"select *.ZN; spacefill 0.7; color cpk;\" +");
		p.println("           \"select all; model 0\",");
		p.println("           \"Backbone model 1\");");
		p.println("jmolButton(\"hide null; select model=2; spacefill off; wireframe off; backbone 0.4;\" +");
		p.println("           \"cartoon off; \" +");
		p.println("           \"color backbone yellow;\" +");
		p.println("           \"select ligand; wireframe 0.16; spacefill 0.5; color cpk; \" +");
		p.println("           \"select *.FE; spacefill 0.7; color cpk;\" +");
		p.println("           \"select *.CU; spacefill 0.7; color cpk;\" +");
		p.println("           \"select *.ZN; spacefill 0.7; color cpk;\" +");
		p.println("           \"select all; model 0\",");
		p.println("           \"Backbone model 2\");");
		p.println("jmolButton(\"hide null; select model=3; spacefill off; wireframe off; backbone 0.4;\" +");
		p.println("           \"cartoon off; \" +");
		p.println("           \"color backbone yellow;\" +");
		p.println("           \"select ligand; wireframe 0.16; spacefill 0.5; color cpk; \" +");
		p.println("           \"select *.FE; spacefill 0.7; color cpk;\" +");
		p.println("           \"select *.CU; spacefill 0.7; color cpk;\" +");
		p.println("           \"select *.ZN; spacefill 0.7; color cpk;\" +");
		p.println("           \"select all; model 0\",");
		p.println("           \"Backbone model 3\");");
		p.println(" ");
		p.println("jmolBr();");
		p.println(" ");
		p.println("// a checkbox:");
		p.println("jmolCheckbox(\"spin on\", \"spin off\", \"spin\");");
		p.println(" ");
		p.println("jmolBr();");
		p.println("jmolBr();");
		p.println(" ");
		p.println("</script>");
		p.println("</form>");
		p.println("</body>");
		p.println("</html>");
		
		
	} // end constructor
}
