package org.simmi.client;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DataTransfer;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
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
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import elemental.html.Blob;
import elemental.html.FileReader;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Convertica implements EntryPoint {
	/**
	 * The message displayed to the user when the server cannot be reached or
	 * returns an error.
	 */
	private static final String SERVER_ERROR = "An error occurred while "
			+ "attempting to contact the server. Please check your network "
			+ "connection and try again.";

	/**
	 * Create a remote service proxy to talk to the server-side Greeting service.
	 */
	private final GreetingServiceAsync greetingService = GWT.create(GreetingService.class);

	public native FileReader newFileReader() /*-{
		return new FileReader();
	}-*/;
	
	public native Blob readFile( DataTransfer dt, int i ) /*-{
		return dt.files[ i ];
	}-*/;
	
	public native String getFilename( Blob blob ) /*-{
		return blob.name;
	}-*/;
	
	public native String createObjectUrl( Blob blob ) /*-{
		return $wnd.URL.createObjectURL( blob );
	}-*/;
	
	public native void setDownload( Element a, String dl ) /*-{
		$wnd.console.log( dl );
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
	    
	    return new Blob( [view], {"type" : "image\/webp" } );
	}-*/;
	
	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		final RootPanel	rp = RootPanel.get();
		Style st = rp.getElement().getStyle();
		st.setMargin(0.0, Unit.PX);
		st.setPadding( 0.0, Unit.PX );
		st.setBorderWidth(0.0, Unit.PX);
		Window.enableScrolling( false );
		
		int w = Window.getClientWidth();
		int h = Window.getClientHeight();
		rp.setSize(w+"px", (h-90)+"px");
		
		Window.addResizeHandler( new ResizeHandler() {
			@Override
			public void onResize(ResizeEvent event) {
				int w = event.getWidth();
				int h = event.getHeight();
				
				rp.setSize(w+"px", (h-90)+"px");
			}
		});
		
		final VerticalPanel subvp = new VerticalPanel();
		FocusPanel	fp = new FocusPanel();
		fp.addDropHandler( new DropHandler() {
			@Override
			public void onDrop(DropEvent event) {
				event.preventDefault();
				
				while( subvp.getWidgetCount() > 2 ) subvp.remove( subvp.getWidgetCount()-1 );
				
				int fi = 0;
				Blob 				blob = readFile( event.getDataTransfer(), fi );
				while( blob != null ) {
					String fn = getFilename( blob );
					int i = fn.lastIndexOf('.');
					
					final String filename;
					if( i == -1 ) filename = fn;
					else filename = fn.substring(0,i);
					
					String				url = createObjectUrl( blob );
					final Image			img = new Image( url );
					img.getElement().getStyle().setDisplay( Style.Display.NONE );
					
					//img.setWidth("400px");
					//img.setHeight("300px");
					subvp.add( img );
					subvp.add( new HTML( "original image size ("+fn+"): " + blob.getSize() ) );
					
					img.addLoadHandler( new LoadHandler() {
						@Override
						public void onLoad(LoadEvent event) {
							final Canvas 		canvas = Canvas.createIfSupported();
							canvas.setCoordinateSpaceWidth( img.getWidth() );
							canvas.setCoordinateSpaceHeight( img.getHeight() );
							//subvp.add( canvas );
							
							Context2d ctx2d = canvas.getContext2d();
							ctx2d.drawImage( ImageElement.as( img.getElement() ), 0, 0 );
							
							String imgurl = canvas.toDataUrl("image/webp");
							
							Blob webpblob = duri2Array( imgurl );
							
							HorizontalPanel hp = new HorizontalPanel();
							hp.add( new HTML( "Congratulations! your new image (" ) );
							
							String imgbloburl = createObjectUrl( webpblob );
							String newname = filename+".webp";
							Anchor anchor = new Anchor( newname );
							anchor.setHref( imgbloburl );
							subvp.add( anchor );
							setDownload( anchor.getElement(), newname );
							hp.add( anchor );
							
							hp.add( new HTML( ") size is: " + webpblob.getSize() ) );
							subvp.add( hp );
							//Browser.getWindow().getConsole().log("erm " + imgurl + "  " + img.getWidth() + "  " + img.getHeight());
							if( img == subvp.getWidget(2) ) Window.open( imgbloburl, "image.webp", "_blank" );
							//Browser.getWindow().open( imgurl, "image.webp" );
						}
					});
					blob = readFile( event.getDataTransfer(), ++fi );
				}
				
				/*final FileReader 	fr = newFileReader();
				fr.setOnload( new EventListener() {
					@Override
					public void handleEvent(Event evt) {
						Object obj = fr.getResult();
						//Browser.getWindow().getConsole().log("erm" + obj);
						
						Canvas 		canvas = Canvas.createIfSupported();
						
					}				
				});
				fr.readAsArrayBuffer( blob );*/
				
				//Datatr
				//DropEvent de;
				
				//final Canvas 		canvas = Canvas.createIfSupported();
				
				//String 	imgurl = event.getData("URL");
				//Browser.getWindow().getConsole().log( "bleh2 " + imgurl );
				/*final Image 	img = new Image( imgurl );
				
				img.addLoadHandler( new LoadHandler() {
					@Override
					public void onLoad(LoadEvent event) {
						canvas.setCoordinateSpaceWidth( img.getWidth() );
						canvas.setCoordinateSpaceHeight( img.getHeight() );
						Context2d 	c2 = canvas.getContext2d();
						c2.drawImage( ImageElement.as(img.getElement()), 0.0, 0.0 );
						String webpurl = canvas.toDataUrl("image/webp");
						
						Window.open( webpurl, "_blank", "");	
					}
				});
				subvp.add( img );*/
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
		fp.setSize( "100%", "100%" );
		
		VerticalPanel vp = new VerticalPanel();
		vp.setSize( "100%", "100%" );
		vp.setHorizontalAlignment( VerticalPanel.ALIGN_CENTER );
		vp.setVerticalAlignment( VerticalPanel.ALIGN_MIDDLE );
		
		subvp.setHorizontalAlignment( VerticalPanel.ALIGN_CENTER );
		subvp.setVerticalAlignment( VerticalPanel.ALIGN_MIDDLE );
		
		/*subvp.add( new HTML("<!-- WebPConvertica -->"
				+ "<ins class=\"adsbygoogle\" "
				+ "style=\"display:inline-block;width:728px;height:90px\" "
				+ "data-ad-client=\"ca-pub-7204381538404733\" "
				+ "data-ad-slot=\"9164920548\"></ins>") );
		ScriptInjector.fromString( "(adsbygoogle = window.adsbygoogle || []).push({})" ).inject();*/
		subvp.add( new HTML("<h3>Make your image files smaller and better</h3>") );
		subvp.add( new HTML("drag-drop (multiple) image files to compress with <a href=\"https://developers.google.com/speed/webp/?csw=1\">WebP</a>") );
		vp.add( subvp );
		
		fp.add( vp );
		rp.add( fp );
	}
}
