

/**
 * bizMOB client xross interface 
 * 
 * @class	bizMOB xross 인터페이스
 * @version 3.0.0
 * @author	mobile C&C
 */
var bizMOB = new Object();

bizMOB.servicename = "bizMOB";

/**
 * 이벤트 등록
 * 
 * @param String sEvent 이벤트명
 * @param String sCallback 이벤트 핸들러
 * 
 * @return 
 */

bizMOB.addEvent = function(sEvent, sCallback) {
	
	var action = "addevent";
	
	// Event Manager에게 event 저장해달라고 요청
	if(bizMOBCore.EventManager.storage[sEvent] ) {
		bizMOBCore.EventManager.storage[sEvent].push(sCallback);
		bizMOBCore.Module.logger(this.servicename, action ,"D", "'"+sEvent+ "' event added.");
		//ex) [bizMOB][response]로 로그 찍힘
	}else{
		bizMOBCore.Module.logger(this.servicename, action ,"D", "Event add failed. Cannot find '"+sEvent+"' eventname.");
	}
	
};

/**
 * 
 * 01.클래스 설명 : Web Application 로그 클래스
 * 02.제품구분 : bizMOB Xross
 * 03.기능(콤퍼넌트) 명 : Web Application개발시 로그 작성
 * 04.관련 API/화면/서비스 : bizMOBCore.Module.logger
 *
 * @author 김승현 
 * @version 1.0 
 * 
 */
bizMOB.Logger = new Object();

/**
 * info Level 로그 작성
 * 
 * @param String _sMessage 로그 메세지
 * 
 * @return 
 */
bizMOB.Logger.info = function(_sMessage) {
	
	bizMOBCore.Module.logger("Page", "logging" ,"I", _sMessage);
	
};

/**
 * warn Level 로그 작성
 * 
 * @param String _sMessage 로그 메세지
 * 
 * @return 
 */
bizMOB.Logger.warn = function(_sMessage) {
	
	bizMOBCore.Module.logger("Page", "logging" ,"W", _sMessage);
	
};

/**
 * debug Level 로그 작성
 * 
 * @param String _sMessage 로그 메세지
 * 
 * @return 
 */
bizMOB.Logger.debug = function(_sMessage) {
	
	bizMOBCore.Module.logger("Page", "logging" ,"D", _sMessage);
	
};

/**
 * error Level 로그 작성.
 * 
 * @param String _sMessage 로그 메세지
 * 
 * @return 
 */
bizMOB.Logger.error = function(_sMessage) {
	
	bizMOBCore.Module.logger("Page", "logging" ,"E", _sMessage);
	
};

/**
 * 
 * 01.클래스 설명 : Web Storage 클래스
 * 02.제품구분 : bizMOB Xross
 * 03.기능(콤퍼넌트) 명 : 휘발성 데이터 저장소
 * 04.관련 API/화면/서비스 : bizMOBCore.Module.checkparam,bizMOBCore.Storage.set, bizMOBCore.Storage.get, bizMOBCore.Storage.remove
 * @author 김승현 
 * @version 1.0 
 * 
 */
bizMOB.Storage = new Object();

/**
 * Storage 데이터 저장
 * 
 * @param String _sKey 저장할 값의 키
 * @param Variable _vValue 저장할 값
 * 
 * @return 
 */
bizMOB.Storage.set = function(_sKey_vValue) {
	
	var required = new Array("_sKey","_vValue");
	
	if(!bizMOBCore.Module.checkparam(arguments[0], required)) {
		return;
	}
	
	bizMOBCore.Storage.set(arguments[0]);
	
};

/**
 * Storage 복수 데이터 저장
 * 
 * @param Array _aList 저장할 데이터들의 배열
 * 
 * @return 
 */
bizMOB.Storage.setList = function(_aList) {
	
	var required = new Array("_aList");
	
	if(!bizMOBCore.Module.checkparam(arguments[0], required)) {
		return;
	}
	
	bizMOBCore.Storage.set(arguments[0]);
	
};

/**
 * Storage 데이터 불러오기
 * 
 * @param String _sKey 저장 값의 키
 * 
 * @return 
 */
bizMOB.Storage.get = function(_sKey) {
	
	var required = new Array("_sKey");
	
	if(!bizMOBCore.Module.checkparam(arguments[0], required)) {
		return;
	}
	
	return bizMOBCore.Storage.get(arguments[0]);
	
};


/**
 * Storage 데이터 삭제
 * 
 * @param String _sKey 저장 값의 키
 * 
 * @return 
 */
bizMOB.Storage.remove = function(_sKey) {
	
	var required = new Array("_sKey");
	
	if(!bizMOBCore.Module.checkparam(arguments[0], required)) {
		return;
	}
	
	bizMOBCore.Storage.remove(arguments[0]);
	
};

/**
 * 
 * 01.클래스 설명 : Properties 저장 클래스
 * 02.제품구분 : bizMOB Xross
 * 03.기능(콤퍼넌트) 명 : 영구 데이터 저장소
 * 04.관련 API/화면/서비스 : bizMOBCore.Module.checkparam, bizMOBCore.Properties.set, bizMOBCore.Properties.get,bizMOBCore.Properties.remove
 *
 * @author 김승현 
 * @version 1.0 
 * 
 */
bizMOB.Properties = new Object();

/**
 * Properties 데이터 저장
 * 
 * @param String _sKey 저장할 값의 키
 * @param Variable _vValue 저장할 값
 * 
 * @return 
 */
bizMOB.Properties.set = function(_sKey_vValue) {
	
	var required = new Array("_sKey","_vValue");
	
	if(!bizMOBCore.Module.checkparam(arguments[0], required)) {
		return;
	}
	
	bizMOBCore.Properties.set(arguments[0]);
	
};

/**
 * Properties 복수 데이터 저장.
 * 
 * @param Array _aList 저장할 데이터들의 배열
 * 
 * @return 
 */

bizMOB.Properties.setList = function(_aList) {
	
	var required = new Array("_aList");
	
	if(!bizMOBCore.Module.checkparam(arguments[0], required)) {
		return;
	}
	
	bizMOBCore.Properties.set(arguments[0]);
	
};

/**
 * Properties 데이터 불러오기
 * 
 * @param String _sKey 저장 값의 키
 * 
 * @return 
 */
 
bizMOB.Properties.get = function(_sKey) {
	
	var required = new Array("_sKey");
	
	if(!bizMOBCore.Module.checkparam(arguments[0], required)) {
		return;
	}
	
	return bizMOBCore.Properties.get(arguments[0]);
	
};

/**
 * Properties 데이터 삭제
 * 
 * @param String _sKey 저장 값의 키
 * 
 * @return 
 */

bizMOB.Properties.remove = function(_sKey) {
	
	var required = new Array("_sKey");
	
	if(!bizMOBCore.Module.checkparam(arguments[0], required)) {
		return;
	}
	
	bizMOBCore.Properties.remove(arguments[0]);
	
};


/**
 * 
 * 01.클래스 설명 : Network 통신 클래스
 * 02.제품구분 : bizMOB Xross
 * 03.기능(콤퍼넌트) 명 : bizMOB Server와 통신
 * 04.관련 API/화면/서비스 : bizMOBCore.Module.checkparam, bizMOBCore.Network.requestTr,bizMOBCore.Network.requestLogin
 *
 * @author 김승현 
 * @version 1.0 
 * 
 */
bizMOB.Network = new Object();

/**
 * bizMOB Server 전문 통신
 * 
 * @param String _aTrList	bizMOB Server 인증 전문 목록
 * @param Function _fCallback	서버와 통신 후 실행될 callback 함수
 *  
 * @return 
 */
bizMOB.Network.requestTrList = function(_aTrList) {
	
	var required = new Array("_aTrList");
	
	if(!bizMOBCore.Module.checkparam(arguments[0], required)) {
		return;
	}
	
	if( arguments[0]._aTrList.length < 1 ) {
		return;
	}
	
	bizMOBCore.Network.requestTrList (arguments[0]);
	
};

