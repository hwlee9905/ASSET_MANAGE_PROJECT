bizMOB.addEvent("onStatusChange", "page.onStatusChange");
bizMOB.addEvent("push", "page.onPush")

var page = {
    pushkey : "not get push key",
	deviceId : "",
	init: function(evt) {
		page.initData(evt.data);
		page.initLayout();
		page.initInterface();

	},
	initData: function(data) {

	},

	initLayout: function() {
		var titlebar = bizMOB.Window.createTitleBar({ _sTitle: "index" });
		titlebar.setProperty({
			_bVisible: false
		});

		bizMOB.Window.draw({
			_aElement: [titlebar]
		});

	},
    onPush: function(json) {
        document.getElementById("showjsonlat").innerHTML = JSON.stringify(json)
//        alert(JSON.stringify(json));
//        alert(json.data.image);
//        document.getElementById("img").src = json.data.image;
    },
	resume: function() {
		alert("resume?");
	},
	initInterface: function() {
		$("#bt01").click(function(){
			var v = {
                "id": "SAMPLE_PLUGIN",
                "param": {
                    callback : "callback"
                }
			};
			bizMOB.System.callCamera({
                "_sFileName" : "capture.jpg",
                "_sDirectory" : "{external}/bizMOB/",
                "_bAutoVerticalHorizontal" : true,
                "_fCallback" : function(result){
                    var capturedImagePath = result.path;
                    displayCapturedImage(capturedImagePath);
                }
            });

		});

	    $("#bt02").click(function(){
            var v = {
                "id": "GET_PUSHKEY",
                "param": {
                    callback : "callback"
                }
            };

            bizMOB.Util.callPlugIn(v.id, v.param);

        });
        $("#bt03").click(function(){

            var v = {
                "id": "GET_NEW_LOCATION",
                "param": {
                    callback : "callback"
                }
            };
            bizMOB.Util.callPlugIn(v.id, v.param);
        });
        $("#bt04").click(function () {
//            alert(pushkey);
//            if(pushkey === ""){
//                alert("pushkey is Required");
//                return;
//            }
            var v = {
                "id": "PRACTICE_PLUGIN",
                "param": {
//                    pushkey : pushkey,
                    callback : "callback"
                }
            };
            bizMOB.Util.callPlugIn(v.id, v.param);


        });
	}, callback : function(json){
		alert(JSON.stringify(json));
	}
};
function callback(json){
	alert(JSON.stringify(json));
	console.log(JSON.stringify(json));
	pushkey  = json.resultMessage;
	alert("결과 메시지: " + pushkey);
//	document.getElementById("showjsonlat").innerHTML = json.latitude;
//	document.getElementById("showjsonlong").innerHTML = json.longitude;
};
function onResume(){
	alert("onResume");
};