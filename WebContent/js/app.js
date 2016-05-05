var BOSH_SERVICE = 'http://bosh.metajack.im:5280/xmpp-httpbind';
var connection = null;

function rawInput(data) {
	console.log('RECV', vkbeautify.xml(data));
}

function rawOutput(data) {
	console.log('SENT', vkbeautify.xml(data));
}
function onMessage(msg) {
    var to = msg.getAttribute('to');
    var from = msg.getAttribute('from');
    var type = msg.getAttribute('type');
    var elems = msg.getElementsByTagName('body');
    if (type == "chat" && elems.length > 0) {
	var body = elems[0];
	var text = Strophe.getText(body);
	console.log('ECHOBOT: I got a message from ' + from + ': ' + 
	    text);
	var xhttp = new XMLHttpRequest();
	xhttp.onreadystatechange = function() {
	  if (xhttp.readyState == 4 && xhttp.status == 200) {
		  
		  document.getElementById("result").innerHTML = xhttp.responseText;
	  }
	};
	//xhttp.open("GET", "GetLocation?location="+document.querySelector("#locationCoordinates").value, true);
	xhttp.open("GET", "https://maps.googleapis.com/maps/api/geocode/json?address="+text, true);
	xhttp.send();	
    }
    latLng = text.split(",")
    var mapOptions = {
        center: new google.maps.LatLng(latLng[0],latLng[1]),
        zoom: 14
    };
    var map = new google.maps.Map(document.querySelector("#map"), mapOptions);
    var marker = new google.maps.Marker({
        position: new google.maps.LatLng(latLng[0],latLng[1]),
        map: map,
        title: 'Current Location of Device.'
      });
    // we must return true to keep the handler alive.  
    // returning false would remove it after it finishes.
    return true;
}

function onConnect(status) {
    if (status == Strophe.Status.CONNECTING) {
    	console.log('Strophe is connecting.');
    } else if (status == Strophe.Status.CONNFAIL) {
    	console.log('Strophe failed to connect.');
	$('#connect').get(0).value = 'connect';
    } else if (status == Strophe.Status.DISCONNECTING) {
    	console.log('Strophe is disconnecting.');
    } else if (status == Strophe.Status.DISCONNECTED) {
    	console.log('Strophe is disconnected.');
	$('#connect').get(0).value = 'connect';
    } else if (status == Strophe.Status.CONNECTED) {
    	console.log('Strophe is connected.');
	var presence = $pres({
		"show" : "available"
	});
	connection.sendIQ(presence, console.log("Presence sent"), console.log("Presence not sent"), 5000)
	connection.addHandler(onMessage, null, 'message', null, null,  null);
    } 
}

$(document).ready(function () {
    connection = new Strophe.Connection(BOSH_SERVICE);
    connection.rawInput = rawInput;
    connection.rawOutput = rawOutput;
    
    $('#connect').bind('click', function () {
	var button = $('#connect').get(0);
//	connection.attach(jid, sid, rid, callback, wait, hold, wind)
    connection.connect($('#jid').get(0).value,
		       $('#pass').get(0).value,
		       onConnect);
    });
});
function start() {
	var message = $msg({
		"to" : "device@ip-172-31-38-82",
		"from" : "frankanstine@ip-172-31-38-82",
		"type" : "chat"
	}).c("body").t("get");
	connection.sendIQ(message, console.log("Message sent"), console.log("Message not sent"), 5000)
}