/**
 * bizMOB Server 전문 통신
 *
 * @param String _sTrcode	bizMOB Server 인증 전문코드
 * @param String _oHeader	bizMOB Server 인증 전문 Header 객체
 * @param String _oBody		bizMOB Server 인증 전문 Body 객체
 * @param Boolean _bProgressEnable		(default:true) 서버에 통신 요청시 progress 표시 여부( true 또는 false )
 * @param Function _fCallback	서버와 통신 후 실행될 callback 함수
 *  
 * @return 
 */

bizMOB.Network.requestTr = function(_sTrcode) {
	
	var required = new Array("_sTrcode");
	
	if(!bizMOBCore.Module.checkparam(arguments[0], required)) {
		return;
	}
	
	bizMOBCore.Network.requestTr (arguments[0]);
	
};

/**
 * bizMOB Server 로그인(인증)전문 통신
 * 
 * @param String _sUserId	인증 받을 사용자 아이디
 * @param String _sPassword	인증 받을 사용자 패스워드
 * @param String _sTrcode	레거시 로그인 인증 전문코드
 * @param String _oHeader	레거시 로그인 인증 전문 Header 객체
 * @param String _oBody		레거시 로그인 인증 전문 Body 객체
 * @param Boolean _bProgressEnable		(default:true) 서버에 통신 요청시 progress 표시 여부( true 또는 false )
 * @param Function _fCallback	서버와 통신 후 실행될 callback 함수
 *  
 * @return 
 */

bizMOB.Network.requestLogin = function(_sUserId_sPassword) {
	
	var required = new Array("_sUserId","_sPassword");
	
	if(!bizMOBCore.Module.checkparam(arguments[0], required)) {
		return;
	}
	
	bizMOBCore.Network.requestLogin (arguments[0]);	
	
};

/**
 * 
 * 01.클래스 설명 : System 기능 클래스
 * 02.제품구분 : bizMOB Xross
 * 03.기능(콤퍼넌트) 명 : OS 기반 기본 기능
 * 04.관련 API/화면/서비스 : bizMOBCore.Module.checkparam,bizMOBCore.System.callBrowser, bizMOBCore.System.callCamera, bizMOBCore.System.callGallery, bizMOBCore.System.callMap, bizMOBCore.System.callSMS, bizMOBCore.System.callTEL, bizMOBCore.System.getGPS
 *
 * @author 김승현 
 * @version 1.0 
 * 
 */
bizMOB.System = new Object();

/**
 * 전화걸기
 * 
 * @param String _sNumber	전화번호
 * @param Function _fCallback	실행후 결과를 처리할 callback 함수
 *  
 * @return 
 */
bizMOB.System.callTEL = function(_sNumber){
	
	var required = new Array("_sNumber");
	
	if(!bizMOBCore.Module.checkparam(arguments[0], required)) {
		return;
	}
	
	var number = arguments[0]._sNumber;
	
	arguments[0]._sNumber = number ? (number.match(/(^[+0-9])|[0-9]/gi).join("")) : "";
	
	bizMOBCore.System.callTEL(arguments[0]);
	
};

/**
 * 문자보내기
 * 
 * @param Array _aNumber	메세지를 보낼 전화번호 배열
 * @param String _sMessage	보낼 메세지
 * @param Function _fCallback	실행후 결과를 처리할 callback 함수
 *  
 * @return 
 */
bizMOB.System.callSMS = function(_aNumber){
	
	var required = new Array("_aNumber");
	
	if(!bizMOBCore.Module.checkparam(arguments[0], required)) {
		return;
	}
	
	bizMOBCore.System.callSMS(arguments[0]);
	
};

/**
 * 단말기 설치된 브라우져 열기
 * 
 * @param String _sURL	메세지를 보낼 전화번호 배열
 *  
 * @return 
 */
bizMOB.System.callBrowser = function(_sURL){
	
	var required = new Array("_sURL");
	
	// if(!bizMOBCore.Module.checkparam(arguments[0], required)) {
	// 	return;
	// }
	
	// bizMOBCore.System.callBrowser(arguments[0]);
	bizMOBCore.System.callBrowser(_sURL);
	
};

/**
 * 단말기 디바이스의 갤러리(사진앨범) 보기
 * 
 * @param String _sType	String	(Default : all) 갤러리에서 불러올 미디어 타입( all, image, video )가 있습니다.
 * @param Function _fCallback	갤러리에서 선택한 미디어를 결과를 전달 받아서 처리할 callback 함수.

 *  
 * @return 
 */
bizMOB.System.callGallery = function(_fCallback){
	
	var required = new Array("_fCallback");
	
	if(!bizMOBCore.Module.checkparam(arguments[0], required)) {
		return;
	}
	
	switch(arguments[0]._sType)
	{
		case "all" :
			arguments[0]._sType = ["video","image"];
			break;
		case "video" :
			arguments[0]._sType = ["video"];
			break;
		case "image" :
			arguments[0]._sType = ["image"];
			break;
		default :
			arguments[0]._sType = ["video","image"];
			break;
	}
	
	bizMOBCore.System.callGallery(arguments[0]);
	
};

/**
 * 단말기 카메라 촬영
 * 
 * @param Function _fCallback		갤러리에서 선택한 미디어를 전달 받아서 처리하는 callback 함수
 * @param String _sFileName		찍은 이미지를 저장할 이름
 * @param String _sDirectory	찍은 이미지를 저장할 경로
 * @param Boolean _bAutoVerticalHorizontal	(Default : true) 찍은 이미지를 화면에 맞게 자동으로 회전시켜 저장할지를 설정 값
 *  
 * @return  
 */
bizMOB.System.callCamera = function(_fCallback){
	
	var required = new Array("_fCallback");
	
	if(!bizMOBCore.Module.checkparam(arguments[0], required)) {
		return;
	}
	
	bizMOBCore.System.callCamera(arguments[0]);
	
};


/**
 * 단말기 지도 실행
 * 
 * @param String _sLocation	위치 정보(주소, 위경도값)
 *  
 * @return  
 */
bizMOB.System.callMap = function(_sLocation){
	
	var required = new Array("_sLocation");
	
	if(!bizMOBCore.Module.checkparam(arguments[0], required)) {
		return;
	}
	
	bizMOBCore.System.callMap(arguments[0]);
	
};

/**
 * OS별 지도 실행
 * 
 * @param String _sLocation	위치 정보(주소, 위경도값)
 *  
 * @return  
 */
bizMOB.System.getGPS = function(_fCallback){
	
	var required = new Array("_fCallback");
	
	if(!bizMOBCore.Module.checkparam(arguments[0], required)) {
		return;
	}
	
	bizMOBCore.System.getGPS(arguments[0]);
	
};

/**
 * biZMOB Window class
 * 
 * 01.클래스 설명 : bizMOB Window 클래스 
 * 02.제품구분 : bizMOB Xross
 * 03.기능(콤퍼넌트) 명 :  bizMOB Client에서 생성하는 Window 객체
 * 04.관련 API/화면/서비스 : bizMOBCore.Module.checkparam,bizMOBCore.Window.alert,bizMOBCore.Window.back,bizMOBCore.Window.close,bizMOBCore.Window.confirm,bizMOBCore.Window.createElement,bizMOBCore.Window.createElement.setProperty,bizMOBCore.Window.createSideBar,bizMOBCore.Window.createSideBar.setProperty,bizMOBCore.Window.createTitleBar,bizMOBCore.Window.createTitleBar.setProperty,bizMOBCore.Window.createToolBar,bizMOBCore.Window.createToolBar.setProperty,bizMOBCore.Window.draw,bizMOBCore.Window.go,bizMOBCore.Window.open,bizMOBCore.Window.openCodeReader,bizMOBCore.Window.openImageViewer,bizMOBCore.Window.openSignPad,bizMOBCore.Window.postMessage,bizMOBCore.Window.replace,bizMOBCore.Window.toast
 *
 * @author 김승현 
 * @version 1.0 
 * 
 */
