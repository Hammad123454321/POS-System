define(function () {
    const RESPONSE_CODE = { CODE_OK: "200" }
    const EDC_TYPES = ["ALL", "CREDIT", "DEBIT", "CHECK", "EBT_FOODSTAMP", "EBT_CASHBENEFIT", "GIFT", "LOYALTY", "CASH", "EBT"]
    const TRANS_TYPES = ["UNKNOWN", "AUTH", "SALE", "RETURN", "VOID", "POSTAUTH", "FORCEAUTH",
        "ADJUST", "INQUIRY", "ACTIVATE", "DEACTIVATE", "RELOAD", "VOID SALE",
        "VOID RETURN", "VOID AUTH", "VOID POSTAUTH", "VOID FORCEAUTH", "VOID WITHDRAWAL",
        "REVERSAL", "WITHDRAWAL", "ISSUE", "CASHOUT", "REPLACE", "MERGE",
        "REPORTLOST", "REDEEM", "VERIFY", "REACTIVATE", "FORCED ISSUE", "FORCED ADD",
        "UNLOAD", "RENEW", "GETCONVERTDETAIL", "CONVERT", "TOKENIZE", "INCREMENTALAUTH",
        "BALANCEWITHLOCK", "REDEMPTIONWITHUNLOCK", "REWARDS", "REENTER"];
    const CARD_TYPES = ["ALL", "UNKNOWN", "VISA", "MASTERCARD", "AMEX",
        "DISCOVER", "DINERCLUB", "ENROUTE", "JCB",
        "REVOLUTIONCARD", "OTHER"];

    // const MANAGE_CMD_TYPS = [
    //     "INIT", "GETVAR", "SETVAR", "SHOWDIALOG", "GETSIGNATURE",
    //     "SHOWMESSAGE", "CLEARMESSAGE", "RESET", "UPDATEIMAGE",
    //     "DOSIGNATURE", "DELETEIMAGE", "SHOWTHANKYOU", "REBOOT",
    //     "GETPINBLOCK", "INPUTACCOUNT", "RESETMSR", "INPUTTEXT",
    //     "CHECKFILE", "AUTHORIZECARD", "COMPLETEONLINEEMV", "REMOVECARD",
    //     "GETEMVTLVDATA", "SETEMVTLVDATA", "INPUTACCOUNTWITHEMV", "COMPLETECONTACTLESSEMV",
    //     "SETSAFPARAMETERS", "SHOWTEXTBOX", "REPRINT", "PRINTER",
    //     "SHOWITEM", "CARDINSERTDETECTION"]
    const MANAGE_CMD_TYPS = ["INIT", "GETVAR", "SETVAR", "GETSIGNATURE", "DOSIGNATURE", "SHOWMESSAGE", "SHOWITEM","INPUTACCOUNTWITHEMV","AUTHORIZECARD","COMPLETEONLINEEMV","GETPINBLOCK","UPDATERESOURCE"]

    const BATCH_CMD_TYPES = [
        "BATCHCLOSE"
    ]

    const REPORT_CMD_TYPES = [
        "LOCALTOTALREPORT"
    ]

    const VIEW_TYPES = {
        editText: 'editText',
        pureText: 'pureText',
        multiText: 'multiText',
        inputFile: 'inputFile',
        selector: 'selector'
    }

    const URL = {
        PAYMENT: '/payment',
        MANAGE: '/manage',
        BATCH: '/batch',
        REPORT: '/report',
        COMM: '/comm',
        GET_COMM: '/get_comm',
        LOG_SET: '/log_set',
        GET_LOG_SET: '/get_log'
    }

    const COMM_TYPES = ["UART", "TCP", "SSL", "HTTP", "HTTPS"]

    const LOG_SWITCH = ["ON", "OFF"]

    const LOG_LEVEL = ["DEBUG", "ERROR"]

    var setupInputID = function (arr, nameSpaceUnderLine) {
        $.each(arr, function (index, item) {
            item['inputKey'] = nameSpaceUnderLine + item.key
        })
    }

    var itemEditTxt = function (id, name, value) {
        inHtml = `
        <div class="item-single">
            <div class="form-group">
                <label class="item-label" for="${id}">${name}</label>
                <input id="${id}" class="fix-input-width" type="text" value="${value}"/>
            </div>
        </div>
        `
        return inHtml
    }

    var itemInputFile = function (id, name, value) {
        inHtml = `
        <div class="item-single">
            <div class="form-group">
                <label class="item-label" for="${id}">${name}</label>
                <input type="file" id="${id}" class="fix-input-width" value="${value}  webkitdirectory mozdirectory msdirectory odirectory directory multiple "/>
            </div>
        </div>
         `
        return inHtml
    }

    var itemSelect = function (id, name, value, optionLs) {
        inHtml = `
        <div class="item-single">
            <div class="form-group">
                <label class="item-label" for="${id}">${name}</label>
                <select class="custom-select fix-input-width" id="${id}">
                    <option>${value}</option>`

        $.each(optionLs, function (index, op) {
            if (op != value) {
                inHtml += `<option>${op}</option>`
            }
        })

        inHtml +=
            `
                </select>
            </div>
        </div>
        `
        return inHtml
    }

    var postJsonImpl = null

    var setPostJsonImpl = function (impl) {
        postJsonImpl = impl
    }
    /**
     *
     * @param {*} page example: "/XXX"
     * @param {*} jsonData JSON Data
     * @param {*} callbacks
     * {
     * doBeforeSend (XMLHttpRequest)
     * doSuccess (data)
     * doComplete (XMLHttpRequest, textStatus)
     * doError (XMLHttpRequest, textStatus, errorThrown)
     * }
     */
    var postJSON = function (page, jsonData, callbacks) {
        postJsonImpl({
            type: "POST",
            contentType: 'application/json; charset=utf-8',
            dataType: "json",
            url: page,
            beforeSend: callbacks.doBeforeSend,
            success: callbacks.doSuccess,
            complete: callbacks.doComplete,
            error: callbacks.doError,
            data: JSON.stringify(jsonData)
        })
    }

    /**
     * cancel Trans.
     */
    var cancelTrans = function () {
        jsBridge.cancelRequest()
    }

    var showReq = function (viewId, request) {
        reqHtml = ``
        $.each(request, function (index, item) {
            if (item.type === VIEW_TYPES.selector) {
                reqHtml += itemSelect(item.inputKey, item.name, item.value, item.options)
            }
            if (item.type === VIEW_TYPES.pureText || item.type === VIEW_TYPES.editText) {
                reqHtml += itemEditTxt(item.inputKey, item.name, item.value)
            }
            if (item.type === VIEW_TYPES.inputFile) {
                reqHtml += itemInputFile(item.inputKey, item.name, item.value)
            }
        })
        $(viewId).empty()
        // $(viewId).append('<p style="text-align:center;">Request</p>')
        $(viewId).append(reqHtml)
    }

    var showResp = function (viewId, response) {
        respHtml = ``
        $.each(response, function (index, item) {
            if (item.type === VIEW_TYPES.pureText && item.value != null) {
                respHtml += itemEditTxt(item.inputKey, item.name, item.value)
            }
        })
        $(viewId).empty()
        // $(viewId).append('<p style="text-align:center;">Response</p>')
        $(viewId).append(respHtml)
    }

    var generateRequestMap = function (reqArr) {
        resultMap = {}
        $.each(reqArr, function (index, item) {
            htmlEle = $('#' + item.inputKey)
            if (htmlEle !== null && htmlEle !== undefined) {
                if (item.type === VIEW_TYPES.selector) {
                    item.value = htmlEle.find("option:selected").text()
                } else {
                    item.value = htmlEle.val()
                }
            }
            resultMap[item.key] = item.value
        })
        return resultMap
    }


    return {
        RESPONSE_CODE: RESPONSE_CODE,
        EDC_TYPES: EDC_TYPES,
        TRANS_TYPES: TRANS_TYPES,
        CARD_TYPES: CARD_TYPES,
        VIEW_TYPES: VIEW_TYPES,
        MANAGE_CMD_TYPS: MANAGE_CMD_TYPS,
        BATCH_CMD_TYPES: BATCH_CMD_TYPES,
        REPORT_CMD_TYPES: REPORT_CMD_TYPES,
        COMM_TYPES: COMM_TYPES,
        LOG_SWITCH: LOG_SWITCH,
        LOG_LEVEL: LOG_LEVEL,
        URL: URL,

        setupInputID: setupInputID,
        itemEditTxt: itemEditTxt,
        itemSelect: itemSelect,
        postJSON: postJSON,
        cancelTrans: cancelTrans,
        setPostJsonImpl: setPostJsonImpl,
        showReq: showReq,
        showResp: showResp,
        generateRequestMap: generateRequestMap
    }
})
