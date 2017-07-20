package edu.uri.cs.gwt.plat.client;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

public class SeqResSequenceData extends JavaScriptObject {
	// GWT overlay types always have protected, zero argument constructors.
	protected SeqResSequenceData() {}
	
	// JSNI methods to get sequence data.
	public final native String getSequence() /*-{ return this.sequence; }-*/;
	public final native String getSecStructure() /*-{ return this.secStructure; }-*/;
	public final native JsArray<AtomTuple> getAtomTuples() /*-{ return this.atomAcidTuples; }-*/;
}
