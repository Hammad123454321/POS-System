package com.pax.poslink.ui.comsetting;

/**
 * Created by linhb on 2015-09-16.
 */

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.fragment.app.DialogFragment;

import com.pax.poslink.R;


public class GetDeviceIPDialogFragment extends DialogFragment {

    private EditText mEditTermId = null;
    private EditText mEditSn = null;
    private EditText mEditToken = null;
    private OnGetDeviceIPDialogListener onGetDeviceIPDialogListener;

    public interface OnGetDeviceIPDialogListener
    {
        void onGetDeviceIPDialogComplete(String termId, String sn, String token);
    }

    public static GetDeviceIPDialogFragment newInstance(String title, String termId, String sn) {
        GetDeviceIPDialogFragment frag = new GetDeviceIPDialogFragment();
        Bundle args = new Bundle();
        args.putString("input_title", title);
        args.putString("input_termId", termId);
        args.putString("input_sn", sn);
        frag.setArguments(args);
        return frag;
    }

    public void setOnGetDeviceIPDialogListener(OnGetDeviceIPDialogListener onGetDeviceIPDialogListener) {
        this.onGetDeviceIPDialogListener = onGetDeviceIPDialogListener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder inputDialog = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View inputView = inflater.inflate(R.layout.input_dialog, null);

        mEditTermId = (EditText) inputView.findViewById(R.id.input_termId);
        mEditTermId.setText(getArguments().getString("input_termId"));

        mEditSn = (EditText) inputView.findViewById(R.id.input_SN);
        mEditSn.setText(getArguments().getString("input_sn"));
        mEditToken = (EditText) inputView.findViewById(R.id.input_token);

        inputDialog.setTitle(getArguments().getString("input_title"));
        inputDialog.setIcon(android.R.drawable.ic_dialog_info);
        inputDialog.setView(inputView);
        inputDialog.setPositiveButton(R.string.input_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int i) {

            }
        });
        inputDialog.setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int i) {
                OnGetDeviceIPDialogListener listener = onGetDeviceIPDialogListener ;
                listener.onGetDeviceIPDialogComplete(mEditTermId.getText().toString(), mEditSn.getText().toString(),
                        mEditToken.getText().toString());
            }
        });
        return inputDialog.create();
    }
}