bizMOB.Window = new Object();

/**
 * 이전 Window로 이동
 * 
 * @param Number __nStep	이전 Window로 이동할 스텝(음수)을 지정.(0 또는 양의 정수일경우 이동하지 않음)
 * @param Object _oMessage	이동할 Window에 전달할 데이터 
 * @param String _sCallback		페이지가 열린 후 실행될 callback함수 이름입니다. 
 *  
 * @return 
 */
bizMOB.Window.back = function(_nStep) {
	
	var required = new Array("_nStep");
	
	if(!bizMOBCore.Module.checkparam(arguments[0], required)) {
		return;
	}
	arguments[0]._sType = "index";
	
	bizMOBCore.Window.go(arguments[0]);
	
};

/**
 * 특정 Window로 이동
 * 
 * @param String _sName		이동할 Window 이름.
 * @param Object _oMessage	이동할 Window에 전달할 데이터. 
 * @param String _sCallback		이동할 Window에서 실행될 callback함수명.
 *  
 * @return 
 */
bizMOB.Window.go = function(_sName) {
	
	var required = new Array("_sName");
	
	if(!bizMOBCore.Module.checkparam(arguments[0], required)) {
		return;
	}
	
	arguments[0]._sType = "name";
	
	bizMOBCore.Window.go(arguments[0]);
	
};


/**
 * 새로운 Window 열기
 * 
 * @param String _sPagePath	페이지 경로.
 * @param String _sType		(Default : normal) 화면을 열 타입. ( normal 또는 popup )
 * @param String _sOrientation		(Default : portrait )페이지 열때 로딩시 회전 기준값. ( portrait 또는 land )
 * @param Object _oMessage	페이지 로딩시 전달할 데이터.
 * @param String _sName		페이지 이름( bizMOB.Window.go API에서 사용할 이름 ).
 * @param Boolean _bHardwareAccel		(default:false)페이지 로딩할 WebView에 대한 하드웨어 가속 옵션 설정값( true 또는 false )
 * @param String _sWidth	팝업으로 페이지를 열 경우 팝업 창의 가로 사이즈입니다.% 와 px로 지정 가능함.
 * @param String _sHeight	팝업으로 페이지를 열 경우 팝업 창의 세로 사이즈입니다.% 와 px로 지정 가능함.
 * @param String _sBaseSize	(default:device) 멀티레이아웃 사용시 팝업으로 페이지를 열경우 사이즈값의 단위가 백분율(%)로 지정될 경우 사이즈의 기준 값 ( device 뚀는 page )
 * @param String _sBaseOrientation	(default : auto) 멀티레이아웃 사용시 팝업으로 페이지를 열경우 사이즈값의 단위가 백분율(%)로 지정될 경우 rotate의 기준 값 (auto , vertical, horizontal )
 *  
 * @return 
 */
bizMOB.Window.open = function(_sPagePath) {
	// replace와 open API구분되어 있음
	// Native API는 동일
	var required = new Array("_sPagePath");
	
	if(!bizMOBCore.Module.checkparam(arguments[0], required)) {
		return;
	}
	arguments[0]._bReplace = false;
	if(arguments[0] == undefined){ arguments[0] = {_sType : "normal"}; }
	
	if(arguments[0]._sType == "popup"){
		bizMOBCore.Window.openpopup(arguments[0]);
	}else{
		bizMOBCore.Window.open(arguments[0]);
	}
	
};


/**
 *  Window 이동(웹뷰 추가X)
 * 
 * @param String _sPagePath			현재 페이지를 대체할 페이지 경로입니다.
 * @param String _sOrientation			페이지 회전 방향입니다. portrait와 land이 있습니다.portrait : 세로 방향land : 가로 방향
 * @param Object _oMessage			열게될 페이지에 전달할 데이터입니다.
 *  
 * @return 
 */
bizMOB.Window.replace = function(_sPagePath) {
	
	var required = new Array("_sPagePath");
	
	if(!bizMOBCore.Module.checkparam(arguments[0], required)) {
		return;
	}
	
	arguments[0]._bReplace = true;
	arguments[0]._sType = "normal";
	
	bizMOBCore.Window.open(arguments[0]);
	
};

/**
 * Window 닫기
 * 
 * @param Object _oMessage	이전 페이지에 전달할 데이터.
 * @param String _sCallback		이전 페이지에서 실행할 callback함수 이름.
 * @param String _sType		(default:normal) 닫을 페이지의 유형입니다. ( normal 또는 popup )
 * 
 * @return 
 */
bizMOB.Window.close = function() {
	
	if(arguments[0] == undefined){ arguments[0] = {_sType : "normal"}; }
	
	if(!bizMOBCore.Module.checkparam(arguments[0])) {
		return;
	}
	
	if(arguments[0]._sType == "popup"){
		bizMOBCore.Window.closepopup(arguments[0]);
	}else{
		bizMOBCore.Window.close(arguments[0]);
	}
	
};


/**
 * Element 생성
 * 
 * @param String _sElementName	생성할 엘리먼트 이름. ( TextButton 또는 ImageButton )
 *  
 * @return Element Object( TextButton Object/ImageButton Object )
 */
bizMOB.Window.createElement = function(_sElementName) {
	// 유일하게 객체 생성해서 return 해주는 API	
	var required = new Array("_sElementName");
	
	if(!bizMOBCore.Module.checkparam(arguments[0],required)) {
		return;
	}
	
	switch(arguments[0]._sElementName){
		case "TextButton" :
			return new bizMOBCore.Window.TextButton();
			break;
		case "ImageButton" :
			return new bizMOBCore.Window.ImageButton();
			break;
		case "BadgeButton" :
			return new bizMOBCore.Window.BadgeButton();
			break;
	}
	
};

/**
 * TitleBar 생성
 * 
 * @param String _sTitle	타이틀바에 표시될 Text
 *  
 * @return TitleBar Object
 */
bizMOB.Window.createTitleBar = function() {
	if(arguments[0] == undefined){ arguments[0] = {_sTitle : ""}; }
	return new bizMOBCore.Window.TitleBar(arguments[0]);
};

/**
 * ToolBar 생성
 * 
 * @param Array _aImageButton	툴바에 들어갈 이미지 버튼 배열
 *  
 * @return ToolBar Object
 */
bizMOB.Window.createToolBar = function() {
	if(arguments[0] == undefined){ arguments[0] = {_aImageButton : []}; }
	return new bizMOBCore.Window.ToolBar(arguments[0]);
};


/**
 * SideBar 생성
 * 
 * @param Array _aImageButton	사이드 툴바에 들어갈 이미지 버튼 배열
 *  
 * @return SideBar Object
 */
bizMOB.Window.createSideBar = function(_aImageButton) {
	if(arguments[0] == undefined){ arguments[0] = {_aImageButton : []}; }
	return new bizMOBCore.Window.SideBar(arguments[0]);
};

/**
 * UI Element 생성 요청
 * 
 * @param Array _aElement	생성할 엘리먼트들의 객체 배열.
 *  
 * @return 
 */
bizMOB.Window.draw = function() {
	bizMOBCore.Window.draw(arguments[0]);
};

/**
 * 경고창  띄우기
 * 
 * @param String  _sTitle		경고창 상단에 표시할 제목.
 * @param Variable _vMessage		경고창에 표시할 내용.
 * @param Object _eTextButton	경고창에 삽입할 TextButton 엘리먼트.
 *  
 * @return 
 */
