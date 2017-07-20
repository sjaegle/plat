package edu.uri.cs.gwt.plat.client;

import com.google.gwt.core.client.JavaScriptObject;

public class AtomTuple extends JavaScriptObject {
	// GWT overlay types always have protected, zero argument constructors.
	protected AtomTuple() {};
	
	// JSNI methods to get atom tuple data.
	public final native String getAtomName() /*-{ return this.atomName; }-*/;
	public final native String getAminoAcidName() /*-{ return this.aminoAcidName; }-*/;
	public final native String getPdbSerial() /*-{ return this.pdbSerial; }-*/;
	public final native String getSecStructureSymbol() /*-{ return this.secStructureSymbol; }-*/;
	public final native String getSeqPosition() /*-{ return this.seqPosition; }-*/;
	public final native String getAtomPosition() /*-{ return this.atomPosition; }-*/;
}
