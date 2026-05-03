package com.pax.poslink.util.view;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import com.pax.poslink.internal.Convenience;
import com.pax.poslink.widget.EnterValueDialog;

public class DialogUtils {

    public static void showMsgDialog(final Context context, final String title, String msg) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle(title);
        dialog.setMessage(msg);
        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        dialog.show();
    }

    public static AlertDialog showWarnDialog(final Context context, final String title, String msg, boolean cancelable) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle(title);
        dialog.setMessage(msg);
        dialog.setCancelable(cancelable);
        return dialog.show();
    }

    public static AlertDialog showListDialog(final Context context, final String title, final String[] strList,
                                             final DialogInterface.OnClickListener listener, DialogInterface.OnCancelListener cancelListener) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle(title);
        dialog.setCancelable(true);
        dialog.setOnCancelListener(cancelListener);
        dialog.setItems(strList, listener);
        return dialog.show();
    }

    public static void showDatePickerDialog(final Context context, final String title,
            final DatePickerDialog.OnDateSetListener listener, int year, int month, int day) {
        DatePickerDialog dialog = new DatePickerDialog(context, listener, year, month, day);
        dialog.setTitle(title);
        dialog.show();
    }


    public static AlertDialog showEditTextDialog(Context context, String title, String value, String okStr, String cancelStr, int inputType, String hint, final EditTextDialogCallback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final EditText editText = new EditText(context);
        editText.setText(value);
        editText.setSelectAllOnFocus(true);
        editText.setInputType(inputType);
        editText.setMaxLines(1);
        editText.setHint(hint);
        editText.setHintTextColor(Color.parseColor("#77808080"));
        builder.setTitle(title);
        builder.setCancelable(true);
        builder.setPositiveButton(okStr, null);
        builder.setNegativeButton(cancelStr, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                callback.onCancel(editText);
            }
        });
        builder.setView(editText);
        final AlertDialog dialog = builder.create();
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focused) {
                if (focused) {
                    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setFocusable(false);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setFocusable(false);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onOK(dialog, editText);
            }
        });
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface di, int keyCode, KeyEvent event) {
                if ((event.getAction()) == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_ENTER) {
                        callback.onOK(dialog, editText);
                        return true;
                    }
                }
                return false;
            }
        });
        return dialog;
    }

    public static Dialog showEnterValueDialog(Context context, String title, int inputType,
                                              String hint, final EnterValueEditTextDialogCallback callback, boolean hideInputArea) {
        final EnterValueDialog enterValueDialog = new EnterValueDialog(context);
        enterValueDialog.show();
        enterValueDialog.setTitle(title);
        enterValueDialog.setInputType(inputType);
        enterValueDialog.setHint(hint);
        if (hideInputArea) enterValueDialog.hideEditText();
        enterValueDialog.setOkOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onOK(enterValueDialog, enterValueDialog.getValue());
            }
        });
        enterValueDialog.setCancelOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onCancel();
            }
        });
        enterValueDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface di, int keyCode, KeyEvent event) {
                if ((event.getAction()) == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_ENTER) {
                        callback.onOK(enterValueDialog, enterValueDialog.getValue());
                        return true;
                    }
                }
                return false;
            }
        });
        return enterValueDialog;
    }

    public static Dialog showEnterValueWithSkipDialog(Context context, String title, int inputType,
                                              String hint, final EnterValueWithSkipCallback callback) {
        final EnterValueDialog enterValueDialog = new EnterValueDialog(context);
        enterValueDialog.needSendArea(true);
        enterValueDialog.show();
        enterValueDialog.setTitle(title);
        enterValueDialog.setInputType(inputType);
        enterValueDialog.setHint(hint);
//        if (supportSkip) enterValueDialog.showSkipButton();
//        if (hideInputArea) enterValueDialog.hideEditText();
        enterValueDialog.setOkOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onOK(enterValueDialog, enterValueDialog.getValue());
            }
        });
        if (Convenience.isButtonClickEnough()) {
            enterValueDialog.showTestArea();
        }
        enterValueDialog.setCancelOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onCancel();
            }
        });
        enterValueDialog.setSkipOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onSkip(enterValueDialog);
            }
        });
        enterValueDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface di, int keyCode, KeyEvent event) {
                if ((event.getAction()) == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_ENTER) {
                        callback.onOK(enterValueDialog, enterValueDialog.getValue());
                        return true;
                    }
                }
                return false;
            }
        });
        return enterValueDialog;
    }

    public static Dialog showEnterValueDialog(Context context, String title, int inputType,
                                              String hint, final EnterValueEditTextDialogCallback callback) {
        return showEnterValueDialog(context, title, inputType, hint, callback, false);
    }

    public interface EditTextDialogCallback {
        void onOK(Dialog dialog, EditText editText);

        void onCancel(EditText editText);
    }

    public interface EnterValueEditTextDialogCallback {
        void onOK(Dialog dialog, String msg);

        void onCancel();
    }

    public interface EnterValueWithSkipCallback {
        void onOK(Dialog dialog, String msg);

        void onCancel();

        void onSkip(Dialog dialog);
    }


    public static AlertDialog showPINDialog(Context context, String title, int inputType, String hint, final PinDialogCallback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final EditText editText = new EditText(context);
        editText.setSelectAllOnFocus(true);
        editText.setInputType(inputType);
        editText.setHint(hint);
        editText.setHintTextColor(Color.parseColor("#77808080"));
        builder.setTitle(title);
        builder.setCancelable(true);
        builder.setView(editText);
        final AlertDialog dialog = builder.create();
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focused) {
                if (focused) {
                    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });
        callback.onStart(dialog, editText);
        return dialog;
    }

    public interface PinDialogCallback {
        void onStart(AlertDialog dialog, EditText editText);
    }
}
