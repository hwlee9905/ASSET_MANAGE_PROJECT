/**
 * @author vertex210@mcnc.co.kr 김영호
 */
(function()
{
	/**
	 * MultiLayout 클래스 생성자
	 *
	 * @param
	 *
	 * @return
	 */
	var bizMOBMultiLayout = new (function()
	{
		this.frameName = "";
		this.parent = "";
		this.broadcastMsgList = new Array();

		this.isPhone;
		this.callbacks = []; //최종 displayView 를 실행된 후 불러질 callback 목록
		this.commandStack = []; //최종 displayView 를 실행된 후 실행된 command 목록
		this.replaceStrings = {}; // 정규식 관리
		
		this.isFrame = false;
		this.properties = {};	//부모창에서 받은 FStorage
		this.localStorage = {};	//부모창에서 받은 localStorage

		this.layout = {};
	})();

	/**
	 * MultiLayout 화면 띄우기
	 *
	 * @param String key 멀티레이아웃 화면키값
	 * @param Object options 새창으로 전달할 옵션값
	 *
	 * @return
	 */
	bizMOBMultiLayout.open = function(key, options)
	{
		var that = this;
		bizMOBMultiLayout._readLayoutFile(function(res) {
            var layouts = res;
            var layout = bizMOBMultiLayout.getLayout(layouts, key);
            if(!layout) bizMOBCore.Module.logger("MultiLayout", "open", "E", "can't find multilayout info. key : " + key);
            // title bar, tool bar inherit
            var inherits = layout.inherit;
            if(inherits)
            {
                if(inherits.constructor === String) inherits = [inherits];
                if(inherits.constructor !== Array) bizMOBCore.Module.logger("MultiLayout", "open", "E", "type error. inherit type : " + inherits.constructor + "\nkey : " + key);
                var inheritLayout={};
                inherits.forEach(function(value)
                {
                    var inherit = layouts[value];
                    if(!inherit) bizMOBCore.Module.logger("MultiLayout", "open", "E", "type error. can't find inherit. inherit : " + inherit + "\nkey : " + key);
                    $.extend(inheritLayout, inherit);
                });
                layout = $.extend(false, inheritLayout, layout);
			}

            // {} 안에 있는 모든것을 추출
			layout = that._replaceSpecChars(layout, /{=(.+)}/g, bizMOBMultiLayout.replaceStrings);

            var html = layout.url;
			var callType = layout.callType;

            switch(callType)
            {
                case "openPage" :
                    $.extend(layout, { opener : bizMOBMultiLayout.frameName });

                    var frameCount = 0;
                    for(key in layout.frames) frameCount++;
                    $.extend(layout, { frameCount : frameCount });
                    var message = $.extend(options._oMessage, layout.message,
                    {
                        bizMOBLayout : layout
                    });
                    var param = $.extend(options, { _oMessage : message, _sPagePath : html });
                    delete options.multiLayout;
                    bizMOBCore.Window.open(param);
                    break;
                case "notifyFrame" :
                	var msg = {
        				type : "notifyFrame",
        				param : {
        					"layout" : layout,
        					"message" : options._oMessage
        				}
        			};
        			window.parent.postMessage( JSON.stringify(msg), '*' );
                    break;
                case "openFrame" :
                    bizMOBMultiLayout.openFrame(
                        $.extend(options._oMessage,
                        {
                            bizMOBLayout : layout
                        })
                    );
                    break;
                default:
                    bizMOBCore.Module.logger("MultiLayout", "open", "E", "unknown callType. callType : " + callType + "\nkey : " + key);
            }
        });
	};

	/*bizMOBMultiLayout.addReplaceString = function(key, string)
	{
		this.replaceStrings[key] = string;
	};*/

	/**
	 * MultiLayout 각 화면에 Data 전달 하기
	 *
	 * @param Object data 화면에 전달할 Data
	 *
	 * @return
	 */
	bizMOBMultiLayout.broadcast = function(data)
	{
		var msg = {
			type : "brodcast",
			param : {
				"data" : data,
				"caller" : bizMOBMultiLayout.frameName
			}
		};
		window.parent.postMessage( JSON.stringify(msg), '*' );
	};

	/**
	 * MultiLayout 설정 JSON파일 읽기
	 *
	 * @param
	 *
	 * @return Object MultiLayout설정 파일에서 읽은 JSON
	 */
	bizMOBMultiLayout._readLayoutFile = function(_callback)
	{
		var dataUrl;
//		var data = {};
		var CURRENT_PATH = location.pathname;
		var MULTI_ROOT_PATH = "../../bizMOB/config/";
//		var MULTI_ROOT_PATH = CURRENT_PATH.substring(0,CURRENT_PATH.indexOf("/contents"))+"/contents/bizMOB/config/";
		// IS_PHONE = true;
		if(bizMOBMultiLayout.isPhone) dataUrl = MULTI_ROOT_PATH+"layout_phone.config";
		else dataUrl = MULTI_ROOT_PATH+"layout_tablet.config";

		if( bizMOB.Device.isIOS() ) {
            var callback = function(res) {
                if(res.result){
                	if(_callback){
						_callback(res.info[Object.keys(res.info)[0]]);
					}
                }
            }
            
            var _fCallback = bizMOBCore.CallbackManager.save(callback);

            var tr = {
                id:"READ_FILE",
                param: {
                	source_path : [dataUrl],
                	callback : _fCallback
                }
            };
            bizMOBCore.Module.gateway(tr, "Module" , "init" );
            
            
            
		}else{
			$.ajax({
	            type : "get",
				url: dataUrl,
	            dataType: "json",
				async : false,
	            success: function(json)
				{
					if(_callback){
						_callback(json);
					}
				},
	            error: function(e)
	            {
	            	bizMOBCore.Module.logger("MultiLayout", "_readLayoutFile", "E", "load layout failed.(" + dataUrl + ") // [" + e.status + "]" + e.statusText);
	            }
	        });
//			return data;
		}
	};

	/**
	 * MultiLayout 설정에서 특정 key값을 추출
	 *
	 * @param Object data MultiLayout설정 값
	 * @param String key MultiLayout 화면 설정키값
	 *
	 * @return Variable MultiLayout설정에서 key와 맵핑되어 읽은 값
	 */
	bizMOBMultiLayout.getLayout = function(data, key)
	{
		var layout;
		//키가 존재할 경우
		if(data[key]) layout=data[key];
		else
		{
			layout = this._getLayoutByPattern(data, key);
		}
		return layout;
	};

	/**
	 * MultiLayout 설정시 사용하는 Keyword( {} ) 추출
	 *
	 * @param Object data MultiLayout설정 값
	 * @param String key MultiLayout 화면 설정키값
	 *
	 * @return Object layout Keyword가 치환되어 반영된 MultiLayout설정 값
	 */
	bizMOBMultiLayout._getLayoutByPattern = function(data, key)
	{
		var layout=undefined;
		var match = [], matchCount = 0;
		for(var dataKey in data)
		{
			var specChars = {};
			var specCharIndexs = [];

			strRegExp = dataKey.replace(/{(\d+)}/g, function($1, $2)
			{
			  specCharIndexs.push($2);
			  return "(.+)";
			});

			//패턴형식 인경우
			if(specCharIndexs.length>0)
			{
				layout = data[dataKey];
				var regExp = eval("/" + strRegExp + "/");

				//key가 패턴에 만족하는지 검사
				var result = regExp.exec(key);
				if(result)
				{
					match.push({
						result: result.slice(1),
						specCharIndexs: specCharIndexs,
						layout: layout
					});
					if(specCharIndexs.length > matchCount) {
						matchCount = specCharIndexs.length;
					}
				}
			}
		}

		if (match.length > 0) {
			var mostMatch = match.filter(function(data) {
				return data.specCharIndexs.length === matchCount;
			})[0];

			mostMatch.result.forEach(function(value, i) {
				specChars[mostMatch.specCharIndexs[i]] = value;
			});

			layout = this._replaceSpecChars(mostMatch.layout, /{(\d+)}/g, specChars);
		} else {
			layout = undefined;
		}

		return layout;
	};

	/**
	 * MultiLayout 설정시 사용하는 Keyword를 특정 문자로 변경
	 *
	 * @param Object layout MultiLayout설정 값
	 * @param RegEx regExp Keyword 정규식
	 * @param String specChars 치환할 문자열
	 *
	 * @return Object result 치환된  MultiLayout설정 값
	 */
	bizMOBMultiLayout._replaceSpecChars = function(layout, regExp, specChars)
	{
		function replaceUrl(url)
		{
			var lastReplaceChar, lastReplaceSpecChar;
			var result =  url.replace(regExp, function($1, $2)
			{
				lastReplaceChar = $1;
				lastReplaceSpecChar = specChars[$2];
				return lastReplaceSpecChar;
			});
			//문자열 전체를 다른 타입의 객체로 바꿀때(이 처리를 하지 않으면 문자열로만 변환됨)
			if(url === lastReplaceChar) return lastReplaceSpecChar;
			return result;
		}

		//대상을 특정문자열(specChars)로 치환함.
		function replaceSpecChars(param)
		{
			var result;
			if(param!==undefined)
			{
				switch(param.constructor)
				{
					case Object :
						result = {};
						for(key in param)
						{
							result[key] = replaceSpecChars(param[key]);
						}
						break;
					case Array :
						result = [];
						for(var i=0;i<param.length;i++)
						{
							result[i] = replaceSpecChars(param[i]);
						}
						break;
					case String :
						result = replaceUrl(param);
						break;
					default :
						result = param;
						break;
				}
			}
			return result;
		};

		return replaceSpecChars(layout);
	};

	/**
	 * Broadcast시 각 페이지로 전달할 Data를 저장
	 *
	 * @param Object data 전달할 Data
	 *
	 * @return
	 */
	bizMOBMultiLayout._reservBroadcast = function(data)
	{
		this.broadcastMsgList.push(data);
	};

	/**
	 * Broadcast 요청
	 *
	 * @param Object data 전달할 Data
	 *
	 * @return
	 */
	bizMOBMultiLayout._broadcastReservs = function(data)
	{
		var that = this;
		this.broadcastMsgList.forEach(function(value)
		{
			that.broadcast(value);
		});
		this.broadcastMsgList = new Array();
	};

	/**
	 * MultiLayout Frame 초기화
	 *
	 * @param Object frame MultiLayout 객체
	 *
	 * @return
	 */
	bizMOBMultiLayout.initFrame = function(frame,param)
	{
		this.frameName = frame.name;
		if(frame.opener) this.opener = frame.opener;
		this.interruptClose = frame.interruptClose;
		var frameName = this.frameName;

		bizMOBCore.Module.gateway = function(message, service, action)
		{
			var msg = {
				type : "setGateway",
				param : {
					"target" : bizMOBMultiLayout.frameName,
					"message" : message,
					"service" : service,
					"action" : action
				}
			};
			window.parent.postMessage( JSON.stringify(msg), '*' );
		};

		// draw를 재정의 하는 이유: 원래 있는 titlebar, toolbar를 multiLayout에서 조합해서 사용하므로
		bizMOB.Window.draw = function(_aElement)
		{
			var layout = {};
			for(var i=0; i < arguments[0]._aElement.length ; i++ ) {
				switch( arguments[0]._aElement[i].constructor )
				{
					case bizMOBCore.Window.TitleBar :
						layout.titlebar = arguments[0]._aElement[i];
						break;
					case bizMOBCore.Window.ToolBar :
						layout.bottom_toolbar = arguments[0]._aElement[i];
						break;
					case bizMOBCore.Window.SideBar :
						if(arguments[0]._aElement[i].position == "left"){
							layout.left_toolbar = arguments[0]._aElement[i];
						}else if(arguments[0]._aElement[i].position == "right"){
							layout.right_toolbar = arguments[0]._aElement[i];
						}
						break;
				}
			}

			bizMOBMultiLayout.layout = $.extend(false, bizMOBMultiLayout.layout, layout);

			// draw에 대한 callback
			if(_aElement._fCallback)
			{
				//TODO :: _aElement에 callback??
				window.parent.bizMOB.MultiLayout._addCallback(_aElement._fCallback);
			}

			
			var msg = {
				type : "setDraw",
				param : {
					"target" : bizMOBMultiLayout.frameName,
					"layout" : bizMOBMultiLayout.layout
				}
			};
			window.parent.postMessage( JSON.stringify(msg), '*' );
		};

		// 저장소를 각각 두지 않고, parent에 일괄 저장
		bizMOB.Storage.set = function() {
			var msg = {
				type : "setStorage",
				param : arguments[0]
			};
			window.parent.postMessage( JSON.stringify(msg), '*' );
		};

		bizMOB.Storage.setList = function() {
			var msg = {
				type : "setStorageList",
				param : arguments[0]
			};
			window.parent.postMessage( JSON.stringify(msg), '*' );
		};
		
		bizMOB.Storage.remove = function() {
			var msg = {
					type : "removeStorage",
					param : arguments[0]
			};
			window.parent.postMessage( JSON.stringify(msg), '*' );
		};

		bizMOB.Storage.get = function() {
			var key = arguments[0]._sKey;
			var value = bizMOBMultiLayout.localStorage[key];
			return bizMOBCore.Module.parsejson(value);
		};

		bizMOB.Properties.set = function() {
			var msg = {
				type : "setProperties",
				param : arguments[0]
			};
			window.parent.postMessage( JSON.stringify(msg), '*' );
		};

		bizMOB.Properties.setList = function() {
			var msg = {
				type : "setPropertiesList",
				param : arguments[0]
			};
			window.parent.postMessage( JSON.stringify(msg), '*' );
		};

		bizMOB.Properties.get = function() {
			var key = arguments[0]._sKey;
			var value = bizMOBMultiLayout.properties[key];
			return bizMOBCore.Module.parsejson(value);
		};

		bizMOB.Properties.remove = function() {
			var msg = {
				type : "removeProperties",
				param : arguments[0]
			};
			window.parent.postMessage( JSON.stringify(msg), '*' );
		};

		delete param.bizMOBFrame;
		delete param.isFrame;
		
		var msg = {
			type : "setInitData",
			param : {
				"target" : bizMOBMultiLayout.frameName,
				"data" : param
			}
		};
		window.parent.postMessage( JSON.stringify(msg), '*' );
	};

	/**
	 * frame height 재설정
	 *
	 * @param
	 *
	 * @return
	 */
	bizMOBMultiLayout.resetFrameHeight = function()
	{
        // Before
         $(document.body).css("height", $(window).height());

        // After
        // 20200612 mhchoi 멀티레이아웃 타이틀바를 Web으로 사용할 경우 높이값을 iframe의 높이값으로 셋팅해야 함
		//TODO
//        var $parentDoc = $(window.document);
//        var $self = $parentDoc.find("iframe[name='" + window.name + "']")
//
//		$(document.body).css("height", $self.height());
	};

	/**
	 * MultiLayout 새창 열기
	 *
	 * @param  Object json MultiLayout 설정값
	 *
	 * @return
	 */
	bizMOBMultiLayout.openPage = function(json)
	{
		var opener = json.bizMOBLayout.opener;
		var views = json.bizMOBLayout.frames;
		bizMOBMultiLayout.views = views;
		bizMOBMultiLayout.opener = opener;
		var message = {};

		for(key in json)
		{
			// bizMOBLayout이 아닌 Window.open의 _oMessage에 해당하는 부분을 message에 저장
			// _oMessage : 이전페이지에서 온 data
			if(key!="bizMOBLayout") message[key] = json[key];
		}
		//TODO:: 순차적으로 로딩?
		for(name in views)
		{
			
			var url = views[name].url;
			var frame = document[name];
			if(!frame) bizMOBCore.Module.logger("MultiLayout", "openPage", "E", "can't find frame. frame name: " + name);
			// param에 message 뽑아내서 위에 있는 message랑 extend시킴
			var param = views[name].message ? $.extend(views[name].message,message) : message;
			param = $.extend({"isFrame" : true},param);
			

			param.bizMOBFrame = { name : name, opener : opener, interruptClose : views[name].interruptClose};
			// url과 message넘겨 줌
			$("iframe[name=" + name +"]").attr("src","../../" + url +"?message=" + encodeURIComponent(JSON.stringify(param)));
		}
	};

	/**
	 * MultiLayout Frame에 새 페이지 열기
	 *
	 * @param  Object json MultiLayout 설정값
	 *
	 * @return
	 */
	bizMOBMultiLayout.openFrame = function(json)
	{
		var opener = json.bizMOBLayout.opener;
		var views = json.bizMOBLayout.frames;
		if(!views) bizMOBCore.Module.logger("MultiLayout", "openFrame", "E", "reference error. frame : " + views);
		var message = {};
		for(key in json)
		{
			if(key!="bizMOBLayout") message[key] = json[key];
		}

		for(name in views)
		{
			var msg = {
				type : "openFrame",
				param : {
					"view" : views[name],
					"name" : name,
					"message" : message,
					"opener" : opener
				}
			};
			window.parent.postMessage( JSON.stringify(msg), '*' );
		}
//		}
	};


	/**
	 * Titlebar, Toolbar에 MultiLayout 설정을 적용하여 요청하기
	 *
	 * @param
	 *
	 * @return
	 */
	bizMOBMultiLayout.displayView = function()
	{
		//bizMOBMultiLayout.displayView 내부함수
		function rebuildBarInfo(bar, strTarget, barClass)
		{
			var result, barObject;
			if(bar)
			{
				result = {};
				if(bar.background)
				{
					bar.image_name = bar.background;
					delete bar.background;
				}

				for(key in bar)
				{
					//치환
					//재귀를 위한 내부함수
					function replaceBarSpecChars(data ,key)
					{
						var replaceResult = undefined;
						switch(data.constructor)
						{
							case Array :
								replaceResult = [];
								data.forEach(function(value)
								{
									var result = replaceBarSpecChars(value, key);
									if(result) replaceResult.push(result);
								});
								break;
							case String :
								var lastReplaceString=undefined, lastReplaceSpecObj=undefined;

								var frameName, frame, index, targetLayout;
								var replaceResult = data.replace(/\{(.*)\}/g, function($1, $2)
								{
									lastReplaceString = $1;
									var split = $2.split(".");
									if(split.length===2) index = split[1];
									frameName = split[0];
									targetLayout = bizMOBMultiLayout.layout[frameName][strTarget];
									if(targetLayout && targetLayout[key])
									{
										if(index!=undefined)
										{
											lastReplaceSpecObj = targetLayout[key][index];
											return lastReplaceSpecObj;
										}
										else
										{
											lastReplaceSpecObj = targetLayout[key];
											return lastReplaceSpecObj;
										}
									}
									else return;
								});

								if(data === lastReplaceString) replaceResult = lastReplaceSpecObj;
								if(replaceResult!=undefined)
								{
									if(replaceResult.action)
									{
										var btn = JSON.parse(JSON.stringify(replaceResult));
										var currentAction = btn.action;
										btn.action = bizMOBCore.CallbackManager.save(function(data)
										{
											// 객체의 함수자체는 parent, 실행되는 실제 함수는 child
											var childWindow = document.getElementsByName(frameName)[0].contentWindow;
											var msg = {
												type : "callbackSave",
												param : {
													"data" : data,
													"callbackId" : currentAction
												}
											};
											childWindow.postMessage( JSON.stringify(msg), '*' );
										}, "listener");

										return btn;
									}
								}
								return replaceResult;

								break;
							default :
								replaceResult = data;
						}
						return replaceResult;
					}
					result[key] = replaceBarSpecChars(bar[key], key);
				}
				barObject = $.extend(new barClass({_sTitle : ""}), result);

				return barObject;
			}
		}

		var titlebar = rebuildBarInfo(bizMOBMultiLayout.titlebar, "titlebar", bizMOBCore.Window.TitleBar);
		var bottomToolbar = rebuildBarInfo(bizMOBMultiLayout.bottom_toolbar, "bottom_toolbar", bizMOBCore.Window.ToolBar);
		var leftToolbar = rebuildBarInfo(bizMOBMultiLayout.left_toolbar, "bottom_toolbar", bizMOBCore.Window.SideBar);
		var rightToolbar = rebuildBarInfo(bizMOBMultiLayout.right_toolbar, "bottom_toolbar", bizMOBCore.Window.SideBar);

		var elements = [titlebar, bottomToolbar, leftToolbar, rightToolbar].filter(function(value) { return value || false; });
		// draw호출
		bizMOB.Window.draw(
		{
			_aElement : elements,
			_fCallback : function()
			{
				bizMOB.MultiLayout.isRunCallback = true;
				bizMOBMultiLayout.callbacks.forEach(function(value)
				{
					eval(value)();
				});
				bizMOBMultiLayout.callbacks = [];
				bizMOB.MultiLayout.isRunCallback = false;

				//커맨드처리를 위한 재귀함수
				function runCommand()
				{
					var cmd = undefined;

					if(bizMOBMultiLayout.commandStack.length>0)
					{
						//쌓아둔 커맨드목록 중 첫번째 항목 선택
						cmd = bizMOBMultiLayout.commandStack[0];
						bizMOBMultiLayout.commandStack = bizMOBMultiLayout.commandStack.splice(1);
						if(cmd.param && cmd.param.callback) // 콜백이 있는경우
						{
							var oriCallback = cmd.param.callback;
							var filterCallback = function(message)
							{
								bizMOB.MultiLayout.isRunCallback = true;
								bizMOBCore.CallbackManager.responser({ callback : oriCallback}, { message : message });
								bizMOB.MultiLayout.isRunCallback = false;
								runCommand();
							};
							cmd.param.callback = bizMOBCore.CallbackManager.save(filterCallback);
							bizMOBCore.Module.gateway(cmd);
						}
						else bizMOBCore.Module.gateway(cmd);
					}
				};
				runCommand();
			}
		});
	};
	window.page =
	{
		init : function(json)
		{

		}
	};

	/**
	 *  MultiLayout Library 초기화
	 *
	 * @param Object  json MultiLayout 객체
	 *
	 * @return
	 */
	bizMOBMultiLayout.init = function(json)
	{
		var layout = json ? json.bizMOBLayout : undefined;
		bizMOBMultiLayout.isPhone = bizMOB.Device.Info.device_type === "Phone";
		if(layout)
		{
			// popup에 대한 opener등록
			if(layout.opener) bizMOBMultiLayout.opener = layout.opener;
			$.extend(bizMOBMultiLayout, layout);
			if(layout.callType)	bizMOBMultiLayout[layout.callType].call(bizMOBMultiLayout, json);
			delete json.bizMOBLayout;
		}
	};


	/**
	 *  MultiLayout Library Method명 정의
	 *
	 * @param
	 *
	 * @return
	 */
	bizMOBMultiLayout.initAPI = function()
	{
		bizMOB.MultiLayout = function(){};

		bizMOB.MultiLayout.init = function(json)
		{
			bizMOBMultiLayout.init(json.data);
		};
		
		bizMOB.MultiLayout.isFrame = function(){
			return bizMOBMultiLayout.isFrame;
		};

		bizMOB.MultiLayout._getLayoutInfo = function(name)
		{
			return bizMOBMultiLayout.layout[name];
		};

		bizMOB.MultiLayout._broadcastReservs = function()
		{
			bizMOBMultiLayout._broadcastReservs();
		};

		bizMOB.MultiLayout._addCallback = function(callback)
		{
			bizMOBMultiLayout.callbacks.push(callback);
		};

		bizMOB.MultiLayout._displayView = function()
		{
			bizMOBMultiLayout.displayView();
		};

		bizMOB.MultiLayout._reservBroadcast = function(data)
		{
			bizMOBMultiLayout._reservBroadcast(data);
		};

		// broadcast: 특정 frame이 전체 frame에게 data전달
		bizMOB.MultiLayout.broadcast =function(data)
		{
			if(bizMOBMultiLayout.isFrame)
			{
				bizMOBMultiLayout.broadcast(data);
			}
		};

		// _dispatchEvent 가 event전달하기 위해 each frame에 broadcast
		bizMOB.MultiLayout._dispatchEvent = function(event)
		{
			$("iframe[multiLayout]").each(function()
			{
				var eventName = event.type.split("bizMOB.")[1];
				this.contentWindow.bizMOBCore.EventManager.responser(
				{
					eventname : eventName
				},
				{
					message : event.data
				});
			});
		};

		var parentOpenPopupFunc = bizMOB.Window.open;
		bizMOB.MultiLayout.open = function(_sLayoutKey)
		{
			if(arguments[0] === undefined){ arguments[0] = {_sType : "normal"}; }
			var param = arguments[0];
			if(param._sType == "popup")
			{
				var required = new Array("_sPagePath");
				if(!bizMOBCore.Module.checkparam(arguments[0], required)) { return; }

				if((param._sHeight && param._sHeight.indexOf("%") > 0) &&
						(param._sWidth && param._sWidth.indexOf("%") > 0) ){

					param = $.extend(true, {
						_sBaseOrientation : "auto",
						_sBaseSize : "device"
					}, param);

					var widthPercent = parseInt(param._sWidth.replace(/\%/,""));
					var heightPercent = parseInt(param._sHeight.replace(/\%/,""));
					
					if(param._sBaseSize == "page" && bizMOBMultiLayout.isFrame){
						var frameName = bizMOBMultiLayout.frameName;
						var frameWidth = window.innerWidth;
						var frameHeight = window.innerHeight;
						frameWidth = frameWidth*(widthPercent/100);
						frameHeight = frameHeight*(heightPercent/100);

						param._sWidth = String(Math.ceil(frameWidth));
						param._sHeight = String(Math.ceil(frameHeight));
					}
				}

				if(bizMOBMultiLayout.frameName)
				{
					param._oMessage = $.extend(param._oMessage,
					{
						bizMOBLayout :
						{
							opener : bizMOBMultiLayout.frameName
						}
					});
				}
				parentOpenPopupFunc(param);
			}
			else
			{
				var required = new Array("_sLayoutKey");
				if(!bizMOBCore.Module.checkparam(arguments[0], required)) { return; }
				bizMOBMultiLayout.open(param._sLayoutKey, param);
			}
		};

		var parentCloseFunc = bizMOB.Window.close;
		var parentClosePopupFunc = bizMOB.Window.close;
		
		bizMOB.MultiLayout.close = function() {
			if(arguments[0] == undefined){ arguments[0] = {_sType : "normal"}; }
			if(!bizMOBCore.Module.checkparam(arguments[0])) {	return; }

			var param = arguments[0];
			if(param._sType == "popup")
			{
				var opener = bizMOBMultiLayout.opener;
				
				if(opener && param && param._sCallback){
					
					var callbackInfo = {
						"callback" : param._sCallback,
						"opener" : opener
					};
					localStorage.setItem("callbackInfo",JSON.stringify(callbackInfo));
					param._sCallback = "bizMOB.MultiLayout.interruptCallback";
				}
				parentClosePopupFunc(param);
			}
			else
			{
				if(bizMOBMultiLayout.interruptClose && bizMOBMultiLayout.interruptClose.targetCallback===param._sCallback)
				{
					var frameName = bizMOBMultiLayout.interruptClose.throwFrame;
					var frame = window.parent.document[frameName];
					if(frame)
					{
						var callbackFunc = frame[param._sCallback];
						if(callbackFunc) callbackFunc(param._oMessage);
					}
					else bizMOBCore.Module.logger("MultiLayout", "close", "E", "bizMOB.MultiLayout.close : can't find interrupt frame. frameName : " + frameName);
				}
				else
				{
					if( param && param._sCallback) {
						var opener = bizMOBMultiLayout.opener;
						if(opener){
							var callbackInfo = {
								"callback" : param._sCallback,
								"opener" : opener
							};
							localStorage.setItem("callbackInfo",JSON.stringify(callbackInfo));
							param._sCallback = "bizMOB.MultiLayout.interruptCallback";
						}
					}
					parentCloseFunc(param);
				}
			}
		};

		bizMOB.MultiLayout.interruptCallback = function(){
			var callbackInfo = localStorage.getItem("callbackInfo");
			if(!callbackInfo) return;
			callbackInfo = JSON.parse(callbackInfo);
			
			if(callbackInfo){
				var childWindow = document.getElementsByName(callbackInfo.opener)[0].contentWindow;
				var msg = {
					type : "callCallback",
					param : {
						"callback" : callbackInfo.callback,
						"message" : arguments[0]
					}
				};
				childWindow.postMessage( JSON.stringify(msg), '*' );
			}
			localStorage.removeItem("callbackInfo");
		};
		
		// 원래 Gateway는 bizMOBCoreGateWay에 저장
		var bizMOBCoreGateWay = bizMOBCore.Module.gateway;
		bizMOBCore.Module.gateway = function(message, service, action, serviceinfo)	{
		// 특정 cmd에 대해 별도 처리
			switch(message.id)	{
			case "RELOAD_WEB" :
			case "AUTH" :
			case "DISMISS_POPUP_VIEW" :
			case "POP_VIEW" :
			case "CHECK_PUSH_RECEIVED" :
			case "GET_MEDIA_PICK" :
			case "CAMERA_CAPTURE" :
			case "FILE_UPLOAD" :
				break;
			// POPUP
			default :
				var opener = bizMOBMultiLayout.opener;
//				alert("opener::" + opener + ", message.id::" + message.id);
				if(opener && message.param && message.param.callback){
					var originCallback = message.param.callback;
					
//					if(originCallback.indexOf("stg") == 0){
//						bizMOBCore.CallbackManager.storage[originCallback]
//					}else if(originCallback.indexOf("lsn") == 0){
//						bizMOBCore.CallbackManager.listener[originCallback]
//					}
//					
					if(!(originCallback.indexOf("stg") == 0 || originCallback.indexOf("lsn") == 0)){
						var param = {
							"opener" : opener,
							"callback" : message.param.callback
						};
						localStorage.setItem("callbackInfo",JSON.stringify(param));
						message.param.callback = "bizMOB.MultiLayout.interruptCallback";
					}
				}
			}

			bizMOBCoreGateWay.call(bizMOBCore.Module, message, service, action, serviceinfo);
		}
	};

	bizMOBMultiLayout.getStorage = function(){
		for(name in bizMOBMultiLayout.views){
			var childWindow = document.getElementsByName(name)[0].contentWindow;
			var msg = {
				type : "setStorage",
				param : {
					"localStorage" : localStorage,
				}
			};
			childWindow.postMessage( JSON.stringify(msg), '*' );
		}
	};
	
	bizMOBMultiLayout.getProperties = function(){
		for(name in bizMOBMultiLayout.views){
			var childWindow = document.getElementsByName(name)[0].contentWindow;
			var msg = {
					type : "setProperties",
					param : {
						"properties" : bizMOB.FStorage,
					}
			};
			childWindow.postMessage( JSON.stringify(msg), '*' );
		}
	};
	
	bizMOBMultiLayout.initInterface = function(){
		window.addEventListener( 'message', function(e){
			if(e.data){
				
				var msg = JSON.parse(e.data);
				var param = msg.param;
				
				if(bizMOBMultiLayout.isFrame){
					console.log("자식>>",msg);
					//자식입장
					switch(msg.type){
						case "callbackSave" :
							var callbackId = param.callbackId;
							var data = param.data || {};
							window.bizMOBCore.CallbackManager.responser({ callback : callbackId }, { message : data });
							break;
						case "setInitData" : 
							bizMOB.Device.Info = param.deviceInfo;
							bizMOBMultiLayout.localStorage = param.localStorage;
							bizMOBMultiLayout.properties = param.properties;
							// Frame에 대한 onReady(MultiLayout이 주는 onReady이벤트)
							bizMOBCore.EventManager.responser(
							{
								eventname : "onReady"
							},
							{
								message : param.data
							});
							break;
						case "setStorage" : 
							bizMOBMultiLayout.localStorage = param.localStorage;
							break;
						case "setProperties" : 
							bizMOBMultiLayout.properties = param.properties;
							break;
						case "openFrame" : 
							var view = param.view,
								message = param.message,
								url = view.url;
							
							var	_param = view.message ? $.extend(view.message,message) : message;
							_param.bizMOBFrame = { name : param.name, opener : param.opener, interruptClose : view.interruptClose};
							_param = $.extend({"isFrame" : true},_param);
							var page = window.location.href.slice(0,window.location.href.indexOf("contents")) + "contents/" + url +"?message=" + encodeURIComponent(JSON.stringify(_param));
							// replace
							window.location.replace(page);
							break;
						case "notifyFrame" :
							var frame = window.document;

		                    var evt = window.document.createEvent("Event");
		                    evt.initEvent("bizMOBMultiLayout.onNotifyFrame", false, true);
		                    evt.data = param.message;
		                    frame.dispatchEvent(evt);
							break;
						case "brodcast" :
							var frame = window.document;
							
							var evt = frame.createEvent("Event");
							evt.initEvent("bizMOBMultiLayout.onBroadcast", false, true);
							evt.data = param.data;
							// 각frame에게 dispatchEvent
							try{ frame.dispatchEvent(evt); }
							catch(e) { bizMOB._warring("MultiLayout - " + e);}
							break;
						case "callCallback" :
							if(param.callback){
								var callbackNameList = param.callback.split(".");
								var callback = window;
								callbackNameList.forEach(function(arg,idx){
									callback = callback[arg];
								});
								if(callback){
									callback.apply(null,[param.message])
								}
							}
							break;
						case "custom" :
							break;
						default :
							break;
					}
				}else{
					//부모입장
					console.log("부모>>",msg);
					switch(msg.type){
						case "setDraw" :
							$("iframe[name=" + param.target + "]").attr("isCallDisplayView", true);
							bizMOBMultiLayout.layout[param.target] = param.layout;

							var isCallDisplayView=true;
							$("iframe", window.document).each(function()
							{
								if(!$(this).attr("isCallDisplayView")) isCallDisplayView = false;
							});
							// 마지막 frame의 DispalyView가 요청되었을 때
							if(isCallDisplayView)
							{
								window.bizMOB.MultiLayout._displayView();
							}
							break;
						case "setStorage" :
							bizMOB.Storage.set(param);
							bizMOBMultiLayout.getStorage();
							break;
						case "setStorageList" :
							bizMOB.Storage.setList(param);
							bizMOBMultiLayout.getStorage();
							break;
						case "removeStorage" :
							bizMOB.Storage.remove(param);
							bizMOBMultiLayout.getStorage();
							break;
						case "setProperties" :
							bizMOB.Properties.set(param);
							bizMOBMultiLayout.getProperties();
							break;
						case "setPropertiesList" :
							bizMOB.Properties.setList(param);
							bizMOBMultiLayout.getProperties();
							break;
						case "removeProperties" :
							bizMOB.Properties.remove(param);
							bizMOBMultiLayout.getProperties();
							break;
						case "setInitData" :
							var childWindow = document.getElementsByName(param.target)[0].contentWindow;
							var msg = {
								type : "setInitData",
								param : {
									"deviceInfo" : bizMOB.Device.Info,
									"localStorage" : localStorage,
									"properties" : bizMOB.FStorage,
									"data" : param.data
								}
							};
							childWindow.postMessage( JSON.stringify(msg), '*' );
							
							$("iframe[name=" + param.target + "]").attr("isLoadMultiLayout", true);
							var isLoadMultiLayout=true;

							$("iframe").each(function()
							{
								if(!$(this).attr("isLoadMultiLayout")) isLoadMultiLayout = false;
							});

							if(isLoadMultiLayout)
							{
								// 전체가 다 로딩되면 _broadcastReservs()
								// _broadcastReservs(): reserve된 msg를 broadcast해주는 함수
								bizMOB.MultiLayout._broadcastReservs();
								bizMOBCore.EventManager.init();
							}
							
							// 방향이 전환 되었을 때, 강제로 resize하도록(iPad에서 resize가 안되는 버그가 있어서 예외처리 함)
							$(window).bind('onorientationchange' in window ? 'orientationchange' : 'resize', function(){ setTimeout(bizMOBMultiLayout.resetFrameHeight, 500); });
							bizMOBMultiLayout.resetFrameHeight();
							break;
						case "setGateway" :
							var message = param.message;
							// POPUP_MESSAGE_BOX: alert창(네이티브 cmd코드는 POPUP_MESSAGE_BOX)
							// alert창은 layer
							if(message.id==="POPUP_MESSAGE_BOX")
							{
								for(var key in message.param.buttons)
								{
									var button = message.param.buttons[key];
									if(button.callback)
									{
										(function()
										{
											var buttonCallback = button.callback;
											// parent callbackmanager에 save(native에서 parent로 주기 때문에)
											button.callback = window.bizMOBCore.CallbackManager.save(function(data)
											{
												// 객체의 함수자체는 parent, 실행되는 실제 함수는 child
												var childWindow = document.getElementsByName(param.target)[0].contentWindow;
												var msg = {
													type : "callbackSave",
													param : {
														"data" : data,
														"callbackId" : buttonCallback
													}
												};
												childWindow.postMessage( JSON.stringify(msg), '*' );
												
											}, "listener");
										})();

									}
								}
							}
							else
							{
								var callback = message.param.callback;
								if(message.param)
								{
									switch(message.id)
									{
										case "SHOW_WEB" :
										case "GOTO_WEB" :
										// 팝업 창(팝업 창은 새창)
										case "POP_VIEW" :
										case "SHOW_MESSAGE" :
										case "DISMISS_POPUP_VIEW" :
										case "CREATE_MENU_VIEW" :
										case "CLOSE_MENU_VIEW" :
											break;
										// 새창으로 열지 않을때(default)
										default :
											message.param.message = $.extend(message.param.message,
											{
												bizMOBLayout :
												{
													opener : param.target
												}
											});

											if(callback)	{
												message.param.callback = window.bizMOBCore.CallbackManager.save(function(data)
												{
													// 객체의 함수자체는 parent, 실행되는 실제 함수는 child
													var childWindow = document.getElementsByName(param.target)[0].contentWindow;
													var msg = {
														type : "callbackSave",
														param : {
															"data" : data,
															"callbackId" : callback
														}
													};
													childWindow.postMessage( JSON.stringify(msg), '*' );
													
//													window.bizMOBCore.CallbackManager.responser({ callback : callback }, { message : data });
												});
											}
											break;
									}

								}
							}
							console.log("TEST :: MultiLayout Frame -- gateway333");
							
							if(bizMOB.MultiLayout.isRunCallback) bizMOBMultiLayout.commandStack.push(message);
							else bizMOBCore.Module.gateway(message, param.service, param.action);
							break;
							
							break;
							
						case "openFrame" :
							var childWindow = document.getElementsByName(param.name)[0].contentWindow;
							var msg = {
								type : "openFrame",
								param : param
							};
							childWindow.postMessage( JSON.stringify(msg), '*' );
							break;
						case "notifyFrame" : 
							var childWindow = document.getElementsByName(param.layout.target)[0].contentWindow;
							var msg = {
								type : "notifyFrame",
								param : param
							};
							childWindow.postMessage( JSON.stringify(msg), '*' );
							break;
						case "brodcast" : 
							var frames = $("iframe");
							var isLoadMultiLayout=true;
							$("iframe").each(function()
							{
								// 하나라도 multiLayout이 아니면 isLoadMultiLayout false
								if(!$(this).attr("isLoadMultiLayout")) isLoadMultiLayout = false;
							});
							if(isLoadMultiLayout)
							{
								frames.each(function()
								{
									if($(this).attr("name") != param.caller)
									{
										var childWindow = $(this)[0].contentWindow;
										var msg = {
											type : "brodcast",
											param : param
										};
										childWindow.postMessage( JSON.stringify(msg), '*' );
									}
								});
							}
							// 하나라도 Loading안되면 reserve함
							else bizMOB.MultiLayout._reservBroadcast(data);
							break;
						case "setEvent" :
							var hasDispatchEvent = !bizMOBCore.EventManager.storage[param.eventName].every(function(value)
							{
								// Event가 발생하면 mulilayout._dispatchEvent를 호출함, _dispatchEvent가 실제로 broadcast로 frame에 event 줌
								return value!=="bizMOB.MultiLayout._dispatchEvent";
							});
							if(!hasDispatchEvent) { bizMOBCore.EventManager.storage[param.eventName].push("bizMOB.MultiLayout._dispatchEvent"); }
							break;
						case "custom" :
							break;
						default :
							break;
					}
				}
			}
		});
	};

	// dom ready event에 대한 handler
	$(document).ready(function()
	{
		var param = location.href.split("?message=");
		if(param.length>1){
			param = param[1];
			param = jQuery.parseJSON(decodeURIComponent(param));
			bizMOBMultiLayout.isFrame = param.isFrame;
		}else{
			param = {};
		}
		// API 선언
		bizMOBMultiLayout.initAPI();
		
		//부모, 자식간 통신 listner 정의
		bizMOBMultiLayout.initInterface();
		
		// Frame인지 아닌지 여부
		if(bizMOBMultiLayout.isFrame)
		{
			bizMOBMultiLayout.initFrame(param.bizMOBFrame,param);
				
			delete param.bizMOBFrame;

			for(var eventName in bizMOBCore.EventManager.storage)
			{
				if(bizMOBCore.EventManager.storage[eventName].length>0)
				{
					var msg = {
        				type : "setEvent",
        				param : {
        					"eventName" : eventName
        				}
        			};
        			window.parent.postMessage( JSON.stringify(msg), '*' );
				}

			}
		}
	});

	bizMOB.addEvent("beforeready", "bizMOB.MultiLayout.init");
})();
