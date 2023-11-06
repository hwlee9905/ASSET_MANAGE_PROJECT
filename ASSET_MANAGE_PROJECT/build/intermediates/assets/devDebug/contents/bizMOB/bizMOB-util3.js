/**
 *
 * 01.클래스 설명 : Util 클래스
 * 02.제품구분 : bizMOB Util
 * 03.기능(콤퍼넌트) 명 : Application 개발시 필요한 Util 기능 관련 클래스
 *
 * @author 김승현
 * @version 1.0
 *
 */
bizMOB.Util = function() {};

/**
 *Util 관련 클래스 로드용 무명함수
 *
 * @param
 *
 * @return
 */
(function() {
	var CURRENT_PATH = location.pathname;
	var PATH_DEPTH = CURRENT_PATH.split("/");
	var PATHARR_IDX=PATH_DEPTH.length-2;
	var RELATE_DEPTH = "";

	for(var i=PATHARR_IDX; i > 0;i--){
		if(PATH_DEPTH[i] != "contents"){
			RELATE_DEPTH += "../";
		}else{
			break;
		}
	}
	/**
	 *Util 관련 Error 로깅
	 *
	 * @param
	 *
	 * @return
	 */
	bizMOB._throwError = function(e)
	{
		bizMOBCore.Module.logger("Util", "Util", "E", e);

		throw("throw - bizMOB error. stop process");
	};
	/**
	 *Util 관련 Warning 로깅
	 *
	 * @param
	 *
	 * @return
	 */
	bizMOB._warning = function(e)
	{
		bizMOBCore.Module.logger("Util", "Util", "W", e);
	};

	/**
	 *
	 * 01.클래스 설명 : bizMOBRender 클래스
	 * 02.제품구분 : bizMOB Util
	 * 03.기능(콤퍼넌트) 명 : 화면 Data Rendering
	 *
	 * @author 김승현
	 * @version 1.0
	 *
	 */
	function bizMOBRender()
	{
		this.options = {};
		this.template = undefined;
	};
	$.extend(bizMOBRender.prototype,
	{
		defaultOptions :
		{
			clone : false, // 대상을 복사한 후 render
			newId : "renderList", // 복사한 대상의 새로운 ID. clone이 true 일 때만 동작함
			replace : true // true=기존 요소 대체. false=기존 요소에 추가
		},
		TYPE_SINGLE : "single",
		TYPE_LOOP : "loop",
		instances : {},
		/**
		 * Render Class Instance 생성
		 *
		 * @param Object key Rendering 대상으로 선정한 Jquery Selector Object
		 *
		 * @return Render Instance
		 */
		_getInstance:function(key)
		{
			if(!this.instances[key]) this.instances[key] = new this.constructor();
			return this.instances[key];
		},
		/**
		 * Render 실행
		 *
		 * @param Object target  Rendering 대상으로 선정한 Jquery Selector Object
		 * @param Object options  Rendering Option
		 *
		 * @return
		 */
		execute:function(target, options)
		{
			if(options)
			{
				switch(options.constructor)
				{
					case Object :
					case Array :
						this.render.apply(this, arguments);
						break;
					case String :
						this[options].apply(this, arguments);
						break;
				}
			}
		},
		/**
		 * 반복 데이터 Render 처리
		 *
		 * @param Object parent  Rendering Templet Jquery Selector Object
		 * @param String target  반복할 대상 Rendering Element
		 * @param Array value  반복할 Data List
		 * @param Array value  반복할 Data Rendering 옵션 List
		 * @param Object value  Data Rendering시 표시할 옵션
		 *
		 * @return Object parent Rendering된 Jquery Selector Object
		 */
		addLoopElement:function(parent, target, value, detail, condition)
		{
			var that = this;
			var template = $(target, parent).length>1 ? $(target, that.template) : $(target, parent);
			var conditionFlag = false;
			if(template.length == 0)	bizMOB._warning("bizMOBRender - invalid taret selector. target selector : " + target);
			if(condition && condition !== "")	{
				if(condition.constructor == String)	{
					if(Object.keys(value[0]).indexOf(condition) < 0)	{
						bizMOB._warning("bizMOBRender - The list has not the column name for condition. condition : " + condition);
					} else	{
						conditionFlag = true;
					}
				} else if(condition.constructor == Function)	{
					conditionFlag = true;
				} else	{
					bizMOB._warning("bizMOBRender - Please define condition value that is String or Function Type. condition : " + condition);
				}
			}

			var docfrag = document.createDocumentFragment();

			$.each(value, function(index, element)
			{
				var isRenderFlag = false;

				if(conditionFlag)	{
					if(that._checkCondition(condition, this, element))	isRenderFlag = true;
				} else	{
					isRenderFlag = true;
				}

				if(isRenderFlag)	{
					var target = template.clone(),
						targetParent = template.parent();

					//that.parseDir(target, this, detail, {index : index}, targetParent.get(0)).appendTo(template.parent());
					that.parseDir(target, this, detail, {index : index}, targetParent.get(0));
					docfrag.appendChild(target.get(0));
				}
			});

			template.parent().get(0).appendChild(docfrag);
			template.remove();
			return parent;
		},
		/**
		 * 데이터 Render시 조건 처리
		 *
		 * @param Variable condition  Data Rendering시 표시 조건
		 * @param Object value  Data Rendering 전체 데이터
		 * @param Object item  Data Rendering 현재 위치 데이터 셋
		 *
		 * @return boolean result 조건 체크결과
		 */
		_checkCondition:function(condition, value, row)	{
			var result = false, conditionValue;

			if(condition.constructor == String)		conditionValue = row[condition];
			else if(condition.constructor == Function)	conditionValue = condition({ context : value, item : row });

			if(conditionValue == true || conditionValue == "true"
				|| conditionValue == 1 || conditionValue == "Y"
				|| conditionValue == "y")	result = true;

			return result;
		},
		/**
		 * 단일 건 데이터 Render 처리
		 *
		 * @param Object parent  Rendering Templet Jquery Selector Object
		 * @param String target  반복할 대상 Rendering Element
		 * @param String valueType  Data Rendering 화면에 표시할 타입( text 또는 html )
		 * @param Variable value  Rendering Data
		 *
		 * @return Object parent Rendering된 Jquery Selector Object
		 */
		addSingleElement:function(parent, target, valueType, value)
		{
			var that=this, realValue;
			realValue = value;

			//target의 마지막 값 저장
			var method = target.length > 0 ? target.substr(target.length-1, 1) : "";
			if(method=="+" || method=="-")
			{
				target = target.substring(0, target.length-1);
			}
			else method = "";

			var targetSplit = target.split("@");
			if (targetSplit.length < 3)
			{

				var element;
				try { element = targetSplit[0] && targetSplit[0] != "."? $(targetSplit[0], parent) : $(parent); }
				catch (e)
				{
					bizMOB._warning("bizMOBRender - invalid taret selector. target selector : " + target);
					return;
				}
				if(element.length==0) bizMOB._warning("bizMOBRender - target is empty. target : " + targetSplit[0]);
				if (targetSplit.length == 2)
				{
					if(!targetSplit[1])
					{
						bizMOB._warning("bizMOBRender - attr is null. attr : " + targetSplit[1] + "\ntarget :" + target);
						return;
					}
					switch (method)
					{
						case "+":
							if(element.attr(targetSplit[1])) realValue = element.attr(targetSplit[1]) + realValue;
							break;
						case "-":
							if(element.attr(targetSplit[1]))
							{
								var attrValue = element.attr(targetSplit[1]);
								realValue = element.attr(targetSplit[1]).replace(realValue, "");
							}
							break;

					}
					if(realValue && (realValue.constructor == Object || realValue.constructor == Array) ) element.data(targetSplit[1], realValue);
					else element.attr(targetSplit[1], realValue);
				}
				else
				{
					switch (method)
					{
						case "+":
							element[valueType](element.text() + realValue);
							break;
						case "-":
							element[valueType](element.text().replace(realValue, ""));
							break;
						default:
							element[valueType](realValue);
							break;
					}
				}
			}
			else {bizMOB._warning("bizMOBRender - syntax error. target : " + target + "(value : " + value + ")"); }
			return parent;
		},
		/**
		 * Render 설정 Parsing
		 *
		 * @param Object parent  Rendering 대상 Jquery Selector Object
		 * @param String data  Rendering 대상 Data Set
		 * @param Array paths  Rendering 설정 값
		 * @param Variable options  현재 데이터 셋의 위치값
		 * @param Object parentAboveOfParent  Rendering대상의 Parent Jquery Selector Object
		 *
		 * @return Object parent Rendering된 Jquery Selector Object
		 */
		parseDir:function(parent, data, paths, options, parentAboveOfParent)
		{
			var that = this;
			$.each(paths, function(index,element)
			{
				if(!this.target) {bizMOB._warning("bizMOBRender - reference error. target : " + this.target + "(value : " + this.value + ")");}
				else if(!this.value) {bizMOB._warning("bizMOBRender - reference error. value : " + this.value + "(target : " + this.target + ")");}
				var valueType = this.valueType ? this.valueType : "text";
				var realValue;
				if (this.value.constructor == Function)
				{
					var param =
					{
						context : that.data,
						item : data,
						element : {},
						parent : parent
					};
					if(options) param.index = options.index;

					var elementTarget = element.target,
						targetSplit = elementTarget.split("@");

					if(targetSplit.length < 3)	{
						var targetSelector = targetSplit[0];

						if(!targetSelector.length || targetSelector == ".")	{
							param.element = parent.get(0);
							param.parent = !parentAboveOfParent ? param.element.parentNode : parentAboveOfParent;
						} else	{
							if(parent.find(targetSelector).length==0){
								bizMOB._warning("bizMOBRender - reference error. target : " + targetSelector);
								return;
							}else {
								param.element = parent.find(targetSelector).get(0);
								param.parent = param.element.parentNode;
							}
						}
					}

					realValue = this.value.apply(that, [param]);
					if(realValue==undefined) realValue = "";
				}
				else if(this.value==".") realValue = data;
				else
				{
					realValue = eval("data." + this.value);
					if(realValue==undefined) bizMOB._warning("bizMOBRender - invalid value. value : " + this.value);
				}

				if(this.detail == undefined)	{
					if(this.value==undefined) {
						bizMOB._warning("bizMOBRender - reference error. value : " + this.value + "(target : " + this.target + ")");
					}

					that.addSingleElement(parent, this.target, valueType, realValue);
				} else	{
					that.addLoopElement(parent, this.target, realValue, this.detail, this.condition);
				}

//				switch(this.type)
//				{
//					case that.TYPE_SINGLE :
//						if(this.value==undefined) {bizMOB._warning("bizMOBRender - reference error. value : " + this.value + "(target : " + this.target + ")");}
//						that.addSingleElement(parent, this.target, valueType, realValue);
//						break;
//					case that.TYPE_LOOP :
//						if(this.detail==undefined) {bizMOB._warning("bizMOBRender - reference error. detail : " + this.detail + "(value :" + this.value + ")");}
//						that.addLoopElement(parent, this.target, realValue, this.detail, this.condition);
//						break;
//					default :
//						bizMOB._warning("bizMOBRender - unknown type. type : " + this.type);
//				}
			});
			return parent;
		},
		/**
		 *  Render 처리
		 *
		 * @param Object root   Rendering대상의 Parent Jquery Selector Object
		 * @param String data  Rendering 대상 Data Set
		 * @param Array dir  Rendering 설정 값
		 * @param Object options  Rendering 옵션 값
		 *
		 * @return
		 */
		render:function(root, data, dir, options)
		{
			if(data==undefined ) { bizMOB._throwError("bizMOBRender - reference error. data : " + data);}
			if(data.constructor != Object && data.constructor != Array) {bizMOB._throwError("bizMOBRender - type error. data type : " + data.constructor);}
			if(dir==undefined) { bizMOB._throwError("bizMOBRender - reference error. dir : " + dir); }
			if(dir.constructor != Array) {bizMOB._throwError("bizMOBRender - type error. dir type : " + dir.constructor);}

			var that = this;
			that.data = data;
			that.template = root;
			that.options = $.extend(false, that.defaultOptions, options);

			var target;
			if(that.options.clone) target = root.clone();
			else target = root;
            that.parseDir(target, data, dir);

			if(that.options.clone)
			{
                var newId = that.options.newId;

                // 20200525 mhchoi: 기존 소스
                // var newBox = $("#" + newId);

                // 20200525 mhchoi: 스와이프 loop시 복사본 때문에 타겟에서 newId를 검색하도록 변경
                var newBox = root.parent().find("#" + newId);

				if (that.options.replace) {
					if (newBox.length != 0) {
                        newBox.empty();
                    }
                }

				if (newBox.length>0 && newBox.parent().length>0) {
                    newBox.append(target.children());
                }
				else {
                    newBox = target.attr("id", newId).insertAfter(root);
                }
            }
			target.show();
		},
		/**
		 * Render Class 초기화
		 *
		 * @param Object options   Render Class 초기 설정값
		 *
		 * @return
		 */
		setDefaults:function(options)
		{
			$.extend(this.defaultOptions, options);
		}
	});
	/**
	 *
	 * 01.클래스 설명 : bizMOB Render 클래스
	 * 02.제품구분 : bizMOB Util
	 * 03.기능(콤퍼넌트) 명 : 사용자 서비스 NameSpace 클래스
	 *
	 * @author 김승현
	 * @version 1.0
	 *
	 */
	bizMOB.Util.Render = new bizMOBRender();

	/**
	 *
	 * 01.클래스 설명 : bizMOB Number  클래스
	 * 02.제품구분 : bizMOB Util
	 * 03.기능(콤퍼넌트) 명 : Number Object 확장 기능
	 *
	 * @author 김승현
	 * @version 1.0
	 *
	 */
	function bizMOBNumber(){};
	$.extend(bizMOBNumber.prototype,
	{
		defaultOptions :
		{
			"toNumber_digits" : undefined,
			"toNumber_default" : 0
		},
		/**
		 * 숫자로 변환.정수 및 소수 지원
		 *
		 * @param Variable value   변환할 Data
		 * @param Object options   변환시 옵션
		 *
		 * @return Number num 변환된 숫자
		 */
		toNumber:function(value, options)
		{

			var newOptions = {};
			for(key in options)
			{
				if(options.hasOwnProperty(key)) newOptions["toNumber_" + key] = options[key];
			}
			options = $.extend(false, this.defaultOptions, newOptions);
			var defaultNum = options["toNumber_default"];

			if(value == undefined || value == "") return defaultNum;
			if(value.constructor!=String && value.constructor!=Number) bizMOB._throwError("bizMOBNumber - type error. value type : " + value.constructor);

			var num;
			switch(value.constructor)
			{
				case Number :
					num = value;
					break;
				case String :
					var sign="";
					var decimal="";
					var numSplit = value.split(".");
					if(numSplit.length == 2)
					{
						num = numSplit[0];
						decimal = numSplit[1];
					}
					else num = value;

					if(num.length>1)
					{
						sign = num.substring(0,1);
						if(sign!="-" && sign!="+") sign="";
						else num=num.substring(1,num.length);
					}

					num = num.replace(/[^0-9]/gi, "");
					if(decimal == undefined) decimal = "";
					decimal = decimal.replace(/[^0-9]/gi, "");

					if(decimal!="") num = [num, decimal].join(".");
					num = Number(sign+num);
					break;
				default :
					return defaultNum;
			}

			for(key in options)
			{
				if(options.hasOwnProperty(key))
				{
					switch(key)
					{
					case "toNumber_digits" :
						var digits = options[key];

						if (digits >= 0) num = parseFloat(num.toFixed(digits)); // 소수부 반올림
						else
						{
							var fixdigits = Math.pow(10, Math.abs(digits));
							digits = Math.pow(10, digits); // 정수부 반올림
							num = Number(Math.round(num * digits).toFixed(0))*fixdigits;
						}
						break;
					}
				}
			}
			return num;
		},
		/**
		 * 숫자를 한글로 변환
		 *
		 * @param Variable value   변환할 Data
		 *
		 * @return String result 변환된 한글
		 */
		toKor:function(value)
		{
			if(value==undefined) return "";
			if(value.constructor!=String && value.constructor!=Number) bizMOB._throwError("bizMOBNumber - type error. value type : " + value.constructor);
			var str = value.bMToStr();
			var unit1 = new Array("", "일", "이", "삼", "사", "오", "육", "칠", "팔", "구");
			var unit2 = new Array("십", "백", "천");
			var unit3 = new Array("만", "억", "조");

			var length = str.length;
			var result = "";
			for (var i = 0; i < length; i++)
			{
				var num = str.substring(i, i + 1);
				if(isNaN(num)) { console.log("(warning)bizMOBNumber - invaild value : " + value); return ""; }
				num = num.bMToNumber();
				result += unit1[num];// 숫자를 한글로 변환
				if (num != 0 && (length - 1) != i)  // 숫자가 0이면 한글로 변환하지 않음. 마지막 숫자에는 단위를 붙이지 않음
				{
					// 십,백,천 단위
					switch(str.substring(0, (length - i)).length % 4)
					{
					case 0 :
						result += unit2[2];
						break;
					case 3 :
						result += unit2[1];
						break;
					case 2 :
						result += unit2[0];
						break;
					}
				}

				// 만 ,억,조 단위
				switch(length-i)
				{
				case 5 : // 만
					result += unit3[0];
					break;
				case 9 : // 억
					result += unit3[1];
					break;
				case 13 : // 조
					result += unit3[2];
					break;
				}
			}
			return result;
		}
	});
	/**
	 *
	 * 01.클래스 설명 : bizMOB Number 클래스
	 * 02.제품구분 : bizMOB Util
	 * 03.기능(콤퍼넌트) 명 : 사용자 서비스 NameSpace 클래스
	 *
	 * @author 김승현
	 * @version 1.0
	 *
	 */
	bizMOB.Util.Number = new bizMOBNumber();

	/**
	 *
	 * 01.클래스 설명 : bizMOB Array 클래스
	 * 02.제품구분 : bizMOB Util
	 * 03.기능(콤퍼넌트) 명 : Array Object 확장 기능
	 *
	 * @author 김승현
	 * @version 1.0
	 *
	 */
	function bizMOBArray()
	{
	};
	$.extend(bizMOBArray.prototype,
	{
		/**
		 * Array내 특정 위치 데이터 삭제
		 *
		 * @param Array value   변환할 Data
		 * @param Array index   변환할 Data의 index
		 *
		 * @return Array newArr 삭제된 후 Array
		 */
		RemoveAt:function(value, index)
		{
			if(value.length == 0) return value;
			if(index > value.length) bizMOB._throwError("bizMOBString - Array Index out of bounds. index : " + index);;

			var newArr = value.slice(0,index).concat(value.slice(index+1));

			return newArr;
		}
	});
	/**
	 *
	 * 01.클래스 설명 : bizMOB Array 클래스
	 * 02.제품구분 : bizMOB Util
	 * 03.기능(콤퍼넌트) 명 : 사용자 서비스 NameSpace 클래스
	 *
	 * @author 김승현
	 * @version 1.0
	 *
	 */
	bizMOB.Util.Array = new bizMOBArray();

	/**
	 *
	 * 01.클래스 설명 : bizMOB String 클래스
	 * 02.제품구분 : bizMOB Util
	 * 03.기능(콤퍼넌트) 명 : Array Object 확장 기능
	 *
	 * @author 김승현
	 * @version 1.0
	 *
	 */
	function bizMOBString()
	{
	};
	$.extend(bizMOBString.prototype,
	{
		REGION_FORMATS :
		{
			"kr" : "{0} 원"
		},
		defaultOptions :
		{
			"toStr_default" : "",
			"toFixLength_fillChar" : " ",
			"toFixLength_fillDirection" : "right",
			"toFormatPhone_delim" : "-"
		},
		/**
		 * 문자열로 변환
		 *
		 * @param Variable value   변환할 Data
		 * @param Object options   변환시 옵션
		 *
		 * @return String value 변환된 String
		 */
		toStr:function(value, options)
		{
			var newOptions = {};
			for(key in options)
			{
				if(options.hasOwnProperty(key)) newOptions["toStr_" + key] = options[key];
			}
			options = $.extend(false, this.defaultOptions, newOptions);
			var defaultStr = options["toStr_default"];
			if(!value) return defaultStr;
			switch(value.constructor)
			{
				case String :
					return value;
				case Object :
				case Array :
					return JSON.stringify(value);
				default :
					return String(value);
			}
		},
		/**
		 * 왼쪽기준으로 문자열 자르기
		 *
		 * @param Variable value   변환할 Data
		 * @param Number length   자를 자리수
		 *
		 * @return String value 변환된 String
		 */
		leftSubstr:function(value, length){
			if(!length) bizMOB._throwError("bizMOBString - reference error. length : " + length);
			value = this.toStr(value);
			if(length <= 0){
				return "";
			}else if(length > value.length){
				return str;
			}else{
				return value.substring(0,length);
			}
		},
		/**
		 * 오른쪽기준으로 문자열 자르기
		 *
		 * @param Variable value   변환할 Data
		 * @param Number length   자를 자리수
		 *
		 * @return String value 변환된 String
		 */
		rightSubstr:function(value,length){
			if(!length) bizMOB._throwError("bizMOBString - reference error. length : " + length);
			value = this.toStr(value);
			if(length <= 0){
				return "";
			}else if(length > value.length){
				return str;
			}else{
				return value.substring(value.length,value.length-length);
			}
		},
		/**
		 * 일정 길이로 문자열 만들기
		 *
		 * @param Variable value   변환할 Data
		 * @param Number length   고정 자리수
		 * @param Number length   변환 옵션
		 *
		 * @return String value 변환된 String
		 */
		toFixLength:function(value, length, options) {
			if(!length) bizMOB._throwError("bizMOBString - reference error. length : " + length);
			var newOptions = {};
			for(key in options)
			{
				if(options.hasOwnProperty(key))	newOptions["toFixLength_" + key] = options[key];
			}
			newOptions = $.extend(false, this.defaultOptions, newOptions);

			var fill = newOptions.toFixLength_fillChar;
			var str = this.toStr(value);
			var padding = length - str.length;
			if(padding < 0) {
				str = newOptions.toFixLength_fillDirection=="right" ? str.substr(str.length-length) : str.substr(0,-padding);
			} else {
				for(var n=0; n<padding; n++) str = newOptions.toFixLength_fillDirection=="right" ? str + fill : fill + str;
			}
			return str;
		},
		/**
		 * 특정 포맷으로 문자열 만들기
		 *
		 * @param Variable values   변환할 Data
		 * @param String format   변환 포맷
		 *
		 * @return String value 변환된 String
		 */
		toFormatString:function(format, values)
		{
			var str = format;
			switch(values.constructor)
			{
			   case String :
				   values = [values];
				   break;
			}

			var count = values.length;
			var strPattern = "";
			for(var i=0; i<count; i++) {

				if (values[i] == null || values[i] == undefined ) continue;

				strPattern = "\\{"+ i + "\\}";
				strPattern = new RegExp(strPattern,"g");
				str = str.replace(strPattern, values[i]);
			}
			return str;
		},

		/**
		 * 문자열이나 숫자를 통화타입으로 변환(세자리마다 ',' 표현)
		 *
		 * @param Variable values   변환할 Data
		 *
		 * @return String value 변환된 String
		 */
		toCommaNumber:function(value)
		{
			var strNum = this.toStr(value).replace(/[^0-9.-]/gi, "") + "";
			var sign="";
			var decimal="";

			var numSplit = strNum.split(".");

			if(numSplit.length == 2)
			{
				strNum = numSplit[0];
				decimal = numSplit[1];
			}
			if(strNum.length>1 && (strNum.substring(0,1)))
			{
				sign = strNum.substring(0,1);
				if(sign!="-" && sign!="+") sign="";
				else strNum=strNum.substring(1,strNum.length);
			}

			var splitPoint = strNum.length%3 != 0 ? strNum.length%3 : 3;
			var firstNum = strNum.substring(0, splitPoint);
			var elseNum =  strNum.substring(splitPoint);
			var won = firstNum + elseNum.replace(/([0-9]{3})/g,",$1");
			if(decimal!="") decimal= "." + decimal;

			return sign + won + decimal;
		},
		/**
		 * 전화 번호 타입으로 변환
		 *
		 * @param Variable values   변환할 Data
		 * @param String delim   번호 구분자
		 *
		 * @return String value 변환된 String
		 */
		toFormatPhone:function(value, delim)
		{
			if(!delim) delim = this.defaultOptions.toFormatPhone_delim;
			var str = this.toStr(value);
			var pt = /^(01\d{1}|02|0505|0506|0502|0\d{1,2})-?(\d{3,4})-?(\d{4})$/g;
			return str.replace(/^\s+|\s+$/g, "").replace(pt, "$1" +delim+ "$2" +delim+ "$3");
		},
		/**
		 * 한글 초성 추출
		 *
		 * @param Variable values   변환할 Data
		 *
		 * @return String value 추출된 String
		 */
		toChoSung:function(value)
		{
			var str = this.toStr(value);
			var ChoSung   = [ 0x3131, 0x3132, 0x3134, 0x3137, 0x3138, 0x3139, 0x3141, 0x3142, 0x3143, 0x3145, 0x3146, 0x3147, 0x3148, 0x3149, 0x314a, 0x314b, 0x314c, 0x314d, 0x314e ];
								// ㅏ	  ㅐ	  ㅑ	  ㅒ	  ㅓ	  ㅔ	  ㅕ	  ㅖ	  ㅗ	  ㅘ	  ㅙ	  ㅚ	  ㅛ	  ㅜ	  ㅝ	  ㅞ	  ㅟ	  ㅠ	  ㅡ	  ㅢ	  ㅣ
			var JwungSung = [ 0x314f, 0x3150, 0x3151, 0x3152, 0x3153, 0x3154, 0x3155, 0x3156, 0x3157, 0x3158, 0x3159, 0x315a, 0x315b, 0x315c, 0x315d, 0x315e, 0x315f, 0x3160, 0x3161, 0x3162, 0x3163 ];
								//		 ㄱ	  ㄲ	  ㄳ	  ㄴ	  ㄵ	  ㄶ	  ㄷ	  ㄹ	  ㄺ	  ㄻ	  ㄼ	  ㄽ	  ㄾ	  ㄿ	  ㅀ	  ㅁ	  ㅂ	  ㅄ	  ㅅ	  ㅆ	  ㅇ	  ㅈ	  ㅊ	  ㅋ	  ㅌ	  ㅍ	  ㅎ
			var JongSung  = [ 0,	  0x3131, 0x3132, 0x3133, 0x3134, 0x3135, 0x3136, 0x3137, 0x3139, 0x313a, 0x313b, 0x313c, 0x313d, 0x313e, 0x313f, 0x3140, 0x3141, 0x3142, 0x3144, 0x3145, 0x3146, 0x3147, 0x3148, 0x314a, 0x314b, 0x314c, 0x314d, 0x314e ];

			//var choSung = ["ㄱ","ㄴ","ㄷ","ㄹ","ㅁ","ㅂ","ㅅ","ㅇ","ㅈ","ㅊ","ㅋ","ㅌ","ㅍ","ㅎ",]

			var a, b, c; // 자소 버퍼: 초성/중성/종성 순
			var ch;
			var result = "";
			for (var i = 0; i < str.length; i++) {
				ch = str.charCodeAt(i);
				if (ch >= 0xAC00 && ch <= 0xD7A3) { // "AC00:가" ~ "D7A3:힣" 에 속한 글자면 분해
					c = ch - 0xAC00;
					a = parseInt(c / (21 * 28));
					c = parseInt(c % (21 * 28));
					b = parseInt(c / 28);
					c = parseInt(c % 28);
					//result = result + ChoSung[a] + JwungSung[b];
					//if (c != 0) result = result + JongSung[c] ; // c가 0이 아니면, 즉 받침이 있으면
					result = result + String.fromCharCode(ChoSung[a]);
				} else {
					result = result + String.fromCharCode(ch);
			  }
			}
			return result;
		},
		/**
		 * 국가 단위 화폐 변환
		 *
		 * @param Variable values   변환할 Data
		 * @param String code   국가코드
		 * @param String type   변환 타입
		 *
		 * @return String value 변경된 String
		 */
		toRegionMoney:function(value, code, type)
		{
			if(!code) bizMOB._throwError("bizMOBString - reference error. code : " + code);
			value = this.toStr(value);

			var format = this.REGION_FORMATS[code];
			if(type == "code") {format = "\\ {0}";}


			if(!format) bizMOB._throwError("bizMOBString - unknown code");
			return this.toFormatString(format, value);
		},
		/**
		 * 파일 사이즈 변환
		 *
		 * @param Variable values   bytes용량 Data
		 * @param String fUnit   변환 단위
		 *
		 * @return String value 변경된 String
		 */
		toFileSizeUnit : function(bytes, fUnit) {
	 		bytes = parseInt(bytes);
			var s = ['bytes', 'KB', 'MB', 'GB', 'TB', 'PB'];
			var e = $.inArray(fUnit, s);

			if(e < 0){
				e =Math.floor(Math.log(bytes)/Math.log(1024));
			}

			if(e == "-Infinity") return "0 "+s[0];
			else return (bytes/Math.pow(1024, Math.floor(e))).toFixed(2)+" "+s[e];

		} ,
		/**
		 * String Util 초기화
		 *
		 * @param Object options  초기화 옵션
		 * 		 *
		 * @return
		 */
		setDefaults:function(options)
		{
			$.extend(this.defaultOptions, options);
		},
		/**
		 * 시작 문자열 확인
		 *
		 * @param String value   기준 Data
		 * @param String str   비교 Data
		 *
		 * @return boolean value 비교 결과
		 */
		startsWith : function (value, str){
			return value.slice(0, str.length) == str;
		},
		/**
		 * 종료 문자열 확인
		 *
		 * @param String value   기준 Data
		 * @param String str   비교 Data
		 *
		 * @return boolean value 비교 결과
		 */
		endsWith : function (value, str){
			return value.slice(-str.length) == str;
		},
		/**
		 * 문자열 포함여부 확인
		 *
		 * @param String value   기준 Data
		 * @param String str   비교 Data
		 *
		 * @return boolean value 비교 결과
		 */
		includes : function(value, str)	{
			return value.indexOf(str) !== -1;
		}
	});
	/**
	 *
	 * 01.클래스 설명 : bizMOB Render 클래스
	 * 02.제품구분 : bizMOB Util
	 * 03.기능(콤퍼넌트) 명 : 사용자 서비스 NameSpace 클래스
	 *
	 * @author 김승현
	 * @version 1.0
	 *
	 */
	bizMOB.Util.String = new bizMOBString();

	/**
	 *
	 * 01.클래스 설명 : bizMOB Date 클래스
	 * 02.제품구분 : bizMOB Util
	 * 03.기능(콤퍼넌트) 명 : Date 처리 기능
	 *
	 * @author 김승현
	 * @version 1.0
	 *
	 */
	function bizMOBDate(){};
	$.extend(bizMOBDate.prototype,
	{
		WEEK_NAMES : ["일","월","화","수","목","금","토"],
		WEEK_DETAIL_NAMES : ["일요일","월요일","화요일","수요일","목요일","금요일","토요일"],
		defaultOptions:
		{
			"toFormatDate_format" : "yyyy-mm-dd"
		},
		/**
		 * 날짜형으로 변환
		 *
		 * @param String value   변환 Data
		 *
		 * @return Date value 변환후 Data
		 */
		toDate:function(value)
		{
			var date;
			if(!value) bizMOB._throwError("bizMOBDate - reference error. value : " + value);
			if(value.constructor!=Date&&value.constructor!=String) bizMOB._throwError("bizMOBDate - type error. value type: " + value.constructor);

			if(value instanceof Date) date = value;
			else if(value.constructor==String)
			{
				var strDate = value;
				strDate=strDate.replace(/([^0-9])/g,"");
				if(strDate.length<5){
					bizMOB._throwError("bizMOBDate - reference error. not enough value length. value : " + strDate);
				}else if(strDate.length==5){
					if(strDate.substring(4,5)=="0"){
						bizMOB._throwError("bizMOBDate - reference error. value string index 5 is out of range(1~9). value : " + strDate.substring(4,5));
					}else {
						date = new Date(Number(strDate.substring(0,4)),Number("0"+strDate.substring(4,5))-1);
					}
				}
				else if(strDate.length==6) date = new Date(Number(strDate.substring(0,4)),Number(strDate.substring(4,6))-1);
				else if(strDate.length==8) date = new Date(Number(strDate.substring(0,4)),Number(strDate.substring(4,6))-1,Number(strDate.substring(6,8)));
				else if(strDate.length==14) date = new Date(Number(strDate.substring(0,4)),Number(strDate.substring(4,6))-1,Number(strDate.substring(6,8)), Number(strDate.substring(8,10)), Number(strDate.substring(10,12)), Number(strDate.substring(12,14)));
				else date = new Date();
			}
			return date;
		},

		/**
		 * 날짜간격구하기
		 *
		 * @param Date startdate   시작일
		 * @param Date enddate   종료일
		 *
		 * @return number value 날짜간 차이
		 */
		diff:function(startdate, enddate) {
			if(!startdate || !enddate) bizMOB._throwError("bizMOBDate - reference error. startdate : " + startdate + ", enddate : " + enddate);
			if(startdate.constructor!=String&&startdate.constructor!=Date ) bizMOB._throwError("bizMOBDate - type error. startdate type: " + value.constructor);
			if(startdate.constructor!=enddate.constructor) bizMOB._throwError("bizMOBDate - different type. startdate type : " + startdate.constructor + ", enddate type : " + enddate.constructor);

			var start,end;
			if(startdate.constructor==String)
			{
				try
				{
					var start = this.toDate(startdate);
					var end = this.toDate(enddate);
				}
				catch(e)
				{
					bizMOB._throwError("bizMOBDate - invalid date format. startdate : " + startdate + ", enddate : " + enddate);
				}
			}
			else if(startdate.constructor==Date)
			{
				start = startdate;
				end = enddate;
			}
			var diff = end.getTime() - start.getTime();
			diff = diff/(1000 * 60 * 60 * 24);
			return diff;
		},
		/**
		 * 연도 더하기
		 *
		 * @param Date value   기준 일자
		 * @param Number year   더할 년
		 *
		 * @return Date value 연산결과 날짜
		 */
		addYear:function(value, year)
		{
			if(!year) bizMOB._throwError("bizMOBDate - reference error. year : " + year);
			if(year.constructor!=Number) bizMOB._throwError("bizMOBDate - type error. year type : " + year.constructor);
			value.setYear(value.getFullYear()+year);
			return value;
		},
		/**
		 * 월 더하기
		 *
		 * @param Date value   기준 일자
		 * @param Number month   더할 월
		 *
		 * @return Date value 연산결과 날짜
		 */
		addMonth:function(value, month)
		{
			if(!month) bizMOB._throwError("bizMOBDate - reference error. month : " + month);
			if(month.constructor!=Number) bizMOB._throwError("bizMOBDate - type error. month type : " + month.constructor);
			value.setMonth(value.getMonth()+month);
			return value;
		},
		/**
		 * 일 더하기
		 *
		 * @param Date value   기준 일자
		 * @param Number day   더할 월
		 *
		 * @return Date value 연산결과 날짜
		 */
		addDay:function(value, day)
		{
			if(!day) bizMOB._throwError("bizMOBDate - reference error. date : " + day);
			if(day.constructor!=Number) bizMOB._throwError("bizMOBDate - type error. date type : " + day.constructor);
			value.setDate(value.getDate()+day);
			return value;
		},
		/**
		 * 다음날 구하기
		 *
		 * @param Date value   기준 일자
		 *
		 *
		 * @return Date value 연산결과 날짜
		 */
		nextDay:function(value)
		{
			value.setDate(value.getDate()+1);
			return value;
		},
		/**
		 * 다음달 구하기
		 *
		 * @param Date value   기준 일자
		 *
		 *
		 * @return Date value 연산결과 날짜
		 */
		nextMonth:function(value)
		{
			var thisMonth = value.getMonth();
			value.setMonth(thisMonth+1);

			if(value.getMonth() != thisMonth+1 && value.getMonth() != 0)
			value.setDate(0);

			return value;
		},
		/**
		 * 다음년 구하기
		 *
		 * @param Date value   기준 일자
		 *
		 *
		 * @return Date value 연산결과 날짜
		 */
		nextYear:function(value)
		{
			value.setYear(value.getFullYear()+1);
			return value;
		},
		/**
		 * 이전일 구하기
		 *
		 * @param Date value   기준 일자
		 *
		 *
		 * @return Date value 연산결과 날짜
		 */
		previousDay:function(value)
		{
			value.setDate(value.getDate()-1);
			return value;
		},
		/**
		 * 이전달 구하기
		 *
		 * @param Date value   기준 일자
		 *
		 *
		 * @return Date value 연산결과 날짜
		 */
		previousMonth:function(value)
		{
			var thisMonth = value.getMonth();
			value.setMonth(thisMonth-1);

			if(value.getMonth() != thisMonth-1 && (value.getMonth() != 11 || (thisMonth == 11 && value.getDate() == 1)))
			value.setDate(0);

			return value;
		},
		/**
		 * 이전년 구하기
		 *
		 * @param Date value   기준 일자
		 *
		 *
		 * @return Date value 연산결과 날짜
		 */
		previousYear:function(value)
		{
			value.setYear(value.getFullYear()-1);
			return value;
		},
		/**
		 * 이달 마지막 날 구하기
		 *
		 * @param Date value   기준 일자
		 *
		 *
		 * @return Date value 연산결과 날짜
		 */
		lastDay:function(value)
		{
			var date = new Date(value);
			date.setMonth(date.getMonth()+1);
			date.setDate(0);
			return date.getDate();
		},
		/**
		 * 특정 포팻으로 날짜 표시하기
		 *
		 * @param Date value   기준 일자
		 * @param String format   특정 포맷
		 *
		 *
		 * @return String value 변환 결과
		 */
		toFormatDate:function(value,format)
		{
			if(!format) format = this.defaultOptions.toFormatDate_format;
			var that = this;
			if(!value) {bizMOB._warning("bizMOBDate - reference error. value : " + value);  return "";}
			else if(!(value instanceof Date))
			{
				if(value.constructor!=String) { bizMOB._warning("bizMOBDate - type error. value type : " + value.constructor); return ""; }
				if(!(value.length==5 || value.length==6 || value.length==8 || value.length==14)) { bizMOB._warning("bizMOBDate - invalid value. value : " + value); return ""; }
			}

			var d = this.toDate(value);

			return format.replace(/(yyyy|yy|mm|m|dd|d|www|w|hh|h|HH|H|nn|n|ss|s|a\/p)/gi,
				function($1)
				{
					var h;
					switch ($1)
					{
						case 'yyyy': return d.getFullYear();
						case 'yy': return d.getFullYear().bMToFixLength(2,{"fillChar" : "0"});
						case 'mm': 	return (d.getMonth() + 1).bMToFixLength(2,{"fillChar" : "0", "fillDirection" : "left"});
						case 'm': 	return (d.getMonth() + 1);
						case 'w': 	return that.WEEK_NAMES[d.getDay()];
						case 'www': return that.WEEK_DETAIL_NAMES[d.getDay()];
						case 'dd':   return d.getDate().bMToFixLength(2,{"fillChar" : "0", "fillDirection" : "left"});
						case 'd':   return d.getDate();
						case 'hh':   return ((h = d.getHours() % 12) ? h : 12).bMToFixLength(2,{"fillChar" : "0", "fillDirection" : "left"});
						case 'h':   return ((h = d.getHours() % 12) ? h : 12);
						case 'HH':   return d.getHours().bMToFixLength(2,{"fillChar" : "0", "fillDirection" : "left"});
						case 'H':   return d.getHours();
						case 'nn':   return d.getMinutes().bMToFixLength(2,{"fillChar" : "0", "fillDirection" : "left"});
						case 'n':   return d.getMinutes();
						case 'ss':   return d.getSeconds().bMToFixLength(2,{"fillChar" : "0", "fillDirection" : "left"});
						case 's':   return d.getSeconds();
						// 200602 jglee
						// as-is : case 'a/p':  return d.getHours() < 12 ? 'a' : 'p';
						case 'a/p':  return d.getHours() < 12 ? '오전' : '오후';
					}
				}
			);
		},
		/**
		 * 현재 일자가 해당 월의 몇 번째 주인지를 반환
		 *
		 * @param Date value   기준 일자
		 *
		 *
		 * @return Number result 주수
		 */
		getWeeksOfMonth:function(value)
		{
			var stdDate = new Date(value),
				stdDay = new Date(stdDate.getFullYear(), stdDate.getMonth(), 1).getDay(),
				date = stdDate.getDate(),
				result = Math.ceil((date + stdDay) / 7);

			return result;
		},
		/**
		 * 현재 일자가 해당 년도의 몇 번째 주인지를 반환
		 *
		 * @param Date value   기준 일자
		 *
		 *
		 * @return Number result 주수
		 */
		getWeeksOfYear:function(value)
		{
			var stdDate = new Date(value),
				month = stdDate.getMonth(),
				date = stdDate.getDate(),
				fDay = new Date(stdDate.getFullYear(), 0, 1).getDay(),
				tempDate,
				result = 0;

			for(var m=0; m<month; m++) {
				tempDate = new Date(stdDate.getFullYear(), m + 1, 0);
				result += tempDate.getDate();
			}

			result = Math.ceil((result + date + fDay) / 7);

			return result;
		},
		/**
		 * 현재 일자가 해당 월의 몇 번째 주인지를 반환
		 *
		 * @param Date value   기준 일자
		 *
		 *
		 * @return Number result 주수
		 */
		getStartEndWeek:function(value)
		{
			var stdDate = new Date(value),
				date = stdDate.getDate(),
				day = stdDate.getDay(),
				startDate = new Date(value),
				endDate = new Date(value),
				result = [];

			startDate.setDate(date - day);
			endDate.setDate(date + 6 - day);
			result.push(startDate);
			result.push(endDate);

			return result;
		},
		/**
		 * Date Class 초기화
		 *
		 * @param Object options   초기화 옵션값
		 *
		 *
		 * @return Number result 주수
		 */
		setDefaults:function(options)
		{
			$.extend(this.defaultOptions, options);
		}
	});
	/**
	 *
	 * 01.클래스 설명 : bizMOB Date 클래스
	 * 02.제품구분 : bizMOB Util
	 * 03.기능(콤퍼넌트) 명 : 사용자 서비스 NameSpace 클래스
	 *
	 * @author 김승현
	 * @version 1.0
	 *
	 */
	bizMOB.Util.Date = new bizMOBDate();

	/**
	 *
	 * 01.클래스 설명 : bizMOB Resource 클래스
	 * 02.제품구분 : bizMOB Util
	 * 03.기능(콤퍼넌트) 명 : 전문 JSON파일 컨트롤 기능
	 *
	 * @author 김승현
	 * @version 1.0
	 *
	 */
	function bizMOBResource(){}
	bizMOBResource.prototype.getString = function(key, locale, callback){};
	bizMOBResource.prototype.getAllString = function(reqlocale, callback){};
	bizMOBResource.prototype.getTr = function(group, trcode, extendValue, callback){};
	bizMOBResource.prototype.getHeader = function(name, callback){};
	bizMOBResource.prototype.getResource = function(filepath, filename, callback){};

	$.extend(bizMOBResource.prototype,
	{
		STRING_PATH : RELATE_DEPTH+"bizMOB/message/res/",
		TR_PATH : RELATE_DEPTH+"bizMOB/message/",
		TR_HEADER_PATH : "header/",
		SPEC_CHARS :
		{
			"INCLUDE_HEADER" : "@",
			"REPLACE_STRING" : "="
		},
		/**
		 * JSON 파일 읽기
		 * 
		 * @param String filepath   JSON File Path
		 * @param String filename   JSON File Name
		 * @param Function callback    callback
		 *    
		 */
		getResource:function(filepath, filename, callback)
		{
			var fPath, fName;
			
			if(arguments.length == 1){
				//파일명 파라미터만 있을 경우
				var arg = arguments[0];
				if(arg.constructor!=String) bizMOB._throwError("bizMOBResource - reference error. A file name is required.");
				if(arg.constructor!=Function) bizMOB._throwError("bizMOBResource - reference error. A Callback is required.");
			}else if(arguments.length == 2){
				var arg1 = arguments[0],
					arg2 = arguments[1];
				
				if(arg2.constructor==Function){
					//파일명,콜백 파라미터
					fName = arg1;
					fPath = this.STRING_PATH+fName;
					callback = arg2;
				}else{
					bizMOB._throwError("bizMOBResource - reference error. A Callback is required.");
				}
			}else{
				if(!filepath) bizMOB._throwError("bizMOBResource - reference error. A file path is required. : " + filepath);
				if(filepath.constructor!=String) bizMOB._throwError("bizMOBResource - type error. file path type : " + filepath.constructor);
				fName = filename; 
				fPath = RELATE_DEPTH + filepath + "/" +filename;
			}
			
			if(!fName) bizMOB._throwError("bizMOBResource - reference error. A file name is required. : " + fName);									
			if(fName.constructor!=String) bizMOB._throwError("bizMOBResource - type error. file name type : " + fName.constructor);
			
			if(!callback) bizMOB._throwError("bizMOBResource - reference error. A callback is required. : " + callback);									
			if(callback.constructor!=Function) bizMOB._throwError("bizMOBResource - type error. callback type : " + callback.constructor);												
			
			this._readJson(fPath,callback);
		},
		/**
		 * string JSON 파일내 특정 Property 읽기
		 * 
		 * @param String key   JSON 객체 key
		 * @param String locale  국가코드
		 * @param Function callback    callback
		 *    
		 */
		getString:function(key, locale, callback)
		{
			if(!key) bizMOB._throwError("bizMOBResource - reference error. key : " + key);
			if(key.constructor!=String) bizMOB._throwError("bizMOBResource - type error. key type : " + key.constructor);
			if(arguments.length == 2){
				if(arguments[1].constructor!=Function) bizMOB._throwError("bizMOBResource - reference error. A Callback is required.");
				else{
					callback = locale;
					locale = "";
				}
			}
			var str = "";
			var result;
			this.getAllString(locale,function(res){
				result = res;
				console.log(JSON.stringify(result));
				var keySplit = key.split(".");
				keySplit.forEach(function(value)
				{
					result = result[value];
				});
				
				if (result) 
				{
					if (result.constructor == String) 
					{
						var otherArgs = Array.prototype.slice.call(arguments, 1);
						if(otherArgs.length>0) str = bizMOB.Util.String.toFormatString.apply(bizMOB.Util.String, [result].concat(otherArgs));
						else str = result;
					} else bizMOB._throwError("bizMOBResource - type error. Result is not String : " + result.constructor);
				} else bizMOB._throwError("bizMOBResource - invalid key. key : " + key);
				
				callback(str);
			});
		},
		/**
		 * string JSON 파일 전체 데이터 읽기
		 * 
		 * @param String reqlocale  국가코드
		 * @param Function callback    callback
		 *    
		 */
		getAllString:function(reqlocale,callback)
		{
			var Locale = "";
			var filename = "string";
			
			if(arguments.length == 0){
				bizMOB._throwError("bizMOBResource - reference error. A Callback is required.");
			}else if(arguments.length == 1){
				var arg = arguments[0];
				if(arg.constructor!=Function){
					bizMOB._throwError("bizMOBResource - reference error. A Callback is required.");
				}else{
					callback = reqlocale;
					reqlocale = "";
				}
			}
			
			if(reqlocale){
				Locale = reqlocale;
			}else{
				if(!bizMOB.Device.Info.locale)	Locale = "ko"
				else	Locale = bizMOB.Device.Info.locale.bMLeftSubstr(2);
			}
			
			switch(Locale)
			{
				case "ko" :
					filename = filename+".json";
					break;
				case undefined :
					filename = filename+".json";
					break;
				default : 
					filename = filename + "_" + Locale + ".json";
			}
			
			var path = this.STRING_PATH+filename;
			this._readJson(path,callback);
		},
		/**
		 * 전문 JSON 파일 읽기
		 * 
		 * @param String group 전문 폴더명
		 * @param String trcode 전문 코드
		 * @param Object extendValue 전문 추가 값
		 * @param Function callback    callback
		 *    
		 */
		getTr:function(group, trcode, extendValue, callback)
		{
			if(!group) bizMOB._throwError("bizMOBResource - reference error. group : " + group);
			if(group.constructor!=String) bizMOB._throwError("bizMOBResource - type error. group type : " + group.constructor);
			if(!trcode) bizMOB._throwError("bizMOBResource - reference error. trcode : " + trcode);
			if(trcode.constructor!=String) bizMOB._throwError("bizMOBResource - type error. trcode type : " + trcode.constructor);
			
			if(arguments.length == 2){
				bizMOB._throwError("bizMOBResource - reference error. A Callback is required.");
			}else if(arguments.length == 3){	//extendValue, callback 둘중 하나만 있을때
				if(extendValue.constructor === Object){ //extendValue
					bizMOB._throwError("bizMOBResource - reference error. A Callback is required.");
				}else if(extendValue.constructor == Function){ //callback
					callback = extendValue;
					extendValue = {};
				}
			}
			
			var result = undefined;
			var that = this;
			if(group) this.groupPath = group + "/";
			var path = this.TR_PATH + this.groupPath + trcode + ".json";
			
			var _callback = function(result){
				$.extend(true, result, extendValue);
				if(result && result.header) 
				{
					var now = new Date();
					result.header.trId = result.header.trcode + "_" +
						bizMOB.Util.Date.toFormatDate(now,"yyyymmddhhnnss") + (now.getMilliseconds().bMToFixLength(3,{"fillChar" : "0", "fillDirection" : "left"})) + "_" + 
						Math.floor(Math.random()*1000).bMToFixLength(3,{"fillChar" : "0"});
				}
				if(callback){
					callback(result);
				}
			}
			
			this._readJson(path,function(data){
				var header;
				if(data && data.header) 
				{
					header =data.header;
					
					that._replaceSpecStr(header,function(headerData){
						try{var header = $.extend(headerData, {"trcode" : trcode}); }
						catch(e)
						{
							bizMOB._throwError("bizMOBResource - JSON syntax error. header : " + header);
						}
						result = $.extend(data, {"header" : header});
						_callback(result);
					});
				}
				else{
					result=data;
					_callback();
				}
			});
		},
		/**
		 * 전문 JSON 파일 Header 읽기
		 * 
		 * @param String name 전문 코드
		 * @param Function callback    callback
		 */
		getHeader:function(name,callback)
		{
			var result;
			var that = this;
			var path = this.TR_PATH + this.groupPath + this.TR_HEADER_PATH  + name + ".json";
			this._readJson(path,function(data){
				for(key in data)
				{
					if(data.hasOwnProperty(key)) data[key] = that._replaceSpecStr(data[key]);
				}
				result = data;
				callback(result);
			});
		},
		/**
		 * 전문 JSON 파일내 키워드 Parsing
		 * 
		 * @param Object name 전문 코드 JSON 객체
		 *    
		 * @return Object result 변환된 JSON 객체
		 */
		_replaceSpecStr:function(data,callback)
		{
			var result;
			var that = this;
			var specChar;
			if(data && typeof data == "string" && data.length > 0) specChar = data.substring(0, 1);
			switch (specChar) 
			{
				case that.SPEC_CHARS["INCLUDE_HEADER"] :
					var headerName = data.substr(1);
//					result = that.getHeader(headerName,callback);
					that.getHeader(headerName,function(res){
						callback(res);
					});
					break;
				case that.SPEC_CHARS["REPLACE_STRING"] :
					var key = data.substr(1);
					result = sessionStorage.getItem(key);
					break;
				default :
					result = data;
					break;
			}
			return result;
		},
		/**
		 * JSON 파일 읽기
		 * 
		 * @param String path JSON File Path
		 *    
		 * @return Object result 읽은 JSON 객체
		 */
		_readJson: function(path,callback){
			console.log("JSON file read : "+path);
			var result;
			if( bizMOB.Device.isIOS() ) {
				var param = {
					source_path : [path],
					callback : function(res) {
						if(res.result){
							callback(res.info[Object.keys(res.info)[0]]);
						}else{
							bizMOB._throwError("bizMOBResource - Resource is empty." );
						}
					}
		        };

		        bizMOB.Util.callPlugIn("READ_FILE", param);
			}else{
				$.ajax({
					url: path, 
					dataType: "json",
	 				async : false,
	 				cache:false,
					success: function(data) {
						console.log("JSON file read sucess : "+JSON.stringify(data));
						if(data==null) {
							bizMOB._throwError("bizMOBResource - Resource is empty." );
							return;
						}
						if(callback){
							callback(data);
						}
//						result=data;
					},
					error: function(error) {  
						var readType = "text";
						if(error.status == "404"){
							console.log("bizMOBResource - Requested Resource not found. Default Resource Read." );
							path = path.substring(0,path.indexOf("string_")+6)+path.substring(path.indexOf(".json"));
							readType = "json";
						}
						console.log("JSON file read : "+path);
						$.ajax({
							url: path, 
							dataType: readType,
			 				async : false,
							success: function(data) {
								console.log("JSON file read sucess : "+JSON.stringify(data));
								if(data=="") {
									bizMOB._throwError("bizMOBResource - Resource is empty." );
									return;
								}
								try{
									if(readType == "text") {
										JSON.parse(data);
									}
									else{
//										result = data;
										if(callback){
											callback(data);
										}
									}
								}catch(e){
									bizMOB._throwError("bizMOBResource - Resource cannot convert to JSON " + e);
								}
							},
							error: function(e) {  
								bizMOB._throwError("bizMOBResource - load resource failed : " + path+ "[" + e.status + "] " + e.statusText);
							}
						});
					}
				});
			}
//			return result;
		}
	});
	/**
	 *
	 * 01.클래스 설명 : bizMOB Resource 클래스
	 * 02.제품구분 : bizMOB Util
	 * 03.기능(콤퍼넌트) 명 : 사용자 서비스 NameSpace 클래스
	 *
	 * @author 김승현
	 * @version 1.0
	 *
	 */
	bizMOB.Util.Resource = new bizMOBResource();

	/**
	 *
	 * 01.클래스 설명 : bizMOB Sort 클래스
	 * 02.제품구분 : bizMOB Util
	 * 03.기능(콤퍼넌트) 명 : Object, Array, Element Sort 기능
	 *
	 * @author 김승현
	 * @version 1.0
	 *
	 */
	function bizMOBSort()
	{
		this.options = {};
	}
	$.extend(bizMOBSort.prototype,
	{
		ORDER_BY_ASC : "asc",
		ORDER_BY_DESC : "desc",
		KEY_TYPE_STRING : "string",
		KEY_TYPE_NUMBER : "number",
		defaultOptions :
		{
		},
		keyDefaultOptions :
		{
			"orderby" : "asc",
			"field" : "",
			"fieldType" : "string"
		},
		/**
		 * 정렬 하기
		 *
		 * @param Variable target 정렬 대상
		 *
		 * @return Variable result 정렬된 변환 객체
		 */
		execute:function(target, options)
		{
			if(!options) this.sortElement.apply(this, arguments);
			else
			{
				switch(options.constructor)
				{
					case Object :
						this.sortElement.apply(this, arguments);
						break;
					case String :
						this[options].apply(this, arguments);
						break;
				}
			}
		},
		/**
		 * 오름차순 정렬 하기
		 *
		 * @param Variable a 비교값 1
		 * @param Variable b 비교값 2
		 * @param String key 정렬할 키값
		 * @param String keyType 정렬 기준 타입
		 *
		 * @return Variable retVal 정렬된 변환 객체
		 */
		_orderByAscElement:function(a,b, key, keyType)
		{
			var retVal = null;

			var target1 = $(a),
				target2 = $(b);
			if(key)
			{
				target1 = target1.find(key);
				target2 = target2.find(key);
			}
			if (target1.text() == target2.text()){
				return 0;
			}
			if(keyType == "number")
			{
				retVal = parseInt(target1.text()) > parseInt(target2.text()) ? 1 : -1;
			} else
			{
				retVal = target1.text() > target2.text() ? 1 : -1;
			}
			return retVal;
		},
		/**
		 * 내림차순 정렬 하기
		 *
		 * @param Variable a 비교값 1
		 * @param Variable b 비교값 2
		 * @param String key 정렬할 키값
		 * @param String keyType 정렬 기준 타입
		 *
		 * @return Variable retVal 정렬된 변환 객체
		 */
		_orderByDescElement:function(a,b, key, keyType){
			return this._orderByAscElement(a,b, key, keyType) * -1;
		},
		/**
		 * JSON객체 오름차순 정렬 하기
		 *
		 * @param Variable a 비교값 1
		 * @param Variable b 비교값 2
		 * @param String key 정렬할 키값
		 * @param String keyType 정렬 기준 타입
		 *
		 * @return Variable retVal 정렬된 변환 객체
		 */
		_orderByAscJson:function(a,b, key, keyType)
		{
			var retVal = null;

			var value1,value2;

			if(key)
			{
				value1 = a[key];
				value2 = b[key];
			}
			else
			{
				value1 = a;
				value2 = b;
			}
			if (value1==value2){
				return 0;
			}

			if(keyType == "number"){
				retVal = parseInt(value1) > parseInt(value2) ? 1 : -1;
			}else{
				retVal = value1 > value2 ? 1 : -1;
			}
			return retVal;
		},
		/**
		 * JSON객체 내림차순 정렬 하기
		 *
		 * @param Variable a 비교값 1
		 * @param Variable b 비교값 2
		 * @param String key 정렬할 키값
		 * @param String keyType 정렬 기준 타입
		 *
		 * @return Variable retVal 정렬된 변환 객체
		 */
		_orderByDescJson:function(a,b, key, keyType)
		{
			return this._orderByAscJson(a,b, key, keyType) * -1;
		},
		/**
		 * Element 정렬 하기
		 *
		 * @param Object root Jquery Selector Object
		 * @param Object options 정렬 옵션
		 *
		 * @return Variable retVal 정렬된 변환 객체
		 */
		sortElement:function(root, options)
		{
			if(root.length==0) bizMOB._throwError("bizMOBSort - target is empty. selector : " + root.selector);
			var that = this;
			that.options = $.extend(false, that.defaultOptions);
			that.options.keys = new Array();
			that.options.keys.push(options);
			var tempBox = $("<div style='background-color:red;'>").insertAfter(root.eq(-1));
			root.pushStack([].sort.apply( root,
			[
			 	function(a,b)
			 	{
			 		var retVal = 0;
			 		for(var i=0;i<that.options.keys.length;i++)
					{
			 			var record = that.options.keys[i];
			 			if(retVal==0)
						{
							var data = $.extend(false, that.keyDefaultOptions);
							if(record.constructor === String) data.field = record;
							else if(record.constructor === Object)
							{
								$.extend(data, record);
							}
							var sortFunc = data.orderby === that.ORDER_BY_DESC ? that._orderByDescElement : that._orderByAscElement;
							retVal = sortFunc.apply(that, [a,b, data.field, data.fieldType]);
						}
					}
			 		return retVal;
			 	}
			 ] ), [] );
			tempBox.append(root);
			root.insertAfter(tempBox);
			tempBox.remove();
		},
		/**
		 * Array 정렬 하기
		 *
		 * @param Object arr 정렬할 Array
		 * @param Object options 정렬 옵션
		 *
		 * @return Variable retVal 정렬된 변환 객체
		 */
		sortArray:function(arr, options)
		{
			if(!arr) bizMOB._throwError("bizMOBSort - array is empty.");
			if(arr.length==0)
			{
				return arr;
			}
			var that = this;
			if(!options){
				that.defaultOptions.keys = new Array();
				that.defaultOptions.keys.push(that.keyDefaultOptions);
			}

			console.log(JSON.stringify(options));
			that.options = $.extend(false, that.defaultOptions, options);
			var sortFunc = that.options.orderby === that.ORDER_BY_DESC ? that._orderByDescJson : that._orderByAscJson;
			return [].sort.call( arr,
				function(a,b)
			 	{
					var retVal = 0;
			 		for(var i=0;i<that.options.keys.length;i++)
					{
			 			var record = that.options.keys[i];
						if(retVal==0)
						{
							var data = $.extend(false, that.keyDefaultOptions);
							if(record.constructor === String) data.field = record;
							else if(record.constructor === Object)
							{
								$.extend(data, record);
							}
							var sortFunc = data.orderby === that.ORDER_BY_DESC ? that._orderByDescJson : that._orderByAscJson;
							retVal = sortFunc.apply(that, [a,b, data.field, data.fieldType]);
						}
					}
			 		return retVal;
			 	}
			);
		}

	});
	/**
	 *
	 * 01.클래스 설명 : bizMOB Sort 클래스
	 * 02.제품구분 : bizMOB Util
	 * 03.기능(콤퍼넌트) 명 : 사용자 서비스 NameSpace 클래스
	 *
	 * @author 김승현
	 * @version 1.0
	 *
	 */
	bizMOB.Util.Sort = new bizMOBSort();

	/**
	 *
	 * 01.클래스 설명 : bizMOB 유효성 체크
	 * 02.제품구분 : bizMOB Util
	 * 03.기능(콤퍼넌트) 명 : 입력값의 유효성 체크 기능
	 *
	 * @author 김승현
	 * @version 1.0
	 *
	 */
	function bizMOBValidate()
	{
		this.options = {};
		this.initialized = false;
	}
	$.extend(bizMOBValidate.prototype,
	{
		instances : {},
		defaultOptions :
		{
			rules : {},
			/**
			 * Error 발생시 경고 처리
			 *
			 * @param Object errors 내부 정의된 Error 객체
			 *
			 * @return
			 */
			onError : function(errors)
			{
				var message = "";
				for(key in errors)
				{
					if(errors.hasOwnProperty(key))
					{
						var errorInfo = errors[key];
						if(message) message+="\n";
						message += errorInfo.message;
					}
				}
				bizMOB.Window.alert({
					'_sTitle' : "확인",
					'_vMessage' : message
				});
			},
			checkAll : false
		},
		/**
		 * 내부 Error 객체 생성자
		 *
		 * @param Object target 에러가 발생된 Jquery Selector Object
		 * @param String message 에러가 발생시 체크된 에러 메세지
		 *
		 * @return Object error Error 객체
		 */
		_createError:function(target, message)
		{
			var error =
			{
				"target" : target,
				"message" : message
			};
			return error;
		},
		/**
		 * 유효성 검증
		 *
		 * @param Object target 유효성체크할  Jquery Selector Object
		 * @param Variable ruleName 유효성 체크 기준
		 * @param Variable ruleName 유효성 체크 기준
		 *
		 * @return
		 */
		_testElement:function(target, ruleName, param)
		{
			var testFunc, testParam;
			if(ruleName && ruleName.constructor == Function)
			{
				var that = this;
				testFunc = ruleName;
				testParam = [target, bizMOB.Util.Validate.Rules];
			}
			else
			{
				testFunc = bizMOB.Util.Validate.Rules[ruleName];
				testParam = [target].concat(param);
			}
			if(testFunc && testFunc.constructor == Function)
			{
				return testFunc.apply(bizMOB.Util.Validate.Rules, testParam);
			}
			else bizMOB._throwError("bizMOBValidate - can't find rule. ruleName -" + ruleName);
		},
		/**
		 * 유효성 설정 값(Rule) Parsing
		 *
		 * @param Object rule 유효성 체크 기준 설정값
		 *
		 * @return result result 유효성 체크 기준 설정값 Parsing 결과
		 */
		_parseRule:function(rule)
		{
			var result = new Array();
			if(rule.constructor==String)
			{
				var ruleSplit = rule.split("//");
				ruleSplit.forEach(function(value)
				{
					var msgSplit = value.split("::");
					if(msgSplit.length!=2) bizMOB._throwError("bizMOBValidate - parse rule failed. rule string : " + rule);
					var msg = msgSplit[1];
					var funcStructNames = msgSplit[0].split("&&");

					var checks = [];
					var regExp = /([0-9|A-z]+)\((.*?)\)/g;
					funcStructNames.forEach(function(funcStructName)
					{
						var expResult=regExp.exec(funcStructName);
						var funcName = undefined,
							param = [];
						if(expResult)
						{
							var funcName = expResult[1];
							param = expResult[2].split(",");
						}
						else funcName = funcStructName;
						checks.push({name : funcName, param : param});
					});

					result.push(
					{
						"checks" : checks,
						"message" : msg
					});
				});
			}
			else if(rule.constructor==Function)
			{
				result.push(
				{
					"checks" : rule
				});
			}
			else bizMOB._throwError("bizMOBValidate - parse rule failed. unknown type.  rule.type : " + rule.constructor);
			return result;
		},
		/**
		 * 유효성 설정 값(Rule) Parsing 실행
		 *
		 * @param Object rules 유효성 체크 기준 전체 설정값
		 *
		 * @return  Object newRules 유효선 체크 기준 전체 설정값
		 */
		_parseRules:function(rules)
		{
			var newRules = new Object();
			for(key in rules)
			{
				if(rules.hasOwnProperty(key))
				{
					var rule = this._parseRule(rules[key]);
					newRules[key] = rule;
				}
			};
			return newRules;
		},
		/**
		 * 유효성 체크 실행
		 *
		 * @param Object rules 유효성 체크 기준 전체 설정값
		 *
		 * @return boolean check 체크 결과
		 */
		_public_check:function(target)
		{
			var check = this.check(this.options.rules, this.options,target);
			return check;
		},
		/**
		 * 유효성 체크 설정 추가하기
		 *
		 * @param Object targetSelector 유효성 체크 대상 Jquery Selector Object
		 * @param Object str 유효성 체크 기준명
		 *
		 * @return
		 */
		_public_addRule:function(targetSelector, str)
		{
			if(!targetSelector) bizMOB._throwError("bizMOBValidate - reference error. targetSelector : " + targetSelector);
			if(!str)bizMOB._throwError("bizMOBValidate - reference error. rule string : " + str);
			this.options.rules[targetSelector] = this._parseRule(str);
		},
		/**
		 * 유효성 체크 설정 제거하기
		 *
		 * @param Object targetSelector 유효성 체크 대상 Jquery Selector Object
		 *
		 * @return
		 */
		_public_removeRule:function(targetSelector)
		{
			if(!targetSelector) bizMOB._throwError("bizMOBValidate - reference error. targetSelector : " + targetSelector);
			if(this.options.rules[targetSelector]) delete this.options.rules[targetSelector];
		},
		/**
		 * 유효성 체크 실행하기
		 *
		 * @param Object $target 유효성 체크 대상 Jquery Selector Object
		 * @param Object options 유효성 체크시 옵션
		 *
		 * @return
		 */
		execute:function($target, options)
		{

			var target = $target[0];
			var otherArgs = Array.prototype.slice.call(arguments, 2);
			if((!target.bizMOBValidateInst) || (!target.bizMOBValidateInst.initialized))
			{
				//Initialise
				target.bizMOBValidateInst = bizMOB.Util.Validate._getInstance($target.selector);
				target.bizMOBValidateInst.extendOptions(bizMOB.Util.Validate.defaultOptions);
				target.bizMOBValidateInst.initialized=true;
			}
			if(typeof options == 'object')
			{
				target.bizMOBValidateInst.extendOptions(options);
			}
			else if(typeof options == 'string')
			{
				var result;
				var func = target.bizMOBValidateInst['_public_' + options];
				var that;
				if(!func)
				{
					that = bizMOB.Util.Validate.Rules;
					func = that[options];
					if(!func) bizMOB._throwError("bizMOBValidate - unknown method. method : " + options);
				}
				else that = target.bizMOBValidateInst;
				return func.apply(that, [$target].concat(otherArgs));
			}
			return target.bizMOBValidateInst;
		},
		/**
		 * 유효성 옵션 추가하기
		 *
		 * @param Object options Merge될 유효성 체크 옵션
		 *
		 * @return
		 */
		extendOptions:function(options)
		{
			if(!options) return;

			var rules = {};
			if(options.rules)
			{
				var rules = this._parseRules(options.rules);
			}
			this.options = $.extend(false, this.options, options, {"rules" : rules});
		},
		/**
		 * Validate Class 생성자
		 *
		 * @param Object key 유효성 체크 대상 Jquery Selector Object
		 *
		 * @return Object result Validate Instance
		 */
		_getInstance:function(key)
		{
			if(!this.instances[key]) this.instances[key] = new this.constructor();
			return this.instances[key];
		},
		/**
		 * Validate 설정 옵션 조회
		 *
		 * @param
		 *
		 * @return Object result Validate Options
		 */
		getOptions:function()
		{
			return this.options;
		},
		/**
		 * 유효성 검사 실행
		 *
		 * @param Object rules 유효성 검증 설정값
		 * @param Object options 유효성 검증 옵션값
		 * @param Object parent 유효성 검증 대상 Jquery Selector Object
		 *
		 * @return boolean result 유효성 검즘 결과값
		 */
		check:function(rules, options,parent)
		{
			var checkAll = options && options.checkAll!=undefined ? options.checkAll : this.defaultOptions.checkAll;
			var that = this;
			var errors = new Array();
			for(var targetSelector in rules)
			{
				if(!(rules.hasOwnProperty(targetSelector))) continue;
				var target = $(targetSelector, parent);
				if(target.length==0)
				{
					bizMOB._warning("bizMOBValidate - target is null. target selector : " + targetSelector);
					continue;
				}
				rules[targetSelector].forEach(function(value)
				{
					if(errors.length!=0 && (!checkAll)) return;
					var record = value;
					var message = record.message;
					var checks = record.checks;
					if(checks.constructor==Function)
					{
						var testResult = that._testElement(target, checks);
						if(!(testResult===true))
						{
							var message = testResult;
							errors.push(that._createError(target, message));
						}
					}
					else
					{
						for(var i=0;i<checks.length;i++)
						{
							var name = checks[i].name;
							var param = checks[i].param;
							var testResult = that._testElement(target, name, param);
							if(testResult==false)
							{
								errors.push(that._createError(target, message));
								break;
							}
						}
					}
				});
				if(errors.length!=0 && (!checkAll)) break;
			}
			if(errors.length!=0)
			{
				if(options && options.onError) options.onError(errors);
				return false;
			}
			else return true;
		},
		/**
		 * Validate 객체 초기화
		 *
		 * @param Object options 유효성 검증 옵션값
		 *
		 * @return boolean result 유효성 검증 결과값
		 */
		setDefaults : function(options)
		{
			$.extend(this.defaultOptions, options);
		}
	});

	/**
	 *
	 * 01.클래스 설명 : bizMOB Rules Class
	 * 02.제품구분 : bizMOB Util
	 * 03.기능(콤퍼넌트) 명 : 입력값의 유효성 규칙 기능
	 *
	 * @author 김승현
	 * @version 1.0
	 *
	 */
	function bizMOBRules()
	{
	}
	bizMOBRules.prototype =
	{
		/**
		 * 유효성 대상 객체 값 조회
		 *
		 * @param Object element 유효성 검증 대상 Jquery Selector Object
		 *
		 * @return Variable value 대상 Element Value
		 */
		_getValue:function(element)
		{
			var value;
			if(element.jquery)
			{
				var nodeName = element[0].nodeName.toLowerCase();
				if(nodeName == "input" || nodeName == "select" || nodeName == "textarea") value = element.val();
				else value = element.text();
			}
			else value = element;
			return value;
		},
		/**
		 * 유효성 대상 검증 함수
		 *
		 * @param Object element 유효성 검증 대상 Jquery Selector Object
		 * @param Function testFunc 유효성 검증 실행 함수
		 *
		 * @return Variable value 대상 Element Value
		 */
		_test:function(element, testFunc)
		{
			var that = this;
			var result = true;
			element.each(function(value)
			{
				if(value == undefined) return false;
				var testValue = that._getValue($(this));
				if(!testFunc.call(that, testValue)) result = false;
			});
			return result;
		},
		/**
		 * 필수값 검증 함수
		 *
		 * @param Object element 유효성 검증 대상 Jquery Selector Object
		 *
		 * @return boolean result 검증 결과
		 */
		required:function(element)
		{
			return this._test(element, function(value){ return $.trim(value)=="" ? "":!!value; });
		},
		/**
		 * 이메일 검증 함수
		 *
		 * @param Object element 유효성 검증 대상 Jquery Selector Object
		 *
		 * @return boolean result 검증 결과
		 */
		email:function(element)
		{
			return this._test(element, function(value)
			{
				if(value == undefined) return false;
				var regExp = /^([a-zA-Z0-9_\.-]+)@([\da-zA-Z\.-]+)\.([a-zA-Z\.]{2,6})$/;
				return regExp.test(value);
			});
		},
		/**
		 * 전화번호 검증 함수
		 *
		 * @param Object element 유효성 검증 대상 Jquery Selector Object
		 *
		 * @return boolean result 검증 결과
		 */
		phone:function(element)
		{
			return this._test(element, function(value)
			{
				if(value == undefined) return false;
				var regExp = /^(01[016789]|02|0[3-9]{1}[0-9]{1})[-.]{0,1}[0-9]{3,4}[-.]{0,1}[0-9]{4}$/g;
				return regExp.test(value);
			});
		},
		/**
		 * 날짜 검증 함수
		 *
		 * @param Object element 유효성 검증 대상 Jquery Selector Object
		 *
		 * @return boolean result 검증 결과
		 */
		date:function(element) {
			return this._test(element, function(value)
			{
				if(value == undefined) return false;
				return /^\d{4}[\/.-]\d{1,2}[\/.-]\d{1,2}$/.test(value);
			});
		},
		/**
		 * 최소길이 검증 함수
		 *
		 * @param Object element 유효성 검증 대상 Jquery Selector Object
		 * @param Object length 최소길이
		 *
		 * @return boolean result 검증 결과
		 */
		minLength:function(element,length) {
			return this._test(element, function(value)
			{
				if(value == undefined) return false;
				return $.trim(this._getValue(value)).length >= length;
			});
		},
		/**
		 * 최대길이 검증 함수
		 *
		 * @param Object element 유효성 검증 대상 Jquery Selector Object
		 * @param Object length 최대길이
		 *
		 * @return boolean result 검증 결과
		 */
		maxLength : function(element,length) {
			return this._test(element, function(value)
			{
				if(value == undefined) return false;
				return $.trim(value).length <= length;
			});
		},
		/**
		 * 특정길이 검증 함수
		 *
		 * @param Object element 유효성 검증 대상 Jquery Selector Object
		 * @param Object minLength 최소길이
		 * @param Object maxLength 최대길이
		 *
		 * @return boolean result 검증 결과
		 */
		rangeLength : function(element, minLength, maxLength) {
			return this._test(element, function(value)
			{
				if(value == undefined) return false;
				var length = $.trim(value).length;
				return (length >= minLength && length <= maxLength);
			});
		},
		/**
		 * 최소값 검증 함수
		 *
		 * @param Object element 유효성 검증 대상 Jquery Selector Object
		 * @param Object min 최소값
		 *
		 * @return boolean result 검증 결과
		 */
		minNumber : function(element, min) {
			return this.number(element) && this._test(element, function(value)
			{
				if(value == undefined) return false;
				return (bizMOB.Util.Number.toNumber(value) >= min);
			});
		},
		/**
		 * 최대값 검증 함수
		 *
		 * @param Object element 유효성 검증 대상 Jquery Selector Object
		 * @param Object max 최대값
		 *
		 * @return boolean result 검증 결과
		 */
		maxNumber : function(element, max) {
			return this.number(element) && this._test(element, function(value)
			{
				if(value == undefined) return false;
				return (bizMOB.Util.Number.toNumber(value) <= max);
			});
		},
		/**
		 * 범위값 검증 함수
		 *
		 * @param Object element 유효성 검증 대상 Jquery Selector Object
		 * @param Object min 최소값
		 * @param Object max 최대값
		 *
		 * @return boolean result 검증 결과
		 */
		rangeNumber : function(element, min, max) {
			return this.number(element) && this._test(element, function(value)
			{
				if(value == undefined) return false;
				var num = bizMOB.Util.Number.toNumber(value);
				return (num >= min && num <= max);
			});
		},
		/**
		 * 정수 검증 함수
		 *
		 * @param Object element 유효성 검증 대상 Jquery Selector Object
		 *
		 * @return boolean result 검증 결과
		 */
		digits : function(element) {
			return this._test(element, function(value)
			{
				if(value == undefined) return false;
				return /^\+*\d+$/.test(value);
			});
		},
		/**
		 * 숫자 검증 함수
		 *
		 * @param Object element 유효성 검증 대상 Jquery Selector Object
		 *
		 * @return boolean result 검증 결과
		 */
		number : function(element) {
			return this._test(element, function(value)
			{
				if(value == undefined) return false;
				return /^[+-]*[0-9]+$/.test(value);
			});
		},
		/**
		 * 동일값 검증 함수
		 *
		 * @param Object element 유효성 검증 대상 Jquery Selector Object
		 * @param Object element 유효성 검증 비교 Jquery Selector Object
		 *
		 * @return boolean result 검증 결과
		 */
		equalTo : function(element, to) {
			return this._test(element, function(value)
			{
				if(value == undefined) return false;
				if(to)
				{
					to = to.replace(/\'{0,}\"{0,}/gi, '');
					if(to.constructor==String) to =$(to);
				}
				return value === this._getValue(to);
			});
		},
		/**
		 * 범위내 값 검증 함수
		 *
		 * @param Object element 유효성 검증 대상 Jquery Selector Object
		 *
		 * @return boolean result 검증 결과
		 */
		"in":function(element)
		{
			var list = Array.prototype.slice.call(arguments, 1).map(function(ch)	{
				return ch.replace(/\'{0,}\"{0,}/gi, '');
			});
			return this._test(element, function(value)
			{
				if(value == undefined) return false;
				var result = false;
				list.forEach(function(loopValue)
				{
					if(value===loopValue) result=true;
				});
				return result;
			});
		},
		/**
		 * 문자 값 검증 함수
		 *
		 * @param Object element 유효성 검증 대상 Jquery Selector Object
		 *
		 * @return boolean result 검증 결과
		 */
		chars:function(element)
		{
			return this._test(element, function(value)
			{
				if(value == undefined) return false;
				return /^[ㄱ-ㅣ가-�R|a-z|A-Z]+$/.test(value);
			});
		},
		/**
		 * 한글 값 검증 함수
		 *
		 * @param Object element 유효성 검증 대상 Jquery Selector Object
		 *
		 * @return boolean result 검증 결과
		 */
		korChars:function(element) {
			return this._test(element, function(value)
			{
				if(value == undefined) return false;
				return /^[가-힣]+$/.test(value);
			});
		}
	};
	/**
	 *
	 * 01.클래스 설명 : bizMOB Validate 클래스
	 * 02.제품구분 : bizMOB Util
	 * 03.기능(콤퍼넌트) 명 : 사용자 서비스 NameSpace 클래스
	 *
	 * @author 김승현
	 * @version 1.0
	 *
	 */
	bizMOB.Util.Validate = new bizMOBValidate();
	/**
	 *
	 * 01.클래스 설명 : bizMOB Rules 클래스
	 * 02.제품구분 : bizMOB Util
	 * 03.기능(콤퍼넌트) 명 : 사용자 서비스 NameSpace 클래스
	 *
	 * @author 김승현
	 * @version 1.0
	 *
	 */
	bizMOB.Util.Validate.Rules = new bizMOBRules();

	/**
	 * 기본 규칙 설정 함수
	 *
	 * @param String ruleName 유효성 검증 키값
	 * @param String testFunc 유효성 검증 수행 함수
	 *
	 * @return
	 */
	bizMOB.Util.Validate.addDefaultRule = function(ruleName, testFunc)
	{
		if(!ruleName) bizMOB._throwError("bizMOBValidate - reference error. rule name : " + ruleName);
		if(!testFunc) bizMOB._throwError("bizMOBValidate - reference error. function : " + testFunc);
		if(testFunc.constructor != Function) bizMOB._throwError("bizMOBValidate - invaild type. function type : " + testFunc.constructor);
		bizMOB.Util.Validate.Rules[ruleName] = testFunc;
	};

	/**
	 *
	 * 01.클래스 설명 : bizMOB Html 클래스
	 * 02.제품구분 : bizMOB Util
	 * 03.기능(콤퍼넌트) 명 : Html 고정 사용 Tag관리 함수
	 *
	 * @author 김승현
	 * @version 1.0
	 *
	 */
	function bizMOBHtml(){};
	$.extend(bizMOBHtml.prototype,
	{
		ID_NO_DATA : "bizMOBHtmlTagNoData",
		CLASS_NO_DATA : "nodata no_data",
		ID_BACK_LAYER : "bizMOBHtmlBackLayer",
		/**
		 * 데이터 없음 표시 Html Tag 생성
		 *
		 * @param String target 화면에 표시할 Jquery Selector 키값
		 * @param String inputTxt 데이터 표시 없음 Text
		 *
		 * @return
		 */
		createNoDataTag:function(target, inputTxt)
		{
			if(!target || target.length==0)
	         {
	            bizMOB._throwError("bizMOBHtml - reference error. target : " + target);
	            return;
	         }
	         this.removeNoDataTag();
	         var tagType;
	         var theadchildsnum =10;
	         var nodeName = target[0].nodeName.toLowerCase();
	         switch(nodeName)
	         {
	            case "table" : tagType = "tr"; break;
	            case "tbody" :
	               tagType = "tr";
	               theadchildsnum = $(target).prevAll("thead").find("tr > th").length;
	               break;
	            case "tr" : tagType = "td"; break;
	            case "ul" : tagType = "li"; break;
	            case "dl" : tagType = "dt"; break;
	            default :
	               tagType = "p";
	               break;
	         }
	         var tag = $("<" + tagType + ">").addClass(this.CLASS_NO_DATA);
	         if(tagType=="tr")
	         {
	            var child = $("<td>").attr("colspan",theadchildsnum).text(inputTxt);
	            tag.append(child);
	         }
	         else
	         {
	            if(tagType=="td") tag.attr("colspan",theadchildsnum);
	            tag.text(inputTxt);
	         }

	         var parent = target.clone();
	         target.addClass(this.ID_NO_DATA + "Target");
	         parent.children().remove().end()
	            .attr("id", this.ID_NO_DATA)
	            .insertAfter(target)
	            .append(tag).show();
		},
		/**
		 * 데이터 없음 표시 Html Tag 삭제
		 *
		 * @param
		 *
		 * @return
		 */
		removeNoDataTag:function()
		{
			$("#" + this.ID_NO_DATA).remove();
			$("." + this.ID_NO_DATA + "Target").removeClass(this.ID_NO_DATA + "Target");
		},
		/**
		 * 전체 화면 가리기 Html 생성
		 *
		 * @param Object target 표시할 대상 Jquery Selector Object
		 *
		 * @return
		 */
		createBackLayer:function(target)
		{
			if(!target || target.length==0)
			{
				bizMOB._throwError("bizMOBHtml - reference error. target : " + target);
				return;
			}
			this.removeBackLayer();
			var backLayer = $("<div id='" + this.ID_BACK_LAYER + "' style='position:absolute; left:0; top:0; width:100%; height:100%; background:#000; opacity:0.7;'>");
			target.append(backLayer);
		},
		/**
		 * 전체 화면 가리기 Html 제거
		 *
		 * @param Object target 표시할 대상 Jquery Selector Object
		 *
		 * @return
		 */
		removeBackLayer:function()
		{
			$("#" + this.ID_BACK_LAYER).remove();
		}
	});
	/**
	 *
	 * 01.클래스 설명 : bizMOB Html 클래스
	 * 02.제품구분 : bizMOB Util
	 * 03.기능(콤퍼넌트) 명 : 사용자 서비스 NameSpace 클래스
	 *
	 * @author 김승현
	 * @version 1.0
	 *
	 */
	bizMOB.Util.Html = new bizMOBHtml();

	///////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////

	function bizMOBFilter()
	{
		this.originalMethods = {};
	}
	$.extend(bizMOBFilter.prototype,
	{
		instances : {},
		NEW_FILTER_CHARS :"",
		TARGET_ELEMENT :"input:not(:reset:checkbox:radio:button:submit:file),textarea",
		DATE : "\.\-",
		PHONE : "\-",
		EMAIL : "\@\_\-\.",
		FILTER_CHARS : "\!\"\#\$\%\&\\\(\)\*\+\,\/\:\;\<\=\>\?\[\\\\]\^\_\{\|\}\~\@",
//		FILTER_CHARS : {'!':'!','"':'"','#':'#','$':'$','%':'%','&':'&','\'':'\'','(':'(',')':')','*':'*','+':'+',',':',','/':'/',':':':',';':';','<':'<','=':'=','>':'>','?':'?','[':'[','\\':'\\',']':']','^':'^','_':'_','{':'{','|':'|','}':'}','~':'~','@':'@'//		},
		defaultOptions :
		{
			TARGET_ELEMENT : [],
			FILTER_CHARS : {},
			onError : function(errors)
			{
				console.log("[Filter]"+$(errors.Object).attr("id")+" value have illegal chars");
				alert("사용할 수 없는 문자가 있습니다.");
			},
			solveType : "replace" //break
		},
		setDefaults : function(options)
		{
			$.extend(this.defaultOptions, options);

			var new_filter = "";
			$.each(this.defaultOptions.FILTER_CHARS,function(index,value){
				new_filter += "\\"+value;
			});

//			var new_filter = {};
//			$.each(this.defaultOptions.FILTER_CHARS,function(index,value){
//				new_filter[value] = value;
//			});

			this.FILTER_CHARS = new_filter;
		},
		_getInstance:function(key)
		{
			if(!this.instances[key]) this.instances[key] = new this.constructor();
			return this.instances[key];
		},
		addTrigger:function(key,options)
		{

			var originalMethod = eval(key);
			if(this.originalMethods[key]){
				console.log("[WARN]<\""+key+"\"> method already exist. It will be overriding!!!");
				if(eval(key+".FilterCreated")){
					console.log("the option of <\""+key+"\"> method changed!!!");
					console.log("option == "+JSON.stringify(options));
					eval(key+" = function(){" +
							"var otherArgs = Array.prototype.slice.call(arguments, 0);" +
							"if( bizMOB.Util.Filter._excute("+JSON.stringify(options)+") ){" +
							" 	bizMOB.Util.Filter.originalMethods[key](otherArgs);" +
							"}" +
						"}"
					);
					eval(key+".FilterCreated = true;");
				}else{
					console.log("<\""+key+"\"> method was changed!!!");
					console.log("option == "+JSON.stringify(options));
					delete this.originalMethods[key];

					this.originalMethods[key] = originalMethod;

					eval(key+" = function(){" +
									"var otherArgs = Array.prototype.slice.call(arguments, 0);" +
									"if( bizMOB.Util.Filter._excute("+JSON.stringify(options)+") ){" +
									" 	bizMOB.Util.Filter.originalMethods[key](otherArgs);" +
									"}" +
								"}"
						);
					eval(key+".FilterCreated = true;");

				}

			}else{

				this.originalMethods[key] = originalMethod;

				eval(key+" = function(){" +
								"var otherArgs = Array.prototype.slice.call(arguments, 0);" +
								"if( bizMOB.Util.Filter._excute("+JSON.stringify(options)+") ){" +
								" 	bizMOB.Util.Filter.originalMethods[key](otherArgs);" +
								"}" +
							"}"
					);
				eval(key+".FilterCreated = true;");

				console.log("<\""+key+"\"> method Filter Created!!!");

			};


		},
		removeTrigger:function(key)
		{
			eval(key +"="+ this.originalMethod[key]);
		},
		_excute:function(rules)
		{
			var $targetlist = $(this.TARGET_ELEMENT);
			//var strCharList = JSON.stringify(this.FILTER_CHARS);

			var result = true;

			$.each($targetlist,function(index,value){
				var $target = $(value);
				//var charList = JSON.parse(strCharList);

			//	console.log("before"+JSON.stringify(charList));

				var checkTarget;
				var permissionchar;
				$.each(rules.permission, function(key,value){
					if($target.is(key)){
						checkTarget = $target;
						permissionchar = value;
					}
				});

				if(!bizMOB.Util.Filter._public_check(checkTarget, permissionchar)){
					console.log("checkTarget"+$(checkTarget).attr("id"));
					result = false;
					return false;
				}

			});

			return result;
		},
		_getRegExStr : function(str){
			var charRegEx = "";
			switch (str)
			{
				case "[date]" : charRegEx += this.DATE;break;
				case "[phone]" : charRegEx += this.PHONE;break;
				case "[email]" : charRegEx += this.EMAIL;break;
				default : charRegEx += "("+str+")";
			}
			return charRegEx;
		},
		_public_check:function(selector, charArr ){

			var result = true;
			var charRegEx = "";

			console.log("default:"+this.FILTER_CHARS);
			console.log("charArr:"+charArr);
			for(var i=0;i<charArr.length;i++){
				//delete charList[value[i]];
				if(charArr[i].length<2){
					charRegEx += "\\"+charArr[i];
				}else{
					console.log("Exception:"+charArr[i]);
					charRegEx += this._getRegExStr(charArr[i]);
				}

			}

			var prohibitRegEx = new RegExp("["+this.FILTER_CHARS+"]","gi");
			var permitRegEx = new RegExp("[^"+charRegEx+"]","gi");
			var resultArr = $(selector).val().match(prohibitRegEx);



			if( resultArr != null  ){
				var resultStr = resultArr.join();
				//console.log("1st:"+resultStr);
				if(resultStr.match(permitRegEx) != null){
					console.log("1st:"+resultStr);
					this.defaultOptions.onError({
						"Object":selector,
						"IllegalChar" : resultStr
					});

					result = false;
				}

			}

			return result;
		}
	});

	bizMOB.Util.Filter = new bizMOBFilter();

	/**
	 * native 인터페이스(플러그인) 호출
	 *
	 * @param id native 인터페이스 ID
	 * @param id native 인터페이스에 필요한 파라미터
	 *
	 * @return
	 */
	bizMOB.Util.callPlugIn = function(id, param)	{
		var newparams = {
			_sID : '',
			_oParam : {}
		};

		newparams._sID = id;
		newparams._oParam = param;

		if(!bizMOBCore.Module.checkparam(newparams))	{
			return;
		}

		bizMOBCore.ExtendsManager.executer(newparams);
	};

	/**
	 * bizMOB.Util.Database
	 *
	 * @param {String} dbName : 데이타베이스 명칭
	 */
	bizMOB.Util.Database = function Database(dbName)	{
		this._transactionQueue = new bizMOB.Util.TransactionQueue();

		this.dbName = dbName;

		var _transactionRunnable = true,
			_transactionIndex = 0,
			_transactionQueue = this._transactionQueue;

		this.getTransactionRunnable = function getTransactionRunnable()	{
			return _transactionRunnable;
		};

		this.setTransactionRunnable = function setTransactionRunnable(transactionRunnable)	{
			_transactionRunnable = transactionRunnable;
		};

		this.openDatabase = function openDatabase(success, fail)	{
			var db = this,
				callback = function(res)	{
					if(res.result)  typeof success == "function" ? success() : void(undefined);
					else   typeof fail == "function" ? fail() : void(undefined);
				};
				param = {
					_sDbName : this.dbName,
					_fCallback : callback
				};

			bizMOB.Database.openDatabase(param);
		};

		this.closeDatabase = function closeDatabase(success, fail)	{
			var callback = function(res)	{
					if(res.result)  typeof success == "function" ? success() : void(undefined);
					else   typeof fail == "function" ? fail() : void(undefined);
				};
				param = {
					_fCallback : callback
				};

			bizMOB.Database.closeDatabase(param);
		};

		this.transaction = function transaction(execute, exception)	{
			var db = this,
				tx = new bizMOB.Util.Transaction(db),
				_execute = function()	{
					execute.apply(tx, arguments);

//					var timer = setInterval(function()	{
//						if(!tx._getQueryQueue().hasNext())	{
//							clearInterval(timer);
//
//							if(_transactionQueue.hasNext())	_transactionQueue.run();
//							else	_transactionQueue.clear();
//						}
//					}, 200);
				},
				_exception = function()	{
					exception.apply(tx, arguments);
//					if(_transactionQueue.hasNext())	_transactionQueue.run();
//					else	_transactionQueue.clear();
				};

			_transactionQueue.add(tx, _execute, _exception);

			if(_transactionQueue.length == 1)	{
				_transactionQueue.run();
			}
		};
	};

	bizMOB.Util.TransactionQueue = function TransactionQueue()	{
		var queue = [],
			index = 0;

		this.add = function add(tx, execute, exception)	{
			queue.push({
				transaction : tx,
				execute : execute,
				exception : exception
			});
		};

		this.hasNext = function hasNext()	{
			return queue[index] ? true : false;
		};

		this.run = function run()	{
			var curQueue = queue[index++];

			curQueue.transaction.begin(curQueue.execute, curQueue.exception);
		};

		this.clear = function clear()	{
			queue = [];
			index = 0;
		};

		this.size = function size()	{
			return queue.length;
		};
	};

	bizMOB.Util.Transaction = function Transaction(bizMOBDatabase)	{
		var _db = bizMOBDatabase,
			_execute,
			_exception,
			_result,
			_queryQueue = new bizMOB.Util.QueryQueue();

		this.begin = function begin(execute, exception)	{
			_execute = execute;
			_exception = exception;

			var tx = this,
				callback = function(res)	{
					if(res.result)	{
						_db.setTransactionRunnable(false);
						typeof _execute == "function" ? _execute(tx) : void(undefined);
					} else   {
						bizMOBCore.Module.logger('Transaction', 'begin', "E", "Transaction is already running.");
						var error = new bizMOB.Util.SQLError(res.exception_msg);

						_db.setTransactionRunnable(true);
						typeof _exception == "function" ? _exception(error) : void(undefined);
					}
				};

				param = {
					_fCallback : callback
				};

//			bizMOBCore.Module.gateway(tr, 'Transaction', 'begin');
			bizMOB.Database.beginTransaction(param);

//			callback({
//				"result" : true
//			});
		};

		this.commit = function commit(success, fail)	{
//			if(!_db.getTransactionRunnable)	{
			var tx = this,
				callback = function(res)	{
					tx.end(res.result, success, fail);
				};
				param = {
					_fCallback : callback
				};

			bizMOB.Database.commitTransaction(param);

//				callback({
//					"result" : true
//				});
//			}
		};

		this.rollback = function rollback(success, fail)	{
			var tx = this,
				callback = function(res)	{
					tx.end(res.result, success, fail);
				};
				param = {
					_fCallback : callback
				};

			bizMOB.Database.rollbackTransaction(param);
//			callback({
//				"result" : true
//			});
		};

		this.end = function(result, success, fail)	{
			var tx = this;

			if(_db._transactionQueue.hasNext())	{
				_db._transactionQueue.run();
			} else	{
				_db._transactionQueue.clear();
			}

			if(result)  {
				typeof success == "function" ? success(tx) : void(undefined);
			} else   {
				typeof fail == "function" ? fail(tx) : void(undefined);
			}
		};

		this.executeSql = function executeSql(query, bindingValues, success, fail)	{
			var tx = this,
				callback = function(res)	{
					if(res.result)	{
						var rs = new bizMOB.Util.ResultSet();

						rs.insertId = "";
						res.result_set ? rs.rows = new bizMOB.Util.ResultSetRowList(res.result_set.rows) : void(undefined);
						rs.rowsAffected = res.affected_number;
						typeof success == "function" ? success(tx, rs) : void(undefined);

						if(_queryQueue.hasNext())	{
							_queryQueue.run();
						}
					} else	{
						var error = new bizMOB.Util.SQLError(res.exception_msg);

						if(typeof fail == "function")	{
							fail(tx, error);
						} else if(typeof _exception == "function")	{
							_exception(error);
						}

						_queryQueue.clear();
					}
				},
				param = {
					_sQuery : query,
					_aBindingValues : bindingValues,
					_fCallback : callback
				},
				executeFunction;

			if(query.toLowerCase().trim().slice(0, 6) == "select")	{
				executeFunction = "executeSelect";
			} else	{
				executeFunction = "executeSql";
			}

			_queryQueue.add(bizMOB.Database[executeFunction], param);

			if(_queryQueue.length == 1)	{
				_queryQueue.run();
			}
		};

		this.executeBatchSql = function executeBatchSql(query, bindingValues, success, fail)	{
			var tx = this,
				callback = function(res)	{
			 		if(res.result)	{
			 			var rs = new bizMOB.Util.ResultSet();
			 			rs.insertId = "";
						res.result_set ? rs.rows = new bizMOB.Util.ResultSetRowList(res.result_set.rows) : void(undefined);
						rs.rowsAffected = res.affected_number;
						typeof success == "function" ? success(tx, rs) : void(undefined);

						if(_queryQueue.hasNext())	{
							_queryQueue.run();
						}
					} else	{
						var error = new bizMOB.Util.SQLError(res.exception_msg);

						if(typeof fail == "function")	{
							fail(tx, error);
						} else if(typeof _exception == "function")	{
							_exception(error);
						}

						_queryQueue.clear();
					}
				};
				param = {
					_sQuery : query,
					_aBindingValues : bindingValues,
					_fCallback : callback
				};

				_queryQueue.add(bizMOB.Database.executeBatchSql, param);

			if(_queryQueue.length == 1)	{
				_queryQueue.run();
			}
		};

		this._getQueryQueue = function()	{
			return _queryQueue;
		};
	};

	bizMOB.Util.ResultSet = function ResultSet()	{
		this.insertId;
		this.rows = [];
		this.rowsAffected = 0;
	};

	bizMOB.Util.ResultSetRowList = function ResultSetRowList()  {
		var rows = arguments[0],
			_this = this;

		this.item = function item(index)	{
			return rows[index];
		};

		this.forEach = function forEach(callback)   {
			rows.forEach(callback);
		};

		this.length = rows.length;

		rows.forEach(function(row, index)	{
			_this[index] = row;
		});
	};

	bizMOB.Util.SQLError = function SQLError(exceptionMsg)  {
		var UNKNOWN_ERR = 0,
			DATABASE_ERR = 1,
			VERSION_ERR = 2,
			TOO_LARGE_ERR = 3,
			QUOTA_ERR = 4,
			SYNTAX_ERR = 5,
			CONSTRAINT_ERR = 6,
			TIMEOUT_ERR = 7;

		this.code = 0;
		this.message = exceptionMsg;
	};

	bizMOB.Util.QueryQueue = function QueryQueue()	{
		var queue = [],
			index = 0;

		this.add = function add(action, param)	{
			queue.push({
				"action" : action,
				"param" : param
			});
		};

		this.hasNext = function hasNext()	{
			return queue[index] ? true : false;
		};

		this.run = function run()	{
			if(queue[index])	{
				queue[index].action(queue[index].param);
				index++;
			}
		};

		this.clear = function clear()	{
			queue = [];
			index = 0;
		};

		this.size = function size()	{
			return queue.length;
		};
	};

	///////////////////////////////////////////////////////////
	// Jquery Plugin Extends
	///////////////////////////////////////////////////////////
	$.fn.bMFilter = function()
	{
		var inst = bizMOB.Util.Filter._getInstance(this);
		return inst._public_check.apply(inst, [this].concat(Array.prototype.slice.call(arguments, 1)));
	};
	$.fn.bMRender = function()
	{
		var inst = bizMOB.Util.Render._getInstance(this);
		inst.execute.apply(inst, [this].concat(Array.prototype.slice.call(arguments, 0)));
	};

	$.fn.bMNoDataTag = function()
	{
		var inst = bizMOB.Util.Html;
		bizMOB.Util.Html.createNoDataTag.apply(inst, [this].concat(Array.prototype.slice.call(arguments, 0)));
	};

	$.fn.bMRemoveDataTag = function()
	{
		var inst = bizMOB.Util.Html;
		bizMOB.Util.Html.removeNoDataTag.apply(inst, [this].concat(Array.prototype.slice.call(arguments, 0)));
	};

	$.fn.bMSort = function()
	{
		var inst = bizMOB.Util.Sort;
		inst.execute.apply(inst, [this].concat(Array.prototype.slice.call(arguments, 0)));
	};

	$.fn.bMValidate = function()
	{
		if(this.length==0) bizMOB._throwError("bizMOBValidate - reference error. selector : " + this.selector);
		var inst = bizMOB.Util.Validate._getInstance(this);
		return inst.execute.apply(inst, [this].concat(Array.prototype.slice.call(arguments, 0)));
	};

	$.fn.bMExist = function ()
	{
		return this.length !== 0;
	};


	///////////////////////////////////////////////////////////
	// DataType Method Extends
	///////////////////////////////////////////////////////////
	Number.prototype.bMToFileSizeUnit=function(unit){ return bizMOB.Util.String.toFileSizeUnit(this, unit);};
	Number.prototype.bMToStr=function(options) { return bizMOB.Util.String.toStr(this, options);};
	Number.prototype.bMToNumber=function(options) { return bizMOB.Util.Number.toNumber(this.valueOf(), options);};
	Number.prototype.bMToFixLength=function(length, options) { return bizMOB.Util.String.toFixLength(this, length, options);};
	Number.prototype.bMToKorNumber=function() { return bizMOB.Util.Number.toKor(this);};
	Number.prototype.bMToRegionMoney=function(countryCode,unitType) { return bizMOB.Util.String.toRegionMoney(this,countryCode,unitType);};

	String.prototype.bMToFileSizeUnit = function(unit){ return bizMOB.Util.String.toFileSizeUnit(this, unit);};
	String.prototype.bMToDate=function(){ return bizMOB.Util.Date.toDate(this);};
	String.prototype.bMToStr=function(options) { return bizMOB.Util.String.toStr(this, options);};
	String.prototype.bMToNumber=function(options) { return bizMOB.Util.Number.toNumber(this, options);};
	String.prototype.bMLeftSubstr=function(length) { return bizMOB.Util.String.leftSubstr(this, length);};
	String.prototype.bMRightSubstr=function(length) { return bizMOB.Util.String.rightSubstr(this, length);};
	String.prototype.bMToFixLength=function(length, options) { return bizMOB.Util.String.toFixLength(this, length, options);};
	String.prototype.bMToFormatString=function(values) { return bizMOB.Util.String.toFormatString(this , values);};
	String.prototype.bMToCommaNumber=function() { return bizMOB.Util.String.toCommaNumber(this);};
	String.prototype.bMToFormatPhone=function(delim) { return bizMOB.Util.String.toFormatPhone(this, delim);};
	String.prototype.bMToFormatDate=function(format) { return bizMOB.Util.Date.toFormatDate(this, format);};
	String.prototype.bMToChoSung=function() { return bizMOB.Util.String.toChoSung(this);};
	String.prototype.bMDateDiff=function(strDate) { return bizMOB.Util.Date.diff(this,strDate);};
	String.prototype.bMToRegionMoney=function(countryCode,unitType) { return bizMOB.Util.String.toRegionMoney(this,countryCode,unitType);};
	String.prototype.bMToKorNumber=function() { return bizMOB.Util.Number.toKor(this);};
	String.prototype.bMStartsWith=function(str) { return bizMOB.Util.String.startsWith(this,str);};
	String.prototype.bMEndsWith=function(str) { return bizMOB.Util.String.endsWith(this,str);};
	String.prototype.bMIncludes=function(str) { return bizMOB.Util.String.includes(this,str);};


	Array.prototype.bMSort = function(options) { return bizMOB.Util.Sort.sortArray(this, options);};
	Array.prototype.bMRemoveAt= function(index) { return bizMOB.Util.Array.RemoveAt(this, index);};
	Array.prototype.max = function( ){ return Math.max.apply( null, this );};

	Date.prototype.bMToDate=function(){ return bizMOB.Util.Date.toDate(this);};
	Date.prototype.bMDiff=function(diffDate){ return bizMOB.Util.Date.diff(this, diffDate);};
	Date.prototype.bMAddYear=function(year){ return bizMOB.Util.Date.addYear(this, year);};
	Date.prototype.bMAddMonth=function(month){ return bizMOB.Util.Date.addMonth(this, month);};
	Date.prototype.bMAddDay=function(day){ return bizMOB.Util.Date.addDay(this,day);};
	Date.prototype.bMNextDay=function(){ return bizMOB.Util.Date.nextDay(this);};
	Date.prototype.bMNextMonth=function(){ return bizMOB.Util.Date.nextMonth(this);};
	Date.prototype.bMNextYear=function(){ return bizMOB.Util.Date.nextYear(this);};
	Date.prototype.bMPreviousDay=function(){ return bizMOB.Util.Date.previousDay(this);};
	Date.prototype.bMPreviousMonth=function(){ return bizMOB.Util.Date.previousMonth(this);};
	Date.prototype.bMPreviousYear=function(){ return bizMOB.Util.Date.previousYear(this);};
	Date.prototype.bMLastDay=function(){ return bizMOB.Util.Date.lastDay(this);};
	Date.prototype.bMToFormatDate=function(format) { return bizMOB.Util.Date.toFormatDate(this, format);};
	Date.prototype.bMGetWeeksOfMonth=function() { return bizMOB.Util.Date.getWeeksOfMonth(this);};
	Date.prototype.bMGetWeeksOfYear=function() { return bizMOB.Util.Date.getWeeksOfYear(this);};
	Date.prototype.bMGetStartEndWeek=function() { return bizMOB.Util.Date.getStartEndWeek(this);};

	Storage.prototype.setObject = function(key, value){ return this.setItem.call(this, key, JSON.stringify(value));};
	Storage.prototype.getObject = function(key){ var value = this.getItem.call(this, key); return value && JSON.parse(value);};


})();