bizMOB.Window.alert = function(_vMessage){
	// alert는 확인창 하나
	// _aButton에서 확인창 setting해주고 showMessage호출	
	var required = new Array("_vMessage");
	
	if(!bizMOBCore.Module.checkparam(arguments[0],required)) {
		return;
	}
	
//	if(!bizMOBCore.Module.checkelement(arguments[0]._eTextButton, "TextButton" ) ){
//		return;
//	}
	
	if(arguments[0]._vMessage && arguments[0]._vMessage.constructor !== String) {
		arguments[0]._sMessage = bizMOBCore.Module.stringjson(arguments[0]._vMessage);
	}else{
		arguments[0]._sMessage = arguments[0]._vMessage;
	}
	
	arguments[0]._aButtons = new Array();
	
	if(!arguments[0]._eTextButton) {
		arguments[0]._aButtons.push(bizMOB.Window.createElement({_sElementName:"TextButton"}));
	}else{
		arguments[0]._aButtons.push(arguments[0]._eTextButton);
	}
	
	bizMOBCore.Window.showMessage(arguments[0]);
	
};

/**
 * 확인창  띄우기
 * 
 * @param String  _sTitle		확인창 상단에 표시할 제목.
 * @param Variable _vMessage		확인창에 표시할 내용.
 * @param Array _aTextButton	확인창에 삽입할 TextButton 엘리먼트들.
 *  
 * @return 
 */
bizMOB.Window.confirm = function(_vMessage_aTextButton){
	
	var required = new Array("_vMessage","_aTextButton");
	
	if(!bizMOBCore.Module.checkparam(arguments[0],required)) {
		return;
	}
	
	if(arguments[0]._vMessage && arguments[0]._vMessage.constructor !== String) {
		arguments[0]._sMessage = bizMOBCore.Module.stringjson(arguments[0]._vMessage);
	}else{
		arguments[0]._sMessage = arguments[0]._vMessage;
	}
	
	arguments[0]._aButtons = arguments[0]._aTextButton;
	
	bizMOBCore.Window.showMessage(arguments[0]);
	
};

/**
 * Toast  띄우기
 * 
 * @param String _sMessage	토스트에 표시할 Text.
 * @param String _sDuration		 (Default : short) 토스트를 표시할 시간( short 또는 long )
 *  
 * @return 
 */
bizMOB.Window.toast = function(_sMessage){
	
	var required = new Array("_sMessage");
	
	if(!bizMOBCore.Module.checkparam(arguments[0],required)) {
		return;
	}

	// android toast기본 API가 long, short
	if (!arguments[0]._sDuration) arguments[0]._sDuration = "long";
		
	bizMOBCore.Window.toast(arguments[0]);
	
};
/**
 * SignPad(서명) Window 띄우기
 * 
 * @param String _sTargetPath		사인패드에서 서명한 이미지를 저장할 File Path.
 * @param Function _fCallback		사인패드 처리 결과값을 받을 callback 함수.
 *  
 * @return 
 */
bizMOB.Window.openSignPad = function(){
	
	if(arguments[0] == undefined) { arguments[0] = {}; }
	
	if(arguments[0]._sTargetPath == undefined)	{
		arguments[0]._sTargetPath = "{external}/signpad/sign.bmp"
	}
	
	bizMOBCore.Window.openSignPad(arguments[0]);
	
};
/**
 * ImageViewer  띄우기
 *
 * @param String _sImagePath	이미지 뷰어로 열 이미지 File Path.
 * @param Function _fCallback	 이미지 뷰어 Close시 결과값을 받을 callback함수.
 *  
 * @return 
 */
bizMOB.Window.openImageViewer = function(){
	
	if(arguments[0] == undefined){ arguments[0] = {}; }
	
	bizMOBCore.Window.openImageViewer(arguments[0]);
	
};
/**
 * CodeReader( BarCode, QRCode )  띄우기
 * 
 * @param Function _fCallback	 Code 판독 결과값을 받을 callback함수.
 *  
 * @return 
 */
bizMOB.Window.openCodeReader = function(){
	
	if(arguments[0] == undefined){ arguments[0] = {}; }
	
	bizMOBCore.Window.openCodeReader(arguments[0]);
	
};

/**
 * FileExplorer 띄우기
 * 
 * @param Function _fCallback	탐색기에서 선택한 파일 정보 결과값을 받을 callback함수.
 *  
 * @return 
 */
bizMOB.Window.openFileExplorer = function(){
	
	var required = new Array("_fCallback");
	
	if(!bizMOBCore.Module.checkparam(arguments[0],required)) {
		return;
	}
	
	bizMOBCore.Window.openFileExplorer(arguments[0]);
	
};

/**
 * 사이드 뷰 쪽의 함수 호출
 * 
 * @param String _sPosition		전달할 사이드 뷰의 위치 지정값. left 또는 right
 * @param String _sCallback	사이드 뷰에 호출될 함수 명
 * @param Object _oMessage	사이드 뷰에 전달 될 메세지
 *   
 * @return 
 */

bizMOB.Window.postMessage = function(_sPosition_sCallback)	{
	var required = new Array("_sPosition", "_sCallback");
	
	if(!bizMOBCore.Module.checkparam(arguments[0], required)) {
		return;
	}
	
	bizMOBCore.SideView.postMessage(arguments[0]);
};

/**
 * 01.클래스 설명 : SideView  클래스 . 
 * 02.제품구분 : bizMOB Xross
 * 03.기능(콤퍼넌트) 명 : 사이드 뷰(SideView) 제어에 관한 클래스입니다. 
 * 04.관련 API/화면/서비스 : bizMOBCore.Module.checkparam,bizMOBCore.SideView.create,bizMOBCore.SideView.hide,bizMOBCore.SideView.show
 *
 * @author 김승현 
 * @version 1.0 
 * 
 */
bizMOB.SideView = new Object();

/**
 * 사이드 뷰 생성
 * 
 * @param String _sPosition		사이드 뷰의 위치 지정값. left 또는 right
 * @param String _sPagePath	사이드 뷰에 열릴 페이지 경로
 * @param String _sWidth		사이드 뷰의 가로 크기( %단위)
 * @param Object _oMessage	사이드 뷰에 전달 될 메세지  
 * @return 
 */
bizMOB.SideView.create = function(_sPosition_sPagePath_sWidth)	{
	var required = new Array("_sPosition", "_sPagePath", "_sWidth");
	
	if(!bizMOBCore.Module.checkparam(arguments[0], required)) {
		return;
	}
	
	bizMOBCore.SideView.create(arguments[0]);
};

/**
 * 사이드 뷰 열기
 * 
 * @param String _sPosition		열어볼 사이드 뷰의 위치 지정값. left 또는 right
 * @param Object _oMessage	사이드 뷰에 전달 될 메세지  
 * @return 
 */
bizMOB.SideView.show = function(_sPosition)	{
	var required = new Array("_sPosition");
	
	if(!bizMOBCore.Module.checkparam(arguments[0], required)) {
		return;
	}
	
	bizMOBCore.SideView.show(arguments[0]);
};

/**
 * 사이드 뷰 닫기
 * 
 * @param Object _oMessage	사이드 뷰에 전달 될 메세지
 *   
 * @return 
 */
bizMOB.SideView.hide = function()	{
	var required = new Array();
	
	if(!arguments[0])	arguments[0] = {};
	
	if(!bizMOBCore.Module.checkparam(arguments[0], required)) {
		return;
	}
	
	bizMOBCore.SideView.hide(arguments[0]);
};

/**
 * 사이드 뷰 함수 호출
 * 
 * @param String _sPosition		전달할 사이드 뷰의 위치 지정값. left 또는 right
 * @param String _sCallback	사이드 뷰에 호출될 함수 명
 * @param Object _oMessage	사이드 뷰에 전달 될 메세지
 *   
 * @return 
 */
bizMOB.SideView.postMessage = function()	{
	var required = new Array("_sPosition","_sCallback");
	
	if(!bizMOBCore.Module.checkparam(arguments[0], required)) {
		return;
	}
	
	bizMOBCore.SideView.postMessage(arguments[0]);
};

