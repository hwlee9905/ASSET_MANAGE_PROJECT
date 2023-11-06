(function(){

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

    var jsUrls = new Array(
        // bizMOB
        "bizMOB/jquery-3.4.1.min.js",
        "bizMOB/bizMOB-core.js",
        "bizMOB/bizMOB-xross3.js",
        "bizMOB/bizMOB-util3.js",
        "bizMOB/bizMOB-multilayout3.js",
        "common/js/common.js"
    );

    var JsList="";

    for(var i=0;i<jsUrls.length;i++)
    {
        JsList +=	"<script type=\"text/javascript\" src=\""+ RELATE_DEPTH + jsUrls[i] + "\" charset=\"utf-8\"></script>";
    }
    document.write(JsList);

})();
