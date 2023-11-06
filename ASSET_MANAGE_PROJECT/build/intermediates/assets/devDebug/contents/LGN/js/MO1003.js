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
	    $(".btn-home").click(function() {
            bizMOB.Window.open({
                _sPagePath: "LGN/html/MO0004.html",
                _sName: "home",
                _bHardwareAccel: checkHardwareAccel()
            });
        });
		$(".btn-device").click(function() {
			bizMOB.Window.open({
				_sPagePath: "LGN/html/MO1005.html",
				_sName: "selectAll",
				_bHardwareAccel: checkHardwareAccel()
			});
		});
		$(".btn-user").click(function() {
            bizMOB.Window.open({
                _sPagePath: "LGN/html/MO1004.html",
                _sName: "profile",
                _bHardwareAccel: checkHardwareAccel()
            });
        });
        $(".btn-menu").click(function() {
            bizMOB.Window.open({
                _sPagePath: "LGN/html/MO0005.html",
                _sName: "menu",
                _bHardwareAccel: checkHardwareAccel()
            });
        });
        $(".btn-menu1").click(function() {
            bizMOB.Window.open({
                _sPagePath: "LGN/html/MO1001.html",
                _sName: "selectAll",
                _bHardwareAccel: checkHardwareAccel()
            });
        });
        $(".btn-menu3").click(function() {
            bizMOB.Window.open({
                _sPagePath: "LGN/html/MO1002.html",
                _sName: "selectHW",
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
