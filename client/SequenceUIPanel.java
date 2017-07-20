package edu.uri.cs.gwt.plat.client;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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
//import com.google.gwt.event.dom.client.MouseEvent;
//import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.core.java.util.Arrays;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A container for sequence and structure selection
 * 
 * @author stephenjaegle
 *
 */
public class SequenceUIPanel extends Composite implements ClickHandler, KeyPressHandler, ChangeHandler {

	private static Logger rootLogger = Logger.getLogger("");
	
	private FlowPanel mainPanel = new FlowPanel();
	private HorizontalPanel getPDBPanel = new HorizontalPanel();
	private TextBox getPDBInfoTextBox = new TextBox();
	private Button getPDBInfoButton = new Button("Get PDB Info");
	private ListBox chainList = new ListBox();
	private HorizontalPanel chooseChainPanel = new HorizontalPanel();
	private Label dispPdbIdLabel = new Label();
	private Button getSeqForStructAndChainButton = new Button("Get Sequence for Chain");
	private Button getAtomsForStructAndChainButton = new Button("Get Atoms for Chain");
	private VerticalPanel seqDispPanel = new VerticalPanel();
	private HorizontalPanel seqStructurePanel = new HorizontalPanel();
	private HorizontalPanel seqAcidPanel = new HorizontalPanel();
	private HorizontalPanel targetSelectPanel = new HorizontalPanel(); // panel to contain target selections and tools
	private ScrollPanel targetGridScrollPanel = new ScrollPanel();
	private Grid targetGrid; // rectangular grid that can contain text, html, or a child Widget within its cells; must be resized explicitly.
	private VerticalPanel targetSelectedPanel = new VerticalPanel();
	private TextBox getSeqStartTextBox = new TextBox();
	private TextBox getSeqStopTextBox = new TextBox();
	private ArrayList<ArrayList> selections = new ArrayList<ArrayList>();
	
	// Create the service proxy class
	//private PDBSoapServiceAsync pdbSoapSvcAsync = GWT.create(PDBSoapService.class);
	
	private String targetSequence = "";		// the target sequence
	private String targetStructure = "";    // the target structure
	
	private String pdbFile;
	private String chainId;
	
	private boolean toggleSeqSelect[];
	private TextBox toggleSeqSelectTextBox[];  // the widget inserted into targetgrid cells 
	private ToggleButton targetStructToggles[];
	private ToggleButton targetAcidToggles[];
	private boolean toggleAtomSelect[];
	private TextBox toggleAtomSelectTextBox[];
	
	private int lastSeqPosition = 1;	
	private int fromSeqPos = 0;
	private int toSeqPos = 0;
	
	private int lastAtomPosition = 1;
	private int fromAtomPos = 0;
	private int toAtomPos = 0;
	
	private String idDelimiter = " ";
	
	private int gridDataWidth = 60; // 60 is used by RCSB PDB
	
	private static final String JSON_URL = GWT.getModuleBaseURL();
	
	private String selectMode = ""; // user may select residues or atoms


	public SequenceUIPanel() {
		
		// Assemble getPDB panel.
		getPDBPanel.add(getPDBInfoTextBox);
		getPDBPanel.add(getPDBInfoButton);

		// Assemble chooseChain panel.
		chooseChainPanel.add(dispPdbIdLabel);
		chooseChainPanel.add(chainList);
		chooseChainPanel.add(getSeqForStructAndChainButton);
		chooseChainPanel.add(getAtomsForStructAndChainButton);

		// Assemble sequence display panel, currently empty as seqStructurePanel and seqAcidPanel contain nothing
		seqDispPanel.add(seqStructurePanel);
		seqDispPanel.add(seqAcidPanel);

		// Assemble Select panel
		targetGrid = new Grid(8, gridDataWidth);
		targetGrid.setBorderWidth(0);
		targetGridScrollPanel.add(targetGrid);
		targetSelectPanel.add(targetGridScrollPanel);
		targetSelectPanel.setBorderWidth(2);

		targetGrid.setWidget(1, 1, new Button("Sequence and secondary structure information will be displayed here."));

		// Assemble Main panel.
		mainPanel.add(getPDBPanel);
		mainPanel.add(chooseChainPanel);
		mainPanel.add(seqDispPanel);
		mainPanel.add(targetSelectPanel);

		// Listen for mouse events on the pdb Info button.
		getPDBInfoButton.addClickHandler(this);

		// Listen for keyboard events in the pdb file input box.
		getPDBInfoTextBox.addKeyPressHandler(this);

		// Listen for mouse events on the sequence and atoms buttons
		getSeqForStructAndChainButton.addClickHandler(this);
		getAtomsForStructAndChainButton.addClickHandler(this);

		initWidget(mainPanel);

	} // end constructor
	
