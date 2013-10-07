package org.simmi.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.DataTransfer;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.dom.client.DragEndEvent;
import com.google.gwt.event.dom.client.DragEndHandler;
import com.google.gwt.event.dom.client.DragEnterEvent;
import com.google.gwt.event.dom.client.DragEnterHandler;
import com.google.gwt.event.dom.client.DragEvent;
import com.google.gwt.event.dom.client.DragHandler;
import com.google.gwt.event.dom.client.DragLeaveEvent;
import com.google.gwt.event.dom.client.DragLeaveHandler;
import com.google.gwt.event.dom.client.DragOverEvent;
import com.google.gwt.event.dom.client.DragOverHandler;
import com.google.gwt.event.dom.client.DragStartEvent;
import com.google.gwt.event.dom.client.DragStartHandler;
import com.google.gwt.event.dom.client.DropEvent;
import com.google.gwt.event.dom.client.DropHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import elemental.client.Browser;
import elemental.html.Blob;

public class Compressor implements EntryPoint {

	public native String getFilename( Blob blob ) /*-{
		return blob.name;
	}-*/;
	
	public native String createObjectUrl( Blob blob ) /*-{
		return $wnd.URL.createObjectURL( blob );
	}-*/;
	
	public native int numberOfFiles( DataTransfer dt ) /*-{
		$wnd.fc = 0;
		return dt.files.length;
	}-*/;
	
	public native int numberOfFiles( Element e ) /*-{
		$wnd.fc = 0;
		return e.files.length;
	}-*/;
	
	public native Blob readFile( DataTransfer dt, int i ) /*-{
		return dt.files[ i ];
	}-*/;
	
	public native Blob readFile( Element e, int i ) /*-{
		return e.files[ i ];
	}-*/;
	
	public native void setDownload( Element a, String dl ) /*-{
		a.download = dl;
		a.click();
	}-*/;
	
	public native Blob duri2Array( String dataURL ) /*-{
		binary = $wnd.atob( dataURL.substr( dataURL.indexOf(',') + 1 ) ),
	    i = binary.length;
	    view = new Uint8Array(i);
	    
	    while (i--) {
	    	view[i] = binary.charCodeAt(i);
		}
	    
	    return new Blob( [view], {"type" : "application\/zip" } );
	}-*/;
	
	public native void addFile( Blob blob, String fn, int nf ) /*-{		
		var s = this;
		var fileReader = new FileReader();
		if( nf == 1 && fn.indexOf(".zip") != -1 ) {
			fileReader.onload = function() {
				$wnd.zip = new $wnd.Zlib.Unzip( new Uint8Array(this.result) );
			    var filenames = $wnd.zip.getFilenames();
			    for( var fkey in filenames ) {
			    	var filename = filenames[fkey];
					var array = $wnd.zip.decompress( filename );
					var blob = new Blob( [array], {"type" : "application\/unknown" } );
					s.@org.simmi.client.Compressor::downloadunzipped(Ljava/lang/String;Lelemental/html/Blob;)( filename, blob );
			    }
			    $wnd.zip = null;
			};
			fileReader.readAsArrayBuffer(blob);
		} else {
			if( $wnd.zip == null ) $wnd.zip = new $wnd.Zlib.Zip();
			
			fileReader.onload = function() {
			    $wnd.zip.addFile( new Uint8Array(this.result), {
			    	filename: $wnd.stringToByteArray( fn )
				});
				
				$wnd.fc++;
				if( $wnd.fc == nf ) s.@org.simmi.client.Compressor::download(Ljava/lang/String;)( fn );
			};
			fileReader.readAsArrayBuffer(blob);
		}
	}-*/;
	
	public native void click( Element e ) /*-{
		e.click();
	}-*/;
	
	public native Blob compress() /*-{
		var array = $wnd.zip.compress();
		$wnd.zip = null;
		return new Blob( [array], {"type" : "application\/zip" } );
	}-*/;
	
	public native void init() /*-{
		$wnd.stringToByteArray = function(str) {
		    var array = new (window.Uint8Array !== void 0 ? Uint8Array : Array)(str.length);
		    var i;
		    var il;
	
		    for (i = 0, il = str.length; i < il; ++i) {
		        array[i] = str.charCodeAt(i) & 0xff;
		    }
	
		    return array;
		}
	}-*/;
	
	public void downloadunzipped( String filename, Blob blob ) {
		String		url = createObjectUrl( blob );
		
		String newname = filename;
		Anchor anchor = new Anchor( newname );
		anchor.setHref( url );
		//subvp.add( anchor );
		setDownload( anchor.getElement(), newname );
		//Window.open( url, firstname+".zip", "_blank" );
	}
	