bizMOB.App = new Object();
/** 
 * 01.클래스 설명 : App 컨트롤 관련 클래스
 * 02.제품구분 : bizMOB Xross
 * 03.기능(콤퍼넌트) 명 : App 컨트롤 관련 기능 
 * 04.관련 API/화면/서비스 : bizMOBCore.Module.checkparam,bizMOBCore.App.exit, bizMOBCore.App.getTimeout, bizMOBCore.App.setTimeout 
 *
 * @author 김승현 
 * @version 1.0 
 * 
 */
bizMOB.App = new Object();

bizMOB.App.servicename = "App";

/**
 * App 종료
 * 
 * @param String _sType	(Default : kill )어플리케이션 종료 유형( logout 또는 kill )
 *   
 * @return 
 */
bizMOB.App.exit = function(){
	
	arguments[0]._sType = arguments[0]._sType? arguments[0]._sType : "kill";
	
	// exit 두가지 옵션(kil or logout[로그아웃 후 첫화면으로 이동])
	// App.exit에서 네이티브로 exit type넘겨줌
	bizMOBCore.App.exit(arguments[0]);
	
};

/**
 * App 자동 종료 시간 설정
 * 
 * @param Number _nSeconds	( default : 7200 )어플리케이션의 세션 만료 시간(초단위) 설정 값.
 *   
 * @return 
 */
bizMOB.App.setTimeout = function(_nSeconds){
	
	var required = new Array("_nSeconds");
	
	if(!bizMOBCore.Module.checkparam(arguments[0],required)) {
		return;
	}

	bizMOBCore.App.requestTimeout(arguments[0]);
	
};

/**
 * App 자동 종료 설정 시간 조회
 * 
 * @param Function _fCallback	세션 만료 시간을 받아서 처리할 Callback 함수.
 *   
 * @return 
 */
bizMOB.App.getTimeout = function(){
	
	var required = new Array("_fCallback");
	
	if(!bizMOBCore.Module.checkparam(arguments[0],required)) {
		return;
	}
	//조회시 -1로 설정
	arguments[0]._nSeconds = -1;
	
	bizMOBCore.App.requestTimeout(arguments[0]);
	
};

/**
 * App 호출가능 인터페이스 목록 조회
 * 
 * @param Function _fCallback	인터페이스 목록을 전달받을 콜백 함수
 *   
 * @return Array list 인터페이스 ID 목록
 */
bizMOB.App.getAvailableInterfaceList = function(){
	
	var required = new Array("_fCallback");
	
	if(!bizMOBCore.Module.checkparam(arguments[0],required)) {
		return;
	}	
	
	bizMOBCore.App.getAvailableInterfaceList(arguments[0]);
	
};

/**
 * 01.클래스 설명 : 전화번호부 클래스
 * 02.제품구분 : bizMOB Xross
 * 03.기능(콤퍼넌트) 명 : 전화번호부 관련 클래스 
 * 04.관련 API/화면/서비스 : bizMOBCore.Module.checkparam,bizMOBCore.App.exit, bizMOBCore.App.getTimeout, bizMOBCore.App.setTimeout 
 *
 * @author 김승현 
 * @version 1.0 
 * 
 */
bizMOB.Contacts = new Object();

/**
 * 전화번호부 검색
 * 
 * @param String _sSearchType	(Default : "", 전체조회) 주소록 검색 대상 필드(name 또는 phone) 
 * @param String _sSearchText	(Default : "") 주소록 검색어
 * @param Function _fCallback	주소록 검색 결과를 받아 처리할 callback함수
 *  
 * @return 
 */
bizMOB.Contacts.get = function() {
	
	if(!arguments[0]._sSearchType) arguments[0]._sSearchType = "";
	if(!arguments[0]._sSearchText) arguments[0]._sSearchText = "";
	
	bizMOBCore.Contacts.get(arguments[0]);
};

/**
 * 01.클래스 설명 : File 클래스
 * 02.제품구분 : bizMOB Xross
 * 03.기능(콤퍼넌트) 명 : File 컨트롤 클래스.  
 * 04.관련 API/화면/서비스 : bizMOBCore.Module.checkparam,bizMOBCore.File.copy,bizMOBCore.File.directory,bizMOBCore.File.download,bizMOBCore.File.exist,bizMOBCore.File.getinfo,bizMOBCore.File.move,bizMOBCore.File.open,bizMOBCore.File.remove,bizMOBCore.File.resizeImage,bizMOBCore.File.rotateImage,bizMOBCore.File.unzip,bizMOBCore.File.upload,bizMOBCore.File.zip
 *
 * @author 김승현 
 * @version 1.0 
 * 
 */
bizMOB.File = new Object();

/**
 * 파일 열기
 * 
 * @param String _sSourcePath 열어볼 파일 경로. 기본 설치App으로 연결.
 * @param Function _fCallback 파일을 열고 난 후 호출될 callback함수.
 *  
 * @return 
 */
bizMOB.File.open = function(_sSourcePath){
	
	var required = new Array("_sSourcePath");
	
	if(!bizMOBCore.Module.checkparam(arguments[0], required)) {
		return;
	}
	
	bizMOBCore.File.open(arguments[0]);
};


/**
 * 파일 압축
 * 
 * @param String _sSourcePath 소스 File Path.
 * @param String _sTargetPath 결과 File Path.
 * @param Function _fCallback 압축 후 호출될 callback함수.
 *  
 * @return 
 */
bizMOB.File.zip= function(_sSourcePath_sTargetPath){
	
	var required = new Array("_sSourcePath","_sTargetPath");
	
	if(!bizMOBCore.Module.checkparam(arguments[0], required)) {
		return;
	}
	
	bizMOBCore.File.zip(arguments[0]);
};


/**
 * 파일 압축해제
 *
 * @param String _sSourcePath 소스 File Path.
 * @param String _sDirectory 압축 해제할 Directory Path.
 * @param Function _fCallback 압축 후 호출될 callback함수.
 *  
 * @return 
 */
bizMOB.File.unzip= function(_sSourcePath_sDirectory){
	var required = new Array("_sSourcePath","_sDirectory");
	
	if(!bizMOBCore.Module.checkparam(arguments[0], required)) {
		return;
	}
	
	bizMOBCore.File.unzip(arguments[0]);
};

/**
 * 파일 이동
 * 
 * @param String _sSourcePath 소스 File Path.
 * @param String _sTargetPath 이동될 File Path.
 * @param Function _fCallback 이동 후 호출될 callback함수.
 *  
 * @return 
 */
bizMOB.File.move = function(_sSourcePath_sTargetPath){
	var required = new Array("_sSourcePath","_sTargetPath");
	
	if(!bizMOBCore.Module.checkparam(arguments[0], required)) {
		return;
	}
	
	bizMOBCore.File.move(arguments[0]);
};

/**
 * 파일 복사
 * 
 * @param String _sSourcePath 소스 File Path.
 * @param String _sTargetPath 복사될 File Path.
 * @param Function _fCallback 복사 후 호출될 callback함수.
 *  
 * @return 
 */
bizMOB.File.copy = function(_sSourcePath_sTargetPath){
	var required = new Array("_sSourcePath","_sTargetPath");
	
	if(!bizMOBCore.Module.checkparam(arguments[0], required)) {
		return;
	}
	
	bizMOBCore.File.copy(arguments[0]);
};

/**
 * 파일 삭제
 * 
 * @param Array _aSourcePath 삭제할 File Path 목록.
 * @param Function _fCallback 삭제 후 호출될 callback함수.
 *  
 * @return 
 */
bizMOB.File.remove = function(_aSourcePath) {
	
	var required = new Array("_aSourcePath");
	
	if(!bizMOBCore.Module.checkparam(arguments[0], required)) {
		return;
	}
	
	bizMOBCore.File.remove(arguments[0]);
	
};

/**
 * 디렉토리 정보 읽기
 * 
 * @param Array _aSourcePath 삭제할 File Path 목록.
 * @param Function _fCallback 삭제 후 호출될 callback함수.
 *  
 * @return 
 */
