package com.pax.poslink.ui.logsetting;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.pax.poslink.LogSetting;
import com.pax.poslink.PosLink;
import com.pax.poslink.R;
import com.pax.poslink.SettingINI;
import com.pax.poslink.dal.print.ProcessingDialog;
import com.pax.poslink.entity.UploadResult;
import com.pax.poslink.internal.Convenience;
import com.pax.poslink.model.logsetting.LogSettingValueSetter;
import com.pax.poslink.ui.DirectoryLogActivity;
import com.pax.poslink.ui.base.BaseFragment;
import com.pax.poslink.util.Constant;
import com.pax.poslink.util.adapter.CommonItemView;
import com.pax.poslink.util.adapter.RenderEntity;
import com.pax.poslink.util.thread.AppThreadPool;
import com.pax.poslink.view.NameStringWithUnitEntity;
import com.pax.poslink.view.NameValueBrowserEntity;
import com.pax.poslink.view.NameValueEntity;
import com.pax.poslink.view.NameValueSelectEntity;
import com.pax.poslink.view.NameValueStringEntity;
import com.pax.poslink.widget.MsgDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Leon.F on 2018/6/8.
 */
public class LogSettingFragment extends BaseFragment {

    public static final List<String> SWITCH_OPTIONS = Arrays.asList("ON", "OFF");
    public static final List<String> LEVEL_OPTIONS = Arrays.asList(LogSetting.LOGLEVEL.ERROR.name(), LogSetting.LOGLEVEL.DEBUG.name());
    private View rootView;
    private ViewGroup containerView;
    private List<RenderEntity> renderEntityList = new ArrayList<>();
    private String filePath;
    private String[] filePaths;
    private TextView logPathsDisplay;

    public static LogSettingFragment newInstance() {
        Bundle args = new Bundle();
        LogSettingFragment fragment = new LogSettingFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_log_settings, container, false);
        initView(rootView);
        return rootView;
    }

    private void initView(View rootView) {
        filePath = LogSetting.getOutputPath();
        filePaths = new String[]{};
        containerView = (ViewGroup) rootView.findViewById(R.id.log_setting_container);
        String logSettingIniFile = getActivity().getApplicationContext().getFilesDir().getAbsolutePath() + "/" + SettingINI.FILENAME;
        if (!SettingINI.loadSettingFromFile(logSettingIniFile)) {
            LogSetting.setLogMode(true);
            LogSetting.setLevel(LogSetting.LOGLEVEL.DEBUG);
            SettingINI.saveLogSettingToFile(logSettingIniFile);
        }

        View mBtnSet = rootView.findViewById(R.id.log_set);
        mBtnSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Convenience.clickBtn();
                setLogs();
            }
        });

        View mBtnViewLog = rootView.findViewById(R.id.log_view);
        logPathsDisplay = rootView.findViewById(R.id.tv_display_data);
        logPathsDisplay.setMovementMethod(new ScrollingMovementMethod());
        mBtnViewLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewLogFile();
            }
        });
        rootView.findViewById(R.id.log_select).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), LogsViewerActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("LogsPath", LogSetting.getOutputPath());
                bundle.putString("SelectMode", "1");
                intent.putExtras(bundle);
