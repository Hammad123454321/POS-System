define(function () {
    var init = function init() {
        $("body").append(
            "<div class='modal fade' id='alert' tabindex='-1' role='dialog' aria-labelledby='myModalLabel' data-backdrop='static'>" +
            "<div class='modal-dialog' role='document'>" +
            "<div class='modal-content'>" +
            "<div class='modal-header'>" +
            "<h4 class='modal-title' id='alertTitle'>Message</h4>" +
            "</div>" +
            "<div id='alertBtnTxt' class='modal-body'>" +
            "<span class='glyphicon glyphicon-refresh' aria-hidden='true'>1</span>" +
            "</div>" +
            "</div>" +
            "</div>" +
            "</div>");

        $("#alertBtnTxt").click(function () {
            hide()
        });
    }
    var show = function show(msg, btnTxt) {
        $("#alertTitle").html(msg);
        $("#alertBtnTxt").html(btnTxt);
        $("#alert").modal("show");
    }
    var hide = function hide() {
        setTimeout(function () {
            $("#alert").modal("hide");
        }, 100);
    }

    return {
        init: init,
        show: show,
        hide: hide
    };
})

