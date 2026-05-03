define([
    'common',
    'cus_loading',
    'cus_alert_dialog',
    'logger'
], function (common, cusLoading, alertDialog, logger) {

    const NAME_SPACE = "setting"
    const NAME_SPACE_UNDERLINE = NAME_SPACE + "_"
    const NAME_SPACE_RESP_UNDERLINE = NAME_SPACE + "_resp_"

    var commSetting = [
        {
            name: 'Comm Type',
            value: common.COMM_TYPES[1],
            key: 'commType',
            type: common.VIEW_TYPES.selector,
            options: common.COMM_TYPES
        },
        {
            name: 'TimeOut',
            value: '120000',
            key: 'timeOut',
            type: common.VIEW_TYPES.editText
        },
        {
            name: 'SerialPort',
            value: 'COM1',
            key: 'serialPort',
            type: common.VIEW_TYPES.editText
        },
        {
            name: 'Baud Rate',
            value: '9600',
            key: 'baudRate',
            type: common.VIEW_TYPES.editText
        },
        {
            name: 'Dest IP',
            value: '172.16.20.15',
            key: 'destIP',
            type: common.VIEW_TYPES.editText
        },
        {
            name: 'Dest Port',
            value: '10009',
            key: 'destPort',
            type: common.VIEW_TYPES.editText
        }
    ]
    common.setupInputID(commSetting, NAME_SPACE_UNDERLINE)

    var logSetting = [
        {
            name: 'Log Switch',
            value: common.LOG_SWITCH[0],
            key: 'logSwitch',
            type: common.VIEW_TYPES.selector,
            options: common.LOG_SWITCH
        },
        {
            name: 'Log Level',
            value: common.LOG_LEVEL[0],
            key: 'logLevel',
            type: common.VIEW_TYPES.selector,
            options: common.LOG_LEVEL
        },
        {
            name: 'Log File Name',
            value: 'POSLog',
            key: 'logFileName',
            type: common.VIEW_TYPES.editText
        },
        {
            name: 'Log File Path',
            value: '',
            key: 'logFilePath',
            type: common.VIEW_TYPES.editText
        },
        {
            name: 'Log Expired Days',
            value: '30',
            key: 'logDays',
            type: common.VIEW_TYPES.editText
        }
    ]
    common.setupInputID(logSetting, NAME_SPACE_RESP_UNDERLINE)

    var show = function () {
        // common.showReq('#comm_setting', commSetting)
        common.showReq('#log_setting', logSetting)
    }


    var init = function () {
        $('#start_comm_setting').click(function () {
            requestMap = common.generateRequestMap(commSetting)
            common.postJSON(common.URL.COMM, requestMap,
                {
                    doBeforeSend: function (XMLHttpRequest) {
                        cusLoading.showLoading("Processing...", "Please wait...")
                    },
                    doSuccess: function (data) {
                        logger.log('set comm...')
                        logger.log(data);
                        cusLoading.hideLoading()
                    },
                    doComplete: function (XMLHttpRequest, textStatus) { cusLoading.hideLoading() },
                    doError: function (XMLHttpRequest, textStatus, errorThrown) {
                        cusLoading.hideLoading()
                        alertDialog.show(errorThrown, "OK")
                    }
                }
            )
        });

        $('#start_log_setting').click(function () {
            requestMap = common.generateRequestMap(logSetting)
            common.postJSON(common.URL.LOG_SET, requestMap,
                {
                    doBeforeSend: function (XMLHttpRequest) {
                        cusLoading.showLoading("Processing...", "Please wait...")
                    },
                    doSuccess: function (data) {
                        logger.log(data);
                        cusLoading.hideLoading()
                    },
                    doComplete: function (XMLHttpRequest, textStatus) { cusLoading.hideLoading() },
                    doError: function (XMLHttpRequest, textStatus, errorThrown) {
                        cusLoading.hideLoading()
                        alertDialog.show(errorThrown, "OK")
                    }
                }
            )
        });
    }

    var getCommSetting = function () {
        common.postJSON(common.URL.GET_COMM, {},
            {
                doBeforeSend: function (XMLHttpRequest) {
                },
                doSuccess: function (data) {
                    logger.log(data);
                    if (data.code === common.RESPONSE_CODE.CODE_OK) {
                        (function parseResp(resp) {
                            $.each(commSetting, function (index, item) {
                                item.value = resp[item.key]
                            })
                        }) (data.data.commSetting)
                        common.showReq('#comm_setting', commSetting)
                    } else {
                        common.showReq('#comm_setting', commSetting)
                        alertDialog.show(data.message, "OK")
                    }
                },
                doComplete: function (XMLHttpRequest, textStatus) { cusLoading.hideLoading() },
                doError: function (XMLHttpRequest, textStatus, errorThrown) {
                    alertDialog.show(errorThrown, "OK")
                }
            }
        )
    }

    var getLogSetting = function () {
        common.postJSON(common.URL.GET_LOG_SET, {},
            {
                doBeforeSend: function (XMLHttpRequest) {
                },
                doSuccess: function (data) {
                    logger.log(data);
                    if (data.code === common.RESPONSE_CODE.CODE_OK) {
                        (function parseResp(resp) {
                            $.each(logSetting, function (index, item) {
                                item.value = resp[item.key]
                            })
                        }) (data.data.logSetting)
                        common.showReq('#log_setting', logSetting)
                    } else {
                        common.showReq('#log_setting', logSetting)
                        alertDialog.show(data.message, "OK")
                    }
                },
                doComplete: function (XMLHttpRequest, textStatus) { cusLoading.hideLoading() },
                doError: function (XMLHttpRequest, textStatus, errorThrown) {
                    alertDialog.show(errorThrown, "OK")
                }
            }
        )
    }

    return {
        init: init,
        show: show,
        getCommSetting: getCommSetting,
        getLogSetting: getLogSetting
    }
});