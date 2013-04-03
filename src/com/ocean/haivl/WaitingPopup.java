package com.ocean.haivl;

import net.rim.device.api.system.Characters;
import net.rim.device.api.system.KeyListener;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.XYEdges;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

public class WaitingPopup extends PopupScreen {
	private WaitingPopup _this;
	private UiApplication _app;
	private LabelField _status;
	private int _totalWidth;	
	private int _totalHeight;

	public WaitingPopup(UiApplication uiapp) {
		super(new VerticalFieldManager());
		_this = this;
		_app = uiapp;
		// addKeyListener(new WaitingPopupListener(_this));
		_status = new LabelField("Loading . . .",FIELD_HCENTER){
			
			protected void layout(int width, int height) {
				// TODO Auto-generated method stub
				super.layout(width, height);
				setExtent(_totalWidth + 30, _totalHeight);
			}			
			
		};	
		_status.setPadding(new XYEdges(5,10,5,10));
		_totalWidth = _status.getPreferredWidth();	
		_totalHeight = _status.getPreferredHeight();
		add(_status);
	}
	
	

//	protected void sublayout(int width, int height) {
//		super.sublayout(width, height);
////        setExtent(200,100);
////        layoutDelegate( 200,100 );
//        setPositionDelegate(10,10);
////        //placing in the center
////        int leftMargin = (Display.getWidth() - 200)/2;
////        int topMargin = (Display.getHeight() - 100)/2;
////        setPosition(leftMargin, topMargin);
//	}



	public void display() {		
		_app.invokeLater(new Runnable() {
			public void run() {
				_app.pushScreen(_this);
			}
		});

	}

	public void dismiss() {
		_app.invokeLater(new Runnable() {
			public void run() {
				_app.popScreen(_this);
			}
		});
	}

	public void updateContent(final String text) {
		// This will create significant garbage, but avoids threading issues
		// (compared with creating a static Runnable and setting the text).
		_app.invokeLater(new Runnable() {
			public void run() {
				_status.setText(text);
			}
		});
	}

	public static class WaitingPopupListener implements KeyListener {
		private WaitingPopup _popupScreen;

		public boolean keyChar(char key, int status, int time) {
			// intercept the ESC and MENU key - exit the splash screen
			boolean retval = false;
			switch (key) {
			case Characters.CONTROL_MENU:
			case Characters.ESCAPE:
				_popupScreen.dismiss();
				retval = true;
				break;
			}
			return retval;
		}

		public boolean keyDown(int keycode, int time) {
			// TODO Auto-generated method stub
			return false;
		}

		public boolean keyRepeat(int keycode, int time) {
			// TODO Auto-generated method stub
			return false;
		}

		public boolean keyStatus(int keycode, int time) {
			// TODO Auto-generated method stub
			return false;
		}

		public boolean keyUp(int keycode, int time) {
			// TODO Auto-generated method stub
			return false;
		}

		public WaitingPopupListener(WaitingPopup waitingPopup) {
			_popupScreen = waitingPopup;
		}

	}
}
