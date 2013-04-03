package com.ocean.haivl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.io.file.FileConnection;
import javax.microedition.io.file.FileSystemRegistry;

import net.rim.blackberry.api.browser.Browser;
import net.rim.device.api.io.IOUtilities;
import net.rim.device.api.math.Fixed32;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Characters;
import net.rim.device.api.system.Display;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.system.PNGEncodedImage;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.XYRect;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.component.RichTextField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.xml.parsers.DocumentBuilder;
import net.rim.device.api.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ocean.Util;

/**
 * This class extends the UiApplication class, providing a graphical user
 * interface.
 */
public class HaivlApplication extends UiApplication {
	// Constants
	// ----------------------------------------------------------------	
	private static final String HAIVL_BASE_URL = "http://www.haivl.com";
	private static final String HAIVL_PARAM_SORT = "sort";
	private static final String HAIVL_PARAM_SORT_NEW = "new";
	private static final String HAIVL_PARAM_PAGE = "page";
	
	private static final String YOU_TUBE_PLAYER_URL_PREFIX = "http://www.youtube.com/v/";
	private static final String YOU_TUBE_PLAYER_URL_SUFFIX = "?autoplay=1&autohide=1";

	// Members
	// ------------------------------------------------------------------
	private HaivlScreen _mainScreen;	
//	private WaitingPopup _waitingPopup;
	
	private ScrollableImageField _photoView;	
	private RichTextField _photoTitle;
	
	private Vector _photoList = new Vector();
	
	private PhotoListFetcher _fetchPhotoListThread = new PhotoListFetcher();
	private ImageFetcher _currentImageFetcher;	
	private int _currentPage = 1;
	private int _currentPhoto = -1;
	
	private MenuItem _fetchNextPhotoMenuItem = new MenuItem("Next" , 100, 10) 
    {
        public void run()
        {
            fetchNextPhoto();
        }
    };
    
    private MenuItem _fetchPrevPhotoMenuItem = new MenuItem("Previous" , 100, 10) 
    {
        public void run()
        {
            fetchPrevPhoto();
        }
    };
    
    private MenuItem _zoomInMenuItem = new MenuItem("Zoom in" , 100, 10) 
    {
        public void run()
        {
            _photoView.zoomIn();
        }
    };
    
    private MenuItem _zoomOutMenuItem = new MenuItem("Zoom out" , 100, 10) 
    {
        public void run()
        {
        	_photoView.zoomOut();
        }
    };
    
    private MenuItem _openVideoMenuItem = new MenuItem("Open video" , 100, 10) 
    {
        public void run()
        {           
            openVideo(getCurrentPhoto());
        }
    };  
    
    private MenuItem _savePhotoMenuItem = new MenuItem("Save photo" , 100, 10) 
    {
        public void run()
        {           
           saveCurrentPhoto();
        }
    };
	
	public static void main(String[] args) {
		HaivlApplication theApp = new HaivlApplication();
		theApp.enterEventDispatcher();
	}

	// Inner Classes
	// -------------------------------------------------------------
	private class PhotoListFetcher extends Thread{
		private static final int TIMEOUT = 500; // ms

		private String _theUrl;
		private int _queueCount = 0;
		private boolean _invokeShowPhoto;

		private volatile boolean _start = false;
		private volatile boolean _stop = false;

		public boolean isStarted() {
			return _start;
		}
		
		// Retrieve the URL.
        public synchronized String getUrl()
        {
            return _theUrl  + "&" + HAIVL_PARAM_PAGE + "=" + _currentPage++;
        }
		
		public void setUrl(String url){
			this._theUrl = url;
		}

		// Fetch a page.
		// Synchronized so that I don't miss requests.
		public void fetch(boolean invokeShowPhoto) {
			if(_theUrl == null){
//				Dialog.alert("The url to fetch photo list is null");
				return;
			}
			
			synchronized (this) {
				_invokeShowPhoto = invokeShowPhoto;
				if(isStarted()){
					_queueCount++;
				}else{					
					_start = true;					
				}	
			}
		}

		// Shutdown the thread.
		public void stop() {
			_stop = true;
		}		

