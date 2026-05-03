define([
    'common',
    'cus_loading',
    'cus_alert_dialog',
    'logger'
], function (common, cusLoading, alertDialog, logger) {

    const NAME_SPACE = "report"
    const NAME_SPACE_UNDERLINE = NAME_SPACE + "_"
    const NAME_SPACE_RESP_UNDERLINE = NAME_SPACE + "_resp"+ "_"

    var request = [
        {
            name: 'TransType',
            value: common.REPORT_CMD_TYPES[0],
            key: 'transType',
            type: common.VIEW_TYPES.selector,
            options: common.REPORT_CMD_TYPES
        },
        {
            name: 'EDCType',
            value: common.EDC_TYPES[0],
            key: 'edcType',
            type: common.VIEW_TYPES.selector,
            options: common.EDC_TYPES
        },
        {
            name: 'CardType',
            value: common.CARD_TYPES[0],
            key: 'cardType',
            type: common.VIEW_TYPES.selector,
            options: common.CARD_TYPES
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
            name: 'ExtData',
            value: '',
            key: 'extData',
            type: common.VIEW_TYPES.pureText
        }
    ]
    common.setupInputID(response, NAME_SPACE_RESP_UNDERLINE)

    var show = function () {
        common.showReq('#report_req', request)
        common.showResp('#report_resp', response)
    }


    var init = function () {
        $('#start_report').click(function () {
            requestMap = common.generateRequestMap(request)
            common.postJSON(common.URL.REPORT, requestMap,
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
                            common.showResp('#report_resp', response)
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
        show: show
    }
});