//                        startActivity(intent);
                startActivityForResult(intent, 1010);
            }
        });
        rootView.findViewById(R.id.log_upload).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProcessingDialog[] processingDialog = new ProcessingDialog[1];
                final String[] endMsg = new String[1];
                AppThreadPool.getInstance().runInBackground(new Runnable() {
                    @Override
                    public void run() {
                        AppThreadPool.getInstance().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                final ProgressDialog progressDialog = new ProgressDialog(getContext());
                                processingDialog[0] = new ProcessingDialog(progressDialog);
                                processingDialog[0].start(getContext().getString(R.string.processing), false);
                            }
                        });
                        PosLink posLink = new PosLink(getContext().getApplicationContext());
                        posLink.appDataFolder = getContext().getFilesDir().getAbsolutePath();
                        posLink.SetCommSetting(SettingINI.getCommSettingFromFile(posLink.appDataFolder + "/" + SettingINI.FILENAME));
                        UploadResult upLoadResult = posLink.uploadLog(filePaths);
                        processingDialog[0].dismiss();
                        endMsg[0] = "Successful: " + upLoadResult.isSuccessful + "\n"
                                + "SN: " + upLoadResult.sn + "\n"
                                + "posLinkUploadErrorCode: " + upLoadResult.posLinkUploadErrorCode + "\n"
                                + "terminalUploadErrorCode: " + upLoadResult.terminalUploadErrorCode;
                        AppThreadPool.getInstance().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                processingDialog[0].dismiss();
                                MsgDialog dialog = new MsgDialog(getActivity(), endMsg[0]);
                                dialog.show();
                            }
                        });
                    }
                });
            }
        });
        updateList();
        updateLogPaths();
    }

    private void updateLogPaths() {
        String display = "";
        if (filePaths.length == 0) {
            display = "";
        } else {
            for (String path : filePaths) {
                display += path;
                display += "\n";
            }
        }
        logPathsDisplay.setText(display);
    }

    private void updateList() {
        List<NameValueEntity<String>> nameValueEntities = Arrays.asList(
                new NameValueSelectEntity(LogSettingValueSetter.LOG_SWITCH, "", SWITCH_OPTIONS, SWITCH_OPTIONS, LogSetting.isLoggable() ? 0 : 1),
                new NameValueSelectEntity(LogSettingValueSetter.LOG_LEVEL, "", LEVEL_OPTIONS, LEVEL_OPTIONS, LogSetting.getLevel().ordinal()),
                new NameValueStringEntity(LogSettingValueSetter.LOG_FILE_NAME, LogSetting.getLogFileName(), InputType.TYPE_CLASS_TEXT, ""),
                new NameValueBrowserEntity(LogSettingValueSetter.LOG_FILE_PATH, "Browse", filePath, InputType.TYPE_CLASS_TEXT, "", new NameValueStringEntity.ClickCallback() {
                    @Override
                    public void onClick(View v, NameValueStringEntity entity) {
                        String Dir = entity.getValue();
                        File f = new File(Dir);
                        if (!f.exists()) {
                            Dir = Environment.getExternalStorageDirectory().toString();
                        } else {
                            if (f.isFile())
                                Dir = Dir.substring(0, Dir.lastIndexOf("/"));
                        }
                        Intent intent2 = new Intent(getActivity(), DirectoryLogActivity.class);
                        Bundle bundle2 = new Bundle();
                        bundle2.putString("FilePath", Dir);
                        intent2.putExtras(bundle2);
                        startActivityForResult(intent2, Constant.MANAGE_SAVE_IMAGE_RESULT);
                    }
                }),
                new NameStringWithUnitEntity(LogSettingValueSetter.LOG_DAYS, LogSetting.getLogDays(), InputType.TYPE_CLASS_NUMBER, "", "Day(s)")
        );

        updateViews(nameValueEntities);
    }

    private void updateViews(List<NameValueEntity<String>> nameValueEntities) {
        renderEntityList.clear();
        renderEntityList.addAll(nameValueEntities);
        containerView.removeAllViews();
        for (RenderEntity renderEntity : renderEntityList) {
            CommonItemView itemView = renderEntity.createView(containerView);
            containerView.addView(itemView.getView());
            itemView.getView().setTag(itemView);
            itemView.render(renderEntity);
        }
    }

    private void setLogs() {
        for (RenderEntity renderEntity : renderEntityList) {
            if (renderEntity instanceof NameValueSelectEntity) {
                NameValueSelectEntity entity = (NameValueSelectEntity) renderEntity;
                LogSettingValueSetter valueSetter = LogSettingValueSetter.VALUE_SETTER_MAP.get(entity.getName());
                valueSetter.onSet(entity.getItemValues().get(entity.getSelectedItem()));

            } else if (renderEntity instanceof NameValueStringEntity) {
                NameValueStringEntity nameValueStringEntity = (NameValueStringEntity) renderEntity;
                LogSettingValueSetter valueSetter = LogSettingValueSetter.VALUE_SETTER_MAP.get(nameValueStringEntity.getName());
                valueSetter.onSet(nameValueStringEntity.getValue());
            }
        }
        android.util.Log.i(TAG, "isLoggable =" + LogSetting.isLoggable() + "; LogLevel=" + LogSetting.getLevel()
                + "; OutputPath=" + LogSetting.getOutputPath() + "; Log Days=" + LogSetting.getLogDays()
                + "; LogFileName=" + LogSetting.getLogFileName() + "; LogFilepath=" + LogSetting.getOutputPath());
        String LogSettingIniFile = getActivity().getApplicationContext().getFilesDir().getAbsolutePath() + "/" + SettingINI.FILENAME;
        SettingINI.saveLogSettingToFile(LogSettingIniFile);
    }

    private void viewLogFile() {

        Intent intent = new Intent(getActivity(), LogsViewerActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("LogsPath", LogSetting.getOutputPath());
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constant.MANAGE_SAVE_IMAGE_RESULT:
                switch (resultCode) {
                    case RESULT_OK:
                        filePath = data.getStringExtra("FilePath");
                        updateList();
                        break;
                    default:
                        break;
                }
                break;
            case 1010:
                switch (resultCode) {
                    case RESULT_OK:
                        filePaths = data.getStringArrayExtra("FilePaths");
                        updateLogPaths();
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
}