		public void run() {
			for (;;) {
				// Thread control.
				while (!_start && !_stop) {
					// Sleep for a bit so we don't spin.
					try {
						sleep(TIMEOUT);
					} catch (InterruptedException e) {
						System.err.println(e.toString());
					}
				}

				// Exit condition.
				if (_stop) {
					return;
				}
				
				synchronized (this) {
					// Open the connection and extract the data.
					HttpConnection connection = null;
					try {
						String url = getUrl();
						connection = (HttpConnection) Connector.open(url + Util.getConnectionString());						
						connection.setRequestMethod(HttpConnection.GET);
						
						int status = connection.getResponseCode();

						if (status == HttpConnection.HTTP_OK) {
							
							InputStream input = connection.openInputStream();

							byte[] data = new byte[256];
							int len = 0;
//							int size = 0;
							StringBuffer raw = new StringBuffer();

							while (-1 != (len = input.read(data))) {
								raw.append(new String(data, 0, len));
//								size += len;
							}
							
							String content = raw.toString();											
							updatePhotoList(content);
							if(_invokeShowPhoto == true){
								fetchNextPhoto();
								_invokeShowPhoto = false;
							}
							input.close();
						}else{
							System.out.println("status error" + status);
//							Dialog.alert("Response code: " + status);
						}
						connection.close();
					} catch (IOException e) {						
//						Dialog.alert(e.toString());	
						System.out.println(e.toString());
					}

					// We're done one connection so reset the start state, else continue fetch photo
					if(_queueCount > 0){
						_queueCount--;
					}else{
						_start = false;
					}					
				}
			}
		}
		
		private void updatePhotoList(String data){
			data = "<div>" + data + "</div>";//System.out.println(data);
			try {
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(new ByteArrayInputStream(data.getBytes()));
				doc.getDocumentElement().normalize();
				
				Element root = doc.getDocumentElement();
				NodeList divs = root.getElementsByTagName("div");
				for(int i = 0;i<divs.getLength();i++){					
					if(getNodeAttr("class",divs.item(i)).compareTo("photoListItem") == 0){
						HaivlPhoto photo = new HaivlPhoto();
						photo.setId(Integer.parseInt(getNodeAttr("data-id",divs.item(i))));
						
						//get title & image
						Element topNode = (Element)divs.item(i);
						NodeList imgs = topNode.getElementsByTagName("img");
						for(int img = 0;img<imgs.getLength();img++){
							String dataSrc = getNodeAttr("data-src",imgs.item(img));
							if(dataSrc.compareTo("") != 0){							
								dataSrc = dataSrc.replace('!', '9');
								dataSrc = dataSrc.replace('@', '1');
								dataSrc = dataSrc.replace('#', '6');								
								photo.setUrl(dataSrc);
								
								//then get title from alt attribute
								photo.setTitle(getNodeAttr("alt",imgs.item(img)));
							}
						}
						
						//get view & comment count (stored in span tag)
						NodeList spans = topNode.getElementsByTagName("span");
						for(int span = 0;span < spans.getLength();span++){
							if(getNodeAttr("class",spans.item(span)).compareTo("views") == 0){
								photo.setViewCount(getNodeValue(spans.item(span)));
							}
							if(getNodeAttr("class",spans.item(span)).compareTo("comments") == 0){
								photo.setCommentCount(getNodeValue(spans.item(span)));
							}
						}
						synchronized (_photoList) {
							_photoList.addElement(photo);
						}						
					}
				}				
			} catch (Exception e) {
//				Dialog.alert(e.toString());
				System.out.println(e.toString());
				
			}
		}
		
		protected String getNodeValue( Node node ) {
		    NodeList childNodes = node.getChildNodes();
		    for (int x = 0; x < childNodes.getLength(); x++ ) {
		        Node data = childNodes.item(x);
		        if ( data.getNodeType() == Node.TEXT_NODE )
		            return data.getNodeValue();
		    }
		    return "";
		}
		 
		protected String getNodeAttr(String attrName, Node node ) {
		    NamedNodeMap attrs = node.getAttributes();
		    for (int y = 0; y < attrs.getLength(); y++ ) {
		        Node attr = attrs.item(y);
		        if (attr.getNodeName().equalsIgnoreCase(attrName)) {
		            return attr.getNodeValue();
		        }
		    }
		    return "";
		}
	}
	
	private class ImageFetcher extends Thread{
		private HaivlPhoto photo;	
		private Thread runThread;
		
		public ImageFetcher(HaivlPhoto photo){
			super();
			this.photo = photo;			
		}
		
