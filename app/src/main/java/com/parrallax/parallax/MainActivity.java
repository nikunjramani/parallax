package com.parrallax.parallax;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.schibsted.spain.parallaxlayerlayout.AnimatedTranslationUpdater;
import com.schibsted.spain.parallaxlayerlayout.ParallaxLayerLayout;
import com.schibsted.spain.parallaxlayerlayout.SensorTranslationUpdater;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ParallaxLayerLayout parallaxLayout;
    private SensorTranslationUpdater translationUpdater;
    private ImageView image1,image2,image3;
    private final int PICK_IMAGE_MULTIPLE =1;
    private Button btn;
    private ArrayList<String> imagesPathList;
    private String[] apppermissions =new String[] {
            Manifest.permission.CAMERA,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.INTERNET,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO

    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_ovni);
        //setContentView(R.layout.activity_main_squares);

        parallaxLayout = (ParallaxLayerLayout) findViewById(R.id.parallax);
        image1= findViewById(R.id.image1);
        image2= findViewById(R.id.image2);
        image3= findViewById(R.id.image3);
        btn= findViewById(R.id.btn);

        checkAndRequestPermission();
        btn.setOnClickListener(v->{
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent,"Select Picture"), PICK_IMAGE_MULTIPLE);
        });
        translationUpdater = new SensorTranslationUpdater(this);


        parallaxLayout.setTranslationUpdater(new AnimatedTranslationUpdater(0.5f));

        // Resets orientation when clicked
        parallaxLayout.setOnClickListener(new View.OnClickListener() {
               @Override
            public void onClick(View v) {
                translationUpdater.reset();
            }
        });
    }
    private boolean checkAndRequestPermission() {
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        java.util.List<String> listPermissionsNeeded = new ArrayList<>();
        for (String perm : apppermissions)
        {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED)
            {
                listPermissionsNeeded.add(perm);
            }
        }

        if (!listPermissionsNeeded.isEmpty())
        {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),
                    2);
            return false;
        }

        return true;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==PICK_IMAGE_MULTIPLE){

            if(resultCode==RESULT_OK){
                //data.getParcelableArrayExtra(name);
                //If Single image selected then it will fetch from Gallery
                if(data.getData()!=null){
                    Uri mImageUri=data.getData();
                    Toast.makeText(this, "You need to select only 3 images", Toast.LENGTH_SHORT).show();

                }else{
                    if(data.getClipData()!=null){
                        if (data.getClipData().getItemCount() < 3 || data.getClipData().getItemCount() > 3 ) {
                            Toast.makeText(this, "You need to select only 3 images", Toast.LENGTH_SHORT).show();
                        }else {
                            ClipData mClipData=data.getClipData();
                            ArrayList<Uri> mArrayUri=new ArrayList<Uri>();
                            for(int i=0;i<mClipData.getItemCount();i++){

                                ClipData.Item item = mClipData.getItemAt(i);
                                Uri uri = item.getUri();
                                mArrayUri.add(uri);
                            }

                image1.setImageURI(mArrayUri.get(0));
                image2.setImageURI(mArrayUri.get(1));
                image3.setImageURI(mArrayUri.get(2));
                        }
                    }

                }

            }

        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        translationUpdater.registerSensorManager();
    }

    @Override
    protected void onPause() {
        super.onPause();
        translationUpdater.unregisterSensorManager();
    }
}
