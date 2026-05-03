package com.pax.poslink.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.text.method.ScrollingMovementMethod;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.pax.poslink.R;
import com.pax.poslink.ui.base.BaseActivity;
import com.pax.poslink.util.Constant;
import com.pax.poslink.util.ConvertSigUtils;
import com.pax.poslink.util.FileUtils;

import java.io.File;
import java.io.IOException;


public class SigDetailActivity extends BaseActivity implements OnClickListener,OnTouchListener {
    private static final String TAG = "DetailActivity";
    private TextView pathView = null;
    private TextView bmppath = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sig_detail);

        Bundle bundle=getIntent().getExtras();
        String path=bundle.getString("SigPath");
        System.out.println("path ="+path);

        pathView = (TextView)findViewById(R.id.sigDetail_sigFilePath);
        pathView.setText(path);

        TextView fileView = (TextView)findViewById(R.id.sigDetail_sigData);
        fileView.setMovementMethod(ScrollingMovementMethod.getInstance());

        ImageView imageView = (ImageView)findViewById(R.id.sigDetail_showImage);

        Button returnBtn = (Button)findViewById(R.id.sigDetail_returnBack);
        Button browserBtn = (Button)findViewById(R.id.sigDetail_bmpBrowser);
        Button saveBtn = (Button)findViewById(R.id.sigDetail_bmpSaveBtn);

        returnBtn.setOnClickListener(this);
        browserBtn.setOnClickListener(this);
        saveBtn.setOnClickListener(this);

        bmppath = (TextView)findViewById(R.id.sigDetail_bmpFilePath);

        if(path.length() == 0)
            return;

        String alldata = FileUtils.readFile(path);
        System.out.println("all ="+alldata);
        fileView.setText(alldata);

        Bitmap bmp = ConvertSigUtils.generateBmp(alldata);

        if(bmp != null)
            imageView.setImageBitmap(bmp);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.sigDetail_returnBack:
                System.out.println("return Detail");
                finish();
                break;
            case R.id.sigDetail_bmpBrowser:
                String imgDir = bmppath.getText().toString();
                File f = new File(imgDir);
                if(!f.exists()){
                    imgDir = Environment.getExternalStorageDirectory().toString();
                }
                else{
                    if(f.isFile())
                        imgDir = imgDir.substring(0, imgDir.lastIndexOf("/"));
                }
                Intent intent2 = new Intent(this,DirectoryManagerActivity.class);
                Bundle bundle2 = new Bundle();
                bundle2.putString("SaveImagePath", imgDir);
                intent2.putExtras(bundle2);
                startActivityForResult(intent2, Constant.MANAGE_SAVE_IMAGE_RESULT);
                break;
            case R.id.sigDetail_bmpSaveBtn:
                System.out.println("save img");

                File bmp = new File(bmppath.getText().toString());
                String bmpSave2 = bmp.getAbsolutePath();
                String bmpFrom = pathView.getText().toString();
                int index = bmpFrom.lastIndexOf("/");
                if (bmp.isDirectory()) {
                    bmpSave2 += bmpFrom.substring(index >= 0 ? index :0);
                }

                try {
                    int ret = ConvertSigToPic(pathView.getText().toString(),"bmp",bmpSave2);
                    if(ret < 0)
                        Toast.makeText(SigDetailActivity.this, "save image fail", Toast.LENGTH_LONG).show();
                    else
                        Toast.makeText(SigDetailActivity.this, "save image sucess", Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constant.MANAGE_SAVE_IMAGE_RESULT:
                switch (resultCode) {
                    case RESULT_OK:
                        bmppath.setText(data.getStringExtra("SaveImagePath"));
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
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }

    public int ConvertSigToPic(String path,String type,String outFile) throws IOException
    {
        if(path.length() == 0)
            return -1;
        if(outFile.length() == 0)
            return -2;

        //open file
        String alldata = FileUtils.readFile(path);

        int index = outFile.lastIndexOf("sig");
        if(index == outFile.length() - 3)
            outFile = outFile.replaceAll("sig", type);

        return ConvertSigUtils.convertSigToPic(alldata, type, outFile);
    }
}