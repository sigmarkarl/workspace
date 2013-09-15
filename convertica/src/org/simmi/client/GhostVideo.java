package org.simmi.client;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.VideoElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.media.client.Video;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import elemental.client.Browser;
import elemental.dom.LocalMediaStream;
import elemental.html.Blob;
import elemental.html.Navigator;
import elemental.html.NavigatorUserMediaSuccessCallback;
import elemental.util.Mappable;

public class GhostVideo implements EntryPoint {
	public native void wham() /*-{
		//$wnd.pause = 1;
		if( !$wnd.encoder ) {
			$wnd.count = 0;
			$wnd.encoder = new $wnd.Whammy.Video(24);
		}
	}-*/;
	
	public native void addFrame( JavaScriptObject encoder, com.google.gwt.dom.client.CanvasElement canvas ) /*-{
		encoder.add( canvas );
	}-*/;
	
	public native Blob compile( JavaScriptObject encoder ) /*-{
		return encoder.compile();
	}-*/;
	
	public native Mappable video() /*-{
		return {video: true};
	}-*/;
	
	public native String getObjectUrl( LocalMediaStream jso ) /*-{
		return $wnd.URL.createObjectURL(jso);
	}-*/;
	
	public native void setStyleAttribute( Style style, String key, String val ) /*-{
		 style[key] = val;
	}-*/;
	
	public void setMirror( Canvas video, boolean mirror ) {
		Style style = video.getElement().getStyle();
		if( mirror ) {
			setStyleAttribute( style, "transform", "scale(-1,1)" );
			setStyleAttribute( style, "-webkit-transform", "scale(-1,1)" );
			setStyleAttribute( style, "-moz-transform", "scale(-1,1)" );
		} else {
			setStyleAttribute( style, "transform", "scale(1,1)" );
			setStyleAttribute( style, "-webkit-transform", "scale(1,1)" );
			setStyleAttribute( style, "-moz-transform", "scale(1,1)" );
		}
	}
	
	public native void addPlayListener( VideoElement v, CanvasElement canvas, CanvasElement back, Context2d context, Context2d backcontext, int cw, int ch ) /*-{
		v.addEventListener('play', function() {
			//cw = v.clientWidth;
        	//ch = v.clientHeight;
        	canvas.width = cw;
        	canvas.height = ch;
        	back.width = cw;
        	back.height = ch;
        	$wnd.draw(v,canvas,context,backcontext,cw,ch);
		}, false);
	}-*/;
	
	public native void init() /*-{
		$wnd.draw = function(v,canvas,c,bc,w,h) {
		    if(v.paused || v.ended) return false;
		    bc.drawImage(v,0,0,w,h);
		    var idata = bc.getImageData(0,0,w,h);
		    var data = idata.data;
		    if( $wnd.snapdata ) {
		    	for(var i = 0; i < data.length; i+=4) {
			        data[i] = (data[i]+$wnd.snapdata[i])/2;
			        data[i+1] = (data[i+1]+$wnd.snapdata[i+1])/2;
			        data[i+2] = (data[i+2]+$wnd.snapdata[i+2])/2;
			    }
		    } else {
			    for(var i = 0; i < data.length; i+=4) {
			        var r = data[i];
			        var g = data[i+1];
			        var b = data[i+2];
			        var brightness = (3*r+4*g+b)>>>3;
			        data[i] = brightness;
			        data[i+1] = brightness;
			        data[i+2] = brightness;
			    }
		    }
		    idata.data = data;
		    c.putImageData(idata,0,0);
		    
		    if( $wnd.encoder ) {
		    	$wnd.encoder.add( canvas );
		    	$wnd.count++;
		    	
		    	if( $wnd.count > 240 ) {
		    		var blob = $wnd.encoder.compile();
		    		$wnd.encoder = null;
		    		$wnd.open( $wnd.URL.createObjectURL( blob ) );
		    	}
		    }
		    
		    setTimeout(function(){ $wnd.draw(v,canvas,c,bc,w,h); }, 0);
		}
	}-*/;
	
	public native void snapshot( VideoElement v, CanvasElement c, Context2d context, int w, int h ) /*-{
		if(v.paused || v.ended) return false;
		c.width = w;
        c.height = h;
        
        context.drawImage(v,0,0,w,h);
        var idata = context.getImageData(0,0,w,h);
		$wnd.snapdata = idata.data;
	}-*/;
	
	@Override
	public void onModuleLoad() {
		final RootPanel		rp = RootPanel.get("cont");
		
		init();
		
		Style st = rp.getElement().getStyle();
		st.setBorderWidth( 0.0, Unit.PX );
		st.setMargin( 0.0, Unit.PX );
		st.setPadding( 0.0, Unit.PX );
		
		final VerticalPanel	vp = new VerticalPanel();
		vp.setHorizontalAlignment( VerticalPanel.ALIGN_CENTER );
		//vp.setVerticalAlignment( VerticalPanel.ALIGN_MIDDLE );
		int w = Window.getClientWidth();
		vp.setSize( w+"px", "500px" );
		
		Window.addResizeHandler( new ResizeHandler() {
			@Override
			public void onResize(ResizeEvent event) {
				int w = event.getWidth();
				
				vp.setSize( w+"px", "500px" );				
			}
		});
		
		final Video video = Video.createIfSupported();
		video.setSize("640px", "480px");
		//vp.getElement().getStyle().setDisplay( Style.Display.NONE );
		//vp.add( video );
		
		final Canvas 		back = Canvas.createIfSupported();
		final Canvas 		canvas = Canvas.createIfSupported();
		final Canvas 		snapcanvas = Canvas.createIfSupported();
		final Context2d 	bcontext = back.getContext2d();
		final Context2d 	context = canvas.getContext2d();
		final Context2d 	snapcontext = canvas.getContext2d();
		
		setMirror( canvas, true );
		vp.add( canvas );
		
		addPlayListener( (VideoElement)VideoElement.as(video.getElement()), canvas.getCanvasElement(), back.getCanvasElement(), context, bcontext, 640, 480 );
		
		final elemental.html.Window 		wnd = Browser.getWindow();
		final Navigator 					nvg = wnd.getNavigator();
		
		nvg.webkitGetUserMedia( video(), new NavigatorUserMediaSuccessCallback() {
			@Override
			public boolean onNavigatorUserMediaSuccessCallback(LocalMediaStream stream) {
				String url = getObjectUrl( stream );
				video.setSrc( url );
				video.play();
				
				return true;
			}
		});
		
		final Button	b = new Button("Snapshot");
		b.addClickHandler( new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				snapshot( (VideoElement)VideoElement.as(video.getElement()), snapcanvas.getCanvasElement(), snapcontext, 640, 480 );
			}
		});
		
		final Button rb = new Button("Record 10 sec");
		rb.addClickHandler( new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				wham();
			}
		});
		
		final CheckBox	cb = new CheckBox("mirrored");
		cb.setValue( true );
		cb.addClickHandler( new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				setMirror( canvas, cb.getValue() );
			}
		});
		
		vp.add( b );
		vp.add( rb );
		vp.add( cb );
		rp.add( vp );
	}
}
