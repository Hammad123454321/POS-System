define(function () {
    var initLoading = function initLoading(){
    $("body").append("<!-- loading -->" +
            "<div class='modal fade' id='loading' tabindex='-1' role='dialog' aria-labelledby='myModalLabel' data-backdrop='static'>" +
            "<div class='modal-dialog' role='document'>" +
            "<div class='modal-content'>" +
            "<div class='modal-header'>" +
            "<h4 class='modal-title' id='myModalLabel'>Message</h4>" +
            "</div>" +
            "<div id='loadingText' class='modal-body'>" +
            "<span class='glyphicon glyphicon-refresh' aria-hidden='true'>1</span>" +
            "</div>" +
            "<div class='modal-footer' style='visibility: none;' id ='button-body'>" +
            "<button type='button' class='btn btn-secondary' id='btn_cancel'>cancel</button>" +
            "</div>" +
            "</div>" +  "</div>");

        var common = require('./common');

        $('#btn_cancel').click(function () {
            common.cancelTrans()
        });

    }
    var showLoading = function showLoading(title, text, cancelable){
        $("#myModalLabel").html(title);
        $("#loadingText").html(text);
        $("#loading").modal("show");
        if (cancelable) {
            document.getElementById("button-body").style.visibility="visible";//隐藏
        } else {
            document.getElementById("button-body").style.visibility="hidden";//隐藏
        }
        // $("#button-body").style.visibility("hidden");
    }
    var hideLoading = function hideLoading(){
        setTimeout(function(){
            $("#loading").modal("hide");
        }, 500);
    }

    return {
        initLoading : initLoading,
        showLoading : showLoading,
        hideLoading : hideLoading
    };
})

