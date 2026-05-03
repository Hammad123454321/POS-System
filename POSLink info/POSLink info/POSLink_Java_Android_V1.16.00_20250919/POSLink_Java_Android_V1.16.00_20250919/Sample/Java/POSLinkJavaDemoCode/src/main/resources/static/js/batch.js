
define([
    'common',
    'cus_loading',
    'cus_alert_dialog',
    'logger'
], function (common, cusLoading, alertDialog, logger) {

    const NAME_SPACE = "batch"
    const NAME_SPACE_UNDERLINE = NAME_SPACE + "_"
    const NAME_SPACE_RESP_UNDERLINE = NAME_SPACE + "_resp_"

    var request = [
        {
            name: 'Command',
            value: common.BATCH_CMD_TYPES[0],
            key: 'commandType',
            type: common.VIEW_TYPES.selector,
            options: common.BATCH_CMD_TYPES
        }
    ]
    common.setupInputID(request, NAME_SPACE_UNDERLINE)

    var response = [
        {
            name: 'Result Code',
            value: '',
            key: 'resultCode',
            type: common.VIEW_TYPES.pureText
        },
        {
            name: 'Result Txt',
            value: '',
            key: 'resultTxt',
            type: common.VIEW_TYPES.pureText
        },
        {
            name: 'CreditCount',
            value: '',
            key: 'creditCount',
            type: common.VIEW_TYPES.pureText
        },
        {
            name: 'CreditAmount',
            value: '',
            key: 'creditAmount',
            type: common.VIEW_TYPES.pureText
        },
        {
            name: 'DebitCount',
            value: '',
            key: 'debitCount',
            type: common.VIEW_TYPES.pureText
        },
        {
            name: 'DebitAmount',
            value: '',
            key: 'debitAmount',
            type: common.VIEW_TYPES.pureText
        },
        {
            name: 'EBTCount',
            value: '',
            key: 'ebtCount',
            type: common.VIEW_TYPES.pureText
        },
        {
            name: 'EBTAmount',
            value: '',
            key: 'ebtAmount',
            type: common.VIEW_TYPES.pureText
        },
        {
            name: 'GiftCount',
            value: '',
            key: 'giftCount',
            type: common.VIEW_TYPES.pureText
        },
        {
            name: 'GiftAmount',
            value: '',
            key: 'giftAmount',
            type: common.VIEW_TYPES.pureText
        },
        {
            name: 'LoyaltyCount',
            value: '',
            key: 'loyaltyCount',
            type: common.VIEW_TYPES.pureText
        },
        {
            name: 'LoyaltyAmount',
            value: '',
            key: 'loyaltyAmount',
            type: common.VIEW_TYPES.pureText
        },
        {
            name: 'CashCount',
            value: '',
            key: 'cashCount',
            type: common.VIEW_TYPES.pureText
        },
        {
            name: 'CashAmount',
            value: '',
            key: 'cashAmount',
            type: common.VIEW_TYPES.pureText
        },
        {
            name: 'CHECKCount',
            value: '',
            key: 'checkCount',
            type: common.VIEW_TYPES.pureText
        },
        {
            name: 'CHECKAmount',
            value: '',
            key: 'checkAmount',
            type: common.VIEW_TYPES.pureText
        },
        {
            name: 'Timestamp',
            value: '',
            key: 'timestamp',
            type: common.VIEW_TYPES.pureText
        },
        {
            name: 'TID',
            value: '',
            key: 'tid',
            type: common.VIEW_TYPES.pureText
        },
        {
            name: 'MID',
            value: '',
            key: 'mid',
            type: common.VIEW_TYPES.pureText
        },
        {
            name: 'HostTraceNum',
            value: '',
            key: 'hostTraceNum',
            type: common.VIEW_TYPES.pureText
        },
        {
            name: 'BatchNum',
            value: '',
            key: 'batchNum',
            type: common.VIEW_TYPES.pureText
        },
        {
            name: 'AuthCode',
            value: '',
            key: 'authCode',
            type: common.VIEW_TYPES.pureText
        },
        {
            name: 'HostCode',
            value: '',
            key: 'hostCode',
            type: common.VIEW_TYPES.pureText
        },
        {
            name: 'HostResponse',
            value: '',
            key: 'hostResponse',
            type: common.VIEW_TYPES.pureText
        },
        {
            name: 'Message',
            value: '',
            key: 'message',
            type: common.VIEW_TYPES.pureText
        },
        {
            name: 'SAFTotalCount',
            value: '',
            key: 'safTotalCount',
            type: common.VIEW_TYPES.pureText
        },
        {
            name: 'SAFTotalAmount',
            value: '',
            key: 'safTotalAmount',
            type: common.VIEW_TYPES.pureText
        },
        {
            name: 'SAFUploadedCount',
            value: '',
            key: 'safUploadedCount',
            type: common.VIEW_TYPES.pureText
        },
        {
            name: 'SAFUploadedAmount',
            value: '',
            key: 'safUploadedAmount',
            type: common.VIEW_TYPES.pureText
        },
        {
            name: 'SAFFailedCount',
            value: '',
            key: 'safFailedCount',
            type: common.VIEW_TYPES.pureText
        },
        {
            name: 'SAFFailedTotal',
            value: '',
            key: 'safFailedTotal',
            type: common.VIEW_TYPES.pureText
        },
        {
            name: 'SAFDeletedCount',
            value: '',
            key: 'safDeletedCount',
            type: common.VIEW_TYPES.pureText
        },
        {
            name: 'BatchFailedRefNum',
            value: '',
            key: 'batchFailedRefNum',
            type: common.VIEW_TYPES.pureText
        },
        {
            name: 'BatchFailedCount',
            value: '',
            key: 'batchFailedCount',
            type: common.VIEW_TYPES.pureText
        },
        {
            name: 'ExtData',
            value: '',
            key: 'extData',
            type: common.VIEW_TYPES.pureText
        }
    ]
    common.setupInputID(response, NAME_SPACE_RESP_UNDERLINE)

    var show = function () {
        common.showReq('#batch_req', request)
        common.showResp('#batch_resp', response)
    }


    var init = function () {
        $('#start_batch').click(function () {
            requestMap = common.generateRequestMap(request)
            common.postJSON(common.URL.BATCH, requestMap,
                {
                    doBeforeSend: function (XMLHttpRequest) {
                        cusLoading.showLoading("Processing...", "Please wait...")
                    },
                    doSuccess: function (data) {
                        logger.log(data);
                        if (data.code === common.RESPONSE_CODE.CODE_OK) {
                            (function parseResp(resp) {
                                $.each(response, function (index, item) {
                                    item.value = resp[item.key]
                                })
                            })(data.data)
                            common.showResp('#batch_resp', response)
                        } else {
                            alertDialog.show(data.message, "OK")
                        }
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

    return {
        init: init,
        request: request,
        response: response,
        show: show
    }
});