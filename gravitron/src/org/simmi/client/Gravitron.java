package org.simmi.client;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;

import elemental.html.WebGLRenderingContext;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Gravitron implements EntryPoint {
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

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		final RootPanel	rp = RootPanel.get();
		
		int w = Window.getClientWidth();
		int h = Window.getClientHeight();
		rp.setSize(w+"px", h+"px");	
		
		Window.addResizeHandler( new ResizeHandler() {
			@Override
			public void onResize(ResizeEvent event) {
				int w = event.getWidth();
				int h = event.getHeight();
				
				rp.setSize(w+"px", h+"px");
			}
		});
		
		Canvas 		canvas = Canvas.createIfSupported();
		canvas.setSize("100%", "100%");
		Context 	context = canvas.getContext("experimental-webgl");
		
		WebGLRenderingContext	webgl = (WebGLRenderingContext)context;
		webgl.clearColor(0.0f, 1.0f, 0.0f, 0.0f);
		
		rp.add( canvas );
	}
}
