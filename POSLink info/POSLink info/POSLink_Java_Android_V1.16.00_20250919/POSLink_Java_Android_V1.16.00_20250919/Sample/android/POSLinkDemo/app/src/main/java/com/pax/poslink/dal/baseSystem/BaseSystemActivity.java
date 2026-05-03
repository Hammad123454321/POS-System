package com.pax.poslink.dal.baseSystem;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.pax.poslink.R;
import com.pax.poslink.exceptions.BaseSystemException;
import com.pax.poslink.peripheries.BaseSystemManager;
import com.pax.poslink.ui.base.BaseActivity;
import com.pax.poslink.util.StringUtil;

import java.util.Arrays;
import java.util.List;

/**
 * @author Justin.Z
 */
public class BaseSystemActivity extends BaseActivity {

    private BaseSystemManager baseSystemManager;
    private Spinner spinner;
    private EditText editText;
    private TextView displayText;

    private int index = 0;

    public static void start(Context context) {
        Intent starter = new Intent(context, BaseSystemActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_system);
        baseSystemManager = BaseSystemManager.getInstance(this);
        initView();
    }

    private void initView() {
        findViewById(R.id.btn_base_sys_beep).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            baseSystemManager.beep();
                        } catch (BaseSystemException e) {
                            e.printStackTrace();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(BaseSystemActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }).start();
            }
        });

        spinner = findViewById(R.id.beep_mode_spinner);
        editText = findViewById(R.id.edt_beep_time);
        List list = Arrays.asList("0", "1", "2", "3", "4", "5", "6");
        final List valueList = Arrays.asList(BaseSystemManager.EBeepMode.FREQUENCY_LEVEL_0, BaseSystemManager.EBeepMode.FREQUENCY_LEVEL_1, BaseSystemManager.EBeepMode.FREQUENCY_LEVEL_2,
                BaseSystemManager.EBeepMode.FREQUENCY_LEVEL_3, BaseSystemManager.EBeepMode.FREQUENCY_LEVEL_4, BaseSystemManager.EBeepMode.FREQUENCY_LEVEL_5,
                BaseSystemManager.EBeepMode.FREQUENCY_LEVEL_6);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, list);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                index = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        findViewById(R.id.btn_base_sys_beepF).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int time = StringUtil.parseInt(editText.getText().toString());
                        try {
                            baseSystemManager.beep((BaseSystemManager.EBeepMode) valueList.get(index), time);
                        } catch (BaseSystemException e) {
                            e.printStackTrace();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(BaseSystemActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }).start();
            }
        });
        boolean isOnBase;
        displayText = findViewById(R.id.isonbase);
        isOnBase = baseSystemManager.isOnBase();
        displayText.setText("IsOnBase ? " + isOnBase);

        initLed();

    }

    private void initLed() {
        final CheckBox redBox = findViewById(R.id.box_led_red);
        final CheckBox greenBox = findViewById(R.id.box_led_green);
        final CheckBox yellowBox = findViewById(R.id.box_led_yellow);
        final CheckBox blueBox = findViewById(R.id.box_led_blue);
        final byte[] ledIndex = new byte[] {0x01, 0x02, 0x04, 0x08};
        findViewById(R.id.btn_base_sys_led_on).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        byte ucledIndex = 0x00;
                        if (redBox.isChecked()) ucledIndex |= ledIndex[0];
                        if (greenBox.isChecked()) ucledIndex |= ledIndex[1];
                        if (yellowBox.isChecked()) ucledIndex |= ledIndex[2];
                        if (blueBox.isChecked()) ucledIndex |= ledIndex[3];
                        try {
                            BaseSystemManager.getInstance(getApplicationContext()).ledControl(ucledIndex, BaseSystemManager.LedMode.LIGHT_ON);
                        } catch (BaseSystemException e) {
                            e.printStackTrace();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(BaseSystemActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }).start();

            }
        });

        findViewById(R.id.btn_base_sys_led_off).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        byte ucledIndex = 0x00;
                        if (redBox.isChecked()) ucledIndex |= ledIndex[0];
                        if (greenBox.isChecked()) ucledIndex |= ledIndex[1];
                        if (yellowBox.isChecked()) ucledIndex |= ledIndex[2];
                        if (blueBox.isChecked()) ucledIndex |= ledIndex[3];
                        try {
                            BaseSystemManager.getInstance(getApplicationContext()).ledControl(ucledIndex, BaseSystemManager.LedMode.LIGHT_OFF);
                        } catch (BaseSystemException e) {
                            e.printStackTrace();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(BaseSystemActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }).start();

            }
        });
    }
}