	/**
	 * Handles click events for all objects
	 */
	public void onClick(ClickEvent event) {
		Widget sender = (Widget)event.getSource();

		if (sender == getPDBInfoButton) {
			fetchPDBChains();
		}
		else if (sender == getSeqForStructAndChainButton) {
			selectMode = "residues";
			fetchSequenceForStructureAndChain();
		}
		else if (sender == getAtomsForStructAndChainButton) {
			selectMode = "atoms";
			fetchAtomsForStructureAndChain();
		}
		else if (sender.getParent() == targetGrid) {
			int row = targetGrid.getCellForEvent(event).getRowIndex();
			int col = targetGrid.getCellForEvent(event).getCellIndex();
			String id = sender.getElement().getId();

			if (selectMode.equals("residues")) {
				rootLogger.log(Level.FINE, "position id = " + id);
				String seqPosStr[] = id.split(idDelimiter);
				int seqPos = Integer.parseInt(seqPosStr[2]);
				rootLogger.log(Level.FINE, "position id " + seqPos + " clicked");
				if (event.isShiftKeyDown()) {
					rootLogger.log(Level.FINE, "seqPos = " + seqPos + " lastSeqPosition = " + lastSeqPosition);
					// determine which direction to go
					if (seqPos > lastSeqPosition) {
						fromSeqPos = lastSeqPosition;
						toSeqPos = seqPos;
					}
					else {
						fromSeqPos = seqPos;
						toSeqPos = lastSeqPosition;
					}
					rootLogger.log(Level.FINE, "fromSeqPos = " + fromSeqPos + " toSeqPos = " + toSeqPos);
					for (int i = fromSeqPos; i <= toSeqPos; i++) {
						toggleSeqSelect[i - 1] = true;
						toggleSeqSelectTextBox[i - 1].addStyleDependentName("down");
					}
				}
				else if (event.isControlKeyDown() || event.isMetaKeyDown() || event.isAltKeyDown() ) {
					rootLogger.log(Level.FINE, "ctrl seqPos = " + seqPos + " lastSeqPosition = " + lastSeqPosition);
					if (!toggleSeqSelect[seqPos - 1]) {
						toggleSeqSelect[seqPos - 1] = true;
						toggleSeqSelectTextBox[seqPos - 1].addStyleDependentName("down");
					}
					else {
						toggleSeqSelect[seqPos - 1] = false;
						toggleSeqSelectTextBox[seqPos - 1].removeStyleDependentName("down");
					}
					lastSeqPosition = seqPos;
				}
				else {
					for (int i = 1; i <= toggleSeqSelect.length; i++) {
						toggleSeqSelect[i - 1] = false;
						toggleSeqSelectTextBox[i - 1].removeStyleDependentName("down");
					}
					toggleSeqSelect[seqPos - 1] = true;
					toggleSeqSelectTextBox[seqPos - 1].addStyleDependentName("down");
					lastSeqPosition = seqPos;
				}
			}  // end if select residues
			else if (selectMode.equals("atoms")) {
				rootLogger.log(Level.FINE, "we are doing atoms");
				rootLogger.log(Level.FINE, "position id = " + id);
				String atomPosStr[] = id.split(idDelimiter);
				int atomPos = Integer.parseInt(atomPosStr[5]);
				rootLogger.log(Level.FINE, "position id " + atomPos + " clicked");
				if (event.isShiftKeyDown()) {
					rootLogger.log(Level.FINE, "atomPos = " + atomPos + " lastAtomPosition = " + lastAtomPosition);
					// determine which direction to go
					if (atomPos > lastAtomPosition) {
						fromAtomPos = lastAtomPosition;
						toAtomPos = atomPos;
					}
					else {
						fromAtomPos = atomPos;
						toAtomPos = lastAtomPosition;
					}
					rootLogger.log(Level.FINE, "fromAtomPos = " + fromAtomPos + " toAtomPos = " + toAtomPos);
					for (int i = fromAtomPos; i <= toAtomPos; i++) {
						toggleAtomSelect[i - 1] = true;
						toggleAtomSelectTextBox[i - 1].addStyleDependentName("down");
					}
				}
				else if (event.isControlKeyDown() || event.isMetaKeyDown() || event.isAltKeyDown() ) {
					rootLogger.log(Level.FINE, "ctrl atomPos = " + atomPos + " lastAtomPosition = " + lastAtomPosition);
					if (!toggleAtomSelect[atomPos - 1]) {
						toggleAtomSelect[atomPos - 1] = true;
						toggleAtomSelectTextBox[atomPos - 1].addStyleDependentName("down");
					}
					else {
						toggleAtomSelect[atomPos - 1] = false;
						toggleAtomSelectTextBox[atomPos - 1].removeStyleDependentName("down");
					}
					lastAtomPosition = atomPos;
				}
				else {
					for (int i = 1; i <= toggleAtomSelect.length; i++) {
						toggleAtomSelect[i - 1] = false;
						toggleAtomSelectTextBox[i - 1].removeStyleDependentName("down");
					}
					toggleAtomSelect[atomPos - 1] = true;
					toggleAtomSelectTextBox[atomPos - 1].addStyleDependentName("down");
					lastAtomPosition = atomPos;
				}
			} // end if select atoms
			
		} // end if targetGrid
		
	}

