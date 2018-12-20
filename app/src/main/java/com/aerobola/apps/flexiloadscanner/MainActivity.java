package com.aerobola.apps.flexiloadscanner;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.github.pinball83.maskededittext.MaskedEditText;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.theartofdev.edmodo.cropper.CropImage;

public class MainActivity extends AppCompatActivity {

    MaskedEditText editText;
ImageButton imageButton;

   Button rechargeButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
imageButton=(ImageButton)findViewById(R.id.imageButton);
        editText = (MaskedEditText) findViewById(R.id.EditText);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                editText.setMaskedText("");

                requestPermissionCamera();

                CropImage.activity()
                        .setAspectRatio(14, 2)
                        .start(MainActivity.this);
            }
        });
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(isValidnumber(editText.getText().toString().replaceAll("\\s+",""))==true&&editText.getText().toString().replaceAll("\\s+","").length()==16){

                    rechargeButton.setEnabled(true);

                }else{

rechargeButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        rechargeButton=(Button)findViewById(R.id.recharge_button);
        rechargeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    list();

            }
        });
        rechargeButton.setEnabled(false);




    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                ImageView imageView = (ImageView) findViewById(R.id.imsgeView);
                imageView.setImageURI(resultUri);
                BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
                Bitmap bitmap = bitmapDrawable.getBitmap();
                textRecognization(bitmap);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }


    private void textRecognization(Bitmap photo) {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(photo);
        FirebaseVisionTextRecognizer textRecognizer = FirebaseVision.getInstance()
                .getOnDeviceTextRecognizer();
        textRecognizer.processImage(image)
                .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                    @Override
                    public void onSuccess(FirebaseVisionText result) {
                        for (FirebaseVisionText.TextBlock block : result.getTextBlocks()) {

                          editText.setMaskedText(block.getText().replaceAll("\\s+",""));
                            if(isValidnumber(editText.getText().toString().replaceAll("\\s+",""))==true&&editText.getText().toString().replaceAll("\\s+","").length()==16){

                                rechargeButton.setEnabled(true);
                                list();

                            }else{
                                Toast.makeText(MainActivity.this, "Sorry! The number is not correct. Try some modifications manually.", Toast.LENGTH_LONG).show();

                                rechargeButton.setEnabled(false);
                            }


                        }
                        // ...
                    }
                })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Task failed with an exception
                                // ...
                            }
                        });
    }

    public void requestPermissionCamera() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity)
                    this, Manifest.permission.CAMERA)) {


            } else {
                ActivityCompat.requestPermissions((Activity) this,
                        new String[]{Manifest.permission.CAMERA},
                        101);
            }

        }
    }

    public void requestPermissionDial() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity)
                    this, Manifest.permission.CALL_PHONE)) {


            } else {
                ActivityCompat.requestPermissions((Activity) this,
                        new String[]{Manifest.permission.CALL_PHONE},
                        102);
            }

        }
    }

    private Uri ussdToCallableUri(String ussd) {

        String uriString = "";

        if (!ussd.startsWith("tel:"))
            uriString += "tel:";

        for (char c : ussd.toCharArray()) {

            if (c == '#')
                uriString += Uri.encode("#");
            else
                uriString += c;
        }

        return Uri.parse(uriString);
    }

    public void list() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);


        ListView modeList = new ListView(this);
        String[] stringArray = new String[]{getString(R.string.gp), getString(R.string.bl), getString(R.string.robi), getString(R.string.airtel), getString(R.string.teletalk)};
        ArrayAdapter<String> modeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, stringArray);
        modeList.setAdapter(modeAdapter);

        builder.setView(modeList);
        final Dialog dialog = builder.create();

        modeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            Intent intent = new Intent(Intent.ACTION_CALL);

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                   requestPermissionDial();
                    return;
                }
                switch (i) {

                    case 0:

                        intent = new Intent(Intent.ACTION_CALL, ussdToCallableUri("*555*" + editText.getText().toString() + "#"));


                        startActivity(intent);
                        break;
                    case 1:
                        intent = new Intent(Intent.ACTION_CALL, ussdToCallableUri("*123*"+editText.getText().toString()+"#"));
                        startActivity(intent);

                        break;

                    case 2:
                        intent = new Intent(Intent.ACTION_CALL, ussdToCallableUri("*111*"+editText.getText().toString()+"#"));
                        startActivity(intent);

                        break;
                    case 3:
                        intent = new Intent(Intent.ACTION_CALL, ussdToCallableUri("*787*"+editText.getText().toString()+"#"));
                        startActivity(intent);



                        break;
                    case 4:

                        intent = new Intent(Intent.ACTION_CALL, ussdToCallableUri("*111*"+editText.getText().toString()+"#"));
                        startActivity(intent);



                        break;
                }
                dialog.dismiss();
            }
        });
        dialog.show();
    }
    public boolean isValidnumber(String number){
        try
        {
            // checking valid integer using parseInt() method

            Long.parseLong(number);
            return true;

        }
        catch (NumberFormatException e)
        {
           return  false;
        }

    }


}