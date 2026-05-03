define(['common', 'logger'], function (common, logger) {
    var shouldStop = false;
    var sync = function () {
        common.postJSON("/heartbeat", {}, {
            doBeforeSend: function (XMLHttpRequest) {
            },
            doSuccess: function (data) {
                if (!shouldStop) {
                    setTimeout(function () {
                        sync()
                    }, 3 * 1000)
                }
            },
            doComplete: function (XMLHttpRequest, textStatus) { },
            doError: function (XMLHttpRequest, textStatus, errorThrown) {
                logger.log("heartbeat err:" + errorThrown)
            }
        })
    }

    var init = function () {
        sync()
    }

    var stop = function () {
        shouldStop = true;
    }

    return {
        init: init,
        stop: stop
    }
});