		public EncodedImage renderImage(byte[] dataArray){			
			EncodedImage _theImage = EncodedImage.createEncodedImage(dataArray, 0, dataArray.length);
			
			//resize
			int currentWidthFixed32 = Fixed32.toFP(_theImage.getWidth());
			int currentHeightFixed32 = Fixed32.toFP(_theImage.getHeight());
			
			
			int width = Display.getWidth();
			int height = (int)Math.floor((_theImage.getHeight() * width)/_theImage.getWidth());	            
			
			int requiredWidthFixed32 = Fixed32.toFP(width);
			int requiredHeightFixed32 = Fixed32.toFP(height);
			
			int scaleXFixed32 = Fixed32.div(currentWidthFixed32, requiredWidthFixed32);
			int scaleYFixed32 = Fixed32.div(currentHeightFixed32, requiredHeightFixed32);
			
			_theImage = _theImage.scaleImage32(scaleXFixed32, scaleYFixed32);
			
			return _theImage;	
//			Bitmap _theImage = Bitmap.createBitmapFromBytes(dataArray, 0, dataArray.length, 1);
//			int width = Display.getWidth();
//			int height = (int)Math.floor((_theImage.getHeight() * width)/_theImage.getWidth());	   
//			Bitmap _scaleImage = new Bitmap(width,height);
//			_theImage.scaleInto();
		}
		
		public void run() {
			runThread = Thread.currentThread();
			synchronized (this) {
				HttpConnection connection = null;
	            InputStream inputStream = null;
				try {						
					connection = (HttpConnection) Connector.open(photo.getUrl() + Util.getConnectionString());
					
	                int responseCode = connection.getResponseCode();
	                if (responseCode != HttpConnection.HTTP_OK)
	                {
	                    throw new IOException("HTTP response code: " + responseCode);
	                }
	                else{
	                	inputStream = connection.openInputStream();
		                byte[] responseData = null;
		                
		                responseData = IOUtilities.streamToBytes(inputStream);
	     
		                EncodedImage image = renderImage(responseData);
	                    if(image != null){                    	
	                    	updatePhoto(image,photo.getTitle());//	                   
	                    }
	                }
				}	
				catch (InterruptedIOException e) {					
					System.out.println("Image fetcher was interrupted.");
				}
				catch (final Exception e) {
					System.out.println(e.toString());
				}
				finally {				
					try
	                {
	                    inputStream.close();
	                    inputStream = null;
	                    connection.close();
	                    connection = null;
	                }
	                catch(Exception e){}
				}
			}			
		}	
		
		public void stop() {
			this.runThread.interrupt();
			try {
				this.runThread.join();
			} catch (InterruptedException e) {
				System.out.println(e.toString());
			}
		}
	}
	
	private class SplashScreen extends MainScreen {		
		private HaivlApplication app;
		private SplashScreen _this;
		private EncodedImage image = EncodedImage.getEncodedImageResource("splash_logo_one_piece.jpg");
			
		public SplashScreen(HaivlApplication ui) {
			super(NO_VERTICAL_SCROLL);			
			this.app = ui;
			_this = this;			
			
			//resize
			int currentWidthFixed32 = Fixed32.toFP(image.getWidth());
			int currentHeightFixed32 = Fixed32.toFP(image.getHeight());		
			int width = Display.getWidth();
			int height = Display.getHeight();			
			int requiredWidthFixed32 = Fixed32.toFP(width);
			int requiredHeightFixed32 = Fixed32.toFP(height);			
			int scaleXFixed32 = Fixed32.div(currentWidthFixed32, requiredWidthFixed32);
			int scaleYFixed32 = Fixed32.div(currentHeightFixed32, requiredHeightFixed32);
			
			image = image.scaleImage32(scaleXFixed32, scaleYFixed32);			
			this.add(new BitmapField(image.getBitmap(),FIELD_VCENTER));
			
			app.pushScreen(_this);			
			app.fetchPhotoList(true);
			
			new Thread(new Runnable() {				
				public void run() {					
					while(true){
						try 
	                    {
	                        Thread.sleep(500);
	                    } 
	                    catch (InterruptedException e) 
	                    {
	                        System.err.println(e.toString());
	                    }						
						
						if(_photoList.size() > 0 && !_currentImageFetcher.isAlive()){
							dismiss();
							return;
						}											
					}
				}
			}).start();
		}

		public void dismiss() {
			app.invokeLater(new Runnable() {
				public void run() {
					app.popScreen(_this);
					app.pushScreen(_mainScreen);
				}
			});		
		}	
	}
	
