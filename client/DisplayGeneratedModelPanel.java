package edu.uri.cs.gwt.plat.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;

public class DisplayGeneratedModelPanel extends Composite implements
		ClickHandler {

	private HorizontalPanel mainPanel = new HorizontalPanel();
	private FlowPanel jmolViewerPanel = new FlowPanel();
	private FlowPanel jmolControlPanel = new FlowPanel();
	
	public DisplayGeneratedModelPanel() {
		testit();
		showit();
	    String jmolButton1Script = "jmolButton(hide null; select model=1; spacefill off; wireframe off; backbone 0.4;" +
	    		     "cartoon off; " +
	    		     "color backbone yellow;" +
	    		     "select ligand; wireframe 0.16; spacefill 0.5; color cpk; " +
	                 "select *.FE; spacefill 0.7; color cpk;" +
	                 "select *.CU; spacefill 0.7; color cpk;" +
	                 "select *.ZN; spacefill 0.7; color cpk;" +
	                 "select all; model 0," +
	                 "Backbone model 1)";
	    
	    System.out.println(jmolButton1Script);
	    
	    Frame frame = new Frame("jmol_manualpoc.html");
	    frame.setSize("700px", "700px");
	    
		//jmolViewerPanel.add(html);
	    jmolViewerPanel.add(frame);
		mainPanel.add(jmolViewerPanel);
		
		initWidget(mainPanel);
		
	} // end default constructor

	public void onClick(ClickEvent event) {
		// TODO Auto-generated method stub

	}
	
	private static native void showit()/*-{
		//var theApplet = document.getElementById(jmolApplet0);
        //$doc.jmolSetAppletColor("black"); // background color
        //theApplet.jmolSetAppletColor("white"); // background color
        //$wnd.alert("color set, about to load applet");
        //$wnd.jmolApplet(500, "load files 'rot_trans.pdb'");
        //$wnd.jmolScript("load files 'rot_trans.pdb'");
        //$wnd.alert("applet loaded");
	}-*/;
	
	private static native void testit()/*-{
	    $wnd.alert("hello world");
    }-*/;


}