bizMOB.File.directory = function(_sDirectory){
	var required = new Array("_sDirectory");
	
	if(!bizMOBCore.Module.checkparam(arguments[0], required)) {
		return;
	}
	
	bizMOBCore.File.directory(arguments[0]);
};


/**
 * 파일 존재 여부 확인
 * 
 * @param String _sSourcePath 확인할 File Path 목록.
 * @param Function _fCallback 확인 후 호출될 callback함수.
 *  
 * @return 
 */
bizMOB.File.exist = function(_sSourcePath) {
	
	var required = new Array("_sSourcePath");
	
	if(!bizMOBCore.Module.checkparam(arguments[0], required)) {
		return;
	}
	
	bizMOBCore.File.exist(arguments[0]);
	
};

/**
 * 파일 다운로드
 * 
 * @param Array _aFileList	다운로드할 URL 주소 목록.
 * @param String _sMode		파일 다운로드 모드. (background 또는 foreground ). 
 * @param String _sProgressBar	다운로드할 때 프로그래스바 설정 값.( off , each, full )
 * @param Function _fCallback		결과를 받을 callback 함수.
 *  
 * @return 
 */
bizMOB.File.download = function(_aFileList) {
	
	var required = new Array("_aFileList");
	
	if(!bizMOBCore.Module.checkparam(arguments[0], required)) {
		return;
	}
	
	var progressbar = {};
	
	switch(arguments[0]._sProgressBar)
	{
	// provider: progressbar를 어디서 그릴지 결정
	// native-> native에서 그림
		case "full" :
			progressbar.provider = "native";
			progressbar.type = "full_list";
			break;
		case "each" :
			progressbar.provider = "native";
			progressbar.type = "each_list";
			break;
		case "off" :
			progressbar.provider = "web";
			progressbar.type = "";
			break;
		default :
			progressbar.provider = "native";
			progressbar.type = "default"; 
			// default: full_list
			break;
	}
	
	arguments[0]._oProgressBar = progressbar;
	
	bizMOBCore.File.download(arguments[0]);
	
};


/**
 * 파일 업로드
 * 
 * @param Array _aFileList	업로드할 File Path 목록.
 * @param Function _fCallback		결과를 받을 callback 함수.
 *  
 * @return 
 */
bizMOB.File.upload = function(_aFileList)	{
	
	var required = new Array("_aFileList");
	
	if(!bizMOBCore.Module.checkparam(arguments[0], required)) {
		return;
	}
	
	bizMOBCore.File.upload(arguments[0]);
};

/**
 * 파일 정보 가져오기
 * 
 * @param Array _aFileList	정보를 가져올 File Path 목록.
 * @param Function _fCallback		결과를 받을 callback 함수.
 *  
 * @return 
 */
bizMOB.File.getInfo = function(_aFileList)	{
	
	var required = new Array("_aFileList");
	
	if(!bizMOBCore.Module.checkparam(arguments[0], required)) {
		return;
	}
	
	bizMOBCore.File.getInfo(arguments[0]);
};

/**
 * 이미지 파일 리사이즈
 * 
 * @param Array _aFileList	이미지 파일 목록.
 * @param Boolean _bIsCopy	(Default : false) 원본 파일 유지 여부. (true 또는 false)
 * @param String _sTargetDirectory	_bIsCopy가 true일 경우 복사본이 저장될 디렉토리 경로.
 * @param Number _nWidth	파일의 가로 크기를 설정.
 * @param Number _nHeight	 파일의 세로 크기를 설정.
 * @param Number _nCompressRate	Number	X (Default : 1.0) 파일의 압축률 값( 0.0부터 1.0까지 값 지정가능 )
 * @param Number _nFileSize	리사이즈 된 파일 용량의 최대값.( byte단위 )
 * @param Function _fCallback	결과를 받아 처리할 callback 함수.
 *  
 * @return 
 */
bizMOB.File.resizeImage = function(_aFileList)	{
	
	var required = new Array("_aFileList");
    
	if(!bizMOBCore.Module.checkparam(arguments[0], required)) {
		return;
	}
	
	bizMOBCore.File.resizeImage(arguments[0]);
};

/**
 * 이미지 파일 회전
 * 
 * @param String _sSourcePath		이미지 File Path.
 * @param String _sTargetPath		회전된 이미지가 저장될 Path.
 * @param Number _nOrientation	회전 시킬 각도(EXIF_Orientation)값.(1, 2, 3, 4, 5, 6, 7, 8 )
 * @param Function _fCallback		결과를 받아 처리할 callback 함수.
 *  
 * @return 
 */
bizMOB.File.rotateImage = function(_sSourcePath_sTargetPath_nOrientation)    {
	
    var required = new Array("_sSourcePath", "_sTargetPath", "_nOrientation");
    
    if(!bizMOBCore.Module.checkparam(arguments[0], required)) {
		return;
	}
    
    bizMOBCore.File.rotateImage(arguments[0]);
};

/**
 * 01.클래스 설명 : Push 기능 클래스
 * 02.제품구분 : bizMOB Xross
 * 03.기능(콤퍼넌트) 명 : bizPush Server Open API연동 본 기능 
 * 04.관련 API/화면/서비스 : bizMOBCore.Module.checkparam,bizMOBCore.Push.getAlarm,bizMOBCore.Push.getMessageList,bizMOBCore.Push.getPushKey,bizMOBCore.Push.getUnreadCount,bizMOBCore.Push.readMessage,bizMOBCore.Push.registerToServer,bizMOBCore.Push.sendMessage,bizMOBCore.Push.setAlarm,bizMOBCore.Push.setBadgeCount
 *
 * @author 김승현 
 * @version 1.0 
 * 
 */
bizMOB.Push = new Object();

/**
 * 푸시 기본 저장 정보 초기화
 * 
 * @param 
 *   
 * @return 
 */
bizMOB.Push.reset = function()	{
	var required = new Array();

	if(!bizMOBCore.Module.checkparam(arguments[0], required)) {
		return;
	}

	bizMOBCore.PushManager.reset(arguments[0]);
};

/**
 * 푸시키 정보 조회
 * 
 * @param Function _fCallback	결과를 받아 처리할 callback 함수.
 * @param Boolean _bProgressEnable		(default:true) 푸시 서버와 통신 중일때 화면에 progress 를 표시할지에 대한 여부( true 또는 false )
 *  
 * @return 
 */
bizMOB.Push.getPushKey = function()	{
	var required = new Array("_fCallback");

	if(!bizMOBCore.Module.checkparam(arguments[0], required)) {
		return;
	}

	bizMOBCore.PushManager.getPushKey(arguments[0]);
};

/**
 * 푸시서버에 사용자 정보 등록
 * 
 * @param String _sServerType		푸시키를 등록할 서버 타입.( bizpush 또는 push )
 * @param String_sUserId			푸시키를 등록할 사용자 아이디.
 * @param String _sAppName		푸시키를 등록할 앱 이름.
 * @param Function _fCallback		결과를 받아 처리할 callback 함수.
 * @param Boolean _bProgressEnable		(default:true) 푸시 서버와 통신 중일때 화면에 progress 를 표시할지에 대한 여부( true 또는 false )
 *  
 * @return 
 */
bizMOB.Push.registerToServer = function(_sServerType_sUserId_sAppName)	{
	var required = new Array("_sServerType", "_sUserId", "_sAppName");
	
	if(!bizMOBCore.Module.checkparam(arguments[0], required)) {
		return;
	}

	bizMOBCore.PushManager.registerToServer(arguments[0]);
};

/**
 * 푸시 알람 수신여부 설정
 * 
 * @param String _sUserId		푸시 알림 설정을 등록할 사용자 이이디.
 * @param Boolean  _bEnabled		(Default : true) 알람 수신 여부 설정 ( true 또는 false )
 * @param Boolean _bProgressEnable		(Default:true) 푸시 알람 설정 요청시 화면에 progress 표시 여부( true 또는 false )
 * @param Function _fCallback	결과를 받아 처리할 callback 함수.
 *  
 * @return 
 */