	/**
	 * Handles keyPress events for all objects
	 */
    public void onKeyPress(KeyPressEvent event) {
        Widget sender = (Widget)event.getSource();
        if (sender == getPDBInfoTextBox) {
        	if (event.getCharCode() == KeyCodes.KEY_ENTER) {
        		fetchPDBChains();
        	}
        } 
    }

	/**
	 * Handles change events for all objects
	 */
    public void onChange(ChangeEvent event) {
    	Widget sender = (Widget)event.getSource();
    	
    	if (sender == chainList) {
    		fetchSequenceForStructureAndChain();
    	}
    }
	
    /**
     * Get PDB info. Executed when the user clicks the getPDBInfoButton or
     * presses enter in the newSymbolTextBox.
     */
    private void fetchPDBChains() {
    	pdbFile = getPDBInfoTextBox.getText().toUpperCase().trim();
    	
    	String url = JSON_URL + "pdbchainfetcher?";
    	url += "pdbFileName=" + pdbFile;
    	
    	url = URL.encode(url);
    	// Send request to server
	    RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);
	    
	    try {
	    	Request request = builder.sendRequest(null, new RequestCallback() {
	    		public void onError(Request request, Throwable exception) {
	    			displayError("Couldn't retrieve JSON");
	    		}
	    		// process response
	    		public void onResponseReceived(Request request, Response response) {
	    			if (200 == response.getStatusCode()) {	    				
	    				JsArray<ChainData> chainNames = asArrayOfChainNames(response.getText());
	    				updateChainList(pdbFile, chainNames);
	    				for (int i = 0; i < chainNames.length(); i++) {
	    					rootLogger.log(Level.FINE, chainNames.get(i).getChainId());
	    				}	    				
	    			} else {
	    				displayError("Couldn't retrieve JSON (" + response.getStatusText() + ")");
	    			}
	    		}
	    	});
	    	
	    } catch (RequestException e) {
	    	displayError("Couldn't retrieve JSON");
	    }
    	
