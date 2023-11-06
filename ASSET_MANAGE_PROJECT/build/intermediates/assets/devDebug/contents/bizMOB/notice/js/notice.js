var page = {
		noticeList : { list : new Array() },
		AppShareArea : {},
		init:function(evt){

			//evt.data = evt.data.noticeData
			var storedAppShareArea =  bizMOB.Properties.get({_sKey : "AppShareArea"});
			if(storedAppShareArea){
				page.AppShareArea = storedAppShareArea;
			}
			
			page.filterNotice(evt.data);
			
	
		},
		filterNotice : function(data){
			
			var today = data.toDay;
			
			for(var i=0; i<data.list.length; i++ ){
				
				
				var skipdays = page.AppShareArea[data.list[i].noticeId];
				
				if(skipdays){
					if(today.bMDateDiff(skipdays.endDate) < 0 ){
						page.noticeList.list.push(data.list[i]);
					};
				}else{
					page.noticeList.list.push(data.list[i]);
				}
				
				
			}
			
			if(page.noticeList.list.length > 0 ){
				page.setNoticeLayer();
			}
						
		},
		setNoticeLayer : function(){

			var data = page.noticeList;
			var dir = [
				{ 
					"type" : "loop", 
					"target" : ".record", 
					"value" : "list",
					"detail" : [
						{ "type" : "single", "target" : "@noticeid", "value" :"noticeId" },
						{ "type" : "single", "target" : ".title", "value" : "title" },
						{ "type" : "single", "target" : ".content", "value" : function(arg, element, myParent){
							if(arg.item.contentType == "T"){
								return arg.item.content;
							}else{
								return "";
							}
						} },
						{ "type" : "single", "target" : ".contentsWrap@style+", "value" : function(arg){
								if(arg.item.imgType && arg.item.imgType == "URL"){
									return "background-image:url("+arg.item.imgUrl+");";
								}else{
									return "background-image:url(../download/"+arg.item.imgName+");";
								}
						}},
						{ "type" : "single", "target" : ".closeBtn@noticeid", "value" :"noticeId" },
						{ "type" : "single", "target" : ".closeBtn@offFlag", "value" : "offFlag" },
						{ "type" : "single", "target" : ".skipBtn", "value" : function(arg){
							if( arg.item.offFlag == false ){	
								if(arg.item.skipDay == "0"){
									return " 다시 보지 않기 ";
								}else{
									return arg.item.skipDay+" 일간 보이지 않기";
								}
							}
						} },
						{ "type" : "single", "target" : ".pop_chk@noticeid", "value" :"noticeId" },
						{ "type" : "single", "target" : ".pop_chk@skipday", "value" : "skipDay" },
						{ "type" : "single", "target" : "footer@class+", "value" : function(arg){
							if(arg.item.skipFlag){
								return "";
							}else{
								return " hide";
							}
						} }
						
					]
				},
			];
			
			$(".templet").bMRender(data, dir,{clone:true});
			page.setButtonHandler();
		
		},
		setButtonHandler : function(){
			
			$(".closeBtn").click(function(){
				
				var closeType = $(this).attr("offFlag");
				var noticeId = $(this).attr("noticeId");
				
				if(closeType == "true"){
					bizMOB.App.exit();
				}else{
					
					page.popupClose(noticeId);
					
				}
				
				
			});
			
			$(".pop_chk").click(function(){
				
				var skipDay = $(this).attr("skipday");
				var noticeId = $(this).attr("noticeId");
				skipDay = skipDay.bMToNumber();
				
				var today = new Date();
				today.setHours(0, 0, 0);
				
				if(!page.AppShareArea[noticeId]) {
					page.AppShareArea[noticeId] = {}
				}
				page.AppShareArea[noticeId].startDate = today.bMToFormatDate("yyyymmddHHnnss");
				if(skipDay != 0){
					page.AppShareArea[noticeId].endDate = today.bMAddDay(skipDay).bMToFormatDate("yyyymmddHHnnss");	
				}else{
					var endDate = new Date(2999,11,31);
					page.AppShareArea[noticeId].endDate = endDate.bMToFormatDate("yyyymmddHHnnss");	
				}
				
				
				bizMOB.Properties.set({
					_sKey : "AppShareArea",
					_vValue : page.AppShareArea
				});
				
				page.popupClose(noticeId);
				
			});
		},
		popupClose: function(noticeid){
			
			var target = $("div[noticeid='"+noticeid+"']");
			
			try{
				target.attr("hide",true);
				target.hide();
				
				if($("#renderList div.record[hide!=true]").length  == 0 ){
					bizMOB.Window.close({_sType:"popup"});
				}
			}catch(e){
				bizMOB.Window.close({_sType:"popup"});
			}
			
		}

	};

		