bizMOB.Push.setAlarm = function(_sUserId)	{
	// bizMOB push on/off기능: 사용자 앱 별 on/off(GCM, APNS는 디바이스 별 on/off)	
	var required = new Array("_sUserId", "_sPushKey", "_bEnabled");
	var params = $.extend(true, {
		"_sPushKey" : bizMOB.Device.getInfo({
			"_sKey" : "push_key"
		}),
		"_bEnabled" : true
	}, arguments[0]);
	
	if(!bizMOBCore.Module.checkparam(params, required)) {
		return;
	}
	
	bizMOBCore.PushManager.setAlarm(params);
};

/**
 * 푸시 알람 수신여부 조회
 * 
 * @param String _sUserId		푸시 알림 설정을 조회할 사용자 이이디.
 * @param Boolean _bProgressEnable		(Default:true) 푸시 알람 설정 요청시 화면에 progress 표시 여부( true 또는 false )
 * @param Function _fCallback	결과를 받아 처리할 callback 함수.
 *  
 * @return 
 */
bizMOB.Push.getAlarm = function(_sUserId_fCallback)	{
	// 앱 종료후 재 시작시에 화면에 설정 여부를 표시하기 서버에 설정된 값을 조회	
	var required = new Array("_sUserId", "_sPushKey", "_fCallback");
	var params = $.extend(true, {
		"_sPushKey" : bizMOB.Device.getInfo({
			"_sKey" : "push_key"
		}),
	}, arguments[0]);
	
	if(!bizMOBCore.Module.checkparam(params, required)) {
		return;
	}
	
	bizMOBCore.PushManager.getAlarm(params);
};

/**
 * 푸시 수신 목록 조회
 * 
 * @param String _sAppName	푸시 서버에 등록된 앱 이름.
 * @param String _sUserId		푸시 메세지를 조회할 사용자 이이디.
 * @param Number _nPageIndex	푸시 메세지를 가져올 페이징 값.
 * @param Number _nItemCount	푸시 메세지를 가져올 페이징 처리 갯수
 * @param Boolean _bProgressEnable		(default:true) 푸시 서버와 통신 중일때 화면에 progress 를 표시할지에 대한 여부( true 또는 false )
 * @param Function _fCallback	결과를 받아 처리할 callback 함수.
 *  
 * @return 
 */
bizMOB.Push.getMessageList = function(_sAppName_nPageIndex_nItemCount_sUserId_fCallback)	{
	
	var required = new Array("_sAppName", "_nPageIndex", "_nItemCount", "_sUserId", "_fCallback");

	if(!bizMOBCore.Module.checkparam(arguments[0], required)) {
		return;
	}
	
	bizMOBCore.PushManager.getMessageList(arguments[0]);
};

/**
 * 푸시 메세지 읽기
 * 
 * @param String _sTrxDay		푸시 메세지를 읽은 날짜.(yyyymmdd)
 * @param String _sTrxId		푸시 메세지 아이디.
 * @param String _sUserId	사용자 아이디.
 * @param Boolean _bProgressEnable		(Default:true) 푸시 알람 설정 요청시 화면에 progress 표시 여부( true 또는 false )
 * @param Function _fCallback	결과를 받아 처리할 callback 함수.
 *  
 * @return 
 */
bizMOB.Push.readMessage = function(_sTrxDay_sTrxId_sUserId)	{
	
	var required = new Array("_sTrxDay", "_sTrxId", "_sUserId");

	if(!bizMOBCore.Module.checkparam(arguments[0], required)) {
		return;
	}
	
	bizMOBCore.PushManager.readMessage(arguments[0]);
};

/**
 * 읽지 않은 푸시 메세지 카운트 조회
 * 
 * @param String _sAppName	푸시 서버에 등록된 앱 이름.
 * @param String _sUserId		푸시 메세지를 조회할 사용자 이이디.
 * @param Boolean _bProgressEnable		(Default:true) 푸시 알람 설정 요청시 화면에 progress 표시 여부( true 또는 false )
 * @param Function _fCallback	결과를 받아 처리할 callback 함수.
 *  
 * @return 
 */
bizMOB.Push.getUnreadCount = function(_sAppName_sUserId_fCallback)	{
	var required = new Array("_sAppName", "_sUserId", "_fCallback");

	if(!bizMOBCore.Module.checkparam(arguments[0], required)) {
		return;
	}
	
	bizMOBCore.PushManager.getUnreadMessageCount(arguments[0]);
};

/**
 * 앱 아이콘에 숫자 표시하기
 * 
 * @param Number _nBadgeCount		뱃지에 표시할 값 .( 양수 : 표시할 갯수 ,  0 : 뱃지카운트 초기화 )
 * @param Function _fCallback	결과를 받아 처리할 callback 함수.
 *  
 * @return 
 */
bizMOB.Push.setBadgeCount = function(_nBadgeCount)	{
	var required = new Array("_nBadgeCount");

	if(!bizMOBCore.Module.checkparam(arguments[0], required)) {
		return;
	}
	
	bizMOBCore.PushManager.setBadgeCount(arguments[0]);
};

/**
 * 푸시 메세지 발송
 * 
 * @param String _sAppName	푸시 메세지 보낼 앱 이름.
 * @param Array _aUsers		푸시 메세지 받을 사용자 목록.
 * @param String _sFromUser	푸시 메세지를 보낼 사용자 아이디.
 * @param String _sSubject		푸시 메세지 제목.
 * @param String _sContent		푸시 메세지 내용.
 * @param String _sTrxType		(Default : INSTANT) 푸시 메세지 전송 방식.( INSTANT 또는 SCHEDULE )
 * @param String _sScheduleDate	푸시 메세지 전송 날짜.
 * @param Array _aGroups	푸시 메세지를 받을 그룹 목록
 * @param Boolean _bToAll	(Default : false) 해당 앱을 사용하는 전체 사용자에게 푸시 메세지를 발송할지 여부.
 * @param String _sCategory	(Default : def) 푸시 메세지 카테고리.
 * @param Object _oPayLoad	푸시 기폰 용량 초과시 전달할 메세지.
 * @param Boolean _bProgressEnable		(Default:true) 푸시 알람 설정 요청시 화면에 progress 표시 여부( true 또는 false )
 * @param Function _fCallback	결과를 받아 처리할 callback 함수.
 *  
 * @return 
 */
bizMOB.Push.sendMessage = function(_sAppName_aUsers_sFromUser_sSubject_sContent)	{
	// Client에서 push를 전송할 때	
	var required = new Array("_sAppName", "_aUsers", "_sFromUser", "_sSubject", "_sContent");
	var params = $.extend(true, {
		// 예약발송/SCHEDULE or 즉시발송/INSTANT
		_sTrxType : "INSTANT",
		_sScheduleDate : "",
		// bizPush에 있는 group ID
		_aGroups : [],
		_bToAll : false,
		_sCategory : "def",
		// 대량 Push 메시지
		_oPayLoad : {} 
	}, arguments[0]);

	if(!bizMOBCore.Module.checkparam(params, required)) {
		return;
	}
	
	bizMOBCore.PushManager.sendMessage(params);
};

/**
 * 대용량 푸시 메세지 읽기
 * 
 * @param String _sMessageId	푸시 메세지 아이디.
 * @param String _sUserId	사용자 아이디.
 * @param Function _fCallback	결과를 받아 처리할 callback 함수.
 *  
 * @return 
 */
bizMOB.Push.readReceiptMessage = function(_sUserId_sMessageId_fCallback)	{
	// APNS와 GCM은 메시지 용량에 제한이 있으므로 appkey만 가지고 메시지 읽는 기능
	var required = new Array("_sUserId", "_sMessageId");

	if(!bizMOBCore.Module.checkparam(arguments[0], required)) {
		return;
	}
	
	bizMOBCore.PushManager.readReceiptMessage(arguments[0]);
};