	private class ScrollableImageField extends BitmapField
	{	   
	    private Bitmap _theImage;
	    private EncodedImage _originalImage;
	    private int _preferredHeight;   //The height the field request.
	    private int _preferredWidth;    //The width the field requests.
	    
	    private int _xCoord = 0;		//The x coordinate for the top left corner of the image.
	    private int _yCoord = 0;         //The y coordinate for the top left corner of the image.
	    
	    private double _scale = 1;
	    
	    public ScrollableImageField(){
	    	super(null,FOCUSABLE);
	    }
	    
	    protected void drawFocus(Graphics graphics, boolean on) {}
	    
	    protected Bitmap drawPlayIcon(Bitmap image){	    	
	    	Graphics g = new Graphics(image);
	    	
	    	Bitmap icon = Bitmap.getBitmapResource("play_icon1.png");
	    	int width = icon.getWidth();
	    	int height = icon.getHeight();
	    	
	    	int x = (image.getWidth() - width) / 2;
	    	int y = (image.getHeight() - height) / 2;
	    	
	    	g.drawBitmap(x, y, width, height, icon, 0, 0);
	    	
	    	return image;
	    }
	    
	    public void setBitmap(Bitmap bitmap){
	    	setBitmap(bitmap, true);
	    }
	    
	    public void setBitmap(Bitmap bitmap, boolean resetCoord){
	    	if(getCurrentPhoto().isYoutube() == true){	    		
	    		_theImage = drawPlayIcon(bitmap);	        	 
	        }else{	        	
	        	 _theImage = bitmap;	        	
	        }
	        
	        super.setBitmap(_theImage);
	        
	        //Reset the image location to display the top left of the image.
	        if(resetCoord){
	        	_xCoord = 0;
		        _yCoord = 0;
		        _scale = 1;
	        }
	        
	        calculateSize();
	    }
	    
	    public void setImage(EncodedImage image) {
			_originalImage = image;
			setBitmap(_originalImage.getBitmap(),true);
		}
	    
	    protected void layout(int width, int height) {
			if(_theImage.getHeight() < Display.getHeight()){
				height = Display.getHeight();
			}
	    	
			super.setExtent(width, height);
		}

		protected void paint(Graphics graphics)
	    {
	        graphics.setBackgroundColor(Color.BLACK);
	        graphics.clear();
	        
	    	//Get the actual field size from its manager.
	        XYRect rect = this.getManager().getContentRect();
	        
	        //Draw the Bitmap, taking up the full size of the BitmapField.
	        //Start drawing the bitmap at the current coordinates.
	        graphics.drawBitmap(0, 0, rect.width, rect.height, _theImage, _xCoord, _yCoord);
	    }

		//Override navigationMovement to provide image scrolling.
	    protected boolean navigationMovement(int dx, int dy, int status, int time)
	    {  
	        if (scrollImage(dx * 30, dy * 50))
	        {
	        	//The image was scrolled and the movement consumed.
	            return true;
	        }
	        else
	        {
	        	//Scrolling is not required.
	            return super.navigationMovement(dx, dy, status, time);
	        }       
	    }
	    	    