	public void download( String filename ) {
		int i = filename.lastIndexOf('.');
		if( i != -1 ) filename = filename.substring(0,i);
		
		Blob 		blob = compress();
		String		url = createObjectUrl( blob );
		
		String newname = filename+".zip";
		Anchor anchor = new Anchor( newname );
		anchor.setHref( url );
		//subvp.add( anchor );
		setDownload( anchor.getElement(), newname );
		//Window.open( url, firstname+".zip", "_blank" );
	}

	@Override
	public void onModuleLoad() {
		init();
		
		final RootPanel	root = RootPanel.get();
		Style st = root.getElement().getStyle();
		st.setMargin(0.0, Unit.PX);
		st.setPadding( 0.0, Unit.PX );
		st.setBorderWidth(0.0, Unit.PX);
		
		final RootPanel	rp = RootPanel.get("cont");
		/*Style st = rp.getElement().getStyle();
		st.setMargin(0.0, Unit.PX);
		st.setPadding( 0.0, Unit.PX );
		st.setBorderWidth(0.0, Unit.PX);*/
		Window.enableScrolling( false );
		
		/*int w = Window.getClientWidth();
		int h = Window.getClientHeight();
		rp.setSize(w+"px", (h-90)+"px");
		
		Window.addResizeHandler( new ResizeHandler() {
			@Override
			public void onResize(ResizeEvent event) {
				int w = event.getWidth();
				int h = event.getHeight();
				
				rp.setSize(w+"px", (h-90)+"px");
			}
		});*/
		
		final FileUpload fu = new FileUpload();
		fu.addChangeHandler( new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				Browser.getWindow().getConsole().log("ermerm");
				int fi = 0;
				int nf = numberOfFiles( fu.getElement() );
				Blob 				blob = readFile( fu.getElement(), fi );
				while( blob != null ) {
					String fn = getFilename( blob );
					addFile( blob, fn, nf );
					blob = readFile( fu.getElement(), ++fi );
				}
			}
		});
		
		final VerticalPanel subvp = new VerticalPanel();
		FocusPanel	fp = new FocusPanel();
		/*st = fp.getElement().getStyle();
		st.setMargin(0.0, Unit.PX);
		st.setPadding( 0.0, Unit.PX );
		st.setBorderWidth(0.0, Unit.PX);*/
		
		fp.getElement().getStyle().setBackgroundColor( "#EEEEEE" );
		fp.addDropHandler( new DropHandler() {
			@Override
			public void onDrop(DropEvent event) {
				event.preventDefault();
				
				//while( subvp.getWidgetCount() > 2 ) subvp.remove( subvp.getWidgetCount()-1 );
				
				int fi = 0;
				int nf = numberOfFiles( event.getDataTransfer() );
				Blob 				blob = readFile( event.getDataTransfer(), fi );
				while( blob != null ) {
					String fn = getFilename( blob );
					addFile( blob, fn, nf );
					blob = readFile( event.getDataTransfer(), ++fi );
				}
			}
		});
		fp.addDragStartHandler( new DragStartHandler() {
			@Override
			public void onDragStart(DragStartEvent event) {}
		});
		fp.addDragEndHandler( new DragEndHandler() {
			@Override
			public void onDragEnd(DragEndEvent event) {}
		});
		fp.addDragOverHandler( new DragOverHandler() {
			@Override
			public void onDragOver(DragOverEvent event) {}
		});
		fp.addDragEnterHandler( new DragEnterHandler() {
			@Override
			public void onDragEnter(DragEnterEvent event) {}
		});
		fp.addDragLeaveHandler( new DragLeaveHandler() {
			@Override
			public void onDragLeave(DragLeaveEvent event) {}
		});
		fp.addDragHandler( new DragHandler() {
			@Override
			public void onDrag(DragEvent event) {}
		});
		fp.addDoubleClickHandler( new DoubleClickHandler() {
			@Override
			public void onDoubleClick(DoubleClickEvent event) {
				click( fu.getElement() );
			}
		});
		fp.setSize( "100%", "300px" );
		
		VerticalPanel vp = new VerticalPanel();
		vp.setSize( "100%", "300px" );
		vp.setHorizontalAlignment( VerticalPanel.ALIGN_CENTER );
		vp.setVerticalAlignment( VerticalPanel.ALIGN_MIDDLE );
		
		subvp.setHorizontalAlignment( VerticalPanel.ALIGN_CENTER );
		subvp.setVerticalAlignment( VerticalPanel.ALIGN_MIDDLE );
		
		subvp.add( new HTML("<h3>Compress files directly in your browser</h3>") );
		subvp.add( new HTML("drag-drop (multiple) files here to compress with zip") );
		subvp.add( new HTML("double click to open a file dialog") );
		subvp.add( fu );
		vp.add( subvp );
		
		fp.add( vp );
		rp.add( fp );
	}
}