/**
 * 01.클래스 설명 : Device 기능 클래스 . 
 * 02.제품구분 : bizMOB Xross
 * 03.기능(콤퍼넌트) 명 : bizMOB Client 단말기 정보 기능 
 * 04.관련 API/화면/서비스 : bizMOBCore.Module.checkparam,bizMOBCore.Push.getAlarm,bizMOBCore.Push.getMessageList,bizMOBCore.Push.getPushKey,bizMOBCore.Push.getUnreadCount,bizMOBCore.Push.readMessage,bizMOBCore.Push.registerToServer,bizMOBCore.Push.sendMessage,bizMOBCore.Push.setAlarm,bizMOBCore.Push.setBadgeCount
 *
 * @author 김승현 
 * @version 1.0 
 * 
 */
bizMOB.Device = new Object();

/**
 * 단말기 정보조회
 * 
 * @param String _sKey	디바이스 정보 키 값.
 *  
 * @return Object 단말기 정보
 */
bizMOB.Device.getInfo = function(_sKey){
	// Core에서 컨테이너에게 정보를 요청하지 않고  Xross에서 저장되어 있는 값을 return함.	
	/*var required = new Array("_sKey");
	
	if(!bizMOBCore.Module.checkparam(arguments[0],required)) {
		return;
	}*/
	
	var rtVal;
	if(arguments[0] && arguments[0]._sKey){
		rtVal = bizMOB.Device.Info[arguments[0]._sKey];
	}else{
		rtVal = bizMOB.Device.Info;
	}
	return rtVal; 
};

/**
 * IOS 판단 여부
 * 
 * @param 
 *  
 * @return Boolean IOS여부
 */
bizMOB.Device.isIOS = function() {
	
	var isIOS = false;
	if(bizMOB.Device.Info.os_type.indexOf("iPhone") > -1
		|| bizMOB.Device.Info.os_type.indexOf("iOS") > -1)	{
		isIOS = true;
	}
	
	return isIOS;
};

/**
 * Android 판단 여부
 * 
 * @param 
 *  
 * @return Boolean Android여부
 */
bizMOB.Device.isAndroid = function() {
	
	var isAndroid = bizMOB.Device.Info.os_type == "Android"?true:false;
	
	return isAndroid;
	
};

/**
 * Phone 판단 여부
 * 
 * @param 
 *  
 * @return Boolean Phone여부
 */
bizMOB.Device.isPhone = function()
{
	var isPhone = bizMOB.Device.Info.device_type == "Phone"?true:false;
	
	return isPhone; 
};

/**
 * Tablet 판단 여부
 * 
 * @param 
 *  
 * @return Boolean Tablet여부
 */
bizMOB.Device.isTablet = function()
{
	var isTablet = bizMOB.Device.Info.device_type == "Tablet"?true:false;
	
	return isTablet; 
};

/** 
 * 01.클래스 설명 : Database 기능 클래스 . 
 * 02.제품구분 : bizMOB Xross
 * 03.기능(콤퍼넌트) 명 : 컨테이너 SQLite DB 사용 기능 
 * 04.관련 API/화면/서비스 : bizMOBCore.Module.checkparam,bizMOBCore.Database.beginTransaction,bizMOBCore.Database.closeDatabase,bizMOBCore.Database.commitTransaction,bizMOBCore.Database.executeBatchSql,bizMOBCore.Database.executeSelect,bizMOBCore.Database.executeSql,bizMOBCore.Database.openDatabase
 *
 * @author 김승현 
 * @version 1.0 
 * 
 */
bizMOB.Database = new Object();

/**
 * DataBase Open
 * 
 * @param String _sDbName		오픈할 데이터베이스 명.
 * @param Function _fCallback		결과를 받아 처리할 callback 함수.
 *  
 * @return 
 */
bizMOB.Database.openDatabase = function(_sDbName_fCallback){
		
	var required = new Array("_sDbName", "_fCallback");
	
	if(!bizMOBCore.Module.checkparam(arguments[0], required)) {
		return;
	}
	
	bizMOBCore.Database.openDatabase(arguments[0]);
};

/**
 * DataBase Close
 * 
 * @param Function _fCallback		결과를 받아 처리할 callback 함수.
 *  
 * @return 
 */
bizMOB.Database.closeDatabase = function(_fCallback){
	
	var required = new Array("_fCallback");
	
	if(!bizMOBCore.Module.checkparam(arguments[0], required)) {
		return;
	}
	
	bizMOBCore.Database.closeDatabase(arguments[0]);
};

/**
 * DataBase Transaction 시작
 * 
 * @param Function _fCallback		결과를 받아 처리할 callback 함수.
 *  
 * @return 
 */
bizMOB.Database.beginTransaction = function(_fCallback){
	
	var required = new Array("_fCallback");
	
	if(!bizMOBCore.Module.checkparam(arguments[0], required)) {
		return;
	}
	
	bizMOBCore.Database.beginTransaction(arguments[0]);
};

/**
 * DataBase Transaction Commit
 * 
 * @param Function _fCallback		결과를 받아 처리할 callback 함수.
 *  
 * @return 
 */
bizMOB.Database.commitTransaction = function(_fCallback){
	
	var required = new Array("_fCallback");
	
	if(!bizMOBCore.Module.checkparam(arguments[0], required)) {
		return;
	}
	
	bizMOBCore.Database.commitTransaction(arguments[0]);
};

/**
 * DataBase Transaction Rollback
 * 
 *  @param Function _fCallback		결과를 받아 처리할 callback 함수.
 *  
 * @return 
 */
bizMOB.Database.rollbackTransaction = function(_fCallback){
	
	var required = new Array("_fCallback");
	
	if(!bizMOBCore.Module.checkparam(arguments[0], required)) {
		return;
	}
	
	bizMOBCore.Database.rollbackTransaction(arguments[0]);
};

/**
 * SQL쿼리문을 실행
 * 
 * @param String _sQuery		실행할 SQL SELECT 쿼리문.
 * @param Array _aBindingValues		쿼리문의 각 변수 위치에 대입해줄 값의 배열.
 * @param Function _fCallback		SQL쿼리문 실행 요청 후 호출되는 callback 함수.
 *  
 * @return 
 */
bizMOB.Database.executeSql = function(_sQuery_fCallback){
	
	var required = new Array("_sQuery","_fCallback");
	
	if(!bizMOBCore.Module.checkparam(arguments[0], required)) {
		return;
	}
	
	bizMOBCore.Database.executeSql(arguments[0]);
};

/**
 * SELECT SQL쿼리문을 실행
 * 
 * @param String _sQuery		실행할 SQL SELECT 쿼리문.
 * @param Array _aBindingValues		쿼리문의 각 변수 위치에 대입해줄 값의 배열.
 * @param Function _fCallback		SQL쿼리문 실행 요청 후 호출되는 callback 함수.
 *  
 * @return 
 */
bizMOB.Database.executeSelect = function(_sQuery_fCallback){
	
	var required = new Array("_sQuery","_fCallback");
	
	if(!bizMOBCore.Module.checkparam(arguments[0], required)) {
		return;
	}
	
	bizMOBCore.Database.executeSelect(arguments[0]);
};

/**
 * SQL쿼리문을 일괄 실행
 * 
 * @param String _sQuery		실행할 SQL SELECT 쿼리문.
 * @param Array _aBindingValues		쿼리문의 각 변수 위치에 대입해줄 값의 배열.
 * @param Function _fCallback		SQL쿼리문 실행 요청 후 호출되는 callback 함수.
 *  
 * @return 
 */
bizMOB.Database.executeBatchSql = function(_sQuery_aBindingValues_fCallback){
	
	var required = new Array("_sQuery","_aBindingValues","_fCallback");
	
	if(!bizMOBCore.Module.checkparam(arguments[0], required)) {
		return;
	}
	
	bizMOBCore.Database.executeBatchSql(arguments[0]);
};