package edu.uri.cs.gwt.plat.client;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.Constants;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.TextBoxBase;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;


/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class PLAT implements EntryPoint, ClickHandler, KeyPressHandler, ChangeHandler {
	
	private static Logger rootLogger = Logger.getLogger("");

	private Label errorMsgLabel = new Label();
	private Label seq1Label = new Label("Sequence 1:");
	private Label seq2Label = new Label("Sequence 2:");
	private SequenceUIPanel panel1 = new SequenceUIPanel();
	private SequenceUIPanel panel2 = new SequenceUIPanel();
	private Button alignButton = new Button("Align");
	private DisplayGeneratedModelPanel resultsDisplay;// = new DisplayGeneratedModelPanel();
	private VerticalPanel transMatrixPanel = new VerticalPanel();
	private VerticalPanel rotMatrixPanel = new VerticalPanel();
	private VerticalPanel rmsdPanel = new VerticalPanel();
	private Label dispResultsLabel = new Label("Translation and Rotation to Superimpose Structure 2 on Structure 1 (with minimum RMSD for the selected amino acids):");
	private Label dispTransLabel = new Label("Translation Matrix");
	private Label dispRotLabel = new Label("Rotation Matrix");
	private Label dispSelRMSDLabel = new Label("Selected Amino Acid RMSD");
	private Grid transMatGrid = new Grid(1,3);
	private Grid rotMatGrid = new Grid(3,3);
	private Grid rmsdGrid = new Grid(1,1);
	private Button showResultsPageButton = new Button("Render alignment in JMOL");
	
	private static final String ALIGNER_JSON_URL = GWT.getModuleBaseURL() + "aligner?";

	// TODO display this in a status label
	/**
	 * The message displayed to the user when the server cannot be reached or
	 * returns an error.
	 */
	private static final String SERVER_ERROR = "An error occurred while "
		+ "attempting to contact the server. Please check your network "
		+ "connection and try again.";

	/**
	 * The constants used in this Content Widget.
	 */
	public static interface CwConstants extends Constants {
		String cwFlexTableAddRow();

		String cwFlexTableDescription();

		String cwFlexTableDetails();

		String cwFlexTableName();

		String cwFlexTableRemoveRow();
	}
	  
	/**
	 * The GWT entry point method.
	 */
	public void onModuleLoad() {
		
		// Listen for mouse events on the pdb Info button.
		alignButton.addClickHandler(this);
		showResultsPageButton.addClickHandler(this);		
		
		errorMsgLabel.setStyleName("errorMessage");
	    errorMsgLabel.setVisible(false);
		
	    // Associate the main labels and panels with the HTML host page.
		RootPanel.get("platDiv").add(errorMsgLabel);
		RootPanel.get("platDiv").add(seq1Label);
	    RootPanel.get("platDiv").add(panel1);
	    RootPanel.get("platDiv").add(seq2Label);
	    RootPanel.get("platDiv").add(panel2);
	    RootPanel.get("platDiv").add(alignButton);
	    //RootPanel.get("platDiv").add(resultsDisplay);
	    
	    // prepare the translation and rotation result display panels
	    seq1Label.addStyleName("resultLabel");
	    seq2Label.addStyleName("resultLabel");
	    dispResultsLabel.addStyleName("resultLabel");
	    dispTransLabel.addStyleName("resultLabel");
	    dispRotLabel.addStyleName("resultLabel");
	    dispSelRMSDLabel.addStyleName("resultLabel");
	    
	    transMatrixPanel.addStyleName("resultPanel");
	    rotMatrixPanel.addStyleName("resultPanel");
	    rmsdPanel.addStyleName("resultPanel");

	    transMatGrid.addStyleName("resultMatrix");
	    rotMatGrid.addStyleName("resultMatrix");
	    rmsdGrid.addStyleName("resultMatrix");

	    transMatrixPanel.add(dispTransLabel);
	    transMatrixPanel.add(transMatGrid);
	    rotMatrixPanel.add(dispRotLabel);
	    rotMatrixPanel.add(rotMatGrid);
	    rmsdPanel.add(dispSelRMSDLabel);
	    rmsdPanel.add(rmsdGrid);

	}

	/**
	 * Handles click events for all objects
	 */
	public void onClick(ClickEvent event) {
		Widget sender = (Widget)event.getSource();
		
		// handle align requests
		if (sender == alignButton) {
	        // TODO handle null pdbs, chainIds, residue lists
			ArrayList<Integer> panel1Selection = new ArrayList();
			ArrayList<Integer> panel2Selection = new ArrayList();
			
			String p1SelectMode = panel1.getSelectMode();
			String p2SelectMode = panel2.getSelectMode();
				
			if (p1SelectMode == "residues") {
				panel1Selection = panel1.getSelectedResidues();
			}
			else if (p1SelectMode == "atoms") {
				panel1Selection = panel1.getSelectedAtoms();
			}
			
			if (p2SelectMode == "residues") {
				panel2Selection = panel2.getSelectedResidues();
			}
			else if (p1SelectMode == "atoms") {
				panel2Selection = panel2.getSelectedAtoms();
			}

			
			String url = ALIGNER_JSON_URL;
			url += "p1=";
			url += panel1.getPdbFile();
			url += "&p2=";
			url += panel2.getPdbFile();
			url += "&m1=";
			url += panel1.getSelectMode();
			url += "&m2=";
			url += panel2.getSelectMode();
			url += "&c1=";
			url += panel1.getChainId();
			url += "&c2=";
			url += panel2.getChainId();
			url += "&s1=";
			for (int i = 0; i < panel1Selection.size(); i++) {
				rootLogger.log(Level.FINE, "panel1Selection.get(i) = " + panel1Selection.get(i));
				url += panel1Selection.get(i);
				if (i < panel1Selection.size() - 1)
					url += "+";
			}			
			url += "&s2=";
			for (int i = 0; i < panel2Selection.size(); i++) {
				url += panel2Selection.get(i);
				if (i < panel2Selection.size() - 1)
					url += "+";
			}

			rootLogger.log(Level.FINE, "url string = " + url);
			url = URL.encode(url);

			// Send request to server
		    RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);

		    // Reset error message
		    errorMsgLabel.setVisible(false);
		    try {
		    	Request request = builder.sendRequest(null, new RequestCallback() {
		    		public void onError(Request request, Throwable exception) {
		    			displayError("Couldn't retrieve JSON");
		    		}

		    		public void onResponseReceived(Request request, Response response) {
		    			if (200 == response.getStatusCode()) {
		    				rootLogger.log(Level.FINE, "response = " + response.getText());
		    				
		    				JsArray<AlignerResult> alignerResults = asArrayOfAlignerResults(response.getText());
		    				
		    				transMatGrid.setText(0, 0, alignerResults.get(0).getTranMatrixX());
		    				transMatGrid.setText(0, 1, alignerResults.get(0).getTranMatrixY());
		    				transMatGrid.setText(0, 2, alignerResults.get(0).getTranMatrixZ());
		    				
		    				rotMatGrid.setText(0, 0, alignerResults.get(0).getRotMatrix00());
		    				rotMatGrid.setText(0, 1, alignerResults.get(0).getRotMatrix01());
		    				rotMatGrid.setText(0, 2, alignerResults.get(0).getRotMatrix02());
		    				rotMatGrid.setText(1, 0, alignerResults.get(0).getRotMatrix10());
		    				rotMatGrid.setText(1, 1, alignerResults.get(0).getRotMatrix11());
		    				rotMatGrid.setText(1, 2, alignerResults.get(0).getRotMatrix12());
		    				rotMatGrid.setText(2, 0, alignerResults.get(0).getRotMatrix20());
		    				rotMatGrid.setText(2, 1, alignerResults.get(0).getRotMatrix21());
		    				rotMatGrid.setText(2, 2, alignerResults.get(0).getRotMatrix22());
		    				
		    				rmsdGrid.setText(0, 0, alignerResults.get(0).getRmsdSelectedAtoms());
		                    
		    				RootPanel.get("platDiv").add(dispResultsLabel);
		    				RootPanel.get("platDiv").add(transMatrixPanel);
		    				RootPanel.get("platDiv").add(rotMatrixPanel);
		    				RootPanel.get("platDiv").add(rmsdPanel);
		    				RootPanel.get("platDiv").add(showResultsPageButton); // TODO 

		    			} else {
		    				displayError("Couldn't retrieve JSON (" + response.getStatusText() + ")");
		    			}
		    		}
		    	});
		    	
		    } catch (RequestException e) {
		    	displayError("Couldn't retrieve JSON");
		    }
		} else if (sender == showResultsPageButton)
		{
			rootLogger.log(Level.FINE, "showResultsPageButton clicked");
			Window.open("jmol_display.html", "_blank", "");
			//Window.open("jsmol_display.html", "_blank", "");
		}
	}


	/**
	 * Handles keyPress events for all objects
	 */
    public void onKeyPress(KeyPressEvent event) {
        Widget sender = (Widget)event.getSource();

    }

	/**
	 * Handles change events for all objects
	 */
    public void onChange(ChangeEvent event) {
    	Widget sender = (Widget)event.getSource();
    	
    }

    /**
     * Convert JSON string into JavaScript object.
     */
    private final native JsArray<AlignerResult> asArrayOfAlignerResults(String json) /*-{
        return eval(json);
    }-*/;
    
    /**
     * Display error message.
     * @param error
     */
    private void displayError(String error) {
    	errorMsgLabel.setText("Error: " + error);
    	errorMsgLabel.setVisible(true);
    }
    
    /**
     * For diagnostic purposes
     */
    private static native void testit()/*-{
        $wnd.alert("it's full of stars");
    }-*/;

}
