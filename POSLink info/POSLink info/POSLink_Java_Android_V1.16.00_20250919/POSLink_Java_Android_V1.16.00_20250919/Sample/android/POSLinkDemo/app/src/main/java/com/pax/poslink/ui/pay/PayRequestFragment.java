package com.pax.poslink.ui.pay;


import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;

import androidx.lifecycle.ViewModelProvider;

import com.google.gson.Gson;
import com.pax.poslink.PaymentRequest;
import com.pax.poslink.R;
import com.pax.poslink.business.ActivityResultReceiver;
import com.pax.poslink.business.UIBusiness;
import com.pax.poslink.entity.FleetCardRequest;
import com.pax.poslink.entity.MultiMerchant;
import com.pax.poslink.entity.Restaurant;
import com.pax.poslink.main.MainConst;
import com.pax.poslink.model.MultiMerchantValueSetter;
import com.pax.poslink.model.RestaurantValueSetter;
import com.pax.poslink.model.payment.PaymentAutoRentalInfoValueSetter;
import com.pax.poslink.model.payment.PaymentCommercialValueSetter;
import com.pax.poslink.model.payment.PaymentFleetCardValueSetter;
import com.pax.poslink.model.payment.PaymentHostCredentialInformationSetter;
import com.pax.poslink.model.payment.PaymentHostGateWayValueSetter;
import com.pax.poslink.model.payment.PaymentItemValueSetter;
import com.pax.poslink.model.payment.PaymentLodgingInfoValueSetter;
import com.pax.poslink.model.payment.PaymentOriginalValueSetter;
import com.pax.poslink.model.payment.PaymentTransactionBehaviorValueSetter;
import com.pax.poslink.ui.DirectoryLogActivity;
import com.pax.poslink.ui.MultiMerchantActivity;
import com.pax.poslink.ui.base.RequestFragment;
import com.pax.poslink.ui.multicmd.MultiCmdViewModel;
import com.pax.poslink.util.Constant;
import com.pax.poslink.util.StringUtil;
import com.pax.poslink.util.adapter.CommonItemView;
import com.pax.poslink.util.adapter.RenderEntity;
import com.pax.poslink.view.ButtonEntity;
import com.pax.poslink.view.ExtDataEntity;
import com.pax.poslink.view.NameValueBrowserEntity;
import com.pax.poslink.view.NameValueEntity;
import com.pax.poslink.view.NameValueSelectEntity;
import com.pax.poslink.view.NameValueStringEntity;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static com.pax.poslink.util.Constant.*;

public class PayRequestFragment extends RequestFragment<PaymentRequest> {

    private NameValueBrowserEntity sigBrowserEntity;
    private NameValueSelectEntity tenderTypeEntity;
    private NameValueSelectEntity transTypeEntity;

    private String commercialJson = "";
    private String restaurantJson = "";
    private String hostGateWayJson = "";
    private String transactionBehaviorJson = "";
    private String originalJson = "";
    private String fleetCardJson = "";
    private String multiMerchantJson = "";
    private String lodgingInfoJson = "";
    private String autoRentalInfoJson = "";
    private String taxDetailMsg = "", lineItemDetailMsg = "";
    private String lodgingRoomRatesMsg = "", lodgingItemsMsg = "";
    private String extraChargeItemsMsg = "";
    private String hostCredentialInformationJson = "";

    private MultiCmdViewModel model;
    private PaymentRequest paymentRequest;

    public PayRequestFragment() {
        // Required empty public constructor
    }

    public static PayRequestFragment newInstance() {
        return new PayRequestFragment();
    }

    public static PayRequestFragment newInstance(String processBtnName) {
        PayRequestFragment fragment = new PayRequestFragment();
        fragment.setProcessBtn(processBtnName);
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_pay_request;
    }

    @Override
    public void initView(View view) {
        requestContainer = view.findViewById(R.id.payment_request_container);
        model = new ViewModelProvider(requireActivity()).get(MultiCmdViewModel.class);
        paymentRequest = model.getPaymentRequest();
        showCorrespondingRequestView();
    }

