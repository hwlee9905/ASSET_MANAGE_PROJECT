$(document).ready(function(){
		//folder
		$(".btn_fold").click(function() {
			$(this).parents("li").find(".listText").toggleClass("hide");
			$(this).toggleClass("active");
		});
		
		//스크롤영역
		var topFH = $(".topFix").height();
		var footH = $(".foot").outerHeight();
		$(".scrollArea").css("top", topFH);
		$(".scrollArea").css("bottom", footH);
		
});