	    //Determines if scrolling is required and handles the positioning of the image 
	    //when scrolling occurs.  Returns true if the image was scrolled.
	    private boolean scrollImage(int xScroll, int yScroll)
	    { 
	    	//Get the actual field size from its manager.
	        XYRect rect = this.getManager().getContentRect();
	       
	        //Determine if the image is larger than the field.
	        if (rect.width < _theImage.getWidth() || rect.height < _theImage.getHeight())
	        {
	        	//prevent scroll top if image at top 
	        	if(_yCoord == 0 && yScroll < 0){
	 	        	_photoTitle.setFocus();
	 	        	return false;
	        	}
	        	
	        	//prevent scroll left if image at left
	        	if(_xCoord == 0 && xScroll < 0){
	        		return false;
	        	}
	        	
	        	//prevent scroll bottom and right
	        	if((_yCoord == _theImage.getHeight() - rect.height && yScroll > 0) || (_xCoord == _theImage.getWidth() - rect.width && xScroll > 0)){
	 	        	return false;
	 	        }
	        	 
	        	//Image is larger than the field.  Enable scrolling support.            
	            _yCoord += yScroll;
	            _xCoord += xScroll;
	            
	            //If the user has scrolled to the end of the image, use the default
	            //navigationMovement to allow focus to scroll off the field.
	            if (_xCoord < 0  || _yCoord < 0  || 
	                    ((_theImage.getWidth() - _xCoord) < rect.width) ||  
	                    ((_theImage.getHeight() - _yCoord) < rect.height))
	            {
	            	
	                //Ensure the coordinates do not go lower than 0.          
	            	if (_xCoord < 0)
	                {
	                    _xCoord = 0;	                    
	                }
	                
	            	if (_yCoord < 0)
	                {
	                    _yCoord = 0;
	                    _photoTitle.setFocus();	                   
	                }
	                
	                //Ensure that we don't scroll beyond the image size (causing white space to be drawn).
	            	if ((_theImage.getWidth() - _xCoord) < rect.width)
	                {
	                    _xCoord = _theImage.getWidth() - rect.width;
	                }
	            	
	                if ((_theImage.getHeight() - _yCoord) < rect.height)
	                {
	                    _yCoord = _theImage.getHeight() - rect.height;                 
	                }	               
	            }
	            
                //The image was scrolled and the movement consumed.
                //Redraw the bitmap.
                this.invalidate();
                return true;	            
	        }
	        else
	        {
	            //Scrolling is not required.
	            return false;
	        }    	
	    }
	    
	    public double getScale(){
	    	return _scale;
	    }
	    
	    public void zoomIn(){
	    	if(_scale == 1){
	    		_scale = 1.5;
	    		_yCoord += (int)(_theImage.getHeight() * 0.25);
	    		_xCoord += (int)(_theImage.getWidth() * 0.25);
	    		zoom();
	    	}
	    }
	    
	    public void zoomOut(){
	    	if(_scale > 1){
	    		_scale = 1;
	    		_xCoord = 0;
	    		int tmp = (int)((_theImage.getHeight() - _originalImage.getBitmap().getHeight())/2);
	    		if(_yCoord - tmp > 0){
	    			_yCoord -= tmp;
	    		}else{
	    			_yCoord = 0;
	    		}	    		
	    		zoom();
	    	}
	    }
	    
	    private void zoom(){
	    	if(!this.isFocus()){
	    		this.setFocus();
	    	}
	    	int scaleWidth = (int)(_originalImage.getBitmap().getWidth() * _scale);
	    	int scaleheight = (int)(_originalImage.getBitmap().getHeight() * _scale);
	    	EncodedImage newImage = resize(_originalImage, scaleWidth, scaleheight);
	    	setBitmap(newImage.getBitmap(), false);
	    }
	    
	    //resize
	    private EncodedImage resize(EncodedImage image, int w, int h){	    	
			int currentWidthFixed32 = Fixed32.toFP(image.getWidth());
			int currentHeightFixed32 = Fixed32.toFP(image.getHeight());		
			
			int requiredWidthFixed32 = Fixed32.toFP(w);
			int requiredHeightFixed32 = Fixed32.toFP(h);	
			
			int scaleXFixed32 = Fixed32.div(currentWidthFixed32, requiredWidthFixed32);
			int scaleYFixed32 = Fixed32.div(currentHeightFixed32, requiredHeightFixed32);
			
			image = image.scaleImage32(scaleXFixed32, scaleYFixed32);			
	    	
			return image;
	    }
	    
	    //Calculates the preferred width and height of the field and determines if the image should scroll. 
	    private void calculateSize()
	    {
	        //Set the preferred height to the image size or screen height if the image is larger than the screen height.
	        if (_theImage.getHeight() > Display.getHeight())
	        {
	            _preferredHeight = Display.getHeight();
	        }
	        else
	        {
	            _preferredHeight = _theImage.getHeight();
	        }
	        
	      //Set the preferred width to the image size or screen width if the image is larger than the screen width.
	        if (_theImage.getWidth() > Display.getWidth())
	        {
	            _preferredWidth = Display.getWidth();
	        }
	        else
	        {
	            _preferredWidth = _theImage.getWidth();
	        }
	    }
	    
	    public int getPreferredHeight()
	    {
	        return _preferredHeight;
	    }
	    
	    public int getPreferredWidth()
	    {
	        return _preferredWidth;
	    }
	    
	    public Bitmap getOriginalBitmap(){
	    	return _originalImage.getBitmap();
	    }
	    
