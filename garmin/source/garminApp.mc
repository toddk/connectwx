using Toybox.Application as App;
using Toybox.WatchUi as Ui;
using Toybox.System as Sys;
using Toybox.Communications as Comm;
using Toybox.Graphics as Gfx;

var descString = "Weather Description";
var locationString = "Location";

class BaseView extends Ui.View {

	function initialize() {
		View.initialize();
		Comm.setMailboxListener( method(:onMail) );
	}
	
	function onUpdate(dc) {
		dc.setColor( Gfx.COLOR_TRANSPARENT, Gfx.COLOR_BLACK );
        dc.clear();
        dc.setColor( Gfx.COLOR_WHITE, Gfx.COLOR_TRANSPARENT );
	
		
		dc.drawText(10, 80, Gfx.FONT_SMALL, locationString, Gfx.TEXT_JUSTIFY_LEFT);
		dc.drawText(10, 110, Gfx.FONT_SMALL, descString, Gfx.TEXT_JUSTIFY_LEFT);
	}
	
	function onMail(mailIter) {
		var mail;
		
		mail = mailIter.next();
		
		while(mail != null) {
			Sys.println(mail);
			
			descString = mail.substring(0,mail.find(","));
			locationString = mail.substring(mail.find(",") + 1, mail.length());
			
			mail = mailIter.next();
		}
		
		Comm.emptyMailbox();
		Ui.requestUpdate();
	}
}

class CommsListener extends Comm.ConnectionListener {

	function initialize() {
		ConnectionListener.initialize();
	}

	function onComplete() {
		Sys.println("transmit complete");
	}
	
	function onError() {
		Sys.println("transmit failed");
	}
}

class garminApp extends App.AppBase {

	function initialize() {
		AppBase.initialize();
	}

    //! onStart() is called on application start up
    function onStart(state) {
    }

    //! onStop() is called when your application is exiting
    function onStop(state) {
    }

    //! Return the initial view of your application here
    function getInitialView() {
        return [ new BaseView() ];
    }

}