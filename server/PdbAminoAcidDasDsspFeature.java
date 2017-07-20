package edu.uri.cs.gwt.plat.server;

public class PdbAminoAcidDasDsspFeature {
	
	private int startPos;
    private int endPos;
    private String featureId;
    private String featureNote;
    
    /**
     * Constructor, id only.
     * @param featureId Feature id
     */
    public PdbAminoAcidDasDsspFeature (String id) {
    	featureId = id;
    }

    /**
     * Full Constructor.
     * @param featureId Feature id
     * @param startPos 	Start base pair position
     * @param endPos 	End base pair position
     */
    public PdbAminoAcidDasDsspFeature (String id, int start, int end, String note) {
    	featureId = id;
    	startPos = start;
    	endPos = end;
    	featureNote = note;
    }

    /**
     * Sets Feature ID.
     * @return void
     */
    public void setFeatureId(String id) {
    	featureId = id;
    }

    /**
     * Sets Start base pair position.
     * @return void
     */
    public void setStartPos(int start) {
    	startPos = start;
    }

    /**
     * Sets End base pair position.
     * @return void
     */
    public void setEndPos(int end) {
    	endPos = end;
    }

    /**
     * Sets note.
     * @return void
     */
    public void setNote(String someNote) {
    	featureNote = someNote;
    }

    /**
     * Gets Feature ID.
     * @return Feature ID
     */
    public String getId() {
    	return featureId;
    }

    /**
     * Gets Start base pair position.
     * @return start base pair position
     */
    public int getStartPos() {
    	return startPos;
    }

    /**
     * Gets End base pair position.
     * @return end base pair position
     */
    public int getEndPos() {
    	return endPos;
    }
    
    /**
     * Gets End base pair position.
     * @return feature note
     */
    public String getNote() {
    	return featureNote;
    }


}
