package edu.uri.cs.gwt.plat.client;

import com.google.gwt.core.client.JavaScriptObject;

public class AlignerResult extends JavaScriptObject {
	// GWT overlay types always have protected, zero argument constructors.
	protected AlignerResult() {}
	
	// JSNI methods to get data
	public final native String getTranMatrixX() /*-{ return this.tranMatrixX; }-*/;
	public final native String getTranMatrixY() /*-{ return this.tranMatrixY; }-*/;
	public final native String getTranMatrixZ() /*-{ return this.tranMatrixZ; }-*/;
	public final native String getRotMatrix00() /*-{ return this.rotMatrix00; }-*/;
	public final native String getRotMatrix01() /*-{ return this.rotMatrix01; }-*/;
	public final native String getRotMatrix02() /*-{ return this.rotMatrix02; }-*/;
	public final native String getRotMatrix10() /*-{ return this.rotMatrix10; }-*/;
	public final native String getRotMatrix11() /*-{ return this.rotMatrix11; }-*/;
	public final native String getRotMatrix12() /*-{ return this.rotMatrix12; }-*/;
	public final native String getRotMatrix20() /*-{ return this.rotMatrix20; }-*/;
	public final native String getRotMatrix21() /*-{ return this.rotMatrix21; }-*/;
	public final native String getRotMatrix22() /*-{ return this.rotMatrix22; }-*/;
	public final native String getRmsdSelectedAtoms() /*-{ return this.rmsdSelectedAtoms; }-*/;

}
