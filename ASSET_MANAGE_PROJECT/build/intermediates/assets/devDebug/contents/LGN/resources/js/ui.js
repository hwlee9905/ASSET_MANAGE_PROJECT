$(document).ready(function(){
		
	/* 하단 bng메뉴 */
	$(".dimmed-foot").css("visibility","hidden");
	
	$(".openBtn").click(function(){
	$(".openBtn").hide();
		$(".dimmed-foot").css("visibility","visible");
	$('.pop-menu').addClass('pop-menu-open');
	});
	
	$(".dimmed-foot .closeBtn").click(function(){
	$(".openBtn").show();
		$(".dimmed-foot").css("visibility","hidden");
	$('.pop-menu').removeClass('pop-menu-open');
	});
	
	//  
});