	    public void scrollTop(){
	    	_yCoord = 0;
	    	this.invalidate();
	    }

		public void scrollBottom(){
	    	this.setFocus();  
	    	_yCoord = _theImage.getHeight() > this.getManager().getContentRect().height ? _theImage.getHeight() - this.getManager().getContentRect().height : 0;
	    	this.invalidate();
	    }
	    
	    public void scrollPageDown(){
	    	this.setFocus();
	    	scrollImage(_xCoord,(int)Math.floor(Display.getHeight()*90/100 - (_photoTitle.isFocus() ? _photoTitle.getHeight() : 0)));
	    }
	    
	    public void scrollPageUp(){
	    	this.setFocus();
	    	scrollImage(_xCoord,(int)Math.floor(Display.getHeight()*90/100*-1));
	    }
	}
	
	private class HaivlScreen extends MainScreen {
		
		public HaivlScreen() {
			super(VERTICAL_SCROLL);
		}
		
//		protected void paint(Graphics g) {
//			g.setBackgroundColor(Color.BLUE);
//			g.clear();
//			super.paint(g);
//		}

		protected void makeMenu(Menu menu, int instance)
        {
            menu.add(_fetchNextPhotoMenuItem);
            menu.add(_fetchPrevPhotoMenuItem);
            if(getCurrentPhoto().isYoutube() == true){
            	menu.add(_openVideoMenuItem);
            }
            if(_photoView.getScale() > 1){
            	menu.add(_zoomOutMenuItem);
            }
        	if(_photoView.getScale() == 1){
        		menu.add(_zoomInMenuItem);
            }
            menu.add(_savePhotoMenuItem);
            menu.addSeparator();    
            super.makeMenu(menu, instance);
        }

		protected boolean keyChar(char c, int status, int time) {			
			switch (c) {
			case Characters.LATIN_SMALL_LETTER_H:
			case Characters.LATIN_SMALL_LETTER_P:
				fetchNextPhoto();
				return true;
			case Characters.LATIN_SMALL_LETTER_G:
			case Characters.LATIN_SMALL_LETTER_Q:
				fetchPrevPhoto();
				return true;
			case Characters.LATIN_SMALL_LETTER_F:
				fetchPhotoList(false);
				return true;
			case Characters.LATIN_SMALL_LETTER_V:
				openVideo(getCurrentPhoto());
				return true;
			case Characters.LATIN_SMALL_LETTER_S:
				saveCurrentPhoto();
				return true;
			case Characters.LATIN_SMALL_LETTER_T:
				scrollTop();
				return true;
			case Characters.LATIN_SMALL_LETTER_B:
				_photoView.scrollBottom();
				return true;
			case Characters.SPACE:
			case Characters.LATIN_SMALL_LETTER_X:
				_photoView.scrollPageDown();
				return true;
			case Characters.LATIN_SMALL_LETTER_E:
				_photoView.scrollPageUp();
				return true;
			case Characters.LATIN_SMALL_LETTER_I:
				_photoView.zoomIn();
				return true;
			case Characters.LATIN_SMALL_LETTER_O:
				_photoView.zoomOut();
				return true;
			default:
				break;
			}
			
			return super.keyChar(c, status, time);
		}

		public void close() {
			_fetchPhotoListThread.stop();
			super.close();
		}
		
		public void scrollTop(){
			_photoTitle.setFocus();
			_photoTitle.setCursorPosition(0);
			_photoView.scrollTop();
		}			
	}

	/**
	 * Creates a new Main Screen object
	 */
	public HaivlApplication() {
		_mainScreen = new HaivlScreen();
		
//		_waitingPopup = new WaitingPopup(this);
		
		_photoView = new ScrollableImageField();
		_photoTitle = new RichTextField(RichTextField.FOCUSABLE|RichTextField.USE_ALL_WIDTH);
		_fetchPhotoListThread.setUrl(HAIVL_BASE_URL + "/photos/more?" + HAIVL_PARAM_SORT + "=" + HAIVL_PARAM_SORT_NEW);
		_fetchPhotoListThread.start();
		
		_mainScreen.add(_photoTitle);
		_mainScreen.add(new SeparatorField());
		_mainScreen.add(_photoView);
		
		new SplashScreen(this);		
	}

	// Methods
	// ------------------------------------------------------------------	
	public void fetchPhotoList(boolean invokeDisplayPhoto)
	{
		_fetchPhotoListThread.fetch(invokeDisplayPhoto);
	}
	