    	// cleanup
    	getPDBInfoTextBox.setFocus(true);
    	// TODO validate the file name with a regular expression here
    	// clear the input box
    	getPDBInfoTextBox.setText("");
    	
    } // end fetchPDBChains
    
    /**
     * Updates a list of molecule chains
     * 
     * @param pdbId the PDB file ID
     * @param chainNames the chain names
     */
    public void updateChainList(String pdbId, JsArray<ChainData> chainNames) {
   		int resultLength = chainNames.length();
   		chainList.clear();
		for (int i = 0; i < chainNames.length(); i++) {
			chainList.addItem(chainNames.get(i).getChainId());
		}
		dispPdbIdLabel.setText("PDB Id: " + pdbId + " Choose a Chain: ");

    }

    /**
     * Obtains amino acid residue sequence from PDB for a particular molecule chain
     */
    private void fetchSequenceForStructureAndChain() {
    	String pdbFileLabel = dispPdbIdLabel.getText();
    	// TODO All of this is to trim the label just to get the pdbId; should be replaced
    	pdbFileLabel = pdbFileLabel.substring(8); // trim off the left "PDB Id: "
    	final String pdbFile = pdbFileLabel.replace(" Choose a Chain: ", "");
    	// get the chosen chainId
    	int selectedIndex = chainList.getSelectedIndex();
    	chainId = chainList.getValue(selectedIndex);
    	
    	String url = JSON_URL + "pdbchainsequencedsspfetcher?";
    	url += "pdbFileName=" + pdbFile;
    	url += "&chainId=" + chainId;
    	rootLogger.log(Level.FINE, "the url is " + url);
    	url = URL.encode(url);
    	
    	// Send request to server
	    RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);
	    
	    try {
	    	rootLogger.log(Level.FINE, "hello from the try");
	    	Request request = builder.sendRequest(null, new RequestCallback() {
	    		public void onError(Request request, Throwable exception) {
	    			displayError("Couldn't retrieve JSON");
	    		}
	    		// process response
	    		public void onResponseReceived(Request request, Response response) {
	    			
	    			if (200 == response.getStatusCode()) {
	    				rootLogger.log(Level.FINE, "fetchSequenceForStructureAndChain: The sequence was returned: " + response.getText());

	    				JsArray<SeqResSequenceData> sequences = asArrayOfSeqResSequences(response.getText());
	    				String sequenceReturned = sequences.get(0).getSequence();
	    				String secStructReturned = sequences.get(0).getSecStructure();
	    				JsArray<AtomTuple> atomListReturned = sequences.get(0).getAtomTuples();
	    				
	    				rootLogger.log(Level.FINE, "the first atom in the pairs list is " + atomListReturned.get(0).getAtomName());
	    				rootLogger.log(Level.FINE, "the length of the pairs list is " + atomListReturned.length());
	    				fillSeqStructGrid(pdbFile, chainId, sequenceReturned, secStructReturned, atomListReturned);
	    			} else {
	    				displayError("Couldn't retrieve JSON (" + response.getStatusText() + ")");
	    			}
	    		}
	    	});
	    	
	    } catch (RequestException e) {
	    	displayError("Couldn't retrieve JSON");
	    }

    } // end fetchSequenceForStructureAndChain

    
    /**
     * Obtains amino acid atoms sequence from PDB for a particular molecule chain
     */
    private void fetchAtomsForStructureAndChain() {
    	rootLogger.log(Level.FINE,"getting atoms");
    	String pdbFileLabel = dispPdbIdLabel.getText();
    	// TODO All of this is to trim the label just to get the pdbId; should be replaced
    	pdbFileLabel = pdbFileLabel.substring(8); // trim off the left "PDB Id: "
    	final String pdbFile = pdbFileLabel.replace(" Choose a Chain: ", "");
    	// get the chosen chainId
    	int selectedIndex = chainList.getSelectedIndex();
    	chainId = chainList.getValue(selectedIndex);
    	
    	String url = JSON_URL + "pdbchainsequencedsspfetcher?";
    	url += "pdbFileName=" + pdbFile;
    	url += "&chainId=" + chainId;
    	rootLogger.log(Level.FINE, "the url is " + url);
    	url = URL.encode(url);
    	// Send request to server
	    RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);
	    
	    try {
	    	Request request = builder.sendRequest(null, new RequestCallback() {
	    		public void onError(Request request, Throwable exception) {
	    			displayError("Couldn't retrieve JSON");
	    		}
	    		// process response
	    		public void onResponseReceived(Request request, Response response) {
	    			if (200 == response.getStatusCode()) {
	    				rootLogger.log(Level.FINE, "fetchAtomsForStructureAndChain: The sequence was returned: " + response.getText());

	    				JsArray<SeqResSequenceData> sequences = asArrayOfSeqResSequences(response.getText());
	    				String sequenceReturned = sequences.get(0).getSequence();
	    				String secStructReturned = sequences.get(0).getSecStructure();
	    				JsArray<AtomTuple> atomListReturned = sequences.get(0).getAtomTuples();
	    				
	    				rootLogger.log(Level.FINE, "the first atom in the tuples list is " + atomListReturned.get(0).getAtomName());
	    				rootLogger.log(Level.FINE, "the length of the tuples list is " + atomListReturned.length());
	    				fillAtomAcidGrid(pdbFile, chainId, sequenceReturned, secStructReturned, atomListReturned);
	    			} else {
	    				displayError("Couldn't retrieve JSON (" + response.getStatusText() + ")");
	    			}
	    		}
	    	});
	    	
	    } catch (RequestException e) {
	    	displayError("Couldn't retrieve JSON");
	    }

    	
    } // end fetchAtomsForStructureAndChain

    
    /**
     * Get the DSSP 
     */
    private void fetchDsspForStructureAndChain() {
    	String pdbFile = dispPdbIdLabel.getText();
    	// TODO All of this is to trim the label just to get the pdbId; should be replaced
    	pdbFile = pdbFile.substring(8); // trim off the left
    	pdbFile = pdbFile.replace(" Choose a Chain: ", "");
    	int selectedIndex = chainList.getSelectedIndex();
    	String chainId = chainList.getValue(selectedIndex);
    	    	
       	// Initialize the service proxy if necessary
    	/*
        if (pdbSoapSvcAsync == null) {
        	pdbSoapSvcAsync = GWT.create(PDBSoapService.class);
        }
        */

        // Set up a call back object.
        AsyncCallback<String> callBack = new AsyncCallback<String>() {
        	
        	public void onFailure(Throwable caught) {
        		// TODO: Do something with errors.
        		Window.alert("fetchDsspForStructureAndChain FAILURE");
        	}

        	public void onSuccess(String result) {
        		targetStructure = result;
        	}
        };
     
        // call the soap service
        //pdbSoapSvcAsync.getKabschSanderDssp(pdbFile, chainId, callBack);

    }
    
    
    
    /**
     * Update selection label text.
     * 
     * @param textBox the text box
     * @param label the label to update
     */
    private void updateSelectionLabel(TextBox textBox, Label label) {
    	label.setText("Selected: " + textBox.getCursorPos() + ", " 
    			+ textBox.getSelectionLength());
    }
    
    /**
     * Populates a grid with with sequence and structure information
     * TODO - make this a general method for populating grids
     * @param sequence
     * @param structure
     * @param atomList 
     */
    public void fillSeqStructGrid(String pdbFile, String chainId, String sequence, String structure, JsArray<AtomTuple> atomList) {
    	int currStructRow = 0;
    	int currSeqRow = 1;
    	int currCol = 0;
    	int row = 0;
    	int col = 0;
    	int seqPosNum = 0;
    	int atomPosNum = 0;
    	String dsspDesc;

    	// calculate the number of rows the sequence requires
    	int seqRows = (sequence.length() + gridDataWidth - 1) / gridDataWidth;
    	
    	// two rows required to describe amino acids: group (amino acid), secondary structure
    	int bodyRows = seqRows * 2;
    	rootLogger.log(Level.FINE,"sequence bodyRows = " + bodyRows);
    	
    	// resize and reformat
    	targetGrid.resize(bodyRows, gridDataWidth);
    	for (row = 0; row < bodyRows; row++) {
    		for (col = 0; col < gridDataWidth; col++) {
    			targetGrid.getCellFormatter().addStyleName(row, col, "gridMonoChars");
    		}
    	}

    	// create an array of toggle buttons for the structure
    	targetStructToggles = new ToggleButton[structure.length()];
    	for (int i = 0; i < structure.length(); i++) {
    		targetStructToggles[i] = new ToggleButton(structure.substring(i, i + 1), structure.substring(i, i + 1));
    		targetStructToggles[i].setStylePrimaryName("struct-Button");
    		seqPosNum = i + 1;
    		dsspDesc = assignDSSPDesc(structure.charAt(i));
    		targetStructToggles[i].setTitle(pdbFile + " " + chainId + " " + seqPosNum + " " + dsspDesc);
    	}

    	// create an array of text boxes for the sequence
    	toggleSeqSelect = new boolean[structure.length()];
    	toggleSeqSelectTextBox = new TextBox[structure.length()];
    	for (int i = 0; i < structure.length(); i++) {
    		toggleSeqSelect[i] = false;
    		toggleSeqSelectTextBox[i] = new TextBox();
    		toggleSeqSelectTextBox[i].setStylePrimaryName("seq-TextBox");
    		toggleSeqSelectTextBox[i].setReadOnly(true);
    		seqPosNum = i + 1;
    		toggleSeqSelectTextBox[i].setTitle(pdbFile + " " + chainId + " " + seqPosNum + " " + sequence.substring(i, i + 1));
    		toggleSeqSelectTextBox[i].getElement().setId(pdbFile + " " + chainId + " " + seqPosNum + " " + sequence.substring(i, i + 1));
    		toggleSeqSelectTextBox[i].setText(sequence.substring(i, i + 1));
    		toggleSeqSelectTextBox[i].addClickHandler(this);
    	}
    	// TODO are we factoring this?
    	// create an array of text boxes for the atoms
    	toggleAtomSelect = new boolean[atomList.length()];
    	toggleAtomSelectTextBox = new TextBox[atomList.length()];
    	for (int i = 0; i < atomList.length(); i++) {
    		toggleAtomSelect[i] = false;
    		toggleAtomSelectTextBox[i] = new TextBox();
    		toggleAtomSelectTextBox[i].setStylePrimaryName("atom-TextBox");
    		toggleAtomSelectTextBox[i].setReadOnly(true);
    		atomPosNum = i + 1;
    		toggleAtomSelectTextBox[i].setTitle(pdbFile + " " + chainId + " " + atomPosNum + " " + atomList.get(i).getAtomName());
    	}
    	
    	// populate sequence targetStructToggles button array into the grid
    	for (int i = 0; i < structure.length(); i++) {
    		currStructRow = (i / gridDataWidth) * 2;	// even, starting at 0
    		currCol = i % gridDataWidth;
    		targetGrid.setWidget(currStructRow, currCol, targetStructToggles[i]);
    	}
    	// populate amino acid sequence text boxes array into the grid
    	for (int i = 0; i < sequence.length(); i++) {
    		currSeqRow = (i / gridDataWidth) * 2 + 1;	// odd, starting at 1
    		currCol = i % gridDataWidth;
    		targetGrid.setWidget(currSeqRow, currCol, toggleSeqSelectTextBox[i]);
    	}
    	
    }	// end fillSeqStructGrid
    
    /**
     * Populates a grid with with atom and amino acid information
     * TODO - make this a general method for populating grids
     * @param sequence
     * @param structure
     */
    public void fillAtomAcidGrid(String pdbFile, String chainId, String sequence, String structure, JsArray<AtomTuple> atomList) {
    	int currStructRow = 0;
    	int currSeqRow = 1;
    	int currAtomRow = 2;
    	int currCol = 0;
    	int row = 0;
    	int col = 0;
    	int seqPosNum = 0;
    	int atomPosNum = 0;
    	String dsspDesc;
    	
    	// calculate the number of rows the sequence atoms require
    	int atomRows = (atomList.length() + gridDataWidth - 1) / gridDataWidth;
    	
    	// three rows to describe atoms: atom, group (amino acid), secondary structure
    	int bodyRows = atomRows * 3;
    	rootLogger.log(Level.FINE,"atom bodyRows = " + bodyRows);
    	
    	// resize and reformat
    	targetGrid.resize(bodyRows, gridDataWidth);
    	for (row = 0; row < bodyRows; row++) {
    		for (col = 0; col < gridDataWidth; col++) {
    			targetGrid.getCellFormatter().addStyleName(row, col, "gridMonoChars");
    		}
    	}
    	
    	// create an array of toggle buttons for the structure
    	targetStructToggles = new ToggleButton[atomList.length()];
    	for (int i = 0; i < atomList.length(); i++) {
    		
    		//rootLogger.log(Level.FINE,"structure symbol = " + atomList.get(i).getSecStructureSymbol());
    		//rootLogger.log(Level.FINE,"seq position = " + atomList.get(i).getSeqPosition());
    		//rootLogger.log(Level.FINE,"amino acid = " + atomList.get(i).getAminoAcidName());
    		//rootLogger.log(Level.FINE,"atom name = " + atomList.get(i).getAtomName());
    		//rootLogger.log(Level.FINE,"pdb serial = " + atomList.get(i).getPdbSerial());
    		
    		String secStructureSymbol = atomList.get(i).getSecStructureSymbol();
    		dsspDesc = assignDSSPDesc(secStructureSymbol.charAt(0));
    		seqPosNum = Integer.parseInt(atomList.get(i).getSeqPosition());
    		
    		targetStructToggles[i] = new ToggleButton(secStructureSymbol, secStructureSymbol);
    		targetStructToggles[i].setStylePrimaryName("struct-Button");
    		
    		targetStructToggles[i].setTitle(pdbFile + " " + chainId + " " + seqPosNum + " " + dsspDesc);
    	}

    	// create an array of toggle buttons for the sequence acid residues
    	targetAcidToggles = new ToggleButton[atomList.length()];
    	for (int i = 0; i < atomList.length(); i++) {
    		String aminoAcidName = atomList.get(i).getAminoAcidName();
    		rootLogger.log(Level.FINE,"amino acid toggle = " + aminoAcidName);
    		targetAcidToggles[i] = new ToggleButton(aminoAcidName, aminoAcidName);
    		targetAcidToggles[i].setStylePrimaryName("seq-Button");
    		seqPosNum = Integer.parseInt(atomList.get(i).getSeqPosition());
    		
    		targetAcidToggles[i].setTitle(pdbFile + " " + chainId + " " + seqPosNum + " " + aminoAcidName);
    	}
    	
    	// create an array of text boxes for the atoms
    	toggleAtomSelect = new boolean[atomList.length()];
    	toggleAtomSelectTextBox = new TextBox[atomList.length()];
    	for (int i = 0; i < atomList.length(); i++) {
    		String aminoAcidName = atomList.get(i).getAminoAcidName();
    		toggleAtomSelect[i] = false;
    		toggleAtomSelectTextBox[i] = new TextBox();
    		toggleAtomSelectTextBox[i].setStylePrimaryName("seq-TextBox");
    		toggleAtomSelectTextBox[i].setReadOnly(true);
    		seqPosNum = Integer.parseInt(atomList.get(i).getSeqPosition());
    		String atomNameShort = atomList.get(i).getAtomName().substring(0,1);
    		String pdbSerial = atomList.get(i).getPdbSerial();
    		toggleAtomSelectTextBox[i].setTitle(pdbFile + " " + chainId + " " + seqPosNum + " " + aminoAcidName + " " + atomNameShort + " " + pdbSerial);
    		toggleAtomSelectTextBox[i].getElement().setId(pdbFile + " " + chainId + " " + seqPosNum + " " + aminoAcidName + " " + atomNameShort + " " + pdbSerial);
    		toggleAtomSelectTextBox[i].setText(atomNameShort);
    		toggleAtomSelectTextBox[i].addClickHandler(this);
    	}

    	// populate sequence targetStructToggles button array into the grid
    	for (int i = 0; i < atomList.length(); i++) {
    		currStructRow = (i / gridDataWidth) * 3;	// even, starting at 0
    		currCol = i % gridDataWidth;
    		targetGrid.setWidget(currStructRow, currCol, targetStructToggles[i]);
    	}

    	// populate sequence targetAcidToggles button array into the grid
    	for (int i = 0; i < atomList.length(); i++) {
    		currStructRow = (i / gridDataWidth) * 3 + 1;	// even, starting at 0
    		currCol = i % gridDataWidth;
    		rootLogger.log(Level.FINE, "about to populate " + currStructRow + ", " + currCol);
    		targetGrid.setWidget(currStructRow, currCol, targetAcidToggles[i]);
    	}

    	// populate atom text boxes array into the grid
    	for (int i = 0; i < atomList.length(); i++) {
    		currSeqRow = (i / gridDataWidth) * 3 + 2;	// odd, starting at 1
    		currCol = i % gridDataWidth;
    		targetGrid.setWidget(currSeqRow, currCol, toggleAtomSelectTextBox[i]);
    	}
    }

    /**
     * Assigns a description to the DSSP symbol for secondary structure
     * @param dsspSymbol
     * @return
     */
    public String assignDSSPDesc(char dsspSymbol) {
    	String result = "DSSP: ";
    	switch (dsspSymbol) {
    		case 'G': result += "3/10 helix"; break;
    		case 'H': result += "alpha helix"; break;
    		case 'I': result += "pi helix"; break;
    		case 'B': result += "beta bridge"; break;
    		case 'E': result += "sheet"; break;
    		case 'T': result += "hydrogen bonded turn"; break;
    		case 'S': result += "high curvature"; break;
    		case 'L': result += "loop or other"; break;
    		default: result += "no secondary structure assigned"; break;
    	}
    	return result;
    }

    /**
     * Returns the PDB file name
     * 
     * @return
     */
    public String getPdbFile() {
    	return pdbFile;
    }
    
    /**
     * Returns the chain ID for the PDB file
     * 
     * @return
     */
    public String getChainId() {
    	return chainId;
    }
    
    /**
     * Returns a list of selected residue positions
     * 
     */
    public ArrayList<Integer> getSelectedResidues() {
    	int maxSize = toggleSeqSelect.length;
    	ArrayList<Integer> result  = new ArrayList(maxSize);
    	
    	for (int i = 0; i < maxSize; i++) {
    		if (toggleSeqSelect[i])
    			result.add(i + 1);
    	}
    	
    	result.trimToSize();
    	return result;
    }

    /**
     * Returns a list of selected atom positions
     * 
     */
    public ArrayList<Integer> getSelectedAtoms() {
    	int maxSize = toggleAtomSelect.length;
    	ArrayList<Integer> result  = new ArrayList(maxSize);
    	
    	for (int i = 0; i < maxSize; i++) {
    		if (toggleAtomSelect[i])
    			result.add(i + 1);
    	}
    	
    	result.trimToSize();
    	return result;
    }

    
    /**
     * Returns the current mode for atoms or residues
     * 
     */
    public String getSelectMode() {
    	String result = "";
    	result = selectMode;
    	return result;
    }
    
    /**
     * Display error message.
     * @param error
     */
    private void displayError(String error) {
    	//errorMsgLabel.setText("Error: " + error);
    	//errorMsgLabel.setVisible(true);
    }

    /**
     * JSNI method to convert JSON string in Javascript objects
     * @param json
     * @return
     */
    private final native JsArray<ChainData> asArrayOfChainNames(String json) /*-{
        return eval(json);
    }-*/;
    
    /**
     * JSNI method to convert JSON string in Javascript objects
     * @param json
     * @return
     */
    private final native JsArray<SeqResSequenceData> asArrayOfSeqResSequences(String json) /*-{
        return eval(json);
    }-*/;
    
}
