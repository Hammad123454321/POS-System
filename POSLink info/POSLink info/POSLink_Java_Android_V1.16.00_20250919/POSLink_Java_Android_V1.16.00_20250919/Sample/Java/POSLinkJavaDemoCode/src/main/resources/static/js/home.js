define(function (require) {
    var logger = require('./logger')
    logger.setLogger(function (message) {
        console.log(message)
    })
    logger.log(navigator.userAgent)

    var common = require('./common');
    common.setPostJsonImpl(function (params) {
        params.beforeSend(null)
        setTimeout(function () {
            jsBridge.doRequest(JSON.stringify({
                url: params.url,
                data: params.data
            }), function (respStr) {
                logger.log("resp:" + JSON.stringify(respStr))
                resp = respStr
                if (resp.code === common.RESPONSE_CODE.CODE_OK) {
                    logger.log("post json succ")
                    params.success(resp)
                } else {
                    logger.log("post json fail")
                    params.error(null, null, resp.message)
                }
            })
        }, 100)
    })

    var cusLoading = require('./cus_loading');
    var payment = require('./payment');
    var manage = require('./manage');
    var batch = require('./batch');
    var report = require('./report');
    var setting = require('./setting');
    var alertDialog = require('./cus_alert_dialog');

    cusLoading.initLoading()
    alertDialog.init()
    payment.init()
    payment.show()
    manage.init()
    manage.show()
    batch.init()
    batch.show()
    report.init()
    report.show()
    setting.init()
    setting.show()
    setting.getCommSetting()
    setting.getLogSetting()
});

