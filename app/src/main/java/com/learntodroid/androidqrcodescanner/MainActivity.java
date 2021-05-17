package com.learntodroid.androidqrcodescanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;

import java.text.DecimalFormat;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CAMERA = 0;

    private PreviewView previewView;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    private Button qrCodeFoundButton,plusbtn,minusbtn,btnViewData;
    private String qrCode;
    public double sum;
    public String total;
    Toast priceToast,db_toast;
    private static DecimalFormat df = new DecimalFormat("0.00");
    String[] fullItem;
    DatabaseHelper mDatabaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView totalamount=findViewById(R.id.totalamount);
        previewView = findViewById(R.id.activity_main_previewView);
        plusbtn=findViewById(R.id.plusbtn);
        minusbtn=findViewById(R.id.minusbtn);
        totalamount.setText("0");

        //Database
        btnViewData=(Button) findViewById(R.id.btn_view_data);
        mDatabaseHelper= new DatabaseHelper(this);

        qrCodeFoundButton = findViewById(R.id.activity_main_qrCodeFoundButton);
        //qrCodeFoundButton.setVisibility(View.INVISIBLE);
        qrCodeFoundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(priceToast!=null){
                        priceToast.cancel(); // Avoid null pointer exceptions!
                    }
                    priceToast=Toast.makeText(getApplicationContext(), qrCode, Toast.LENGTH_LONG);
                    priceToast.show();
                    //onlyPrice=qrCode.substring(qrCode.indexOf("=") + 1); //get text from QR code after = (that means the price)
                    fullItem=qrCode.split("=");
                    Log.i(MainActivity.class.getSimpleName(), "FIRSTTTTT: " + fullItem[1]);
                    Log.i(MainActivity.class.getSimpleName(), "SECONDDD: " + fullItem[2]);

                    //priceToast=Toast.makeText(getApplicationContext(), onlyPrice, Toast.LENGTH_LONG);
                    //priceToast.show();
                    Log.i(MainActivity.class.getSimpleName(), "QR Code Found: " + sum);
                }catch (Exception e){
                    Log.i(MainActivity.class.getSimpleName(), "Exception: " + e);
                }
            }
        });

        plusbtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                sum = sum + Double.parseDouble(fullItem[1]);
                sum= Double.parseDouble(df.format(sum));
                total = String.valueOf(sum);
                if (sum < 0) {
                    totalamount.setTextColor(Color.RED);
                    totalamount.setText(total);
                } else {
                    totalamount.setTextColor(Color.GREEN);
                    totalamount.setText(total);
                    Log.i(MainActivity.class.getSimpleName(), "SUM== " + sum);
                }
                //Save data into database
                AddData(fullItem[0],fullItem[1]);
            }
        });
        minusbtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                sum = sum - Double.parseDouble(fullItem[1]);
                sum= Double.parseDouble(df.format(sum));
                total = String.valueOf(sum);
                if (sum < 0) {
                    totalamount.setTextColor(Color.RED);
                    totalamount.setText(total);
                } else {
                    totalamount.setTextColor(Color.GREEN);
                    totalamount.setText(total);
                    Log.i(MainActivity.class.getSimpleName(), "SUM== " + sum);
                }
            }
        });
        btnViewData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ListDataActivity.class);
                startActivity(intent);
            }
        });
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        requestCamera();
    }

    //Database
    public void AddData(String name,String price){
        boolean insertData= mDatabaseHelper.addData(name,price);

        if(insertData){
            db_toast=Toast.makeText(getApplicationContext(),"Data Saved", Toast.LENGTH_LONG);
            db_toast.show();
        }
        else{
            db_toast=Toast.makeText(getApplicationContext(),"Something went wrong", Toast.LENGTH_LONG);
            db_toast.show();
        }
    }


    //Camera
    private void requestCamera() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "Camera Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startCamera() {
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindCameraPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(this, "Error starting camera " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindCameraPreview(@NonNull ProcessCameraProvider cameraProvider) {
        previewView.setPreferredImplementationMode(PreviewView.ImplementationMode.SURFACE_VIEW);

        Preview preview = new Preview.Builder()
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        preview.setSurfaceProvider(previewView.createSurfaceProvider());

        ImageAnalysis imageAnalysis =
                new ImageAnalysis.Builder()
                        .setTargetResolution(new Size(1280, 720))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), new QRCodeImageAnalyzer(new QRCodeFoundListener() {
            @Override
            public void onQRCodeFound(String _qrCode) {
                qrCode = _qrCode;
                qrCodeFoundButton.setVisibility(View.VISIBLE);
                //plusbtn.setVisibility(View.VISIBLE);
                //minusbtn.setVisibility(View.VISIBLE);
            }

            @Override
            public void qrCodeNotFound() {
                //qrCodeFoundButton.setVisibility(View.INVISIBLE);
                //plusbtn.setVisibility(View.INVISIBLE);
                //minusbtn.setVisibility(View.INVISIBLE);
                //Toast.makeText(getApplicationContext(),"", Toast.LENGTH_LONG).cancel();

            }
        }));

        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, imageAnalysis, preview);
    }
}