using Toybox.Application as App;
using Toybox.WatchUi as Ui;
using Toybox.System as Sys;
using Toybox.Communications as Comm;

class BaseView extends Ui.View {

	function initialize() {
		Comm.setMailboxListener( method(:onMail) );
	}
	
	function onMail(mailIter) {
		var mail;
		
		mail = mailIter.next();
		
		while(mail != null) {
			Sys.println(mail);
			
			mail = mailIter.next();
		}
		
		Comm.emptyMailbox();
		Ui.requestUpdate();
	}
}

class CommsListener extends Comm.ConnectionListener {
	
	function onComplete() {
		Sys.println("transmit complete");
	}
	
	function onError() {
		Sys.println("transmit failed");
	}
}

class garminApp extends App.AppBase {

    //! onStart() is called on application start up
    function onStart() {
    }

    //! onStop() is called when your application is exiting
    function onStop() {
    }

    //! Return the initial view of your application here
    function getInitialView() {
        return [ new BaseView() ];
    }

}