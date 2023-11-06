function appcallOnFocus(){

	
}

// 하드웨어 가속 사용 필요 여부 체크
function checkHardwareAccel(){
	var AVOID_VERSION_LIST = [ "4.1", "4.2", "4.3" ];
	
	var osVersion = bizMOB.Device.getInfo({ "_sKey" : "device_os_version" })!=undefined ? bizMOB.Device.getInfo({ "_sKey" : "device_os_version" }).split(".").slice(0, 2).join("."):"0";
	var modelName = bizMOB.Device.getInfo({ "_sKey" : "model" });
	var enableHardwareAccel = true;
	
	if(modelName.indexOf("LG-") == -1)	{
		if(AVOID_VERSION_LIST.indexOf(osVersion) >= 0)	{
			enableHardwareAccel = false;
		}
	}
	
	// 더 추가되어야할 단말 조건이 있을 경우 조건 추가
	
	return enableHardwareAccel;
}

function onNetworkStateChange(res){
	if(location.href.indexOf("EVT0001")<0){
		bizMOB.Window.alert({_sTitle : "알림", _vMessage : "통신 상태 변경 : " + res.data.type})
	}
}

function setConvertButton(){
	var convertEnable = false;
	
	if(convertEnable){
		$(".btnConvert").removeClass("none");
	}
}

bizMOB.addEvent("networkstatechange", "onNetworkStateChange");
bizMOB.addEvent("ready", "setConvertButton");
//bizMOB.addEvent("onsessiontimeout", "onSessionTimeout");

