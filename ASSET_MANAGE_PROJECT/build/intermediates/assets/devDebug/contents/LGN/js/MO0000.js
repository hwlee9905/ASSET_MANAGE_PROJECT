bizMOB.addEvent("resume", "page.resume");
bizMOB.addEvent("onsessiontimeout", "page.resume");
bizMOB.addEvent("sessiontimeout", "page.resume");

var page = {
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
	initInterface: function() {
		$(".loginBtn").click(function() {
			bizMOB.Window.open({
				_sPagePath: "LGN/html/MO0004.html",
				_sName: "main",
				_bHardwareAccel: checkHardwareAccel()
			});
		});
	},
	callbackFunc : function(res){
        $("#resultUserName").text(res.name);
        $("#resultUserAge").text(res.age);
    }
};
function callback(json){
	alert(JSON.stringify(json));
}
