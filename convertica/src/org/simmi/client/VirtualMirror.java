package org.simmi.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.media.client.Video;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import elemental.client.Browser;
import elemental.dom.LocalMediaStream;
import elemental.html.Navigator;
import elemental.html.NavigatorUserMediaSuccessCallback;
import elemental.util.Mappable;

public class VirtualMirror implements EntryPoint {
	public native Mappable video() /*-{
		return {video: true};
	}-*/;
	
	public native String getObjectUrl( LocalMediaStream jso ) /*-{
		return $wnd.URL.createObjectURL(jso);
	}-*/;
	
	public native void setStyleAttribute( Style style, String key, String val ) /*-{
		 style[key] = val;
	}-*/;
	
	public void setMirror( Video video, boolean mirror ) {
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
	
	@Override
	public void onModuleLoad() {
		final RootPanel		rp = RootPanel.get();
		
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
				int h = event.getHeight();
				
				vp.setSize( w+"px", "500px" );				
			}
		});
		
		final Video video = Video.createIfSupported();
		video.setSize("640px", "480px");
		vp.add( video );
		
		setMirror( video, true );
		
		final elemental.html.Window wnd = Browser.getWindow();
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
		
		final CheckBox	cb = new CheckBox("mirrored");
		cb.setValue( true );
		cb.addClickHandler( new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				setMirror( video, cb.getValue() );
			}
		});
		vp.add( cb );
		rp.add( vp );
	}
}