    private void showCorrespondingRequestView() {
        List<String> edcTypes = MainConst.PAYMENT_EDC_TYPES;
        List<String> transTypes = MainConst.TRANS_TYPE;

        int transTypeIndex = 2;
        try {
            transTypeIndex = MainConst.TRANS_TYPE.indexOf(MainConst.slTrans[paymentRequest.TransType]);
        } catch (Exception e) {
        }
        int edcTypeIndex = 0;
        try {
            edcTypeIndex = MainConst.PAYMENT_EDC_TYPES.indexOf(MainConst.slTrend[paymentRequest.TenderType]);
        } catch (Exception e) {
        }

        sigBrowserEntity = new NameValueBrowserEntity(PaymentItemValueSetter.SIG_SAVE_PATH, "Browse", "", InputType.TYPE_CLASS_TEXT, "", new NameValueStringEntity.ClickCallback() {
            @Override
            public void onClick(View v, final NameValueStringEntity entity) {
                setActivityResultReceiver(new ActivityResultReceiver() {
                    @Override
                    public void onReceive(String data) {
                        entity.setValue(data);
                        UIBusiness.notifyDataSetChangeForContainer(requestRenderEntityList, requestContainer);
                    }
                });
                String Dir = entity.getValue();
                File f = new File(Dir);
                if (!f.exists()) {
                    Dir = Environment.getExternalStorageDirectory().toString();
                } else {
                    if (f.isFile())
                        Dir = Dir.substring(0, Dir.lastIndexOf("/"));
                }
                Intent intent = new Intent(getActivity(), DirectoryLogActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("FilePath", Dir);
                intent.putExtras(bundle);
                startActivityForResult(intent, Constant.MANAGE_SAVE_IMAGE_RESULT);
            }
        });
        tenderTypeEntity = new NameValueSelectEntity(PaymentItemValueSetter.EDC_TYPE, "", edcTypes, edcTypes, edcTypeIndex);
        transTypeEntity = new NameValueSelectEntity(PaymentItemValueSetter.TRANS_TYPE, "", transTypes, transTypes, transTypeIndex);
        List<NameValueEntity<String>> nameValueEntities = Arrays.asList(
               tenderTypeEntity,
               transTypeEntity,
                new NameValueStringEntity(PaymentItemValueSetter.AMOUNT, StringUtil.isEmpty(paymentRequest.Amount) ? "100" : paymentRequest.Amount, InputType.TYPE_CLASS_NUMBER, ""),
                new NameValueStringEntity(PaymentItemValueSetter.CASH_BACK_AMOUNT, paymentRequest.CashBackAmt, InputType.TYPE_CLASS_TEXT, ""),
                new NameValueStringEntity(PaymentItemValueSetter.CLERK_ID, paymentRequest.ClerkID, InputType.TYPE_CLASS_TEXT, ""),
                sigBrowserEntity,
                new NameValueStringEntity(PaymentItemValueSetter.ZIP, paymentRequest.Zip, InputType.TYPE_CLASS_TEXT, ""),
                new NameValueStringEntity(PaymentItemValueSetter.TIP_AMT, paymentRequest.TipAmt, InputType.TYPE_CLASS_TEXT, ""),
                new NameValueStringEntity(PaymentItemValueSetter.TAX_AMT, paymentRequest.TaxAmt, InputType.TYPE_CLASS_TEXT, ""),
                new NameValueStringEntity(PaymentItemValueSetter.FUEL_AMT, paymentRequest.FuelAmt, InputType.TYPE_CLASS_TEXT, ""),
                new NameValueStringEntity(PaymentItemValueSetter.STREET, paymentRequest.Street, InputType.TYPE_CLASS_TEXT, ""),
                new NameValueStringEntity(PaymentItemValueSetter.STREET2, paymentRequest.Street2, InputType.TYPE_CLASS_TEXT, ""),
                new NameValueStringEntity(PaymentItemValueSetter.SURCHARGE_AMT, paymentRequest.SurchargeAmt, InputType.TYPE_CLASS_TEXT, ""),
                new NameValueStringEntity(PaymentItemValueSetter.PO_NUM, paymentRequest.PONum, InputType.TYPE_CLASS_TEXT, ""),
                new NameValueStringEntity(PaymentItemValueSetter.ORIG_REF_NUM, paymentRequest.OrigRefNum, InputType.TYPE_CLASS_TEXT, ""),
                new NameValueStringEntity(PaymentItemValueSetter.INV_NUM, paymentRequest.InvNum, InputType.TYPE_CLASS_TEXT, ""),
                new NameValueStringEntity(PaymentItemValueSetter.ECR_REF_NUM, StringUtil.isEmpty(paymentRequest.ECRRefNum) ? "1" : paymentRequest.ECRRefNum, InputType.TYPE_CLASS_TEXT, ""),
                new NameValueStringEntity(PaymentItemValueSetter.ORIG_ECR_REF_NUM, paymentRequest.OrigECRRefNum, InputType.TYPE_CLASS_TEXT, ""),
                new NameValueStringEntity(PaymentItemValueSetter.ECR_Trans_ID, paymentRequest.ECRTransID, InputType.TYPE_CLASS_TEXT, ""),
                new NameValueStringEntity(PaymentItemValueSetter.AUTH_CODE, paymentRequest.AuthCode, InputType.TYPE_CLASS_TEXT, ""),
                new NameValueStringEntity(PaymentItemValueSetter.SERVICE_FEE, paymentRequest.ServiceFee, InputType.TYPE_CLASS_TEXT, ""),
                new NameValueStringEntity(PaymentItemValueSetter.GIFTCARDTYPE, paymentRequest.GiftCardType, InputType.TYPE_CLASS_TEXT, ""),
                new NameValueStringEntity(PaymentItemValueSetter.CVVBYPASSREASON, paymentRequest.CVVBypassReason, InputType.TYPE_CLASS_TEXT, ""),
                new NameValueStringEntity(PaymentItemValueSetter.GIFTTENDERTYPE,paymentRequest.GiftCardType,InputType.TYPE_CLASS_TEXT,""),
                new NameValueStringEntity(PaymentItemValueSetter.TRACENUMBER, paymentRequest.OrigTraceNum, InputType.TYPE_CLASS_TEXT, ""),
//                new NameValueStringEntity(PaymentItemValueSetter.AID_FILTER_RULE, "", InputType.TYPE_CLASS_TEXT, ""),
                new ExtDataEntity(PaymentItemValueSetter.EXT_DATA, paymentRequest.ExtData, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE, "", new NameValueStringEntity.ClickCallback() {
                    @Override
                    public void onClick(View v, final NameValueStringEntity entity) {
                        setActivityResultReceiver(new ActivityResultReceiver() {
                            @Override
                            public void onReceive(String data) {
                                entity.setValue(data);
                                UIBusiness.notifyDataSetChangeForContainer(requestRenderEntityList, requestContainer);
                            }
                        });

                        Intent intent = new Intent(getActivity(), PaymentExtDataActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString(BUNDLE_KEY_EXTDATA, entity.getValue());
                        intent.putExtras(bundle);
                        startActivityForResult(intent, Constant.PAYMENT_EXTDATA_RESULT);
                    }
                }),
                new ButtonEntity(PaymentItemValueSetter.COMMERCIALCARD, "", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE, "", new NameValueStringEntity.ClickCallback() {
                    @Override
                    public void onClick(View v, final NameValueStringEntity entity) {
                        setActivityResultReceiver(new ActivityResultReceiver() {
                            @Override
                            public void onReceive(String data) {
                                commercialJson = data;
                                entity.setValue(formatCommercialMsg(data));
                                entity.setRealData(commercialJson);
                                UIBusiness.notifyDataSetChangeForContainer(requestRenderEntityList, requestContainer);
                            }
                        });

                        Intent intent = new Intent(getActivity(), PaymentCommercialCardActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString(BUNDLE_KEY_PAYMENT_COMMERCIAL, commercialJson);
                        bundle.putString(BUNDLE_KEY_PAYMENT_TAXDETAIL_DISPLAY, taxDetailMsg);
                        bundle.putString(BUNDLE_KEY_PAYMENT_LINEITEM_DISPLAY, lineItemDetailMsg);
                        intent.putExtras(bundle);
                        startActivityForResult(intent, Constant.PAYMENT_COMMERCIAL_RESULT);
                    }
                }),

                new ButtonEntity(PaymentItemValueSetter.RESTAURANT, "", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE, "", new NameValueStringEntity.ClickCallback() {
                    @Override
                    public void onClick(View v, final NameValueStringEntity entity) {
                        setActivityResultReceiver(new ActivityResultReceiver() {
                            @Override
                            public void onReceive(String data) {
                                restaurantJson = data;
                                entity.setValue(formatRestaurant(data));
                                entity.setRealData(restaurantJson);
                                UIBusiness.notifyDataSetChangeForContainer(requestRenderEntityList, requestContainer);
                            }
                        });
                        Intent intent = new Intent(getActivity(), PaymentRestaurantActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString(BUNDLE_KEY_PAYMENT_RESTAURANT, restaurantJson);
                        intent.putExtras(bundle);
                        startActivityForResult(intent, RESTAURANT_RESULT);
                    }
                }),
                new ButtonEntity(PaymentItemValueSetter.HOST_GATEWAY, "", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE, "", new NameValueStringEntity.ClickCallback() {
                    @Override
                    public void onClick(View v, final NameValueStringEntity entity) {
                        setActivityResultReceiver(new ActivityResultReceiver() {
                            @Override
                            public void onReceive(String data) {
                                hostGateWayJson = data;
                                entity.setValue(formatHostGateWay(data));
                                entity.setRealData(hostGateWayJson);
                                UIBusiness.notifyDataSetChangeForContainer(requestRenderEntityList, requestContainer);
                            }
                        });
                        Intent intent = new Intent(getActivity(), PaymentHostGateWayActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString(BUNDLE_KEY_PAYMENT_HOST_GATEWAY, hostGateWayJson);
                        intent.putExtras(bundle);
                        startActivityForResult(intent, PAYMENT_HOST_GATEWAY_RESULT);
                    }
                }),
                new ButtonEntity(PaymentItemValueSetter.TRANSACTION_BEHAVIOR, "", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE, "", new NameValueStringEntity.ClickCallback() {
                    @Override
                    public void onClick(View v, final NameValueStringEntity entity) {
                        setActivityResultReceiver(new ActivityResultReceiver() {
                            @Override
                            public void onReceive(String data) {
                                transactionBehaviorJson = data;
                                entity.setValue(formatTransactionBehavior(data));
                                entity.setRealData(transactionBehaviorJson);
                                UIBusiness.notifyDataSetChangeForContainer(requestRenderEntityList, requestContainer);
                            }
                        });
                        Intent intent = new Intent(getActivity(), PaymentTransactionBehaviorActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString(BUNDLE_KEY_PAYMENT_TRANSACTION, transactionBehaviorJson);
                        intent.putExtras(bundle);
                        startActivityForResult(intent, PAYMENT_TRANSACTION_BEHAVIOR_RESULT);
                    }
                }),
                new ButtonEntity(PaymentItemValueSetter.ORIGINAL, "", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE, "", new NameValueStringEntity.ClickCallback() {
                    @Override
                    public void onClick(View v, final NameValueStringEntity entity) {
                        setActivityResultReceiver(new ActivityResultReceiver() {
                            @Override
                            public void onReceive(String data) {
                                originalJson = data;
                                entity.setValue(formatOriginal(data));
                                entity.setRealData(originalJson);
                                UIBusiness.notifyDataSetChangeForContainer(requestRenderEntityList, requestContainer);
                            }
                        });
                        Intent intent = new Intent(getActivity(), PaymentOriginalActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString(BUNDLE_KEY_PAYMENT_ORIGINAL, originalJson);
                        intent.putExtras(bundle);
                        startActivityForResult(intent, PAYMENT_ORIGINAL_RESULT);
                    }
                }),
                new ButtonEntity(PaymentItemValueSetter.FLEET_CARD, "", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE, "", new NameValueStringEntity.ClickCallback() {
                    @Override
                    public void onClick(View v, final NameValueStringEntity entity) {
                        setActivityResultReceiver(new ActivityResultReceiver() {
                            @Override
                            public void onReceive(String data) {
                                fleetCardJson = data;
                                entity.setValue(formatFleetCard(data));
                                entity.setRealData(fleetCardJson);
                                UIBusiness.notifyDataSetChangeForContainer(requestRenderEntityList, requestContainer);
                            }
                        });
                        Intent intent = new Intent(getActivity(), PaymentFleetCardActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString(BUNDLE_KEY_PAYMENT_FLEET_CARD, fleetCardJson);
                        intent.putExtras(bundle);
                        startActivityForResult(intent, PAYMENT_FLEET_CARD_RESULT);
                    }
                }),
                new ButtonEntity(PaymentItemValueSetter.MULTI_MERCHANT, "", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE, "", new NameValueStringEntity.ClickCallback() {
                    @Override
                    public void onClick(View v, final NameValueStringEntity entity) {
                        setActivityResultReceiver(new ActivityResultReceiver() {
                            @Override
                            public void onReceive(String data) {
                                multiMerchantJson = data;
                                entity.setValue(formatMultiMerchant(data));
                                entity.setRealData(multiMerchantJson);
                                UIBusiness.notifyDataSetChangeForContainer(requestRenderEntityList, requestContainer);
                            }
                        });
                        Intent intent = new Intent(getActivity(), MultiMerchantActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString(BUNDLE_KEY_PAYMENT_MULTI_MERCHANT, multiMerchantJson);
                        intent.putExtras(bundle);
                        startActivityForResult(intent, PAYMENT_MULTI_MERCHANT_RESULT);
                    }
                }),
                new ButtonEntity(PaymentItemValueSetter.LODGINGINFO, "", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE, "", new NameValueStringEntity.ClickCallback() {
                    @Override
                    public void onClick(View v, final NameValueStringEntity entity) {
                        setActivityResultReceiver(new ActivityResultReceiver() {
                            @Override
                            public void onReceive(String data) {
                                lodgingInfoJson = data;
                                entity.setValue(formatLodging(data));
                                entity.setRealData(lodgingInfoJson);
                                UIBusiness.notifyDataSetChangeForContainer(requestRenderEntityList, requestContainer);
                            }
                        });
                        Intent intent = new Intent(getActivity(), PaymentLodgingInfoActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString(BUNDLE_KEY_PAYMENT_LODGING, lodgingInfoJson);
                        bundle.putString(BUNDLE_KEY_PAYMENT_LODGING_ROOMS_DISPLAY, lodgingRoomRatesMsg);
                        bundle.putString(BUNDLE_KEY_PAYMENT_LODGING_ITEMS_DISPLAY, lodgingItemsMsg);
                        intent.putExtras(bundle);
                        startActivityForResult(intent, PAYMENT_LODGING_RESULT);
                    }
                }),
                new ButtonEntity(PaymentItemValueSetter.AUTORENTALINFO, "", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE, "", new NameValueStringEntity.ClickCallback() {
                    @Override
                    public void onClick(View v, final NameValueStringEntity entity) {
                        setActivityResultReceiver(new ActivityResultReceiver() {
                            @Override
                            public void onReceive(String data) {
                                autoRentalInfoJson = data;
                                entity.setValue(formatAutoRental(data));
                                entity.setRealData(autoRentalInfoJson);
                                UIBusiness.notifyDataSetChangeForContainer(requestRenderEntityList, requestContainer);
                            }
                        });
                        Intent intent = new Intent(getActivity(), PaymentAutoRentalInfoActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString(BUNDLE_KEY_PAYMENT_AUTO_RENTAL, autoRentalInfoJson);
                        bundle.putString(BUNDLE_KEY_PAYMENT_AUTO_RENTAL_ITEMS_DISPLAY, extraChargeItemsMsg);
                        intent.putExtras(bundle);
                        startActivityForResult(intent, PAYMENT_AUTO_RENTAL_ITEMS_RESULT);
                    }
                }),
                new NameValueStringEntity(PaymentItemValueSetter.CONTINUOUS_SCREEN, StringUtil.isEmpty(paymentRequest.ContinuousScreen) ? "0" : paymentRequest.ContinuousScreen, InputType.TYPE_CLASS_NUMBER, "Default 0, valid when set 1"),
                new ButtonEntity(PaymentItemValueSetter.HOSTCREDENTIALINFORMATION, "", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE, "", new NameValueStringEntity.ClickCallback() {
                    @Override
                    public void onClick(View v, final NameValueStringEntity entity) {
                        setActivityResultReceiver(new ActivityResultReceiver() {
                            @Override
                            public void onReceive(String data) {
                                hostCredentialInformationJson = data;
                                entity.setValue(formatHostCredentialInformation(data));
                                entity.setRealData(hostCredentialInformationJson);
                                UIBusiness.notifyDataSetChangeForContainer(requestRenderEntityList, requestContainer);
                            }
                        });
                        Intent intent = new Intent(getActivity(), PaymentHostCredentialInformationActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString(BUNDLE_KEY_PAYMENT_HOSTCREDENTIALINFORMATION, hostCredentialInformationJson);
                        intent.putExtras(bundle);
                        startActivityForResult(intent, PAYMENT_HOST_CREDENTIAL_INFORMATION_RESULT);
                    }
                }),
                new NameValueStringEntity(PaymentItemValueSetter.PAYLOAD_DATA, paymentRequest.PayloadData, InputType.TYPE_CLASS_TEXT, "")
        );
        requestRenderEntityList.addAll(nameValueEntities);
        for (RenderEntity renderEntity : requestRenderEntityList) {
            String name = ((NameValueEntity) renderEntity).getName();
            CommonItemView itemView = renderEntity.createView(requestContainer);
            requestContainer.addView(itemView.getView());
            itemView.getView().setTag(itemView);
            itemView.render(renderEntity);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constant.PAYMENT_EXTDATA_RESULT:
                switch (resultCode) {
                    case RESULT_OK:
                        onActivityResultReceive(data.getStringExtra("Payment_ExtData"));
                        break;
                    default:
                        break;
                }
                break;
            case Constant.MANAGE_SAVE_IMAGE_RESULT:
                switch (resultCode) {
                    case RESULT_OK:
                        onActivityResultReceive(data.getStringExtra("FilePath"));
                        break;
                    default:
                        break;
                }
                break;
            case Constant.PAYMENT_COMMERCIAL_RESULT:
                switch (resultCode) {
                    case RESULT_OK:
                        taxDetailMsg = data.getStringExtra(BUNDLE_KEY_PAYMENT_TAXDETAIL_DISPLAY);
                        lineItemDetailMsg = data.getStringExtra(BUNDLE_KEY_PAYMENT_LINEITEM_DISPLAY);
                        onActivityResultReceive(data.getStringExtra(BUNDLE_KEY_PAYMENT_COMMERCIAL));
                        break;
                    default:
                        break;
                }
                break;
            case Constant.RESTAURANT_RESULT:
                switch (resultCode) {
                    case RESULT_OK:
                        onActivityResultReceive(data.getStringExtra(BUNDLE_KEY_PAYMENT_RESTAURANT));
                        break;
                    default:
                        break;
                }
                break;
            case PAYMENT_HOST_GATEWAY_RESULT:
                switch (resultCode) {
                    case RESULT_OK:
                        onActivityResultReceive(data.getStringExtra(BUNDLE_KEY_PAYMENT_HOST_GATEWAY));
                        break;
                    default:
                        break;
                }
                break;
            case PAYMENT_TRANSACTION_BEHAVIOR_RESULT:
                switch (resultCode) {
                    case RESULT_OK:
                        onActivityResultReceive(data.getStringExtra(BUNDLE_KEY_PAYMENT_TRANSACTION));
                        break;
                    default:
                        break;
                }
                break;
            case PAYMENT_ORIGINAL_RESULT:
                switch (resultCode) {
                    case RESULT_OK:
                        onActivityResultReceive(data.getStringExtra(BUNDLE_KEY_PAYMENT_ORIGINAL));
                        break;
                    default:
                        break;
                }
                break;
            case PAYMENT_FLEET_CARD_RESULT:
                switch (resultCode) {
                    case RESULT_OK:
                        onActivityResultReceive(data.getStringExtra(BUNDLE_KEY_PAYMENT_FLEET_CARD));
                        break;
                    default:
                        break;
                }
                break;
            case PAYMENT_MULTI_MERCHANT_RESULT:
                switch (resultCode) {
                    case RESULT_OK:
                        onActivityResultReceive(data.getStringExtra(BUNDLE_KEY_PAYMENT_MULTI_MERCHANT));
                        break;
                    default:
                        break;
                }
                break;
            case PAYMENT_LODGING_RESULT:
                switch (resultCode) {
                    case RESULT_OK:
                        lodgingRoomRatesMsg = data.getStringExtra(BUNDLE_KEY_PAYMENT_LODGING_ROOMS_DISPLAY);
                        lodgingItemsMsg = data.getStringExtra(BUNDLE_KEY_PAYMENT_LODGING_ITEMS_DISPLAY);
                        onActivityResultReceive(data.getStringExtra(BUNDLE_KEY_PAYMENT_LODGING));
                        break;
                    default:
                        break;
                }
                break;
            case PAYMENT_AUTO_RENTAL_ITEMS_RESULT:
                switch (resultCode) {
                    case RESULT_OK:
                        extraChargeItemsMsg = data.getStringExtra(BUNDLE_KEY_PAYMENT_AUTO_RENTAL_ITEMS_DISPLAY);
                        onActivityResultReceive(data.getStringExtra(BUNDLE_KEY_PAYMENT_AUTO_RENTAL));
                        break;
                    default:
                        break;
                }
                break;
            case PAYMENT_HOST_CREDENTIAL_INFORMATION_RESULT:
                switch (resultCode) {
                    case RESULT_OK:
                        onActivityResultReceive(data.getStringExtra(BUNDLE_KEY_PAYMENT_HOSTCREDENTIALINFORMATION));
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onClickProcessBtn(View v) {
        preRequest(getRequest());
    }

    private void setPaymentRequest(PaymentRequest request) {
        for (RenderEntity renderEntity : requestRenderEntityList) {
            if (renderEntity instanceof NameValueSelectEntity) {
                NameValueSelectEntity entity = (NameValueSelectEntity) renderEntity;
                PaymentItemValueSetter valueSetter = PaymentItemValueSetter.VALUE_SETTER_MAP.get(entity.getName());
                valueSetter.onSet(request, entity.getItemValues().get(entity.getSelectedItem()));

            } else if (renderEntity instanceof NameValueStringEntity) {
                NameValueStringEntity nameValueStringEntity = (NameValueStringEntity) renderEntity;
                PaymentItemValueSetter valueSetter = PaymentItemValueSetter.VALUE_SETTER_MAP.get(nameValueStringEntity.getName());
                if (!TextUtils.isEmpty(nameValueStringEntity.getRealData())) {
                    valueSetter.onSet(request, nameValueStringEntity.getRealData());
                } else {
                    valueSetter.onSet(request, nameValueStringEntity.getValue());
                }
            }
        }
    }

    @Override
    public PaymentRequest getRequest() {
        PaymentRequest payrequest = new PaymentRequest();
        setPaymentRequest(payrequest);
        return payrequest;
    }

    public String getSelectedTenderType() {
        return tenderTypeEntity.getItemValues().get(tenderTypeEntity.getSelectedItem());
    }

    public String getSelectedTransType() {
        return transTypeEntity.getItemValues().get(transTypeEntity.getSelectedItem());
    }

    public String getSigBrowserValue() {
        return sigBrowserEntity.getValue();
    }

    public void updateSigBrowserIfNeeded(String value) {
        sigBrowserEntity.setValue(value);
        UIBusiness.notifyDataSetChangeForContainer(requestRenderEntityList, requestContainer);
    }

    private String formatCommercialMsg(String commercialJson) {
        String result = "";
        Gson gson = new Gson();
        PaymentRequest.CommercialCard commercialCard = gson.fromJson(commercialJson, PaymentRequest.CommercialCard.class);
        result += toFormat(PaymentCommercialValueSetter.PO_NUMBER, commercialCard.PONumber);
        result += toFormat(PaymentCommercialValueSetter.CUSTOMER_CODE, commercialCard.CustomerCode);
        result += toFormat(PaymentCommercialValueSetter.TAX_EXEMPT, commercialCard.TaxExempt);
        result += toFormat(PaymentCommercialValueSetter.TAX_EXEMPT_ID, commercialCard.TaxExemptID);
        result += toFormat(PaymentCommercialValueSetter.MERCHANT_TAX_ID, commercialCard.MerchantTaxID);
        result += toFormat(PaymentCommercialValueSetter.DESTINATION_ZIP_CODE, commercialCard.DestinationZipCode);
        result += toFormat(PaymentCommercialValueSetter.PRODUCT_DESCRIPTION, commercialCard.ProductDescription);
        result += toFormat(PaymentCommercialValueSetter.SHIP_FROM_ZIP_CODE, commercialCard.ShipFromZipCode);
        result += toFormat(PaymentCommercialValueSetter.DESTINATION_COUNTRY_CODE, commercialCard.DestinationCountryCode);
        result += toFormat(PaymentCommercialValueSetter.TAX_DETAIL, taxDetailMsg);
        result += toFormat(PaymentCommercialValueSetter.SUMMARY_COMMODITY_CODE, commercialCard.SummaryCommodityCode);
        result += toFormat(PaymentCommercialValueSetter.DISCOUNT_AMOUNT, commercialCard.DiscountAmount);
        result += toFormat(PaymentCommercialValueSetter.FREIGHT_AMOUNT, commercialCard.FreightAmount);
        result += toFormat(PaymentCommercialValueSetter.DUTY_AMOUNT, commercialCard.DutyAmount);
        result += toFormat(PaymentCommercialValueSetter.ORDER_DATE, commercialCard.OrderDate);
        result += toFormat(PaymentCommercialValueSetter.LINEITEMDETAIL, lineItemDetailMsg);
        result += toFormat(PaymentCommercialValueSetter.SHIPPING_COMPANY, commercialCard.ShippingCompany);
        result += toFormat(PaymentCommercialValueSetter.SHIPPING_TRACKING_NUMBER, commercialCard.ShippingTrackingNumber);
        result += toFormat(PaymentCommercialValueSetter.ADDITIONAL_FEES, commercialCard.AdditionalFees);
        return result;
    }

    private String formatRestaurant(String restaurantJson) {
        String result = "";
        Gson gson = new Gson();
        Restaurant restaurant = gson.fromJson(restaurantJson, Restaurant.class);
        result += toFormat(RestaurantValueSetter.TABLE_NUMBER, restaurant.TableNumber);
        result += toFormat(RestaurantValueSetter.GUEST_NUMBER, restaurant.GuestNumber);
        result += toFormat(RestaurantValueSetter.TICKET_NUMBER, restaurant.TicketNumber);
        return result;
    }

    private String formatHostGateWay(String hostGateWayJson) {
        String result = "";
        Gson gson = new Gson();
        PaymentRequest.HostGateWay hostGateWay = gson.fromJson(hostGateWayJson, PaymentRequest.HostGateWay.class);
        result += toFormat(PaymentHostGateWayValueSetter.HREF, hostGateWay.HRef);
        result += toFormat(PaymentHostGateWayValueSetter.GATEWAYID, hostGateWay.GatewayId);
        result += toFormat(PaymentHostGateWayValueSetter.TOKENREQUEST, hostGateWay.TokenRequestFlag);
        result += toFormat(PaymentHostGateWayValueSetter.TOKEN, hostGateWay.Token);
        result += toFormat(PaymentHostGateWayValueSetter.CARDTYPE, hostGateWay.CardType);
        result += toFormat(PaymentHostGateWayValueSetter.PASSTHRUDATA, hostGateWay.PassThruData);
        result += toFormat(PaymentHostGateWayValueSetter.RETURNREASON, hostGateWay.ReturnReason);
        result += toFormat(PaymentHostGateWayValueSetter.STATIONNO, hostGateWay.StationId);
        result += toFormat(PaymentHostGateWayValueSetter.GLOBALUID, hostGateWay.GlobalUid);
        result += toFormat(PaymentHostGateWayValueSetter.CUSTOMIZEDATA1, hostGateWay.CustomizeData1);
        result += toFormat(PaymentHostGateWayValueSetter.CUSTOMIZEDATA2, hostGateWay.CustomizeData2);
        result += toFormat(PaymentHostGateWayValueSetter.CUSTOMIZEDATA3, hostGateWay.CustomizeData3);
        result += toFormat(PaymentHostGateWayValueSetter.EWICDISCOUNTAMOUNT, hostGateWay.EwicDiscountAmount);
        result += toFormat(PaymentHostGateWayValueSetter.TOKENSERIALNUMBER, hostGateWay.TokenSerialNum);
        result += toFormat(PaymentHostGateWayValueSetter.STATEMENTDESCRIPTOR, hostGateWay.StatementDescriptor);
        return result;
    }

    private String formatTransactionBehavior(String transactionBehaviorJson) {
        String result = "";
        Gson gson = new Gson();
        PaymentRequest.TransactionBehavior transactionBehavior = gson.fromJson(transactionBehaviorJson, PaymentRequest.TransactionBehavior.class);
        result += toFormat(PaymentTransactionBehaviorValueSetter.SIGN, transactionBehavior.SignatureCaptureFlag);
        result += toFormat(PaymentTransactionBehaviorValueSetter.TIPREQ, transactionBehavior.TipRequestFlag);
        result += toFormat(PaymentTransactionBehaviorValueSetter.SIGNUPLOAD, transactionBehavior.SignatureUploadFlag);
        result += toFormat(PaymentTransactionBehaviorValueSetter.REPORTSTATUS, transactionBehavior.StatusReportFlag);
        result += toFormat(PaymentTransactionBehaviorValueSetter.CARDTYPEBITMAP, transactionBehavior.AcceptedCardType);
        result += toFormat(PaymentTransactionBehaviorValueSetter.DISPROGPROMPTS, transactionBehavior.ProgramPromptsFlag);
        result += toFormat(PaymentTransactionBehaviorValueSetter.GETSIGN, transactionBehavior.SignatureAcquireFlag);
        result += toFormat(PaymentTransactionBehaviorValueSetter.ENTRYMODEBITMAP, transactionBehavior.EntryMode);
        result += toFormat(PaymentTransactionBehaviorValueSetter.RECEIPTPRINT, transactionBehavior.ReceiptPrintFlag);
        result += toFormat(PaymentTransactionBehaviorValueSetter.CPMODE, transactionBehavior.CardPresentMode);
        result += toFormat(PaymentTransactionBehaviorValueSetter.DEBITNETWORK, transactionBehavior.DebitNetwork);
        result += toFormat(PaymentTransactionBehaviorValueSetter.USERLANGUAGE, transactionBehavior.UserLanguage);
        result += toFormat(PaymentTransactionBehaviorValueSetter.ADDLRSPDATAREQUEST, transactionBehavior.AddlRspDataFlag);
        result += toFormat(PaymentTransactionBehaviorValueSetter.FORCEFSA, transactionBehavior.ForceFsa);
        result += toFormat(PaymentTransactionBehaviorValueSetter.FORCECC, transactionBehavior.ForceCC);
        result += toFormat(PaymentTransactionBehaviorValueSetter.FORCE, transactionBehavior.ForceDuplicate);
        result += toFormat(PaymentTransactionBehaviorValueSetter.ACCESSIBILITYPINPAD, transactionBehavior.AccessibilityPinPad);
        result += toFormat(PaymentTransactionBehaviorValueSetter.DISTRANSPROMPTS, transactionBehavior.DistransPrompts);
        result += toFormat(PaymentTransactionBehaviorValueSetter.COFINDICATOR, transactionBehavior.CoFIndicator);
        result += toFormat(PaymentTransactionBehaviorValueSetter.COFINITIATOR, transactionBehavior.CoFInitiator);
        result += toFormat(PaymentTransactionBehaviorValueSetter.GIFTCARDINDICATOR, transactionBehavior.GiftCardIndicator);

        return result;
    }

    private String formatOriginal(String originalJson) {
        String result = "";
        Gson gson = new Gson();
        PaymentRequest.Original original = gson.fromJson(originalJson, PaymentRequest.Original.class);
        result += toFormat(PaymentOriginalValueSetter.ORIGEXPIRYDATE, original.TransDate);
        result += toFormat(PaymentOriginalValueSetter.ORIGPAN, original.Pan);
        result += toFormat(PaymentOriginalValueSetter.ORIGEXPIRYDATE, original.ExpiryDate);
        result += toFormat(PaymentOriginalValueSetter.ORIGTRANSTIME, original.TransTime);
        result += toFormat(PaymentOriginalValueSetter.ORIGSETTLEMENTDATE, original.SettlementDate);
        result += toFormat(PaymentOriginalValueSetter.ORIGTRANSTYPE, original.TransType);
        result += toFormat(PaymentOriginalValueSetter.ORIGAMOUNT, original.Amount);
        result += toFormat(PaymentOriginalValueSetter.ORIGBATCHNUMBER, original.BatchNumber);
        result += toFormat(PaymentOriginalValueSetter.ORIGTRANSID, original.TransId);
        result += toFormat(PaymentOriginalValueSetter.PS2000, original.PaymentService2000);
        result += toFormat(PaymentOriginalValueSetter.AUTH_RESPONSE, original.AuthorizationResponse);
        result += toFormat(PaymentOriginalValueSetter.TRANSACTIONIDENTIFIER, original.TransactionIdentifier);

        return result;
    }

    private String formatFleetCard(String fleetCardJson) {
        String result = "";
        Gson gson = new Gson();
        FleetCardRequest fleetCard = gson.fromJson(fleetCardJson, FleetCardRequest.class);
        result += toFormat(PaymentFleetCardValueSetter.ODOMETER, fleetCard.Odometer);
        result += toFormat(PaymentFleetCardValueSetter.VEHICLENUMBER, fleetCard.VehicleNumber);
        result += toFormat(PaymentFleetCardValueSetter.JOBNUMBER, fleetCard.JobNumber);
        result += toFormat(PaymentFleetCardValueSetter.DRIVERID, fleetCard.DriverId);
        result += toFormat(PaymentFleetCardValueSetter.EMPLOYEENUMBER, fleetCard.EmployeeNumber);
        result += toFormat(PaymentFleetCardValueSetter.LICENSENUMBER, fleetCard.LicenseNumber);
        result += toFormat(PaymentFleetCardValueSetter.JOBID, fleetCard.JobId);
        result += toFormat(PaymentFleetCardValueSetter.DEPARTMENTNUMBER, fleetCard.DepartmentNumber);
        result += toFormat(PaymentFleetCardValueSetter.CUSTOMERDATA, fleetCard.CustomerData);
        result += toFormat(PaymentFleetCardValueSetter.USERID, fleetCard.UserId);
        result += toFormat(PaymentFleetCardValueSetter.VEHICLEID, fleetCard.VehicleId);
        result += toFormat(PaymentFleetCardValueSetter.FLEETPROMTPCODE, fleetCard.FleetPromptCode);
        result += toFormat(PaymentFleetCardValueSetter.VEHICLEUSAGECODE, fleetCard.VehicleUsageCode);
        result += toFormat(PaymentFleetCardValueSetter.HUBOMETER, fleetCard.Hubometer);
        result += toFormat(PaymentFleetCardValueSetter.MAINTENANCEID, fleetCard.MainTenanceId);
        result += toFormat(PaymentFleetCardValueSetter.FLEETPONUMBER, fleetCard.FleetPoNumber);
        result += toFormat(PaymentFleetCardValueSetter.REEFERHOURS, fleetCard.ReeferHours);
        result += toFormat(PaymentFleetCardValueSetter.RESTRICTIONCODE, fleetCard.RestrictionCode);
        result += toFormat(PaymentFleetCardValueSetter.TRAILERID, fleetCard.TrailerId);
        result += toFormat(PaymentFleetCardValueSetter.TRIPNUMBER, fleetCard.TripNumber);
        result += toFormat(PaymentFleetCardValueSetter.UNITID, fleetCard.UnitId);
        result += toFormat(PaymentFleetCardValueSetter.ADDITIONALFLEETDATA1, fleetCard.AdditionalFleetData1);
        result += toFormat(PaymentFleetCardValueSetter.ADDITIONALFLEETDATA2, fleetCard.AdditionalFleetData2);

        return result;
    }

    private String formatMultiMerchant(String fleetCardJson) {
        String result = "";
        Gson gson = new Gson();
        MultiMerchant multiMerchant = gson.fromJson(fleetCardJson, MultiMerchant.class);
        result += toFormat(MultiMerchantValueSetter.MM_ID, multiMerchant.Id);
        result += toFormat(MultiMerchantValueSetter.MM_NAME, multiMerchant.Name);

        return result;
    }

    private String formatLodging(String lodgingInfoJson) {
        String result = "";
        Gson gson = new Gson();
        PaymentRequest.LodgingInfo lodgingInfo = gson.fromJson(lodgingInfoJson, PaymentRequest.LodgingInfo.class);
        result += toFormat(PaymentLodgingInfoValueSetter.ROOMNUMBER, lodgingInfo.RoomNumber);
        result += toFormat(PaymentLodgingInfoValueSetter.FOLIONUMBER, lodgingInfo.FolioNumber);
        result += toFormat(PaymentLodgingInfoValueSetter.ROOMRATES, lodgingRoomRatesMsg);
        result += toFormat(PaymentLodgingInfoValueSetter.CHARGETYPE, lodgingInfo.ChargeType);
        result += toFormat(PaymentLodgingInfoValueSetter.NOSHOWFLAG, lodgingInfo.NoShowFlag);
        result += toFormat(PaymentLodgingInfoValueSetter.CHECKINDATE, lodgingInfo.CheckInDate);
        result += toFormat(PaymentLodgingInfoValueSetter.CHECKOUTDATE, lodgingInfo.CheckOutDate);
        result += toFormat(PaymentLodgingInfoValueSetter.SPECIALPROGRAMCODE, lodgingInfo.SpecialProgramCode);
        result += toFormat(PaymentLodgingInfoValueSetter.DEPARTUREADJUSTEDAMOUNT, lodgingInfo.DepartureAdjustedAmount);
        result += toFormat(PaymentLodgingInfoValueSetter.LODGINGITEMS, lodgingItemsMsg);

        return result;
    }

    private String formatAutoRental(String autoRentalInfoJson) {
        String result = "";
        Gson gson = new Gson();
        PaymentRequest.AutoRentalInfo autoRentalInfo = gson.fromJson(autoRentalInfoJson, PaymentRequest.AutoRentalInfo.class);
        result += toFormat(PaymentAutoRentalInfoValueSetter.AGREEMENTNUMBER, autoRentalInfo.AgreementNumber);
        result += toFormat(PaymentAutoRentalInfoValueSetter.DAILYRATE, autoRentalInfo.DailyRate);
        result += toFormat(PaymentAutoRentalInfoValueSetter.RENTALDURATION, autoRentalInfo.RentalDuration);
        result += toFormat(PaymentAutoRentalInfoValueSetter.INSURANCEAMOUNT, autoRentalInfo.InsuranceAmount);
        result += toFormat(PaymentAutoRentalInfoValueSetter.ALLOCATEDMILES, autoRentalInfo.AllocatedMiles);
        result += toFormat(PaymentAutoRentalInfoValueSetter.MILERATE, autoRentalInfo.MileRate);
        result += toFormat(PaymentAutoRentalInfoValueSetter.NAME, autoRentalInfo.Name);
        result += toFormat(PaymentAutoRentalInfoValueSetter.DRIVERLICENSENUMBER, autoRentalInfo.DriverLicenseNumber);
        result += toFormat(PaymentAutoRentalInfoValueSetter.RENTALPROGRAMTYPE, autoRentalInfo.RentalProgramType);
        result += toFormat(PaymentAutoRentalInfoValueSetter.PICKUPLOCATIONNAME, autoRentalInfo.PickupLocationName);
        result += toFormat(PaymentAutoRentalInfoValueSetter.PICKUPCITY, autoRentalInfo.PickupCity);
        result += toFormat(PaymentAutoRentalInfoValueSetter.PICKUPSTATE, autoRentalInfo.PickupState);
        result += toFormat(PaymentAutoRentalInfoValueSetter.PICKUPCOUNTRYCODE, autoRentalInfo.PickupCountryCode);
        result += toFormat(PaymentAutoRentalInfoValueSetter.PICKUPDATETIME, autoRentalInfo.PickupDatetime);
        result += toFormat(PaymentAutoRentalInfoValueSetter.RETURNLOCATION, autoRentalInfo.ReturnLocation);
        result += toFormat(PaymentAutoRentalInfoValueSetter.RETURNCITY, autoRentalInfo.ReturnCity);
        result += toFormat(PaymentAutoRentalInfoValueSetter.RETURNSTATE, autoRentalInfo.ReturnState);
        result += toFormat(PaymentAutoRentalInfoValueSetter.RETURNCOUNTRYCODE, autoRentalInfo.ReturnCountryCode);
        result += toFormat(PaymentAutoRentalInfoValueSetter.RETURNDATETIME, autoRentalInfo.ReturnDatetime);
        result += toFormat(PaymentAutoRentalInfoValueSetter.TOTALMILES, autoRentalInfo.TotalMiles);
        result += toFormat(PaymentAutoRentalInfoValueSetter.CUSTOMERTAXID, autoRentalInfo.CustomerTaxID);
        result += toFormat(PaymentAutoRentalInfoValueSetter.VEHICLECLASSID, autoRentalInfo.VehicleClassID);
        result += toFormat(PaymentAutoRentalInfoValueSetter.EXTRACHARGESAMOUNT, autoRentalInfo.ExtraChargesAmount);
        result += toFormat(PaymentAutoRentalInfoValueSetter.EXTRACHARGEITEMS, extraChargeItemsMsg);

        return result;
    }

    private String formatHostCredentialInformation(String hostCredentialInformationJson) {
        String result = "";
        Gson gson = new Gson();
        PaymentRequest.HostCredentialInformation hostCredentialInformation = gson.fromJson(hostCredentialInformationJson, PaymentRequest.HostCredentialInformation.class);
        result += toFormat(PaymentHostCredentialInformationSetter.MID, hostCredentialInformation.MID);
        result += toFormat(PaymentHostCredentialInformationSetter.SERVICEUSER, hostCredentialInformation.ServiceUser);
        result += toFormat(PaymentHostCredentialInformationSetter.SERVICEPASSWORD, hostCredentialInformation.ServicePassword);
        result += toFormat(PaymentHostCredentialInformationSetter.USERNAME, hostCredentialInformation.UserName);
        result += toFormat(PaymentHostCredentialInformationSetter.PASSWORD, hostCredentialInformation.Password);

        return result;
    }

    private String toFormat(String name, String value) {
        String result = "";
        if (TextUtils.isEmpty(value))
            return result;
        result += name + ": " + value + "\n";
        return result;
    }
}
