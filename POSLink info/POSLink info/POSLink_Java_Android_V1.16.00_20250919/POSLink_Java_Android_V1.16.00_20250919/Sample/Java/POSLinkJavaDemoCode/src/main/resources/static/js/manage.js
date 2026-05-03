
define([
    'common',
    'cus_loading',
    'cus_alert_dialog',
    'logger'
], function (common, cusLoading, alertDialog, logger) {
    const NAME_SPACE = "manage"
    const NAME_SPACE_UNDERLINE = NAME_SPACE + "_"
    const NAME_SPACE_RESP_UNDERLINE = NAME_SPACE + "_resp_"

    function createUIItem(name, value, key, type, options) {
        return {
            name: name,
            value: value,
            key: key,
            type: type,
            options: options
        };
    }

    var request = {
        commandSelector: createUIItem('Command Type', common.MANAGE_CMD_TYPS[0], 'transType', common.VIEW_TYPES.selector, common.MANAGE_CMD_TYPS),
        edcType: createUIItem('Edc Type', common.EDC_TYPES[0], 'edcType', common.VIEW_TYPES.selector, common.EDC_TYPES),
        transType:createUIItem('Trans Type', common.TRANS_TYPES[0], 'trans', common.VIEW_TYPES.selector, common.TRANS_TYPES),
        varName: createUIItem('Var Name', '', 'varName', common.VIEW_TYPES.editText),
        varName1: createUIItem('Var Name1', '', 'varName1', common.VIEW_TYPES.editText),
        varName2: createUIItem('Var Name2', '', 'varName2', common.VIEW_TYPES.editText),
        varName3: createUIItem('Var Name3', '', 'varName3', common.VIEW_TYPES.editText),
        varName4: createUIItem('Var Name4', '', 'varName4', common.VIEW_TYPES.editText),
        varValue: createUIItem('Var Value', '', 'varValue', common.VIEW_TYPES.editText),
        varValue1: createUIItem('Var Value1', '', 'varValue1', common.VIEW_TYPES.editText),
        varValue2: createUIItem('Var Value2', '', 'varValue2', common.VIEW_TYPES.editText),
        varValue3: createUIItem('Var Value3', '', 'varValue3', common.VIEW_TYPES.editText),
        varValue4: createUIItem('Var Value4', '', 'varValue4', common.VIEW_TYPES.editText),
        extData: createUIItem('Ext Data', '', 'extData', common.VIEW_TYPES.editText, null),
        title: createUIItem('Title', '', 'title', common.VIEW_TYPES.editText, null),
        displayMessage: createUIItem('DisplayMessage', '', 'displayMessage', common.VIEW_TYPES.editText, null),
        displayMessage2: createUIItem('DisplayMessage2', '', 'displayMessage2', common.VIEW_TYPES.editText, null),
        topdown: createUIItem('Topdown', '', 'topdown', common.VIEW_TYPES.editText, null),
        taxLine: createUIItem('TaxLine', '', 'taxLine', common.VIEW_TYPES.editText, null),
        totalLine: createUIItem('TotalLine', '', 'totalLine', common.VIEW_TYPES.editText, null),
        imageName: createUIItem('ImageName', '', 'imageName', common.VIEW_TYPES.editText, null),
        imageDescription: createUIItem('ImageDescription', '', 'imageDescription', common.VIEW_TYPES.editText, null),
        itemsPassthrudata: createUIItem('ItemsPassthrudata', 'Fruit,,3,0,,4,,,', 'itemData', common.VIEW_TYPES.editText, null),
        timeout: createUIItem('TimeOut', '300', 'timeOut', common.VIEW_TYPES.editText, null),
        saveSigPath: createUIItem('SaveSigPath', './', 'sigSavePath', common.VIEW_TYPES.editText, null),
        upload: createUIItem('Upload', '0', 'upload', common.VIEW_TYPES.editText, null),
        hrefNum: createUIItem('HRefNum', '', 'hrefNum', common.VIEW_TYPES.editText, null),

        amount:createUIItem('Amount', '100', 'amount', common.VIEW_TYPES.editText),
        cashBackAmt:createUIItem('CashBackAmount', '', 'cashBackAmt', common.VIEW_TYPES.editText),
        magneticSwipeEntryFlag:createUIItem('MagneticSwipeEntryFlag', '1', 'magneticSwipeEntryFlag', common.VIEW_TYPES.editText),
        manualEntryFlag:createUIItem('ManualEntryFlag', '1', 'manualEntryFlag', common.VIEW_TYPES.editText),
        contactlessEntryFlag:createUIItem('ContactlessEntryFlag', '1', 'contactlessEntryFlag', common.VIEW_TYPES.editText),
        contactEMVEntryFlag:createUIItem('ContactEMVEntryFlag', '1', 'contactEMVEntryFlag', common.VIEW_TYPES.editText),
        fallbackSwipeEntryFlag:createUIItem('FallbackSwipeEntryFlag', '1', 'fallbackSwipeEntryFlag', common.VIEW_TYPES.editText),
        expiryDatePrompt:createUIItem('ExpiryDatePrompt', '1', 'expiryDatePrompt', common.VIEW_TYPES.editText),
        cvvPrompt:createUIItem('CVVPrompt', '1', 'cvvPrompt', common.VIEW_TYPES.editText),
        zipCodePrompt:createUIItem('ZipPrompt', '1', 'zipCodePrompt', common.VIEW_TYPES.editText),
        encryptionFlag:createUIItem('EncryptionFlag', '0', 'encryptionFlag', common.VIEW_TYPES.editText),
        keySlot:createUIItem('KeySlot', '1', 'keySlot', common.VIEW_TYPES.editText),
        paddingChar:createUIItem('PaddingChar', '', 'paddingChar', common.VIEW_TYPES.editText),
        trackDataSentinel:createUIItem('TrackDataSentinel', '1', 'trackDataSentinel', common.VIEW_TYPES.editText),
        minAccountLength:createUIItem('MinAccountLength', '', 'minAccountLength', common.VIEW_TYPES.editText),
        maxAccountLength:createUIItem('MaxAccountLength', '', 'maxAccountLength', common.VIEW_TYPES.editText),
        emvKernelConfigurationSelection:createUIItem('EMVKernelConfigSelection', '', 'emvKernelConfigurationSelection', common.VIEW_TYPES.editText),
        transactionDate:createUIItem('TransactionDate', '', 'transactionDate', common.VIEW_TYPES.editText),
        transactionTime:createUIItem('TransactionTime', '', 'transactionTime', common.VIEW_TYPES.editText),
        currencyCode:createUIItem('CurrencyCode', '840', 'currencyCode', common.VIEW_TYPES.editText),
        currencyExponent:createUIItem('CurrencyExponent', '', 'currencyExponent', common.VIEW_TYPES.editText),
        merchantCategoryCode:createUIItem('MerchantCategoryCode', '', 'merchantCategoryCode', common.VIEW_TYPES.editText),
        transactionSequenceNumber:createUIItem('TransSequenceNumber', '', 'transactionSequenceNumber', common.VIEW_TYPES.editText),
        tagList:createUIItem('TagList', '', 'tagList', common.VIEW_TYPES.editText),
        merchantDecision:createUIItem('MerchantDecision', '0', 'merchantDecision', common.VIEW_TYPES.editText),
        encryptionType:createUIItem('EncryptionType', '1', 'encryptionType', common.VIEW_TYPES.editText),
        pinMinLength:createUIItem('PINMinLength', '4', 'pinMinLength', common.VIEW_TYPES.editText),
        pinMaxLength:createUIItem('PINMaxLength', '12', 'pinMaxLength', common.VIEW_TYPES.editText),
        pinBypass:createUIItem('PINBypass', '', 'pinBypass', common.VIEW_TYPES.editText),
        pinAlgorithm:createUIItem('PINAlgorithm', '', 'pinAlgorithm', common.VIEW_TYPES.editText),
        onlineAuthorizationResult:createUIItem('OnlineAuthResult', '0', 'onlineAuthorizationResult', common.VIEW_TYPES.editText),
        responseCode:createUIItem('ResponseCode', '', 'responseCode', common.VIEW_TYPES.editText),
        authorizationCode:createUIItem('AuthCode', '', 'authorizationCode', common.VIEW_TYPES.editText),
        issuerAuthenticationData:createUIItem('IssuerAuthData', '', 'issuerAuthenticationData', common.VIEW_TYPES.editText),
        issuerScript1:createUIItem('IssuerScript1', '', 'issuerScript1', common.VIEW_TYPES.editText),
        issuerScript2:createUIItem('IssuerScript2', '', 'issuerScript2', common.VIEW_TYPES.editText),
        accountNumber:createUIItem('AccountNumber', '5454545454545454', 'accountNumber', common.VIEW_TYPES.editText),
        nullPIN:createUIItem('NullPIN', '1', 'nullPIN', common.VIEW_TYPES.editText),
        filePath: createUIItem('FilePath', '', 'filePath', common.VIEW_TYPES.editText, null),
        fileType:createUIItem('FileType', '0', 'fileType', common.VIEW_TYPES.editText),
        targetDevice:createUIItem('TargetDevice', '0', 'targetDevice', common.VIEW_TYPES.editText),
        fileTimeout: createUIItem('TimeOut', '1200', 'timeOut', common.VIEW_TYPES.editText, null)

    };
    var cancelableMap = {
        INIT: false,
        GETVAR: false,
        SETVAR: false,
        SHOWMESSAGE: false,
        SHOWITEM: false,
        DOSIGNATURE: true,
        GETSIGNATURE: false,
        INPUTACCOUNTWITHEMV: true,
        AUTHORIZECARD: false,
        COMPLETEONLINEEMV: false,
        GETPINBLOCK: false,
        UPDATERESOURCE: false
    }
    var requestMap = {
        INIT: [request.commandSelector],
        GETVAR: [
            request.commandSelector,
            request.edcType,
            request.varName,
            request.varName1,
            request.varName2,
            request.varName3,
            request.varName4,
            request.extData
        ],
        SETVAR: [
            request.commandSelector,
            request.edcType,
            request.varName,
            request.varValue,
            request.varName1,
            request.varValue1,
            request.varName2,
            request.varValue2,
            request.varName3,
            request.varValue3,
            request.varName4,
            request.varValue4,
            request.extData
        ],
        SHOWMESSAGE: [
            request.commandSelector,
            request.title,
            request.displayMessage,
            request.displayMessage2,
            request.topdown,
            request.taxLine,
            request.totalLine,
            request.imageName,
            request.imageDescription
        ],
        SHOWITEM: [
            request.commandSelector,
            request.title,
            request.topdown,
            request.taxLine,
            request.totalLine,
            request.itemsPassthrudata
        ],
        DOSIGNATURE:[
            request.commandSelector,
            request.edcType,
            request.upload,
            request.hrefNum,
            request.timeout
        ],
        GETSIGNATURE:[
            request.commandSelector,
            request.saveSigPath
        ],
        INPUTACCOUNTWITHEMV: [
        request.commandSelector,
        request.edcType,
        request.transType,
        request.amount,
        request.cashBackAmt,
        request.magneticSwipeEntryFlag,
        request.manualEntryFlag,
        request.contactlessEntryFlag,
        request.contactEMVEntryFlag,
        request.fallbackSwipeEntryFlag,
        request.expiryDatePrompt,
        request.cvvPrompt,
        request.zipCodePrompt,
        request.encryptionFlag,
        request.keySlot,
        request.paddingChar,
        request.trackDataSentinel,
        request.minAccountLength,
        request.maxAccountLength,
        request.emvKernelConfigurationSelection,
        request.transactionDate,
        request.transactionTime,
        request.currencyCode,
        request.currencyExponent,
        request.merchantCategoryCode,
        request.transactionSequenceNumber,
        request.tagList,
        request.timeout
        ],
        AUTHORIZECARD: [
        request.commandSelector,
        request.amount,
        request.cashBackAmt,
        request.merchantDecision,
        request.encryptionType,
        request.keySlot,
        request.pinMinLength,
        request.pinMaxLength,
        request.pinBypass,
        request.pinAlgorithm,
        request.emvKernelConfigurationSelection,
        request.transactionDate,
        request.transactionTime,
        request.currencyCode,
        request.currencyExponent,
        request.tagList,
        request.timeout
        ],
        COMPLETEONLINEEMV: [
        request.commandSelector,
        request.onlineAuthorizationResult,
        request.responseCode,
        request.authorizationCode,
        request.issuerAuthenticationData,
        request.issuerScript1,
        request.issuerScript2,
        request.title,
        request.extData
        ],
        GETPINBLOCK:[
        request.commandSelector,
        request.edcType,
        request.transType,
        request.title,
        request.accountNumber,
        request.encryptionType,
        request.keySlot,
        request.pinMinLength,
        request.pinMaxLength,
        request.nullPIN,
        request.pinAlgorithm,
        request.timeout
        ],
        UPDATERESOURCE:[
        request.commandSelector,
        request.filePath,
        request.fileType,
        request.targetDevice,
        request.fileTimeout
        ]
    };

    (function (arr, nameSpaceUnderLine) {
        for (item in arr) {
            arr[item]['inputKey'] = nameSpaceUnderLine + arr[item].key
        }
    })(request, NAME_SPACE_UNDERLINE)

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
            name: 'SN',
            value: '',
            key: 'sn',
            type: common.VIEW_TYPES.pureText
        },
        {
            name: 'VarValue',
            value: '',
            key: 'varValue',
            type: common.VIEW_TYPES.pureText
        },
        {
            name: 'VarValue1',
            value: '',
            key: 'varValue1',
            type: common.VIEW_TYPES.pureText
        },
        {
            name: 'VarValue2',
            value: '',
            key: 'varValue2',
            type: common.VIEW_TYPES.pureText
        },
        {
            name: 'VarValue3',
            value: '',
            key: 'varValue3',
            type: common.VIEW_TYPES.pureText
        },
        {
            name: 'VarValue4',
            value: '',
            key: 'varValue4',
            type: common.VIEW_TYPES.pureText
        },
        {
            name: 'ButtonNum',
            value: '',
            key: 'buttonNum',
            type: common.VIEW_TYPES.pureText
        },
        {
            name: 'SigFileName',
            value: '',
            key: 'sigFileName',
            type: common.VIEW_TYPES.pureText
        },
        {
            name: 'PinBlock',
            value: '',
            key: 'pinBlock',
            type: common.VIEW_TYPES.pureText
        },
        {
            name: 'KSN',
            value: '',
            key: 'ksn',
            type: common.VIEW_TYPES.pureText
        },
        {
            name: 'EntryMode',
            value: '',
            key: 'entryMode',
            type: common.VIEW_TYPES.pureText
        },
        {
        name: 'ETB',
        value: '',
        key: 'encryptionTransmissionBlock',
        type: common.VIEW_TYPES.pureText
        },
        {
            name: 'Track1Data',
            value: '',
            key: 'track1Data',
            type: common.VIEW_TYPES.pureText
        },
        {
            name: 'Track2Data',
            value: '',
            key: 'track2Data',
            type: common.VIEW_TYPES.pureText
        },
        {
            name: 'Track3Data',
            value: '',
            key: 'track3Data',
            type: common.VIEW_TYPES.pureText
        },
        {
            name: 'PAN',
            value: '',
            key: 'pan',
            type: common.VIEW_TYPES.pureText
        },
        {
            name: 'ExpiryDate',
            value: '',
            key: 'expiryDate',
            type: common.VIEW_TYPES.pureText
        },
        {
        name: 'ServiceCode',
        value: '',
        key: 'serviceCode',
        type: common.VIEW_TYPES.pureText
        },
        {
        name: 'CVV',
        value: '',
        key: 'cvvCode',
        type: common.VIEW_TYPES.pureText
        },
        {
        name: 'Zip',
        value: '',
        key: 'zipCode',
        type: common.VIEW_TYPES.pureText
        },
        {
        name: 'PinBypassStatus',
        value: '',
        key: 'pinBypassStatus',
        type: common.VIEW_TYPES.pureText
        },
        {
        name: 'CardHolder',
        value: '',
        key: 'cardholder',
        type: common.VIEW_TYPES.pureText
        },
        {
            name: 'QRCode',
            value: '',
            key: 'qrCode',
            type: common.VIEW_TYPES.pureText
        },
        {
        name: 'BarcodeType',
        value: '',
        key: 'barcodeType',
        type: common.VIEW_TYPES.pureText
        },
        {
        name: 'BarcodeData',
        value: '',
        key: 'barcodeData',
        type: common.VIEW_TYPES.pureText
        },
        {
            name: 'Text',
            value: '',
            key: 'text',
            type: common.VIEW_TYPES.pureText
        },
        {
        name: 'ContactlessTransactionPath',
        value: '',
        key: 'contactlessTransactionPath',
        type: common.VIEW_TYPES.pureText
        },
        {
            name: 'EMVData',
            value: '',
            key: 'emvData',
            type: common.VIEW_TYPES.pureText
        },
        {
        name: 'EncryptedEMVTLVData',
        value: '',
        key: 'encryptedEMVTLVData',
        type: common.VIEW_TYPES.pureText
        },
        {
        name: 'EncryptedSensitiveTLVData',
        value: '',
        key: 'encryptedSensitiveTLVData',
        type: common.VIEW_TYPES.pureText
        },
        {
            name: 'AuthorizationResult',
            value: '',
            key: 'authorizationResult',
            type: common.VIEW_TYPES.pureText
        },
        {
            name: 'SignatureFlag',
            value: '',
            key: 'signatureFlag',
            type: common.VIEW_TYPES.pureText
        },
        {
        name:'MaskedPAN',
        value:'',
        key:'maskedPAN',
        type: common.VIEW_TYPES.pureText
        },
        {
        name:'Online PIN Flag',
        value:'',
        key:'onlinePINFlag',
        type: common.VIEW_TYPES.pureText
        },
        {
            name: 'TagList',
            value: '',
            key: 'tagList',
            type: common.VIEW_TYPES.pureText
        },
        {
            name: 'ModelName',
            value: '',
            key: 'modelName',
            type: common.VIEW_TYPES.pureText
        },
        {
            name: 'PrimaryFirmVersion',
            value: '',
            key: 'primaryFirmVersion',
            type: common.VIEW_TYPES.pureText
        },
        {
            name: 'MacAddress',
            value: '',
            key: 'macAddress',
            type: common.VIEW_TYPES.pureText
        },
        {
            name: 'LinesPerScreen',
            value: '',
            key: 'linesPerScreen',
            type: common.VIEW_TYPES.pureText
        },
        {
            name: 'CharsPerLine',
            value: '',
            key: 'charsPerLine',
            type: common.VIEW_TYPES.pureText
        },
        {
            name: 'Status',
            value: '',
            key: 'status',
            type: common.VIEW_TYPES.pureText
        },
        {
            name: 'ExtData',
            value: '',
            key: 'extData',
            type: common.VIEW_TYPES.pureText
        },
        {
        name: 'VASCode',
        value: '',
        key: 'vasCode',
        type: common.VIEW_TYPES.pureText
        },
        {
        name: 'VASData',
        value: '',
        key: 'vasData',
        type: common.VIEW_TYPES.pureText
        },
        {
        name: 'NDEFData',
        value: '',
        key: 'NDEFData',
        type: common.VIEW_TYPES.pureText
        },
        {
                name: 'IssuerScriptResults',
                value: '',
                key: 'issuerScriptResults',
                type: common.VIEW_TYPES.pureText
        },
        {
                name: 'LuhnValidationResult',
                value: '',
                key: 'luhnValidationResult',
                type: common.VIEW_TYPES.pureText
        }
    ]
    common.setupInputID(response, NAME_SPACE_RESP_UNDERLINE)


    var show = function () {
        $('#manage_req').on("change", '#' + request['commandSelector'].inputKey, function (item) {
            request.commandSelector.value = item.target.value;
            common.showReq('#manage_req', requestMap[request.commandSelector.value])
        })
        common.showReq('#manage_req', requestMap[request.commandSelector.value])
        common.showResp('#manage_resp', response)
    }

    var init = function () {
        $('#start_manage').click(function () {
            jsonRequestMap = common.generateRequestMap(requestMap[request.commandSelector.value])
            common.postJSON(common.URL.MANAGE, jsonRequestMap,
                {
                    doBeforeSend: function (XMLHttpRequest) {

                        cusLoading.showLoading("Processing...", "Please wait...", cancelableMap[request.commandSelector.value])
                    },
                    doSuccess: function (data) {
                        logger.log(data);
                        if (data.code === common.RESPONSE_CODE.CODE_OK) {
                            (function parseResp(resp) {
                                $.each(response, function (index, item) {
                                    item.value = resp[item.key]
                                })
                            })(data.data)
                            common.showResp('#manage_resp', response)
                        } else {
                            alertDialog.show(data.message, "Close")
                        }
                        cusLoading.hideLoading()
                    },
                    doComplete: function (XMLHttpRequest, textStatus) { cusLoading.hideLoading() },
                    doError: function (XMLHttpRequest, textStatus, errorThrown) {
                        logger.log("manage fail")
                        cusLoading.hideLoading()
                        alertDialog.show(errorThrown, "Close")
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
