define([
    'common',
    'cus_loading',
    'cus_alert_dialog',
    'logger'
], function (common, cusLoading, alertDialog, logger) {

    const NAME_SPACE = "payment"
    const NAME_SPACE_UNDERLINE = NAME_SPACE + "_"
    const NAME_SPACE_RESP_UNDERLINE = NAME_SPACE + "_resp"+ "_"

    var request = [
        {
            name: 'EDC Type',
            value: common.EDC_TYPES[1],
            key: 'edcType',
            type: common.VIEW_TYPES.selector,
            options: common.EDC_TYPES
        },
        {
            name: 'Trans Type',
            value: common.TRANS_TYPES[2],
            key: 'transType',
            type: common.VIEW_TYPES.selector,
            options: common.TRANS_TYPES
        },
        {
            name: 'Amount',
            value: '100',
            key: 'amount',
            type: common.VIEW_TYPES.editText
        },
        {
            name: 'CashBackAmt',
            value: '',
            key: 'cashBackAmt',
            type: common.VIEW_TYPES.editText
        },
        {
            name: 'ClerkID',
            value: '',
            key: 'clerkID',
            type: common.VIEW_TYPES.editText
        },
        {
            name: 'TipAmt',
            value: '',
            key: 'tipAmt',
            type: common.VIEW_TYPES.editText
        },
        {
            name: 'TaxAmt',
            value: '',
            key: 'taxAmt',
            type: common.VIEW_TYPES.editText
        },
        {
            name: 'Street',
            value: '',
            key: 'street',
            type: common.VIEW_TYPES.editText
        },
        {
            name: 'Street2',
            value: '',
            key: 'street2',
            type: common.VIEW_TYPES.editText
        },
        {
            name: 'SurchargeAmt',
            value: '',
            key: 'surchargeAmt',
            type: common.VIEW_TYPES.editText
        },
        {
            name: 'ServerID',
            value: '',
            key: 'serverID',
            type: common.VIEW_TYPES.editText
        },
        {
            name: 'AutoSubmit',
            value: '',
            key: 'autoSubmit',
            type: common.VIEW_TYPES.editText
        },
        {
            name: 'PONum',
            value: '',
            key: 'PONum',
            type: common.VIEW_TYPES.editText
        },
        {
            name: 'OrigRefNum',
            value: '',
            key: 'origRefNum',
            type: common.VIEW_TYPES.editText
        },
        {
            name: 'MerchantKey',
            value: '',
            key: 'merchantKey',
            type: common.VIEW_TYPES.editText
        },
        {
            name: 'InvNum',
            value: '',
            key: 'invNum',
            type: common.VIEW_TYPES.editText
        },
        {
            name: 'ECRRefNum',
            value: '1',
            key: 'ECRRefNum',
            type: common.VIEW_TYPES.editText
        },
        {
            name: 'ECRTransID',
            value: '',
            key: 'ECRTransID',
            type: common.VIEW_TYPES.editText
        },
        {
            name: 'OrigECRRefNum',
            value: '',
            key: 'origECRRefNum',
            type: common.VIEW_TYPES.editText
        },
        {
            name: 'AuthCode',
            value: '',
            key: 'authCode',
            type: common.VIEW_TYPES.editText
        },
        {
            name: 'FuelAmt',
            value: '',
            key: 'fuelAmt',
            type: common.VIEW_TYPES.editText
        },
        {
            name: 'ExtData',
            value: '',
            key: 'extData',
            type: common.VIEW_TYPES.editText
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
            name: 'AuthCode',
            value: '',
            key: 'authCode',
            type: common.VIEW_TYPES.pureText
        },
        {
            name: 'ApprovedAmount',
            value: '',
            key: 'approvedAmount',
            type: common.VIEW_TYPES.pureText
        },
        {
            name: 'AvsResponse',
            value: '',
            key: 'avsResponse',
            type: common.VIEW_TYPES.pureText
        },
        {
            name: 'BogusAccountNum',
            value: '',
            key: 'bogusAccountNum',
            type: common.VIEW_TYPES.pureText
        },
        {
            name: 'CardType',
            value: '',
            key: 'cardType',
            type: common.VIEW_TYPES.pureText
        },
        {
            name: 'CvResponse',
            value: '',
            key: 'cvResponse',
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
            name: 'RefNum',
            value: '',
            key: 'refNum',
            type: common.VIEW_TYPES.pureText
        },
        {
            name: 'RawResponse',
            value: '',
            key: 'rawResponse',
            type: common.VIEW_TYPES.pureText
        },
        {
            name: 'RemainingBalance',
            value: '',
            key: 'remainingBalance',
            type: common.VIEW_TYPES.pureText
        },
        {
            name: 'ExtraBalance',
            value: '',
            key: 'extraBalance',
            type: common.VIEW_TYPES.pureText
        },
        {
            name: 'RequestedAmount',
            value: '',
            key: 'requestedAmount',
            type: common.VIEW_TYPES.pureText
        },
        {
            name: 'Timestamp',
            value: '',
            key: 'timestamp',
            type: common.VIEW_TYPES.pureText
        },
        {
            name: 'SigFileName',
            value: '',
            key: 'sigFileName',
            type: common.VIEW_TYPES.pureText
        },
        {
            name: 'SignData',
            value: '',
            key: 'signData',
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
        common.showReq('#payment_req', request)
        common.showResp('#payment_resp', response)
    }


    var init = function () {
        $('#start_payment').click(function () {
            requestMap = common.generateRequestMap(request)
            common.postJSON(common.URL.PAYMENT, requestMap,
                {
                    doBeforeSend: function (XMLHttpRequest) {
                        cusLoading.showLoading("Processing...", "Please wait...", true)
                    },
                    doSuccess: function (data) {
                        logger.log(data);
                        if (data.code === common.RESPONSE_CODE.CODE_OK) {
                            (function parseResp(resp) {
                                $.each(response, function (index, item) {
                                    item.value = resp[item.key]
                                })
                            })(data.data)
                            common.showResp('#payment_resp', response)
                        } else {
                            alertDialog.show(data.message, "Close")
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
