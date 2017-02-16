package com.putuguna.signaturepad;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.github.gcacace.signaturepad.views.SignaturePad;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    public static final int REQUEST_EXTERNAL_STORAGE = 1;

    private SignaturePad signaturePad;
    private Button btnClear;
    private Button btnSave;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        signaturePad = (SignaturePad) findViewById(R.id.signature_pad);
        btnClear = (Button) findViewById(R.id.btn_clear);
        btnSave = (Button) findViewById(R.id.btn_save);

        signaturePad.setOnSignedListener(new SignaturePad.OnSignedListener() {
            @Override
            public void onStartSigning() {
                Toast.makeText(MainActivity.this, "OnStartSigning", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSigned() {
                btnSave.setEnabled(true);
                btnClear.setEnabled(true);
            }

            @Override
            public void onClear() {
                btnClear.setEnabled(false);
                btnSave.setEnabled(false);
            }
        });

        btnClear.setOnClickListener(this);
        btnSave.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int id= view.getId();
        if(id==R.id.btn_clear){
            signaturePad.clear();
        }else if(id==R.id.btn_save){
            createSignature();
        }
    }

    private void createSignature(){
        Bitmap signatureBitmap = signaturePad.getSignatureBitmap();
        if(addJpgSignatureToGallery(signatureBitmap)){
            Toast.makeText(this, "JPG format saved into Gallery", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "Failed to save jpg format to Gallery", Toast.LENGTH_SHORT).show();
        }

        if(saveSvgSignatureToGallery(signaturePad.getSignatureSvg())){
            Toast.makeText(this, "SVG Format saved into Gallery", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "Failed to save SVG format to gallery", Toast.LENGTH_SHORT).show();
        }
    }
    /**
     * this method used to create the signature's album in Gallery
     * @param albumName
     * @return
     */
    public File getAlbumStorageDir(String albumName){
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), albumName);

        if(!file.mkdirs()){
            Log.d("TAG", "Directory not created");
        }

        return file;
    }

    /**
     * this method used to convert the signature into bitmap format
     * @param bitmap
     * @param photo
     */
    private void saveBitmapToJPG(Bitmap bitmap, File photo){
        Bitmap newBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bitmap, 0,0, null);
        try {
            OutputStream stream = new FileOutputStream(photo);
            newBitmap.compress(Bitmap.CompressFormat.JPEG,80,stream);
            stream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * this method used to save the signature as jgp into gallery
     * @param signature
     * @return
     */
    public boolean addJpgSignatureToGallery(Bitmap signature) {
        boolean result = false;
        File photo = new File(getAlbumStorageDir("SignaturePad"), String.format("Signature_%d.jpg", System.currentTimeMillis()));
        saveBitmapToJPG(signature, photo);
        scanMediaFile(photo);
        result = true;
        return result;
    }

    /**
     * this method used to the file
     * @param photo
     */
    private void scanMediaFile(File photo) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(photo);
        mediaScanIntent.setData(contentUri);
        MainActivity.this.sendBroadcast(mediaScanIntent);
    }

    /**
     * this method used to save the signature as format svg into gallery
     * @param signatureSvg
     * @return
     */
    private boolean saveSvgSignatureToGallery(String signatureSvg){
        boolean result = false;

        File svgFile = new File(getAlbumStorageDir("SignaturePad"), String.format("Signature_%d.svg", System.currentTimeMillis()));
        try {
            OutputStream stream = new FileOutputStream(svgFile);
            OutputStreamWriter writer = new OutputStreamWriter(stream);
            writer.write(signatureSvg);
            writer.close();
            stream.flush();
            stream.close();
            scanMediaFile(svgFile);
            result = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }
}
