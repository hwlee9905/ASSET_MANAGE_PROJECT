/**
 * @author jhchul@mcnc.co.kr
 * @since 2017.08.18
 * @desc 유틸리티 함수
 */
(function($, undefined){
	window.util = {};
	
	/**
	 * 화면 open
	 * @param {String} _sPagePath : 화면 이동경로 (필수)
	 * @param 기타 옵션은 bizMOB-core.js (bizMOBCore.Window.open)참고
	 * @desc
	 */
	util.WindowOpen = function(_sPagePath){
		var param = $.extend(false, {
			_bReplace : false,
			_sOrientation : "portrait"
		}, arguments[0]);
		
		if(param._bReplace == false) {
			bizMOB.Window.open(param);
		} else {
			bizMOB.Window.replace(param);
		}
	};
	
	/**
	 * Main Open (메인 화면으로 이동)
	 */
	util.goMain = function(){
		bizMOB.Window.go({
			_sName : "main",
			_sCallback : "page.resume",
			_oMessage : {}
		});
	};
	
	/**
	 * App Exit (앱 종료)
	 * @param {String} type : "logout", "kill"
	 */
	util.appExit = function(type){
		var btnOk = bizMOB.Window.createElement({_sElementName : "TextButton"});
		btnOk.setProperty({
			_sText : languagePack.getCommonScriptText("confirm"),
			_fCallback : function(){
				if(type == "kill"){ bizMOB.App.exit({_sType:"kill"}); }
				else { bizMOB.Window.go({_sName : "index"}); }
			}
		});
		
		var btnCancel = bizMOB.Window.createElement({_sElementName : "TextButton"});
		btnCancel.setProperty({
			_sText : languagePack.getCommonScriptText("cancel"),
			_fCallback : function(){}
		});
		
		bizMOB.Window.confirm({
			_sTitle : languagePack.getCommonScriptText("noti"),
			_vMessage : type == "kill" ? languagePack.getCommonScriptText("appKill") : languagePack.getCommonScriptText("logout"),
			_aTextButton : [btnOk, btnCancel]
		});
	};
	
	/**
	 * 프로그레스 상태창
	 * ===========================
	 * @name : show (프로그레스 오픈)
	 * @param {String} title : 제목
	 * @param {String} message : 메시지
	 * ===========================
	 * @name : dismiss (프로그레스 닫기)
	 * ===========================
	 */
	util.Progress = {
		"show" : function(title, message){
			var param = {
				type : "progress",
				title : title,
				message : message,
				callback : ""
			};
			bizMOB.Util.callPlugIn("SHOW_PROGRESS", param);
		},
		"dismiss" : function(){
			var param = {
				type : "progress",
				callback : ""
			};
				
			bizMOB.Util.callPlugIn("DISMISS_PROGRESS", param);
		}
	};
	
	
	/**
	 * 현재 화면ID 정보
	 * @returns
	 */
	util.currPageId = function(){
		return document.URL.substring(document.URL.lastIndexOf("/") + 1, document.URL.lastIndexOf("."));
	};
	
	/**
	 * 현재 화면패스 정보
	 * @returns
	 */
	util.currPagePath = function(){
		return location.pathname.replace(/.+contents\//gi, '');
	};
	
	/**
	 * 에뮬레이터 여부 판단
	 */
	util.detectEmulator = function(){
		return bizMOB.Device.getInfo({ _sKey : "model" }).toLowerCase() == "emulator" ? true : false;
	};
	
	
	/**
	 * popup sorting open
	 */
	util.popupSortingOpen = function(){
		var msg = { jobStatus : page.jobStatus };
		
		if(util.currPageId() == "SVC0100"){
			msg.eventType = "cody";
			msg.filterChange = page.filterChange;
		} else {
			msg.eventType = "ct";
		}
		util.WindowOpen({
			_sPagePath : "SVC/html/SVC9003.html",
			_sType : "popup",
			_sWidth : "100%",
			_sHeight : "100%",
			_oMessage : msg
		});
	};
	
	/**
	 * 메인 화면을 통하여 들어온 사이드 메뉴 뷰 닫은 후 이벤트 처리
	 * @param {Object} event : close 후 처리할 데이터
	 * =============================================
	 * event arguments
	 * 		{String} type : 페이지 전환 옵션 ("main", "pageOpen")
	 * 		{Object} param : 화면 open data (type 이 "pageOpen" 인 경우)
	 */
	util.mainSideMenuCloseCallback = function(event){
		var infoData = event.data;
		alert(JSON.stringify(infoData));
		switch (infoData.type) {
		case "pageOpen" :
			util.WindowOpen(infoData.info);
			break;
		case "main" :
			util.goMain();
			break;
		}
	};
	
	/**
	 *  메인 화면이 아닌 다른 화면을 통하여 사이드 메뉴 뷰 닫은 후 이벤트 처리
	 * @param {Object} arg : close 후 처리할 데이터
	 * @desc 페이지 replace 처리함
	 */
	util.postSideMenuCloseCallback = function(arg){
		bizMOB.SideView.hide();
		alert(JSON.stringify(arg));
		arg.info._bReplace = true;
		util.WindowOpen(arg.info);
	};
	
	// LNB 보여주기 
	util.openLNB = function()
	{		
		bizMOB.SideView.show({
			_sPosition 	: "left",
			_oMessage 	: {
				speed : 200,
				openerPageId : util.getPageId()
			}
		});			
	};	
	
	/**
	 * 화면ID
	 * */
	util.getPageId = function()
	{
		return document.URL.substring(document.URL.lastIndexOf("/") + 1, document.URL.lastIndexOf("."));
	},
	
	/**
	 * 버튼 더블 클릭 방지 플러그인
	 */
	$.fn.preventBtnDbclick = function(){
		var $that = $(this);
		$that.prop("disabled", true);
		setTimeout(function(){ $that.prop("disabled", false) }, 400);
	};
	
})(jQuery, undefined);