	public void fetchNextPhoto(){
		if(_currentPhoto < _photoList.size() - 1){
			HaivlPhoto photo = (HaivlPhoto)_photoList.elementAt(++_currentPhoto);
			showPhoto(photo);
			
			if(_currentPhoto == _photoList.size() - 2){
				fetchPhotoList(false);
			}
		}else{
			fetchPhotoList(true);
		}
	}
	
	public void fetchPrevPhoto(){
		if(_currentPhoto > 0){
			HaivlPhoto photo = (HaivlPhoto)_photoList.elementAt(--_currentPhoto);
			showPhoto(photo);
		}
	}	
	
	private void showPhoto(HaivlPhoto photo){		
		try {
			if(_currentImageFetcher != null && _currentImageFetcher.isAlive()){
				ImageFetcher tmp = _currentImageFetcher;
				_currentImageFetcher = null;
				tmp.stop();
			}
		} catch (Exception e) {
			System.out.println(e.toString());
		}
		
		_currentImageFetcher = new ImageFetcher(photo);
		_currentImageFetcher.start();				
	}
	
	private void updatePhoto(final EncodedImage image, final String photoTitle){
		UiApplication.getUiApplication().invokeLater(new Runnable() {
			public void run() {
				String title = String.valueOf(_currentPhoto + 1) + ". ";		
				_photoTitle.setText(title + photoTitle);
				_mainScreen.scrollTop();
				_photoView.setImage(image);				
			}
		});
	}
	
	public HaivlPhoto getCurrentPhoto(){
		HaivlPhoto photo = (HaivlPhoto)_photoList.elementAt(_currentPhoto);
		return photo;
	}
	
	public void openVideo(HaivlPhoto photo){
    	if(photo.isYoutube() == true && photo.getYoutubeId() != ""){
        	String youtubeId = photo.getYoutubeId();
        	
        	String url = YOU_TUBE_PLAYER_URL_PREFIX + youtubeId + YOU_TUBE_PLAYER_URL_SUFFIX;
        	
        	Browser.getDefaultSession().displayPage(url);
        }
    }
	
	private void saveCurrentPhoto() 
    {		
        this.invokeLater(new Runnable() {
			public void run() {
				FileConnection fconn = null;
				OutputStream outputStream = null;
				
		        try
		        {
		        	String PHOTO_DIR;
		        	boolean hasSDCard = false;
		        	
		        	Enumeration rootEnum = FileSystemRegistry.listRoots();
		        	while (rootEnum.hasMoreElements()) {
		        		String root = (String) rootEnum.nextElement();
		        		if(root.indexOf("SDCard") > -1){
		        			hasSDCard = true;
		        			break;
		        		}
		        	}
		        	
		        	if(hasSDCard){
		        		PHOTO_DIR = System.getProperty("fileconn.dir.memorycard.photos");
		        	}else{
		        		PHOTO_DIR = System.getProperty("fileconn.dir.photos");
		        	}			    
		        
					String fileName = "haivl/photo - " + getCurrentPhoto().getId() + ".png";
			        String filePath = PHOTO_DIR + fileName;      
			        
			        //try create haivl folder if not exists
			        fconn = (FileConnection)Connector.open(PHOTO_DIR + "haivl/",Connector.READ_WRITE);
			        if(!fconn.exists()){
			        	fconn.mkdir();
			        }			        
			        
		        	fconn = (FileConnection)Connector.open(filePath, Connector.READ_WRITE);
		            if(!fconn.exists()){
		            	 fconn.create();
		            }

		            outputStream = fconn.openOutputStream();
		           
		            byte[] imageBytes = PNGEncodedImage.encode(_photoView.getOriginalBitmap()).getData();                   
		            outputStream.write(imageBytes);
		            outputStream.close();
		            fconn.close();
		            
		            Dialog.alert("Save image success to \"" + filePath.substring(7, filePath.length()) + "\"");
		        }
		        catch(Exception e){
		        	Dialog.alert(e.toString());
		            System.out.println("Exception while saving Bitmap: "+e.toString());
		        }
		        finally{
		        	try{
		        		if(outputStream != null){
		        			outputStream.close();
		        		}
		        		if(fconn != null){
		        			fconn.close();
		        		}			        	
		        	}catch (Exception e) {						
					}		        	
		        }
			}
		});		
    }	
}
