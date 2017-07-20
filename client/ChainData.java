package edu.uri.cs.gwt.plat.client;

import com.google.gwt.core.client.JavaScriptObject;

class ChainData extends JavaScriptObject {
	// Overlay types always have protected, zero argument constructors.
	protected ChainData() {}

	// JSNI method to get chain data.
	public final native String getChainId() /*-{ return this.chainId; }-*/;

}