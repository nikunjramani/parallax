package com.parrallax.parallax;

import android.Manifest;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.parrallax.parallax.databinding.ActivityMainOvniBinding;
import com.parrallax.parallax.lib.AnimatedTranslationUpdater;
import com.parrallax.parallax.lib.ParallaxLayerLayout;
import com.parrallax.parallax.lib.SensorTranslationUpdater;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ParallaxLayerLayout parallaxLayout;
    private SensorTranslationUpdater translationUpdater;
    private ArrayList<String> imagesPathList;
    ArrayList<String> mArrayUri = new ArrayList<String>();
    private int speed = 1500;
    ActivityMainOvniBinding binding;
    String[] effect = { "Sideways", "Up Down"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainOvniBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        parallaxLayout = (ParallaxLayerLayout) findViewById(R.id.parallax);
        mArrayUri = getIntent().getStringArrayListExtra("images");

        try {
            binding.image1.setImageBitmap(uriToBitmap(Uri.parse(mArrayUri.get(0))));
            binding.image2.setImageBitmap(uriToBitmap(Uri.parse(mArrayUri.get(1))));
            binding.image3.setImageBitmap(uriToBitmap(Uri.parse(mArrayUri.get(2))));

        } catch (IOException e) {
            e.printStackTrace();
        }



        binding.smallImage1.setImageURI(Uri.parse(mArrayUri.get(0)));
        binding.smallImage2.setImageURI(Uri.parse(mArrayUri.get(1)));
        binding.smallImage3.setImageURI(Uri.parse(mArrayUri.get(2)));

        binding.btn.setOnClickListener(v -> {

        });
        translationUpdater = new SensorTranslationUpdater(this);

        parallaxLayout.setTranslationUpdater(new AnimatedTranslationUpdater(0.5f),speed);

        // Resets orientation when clicked
        parallaxLayout.setOnClickListener(v -> translationUpdater.reset());

        ArrayAdapter aa = new ArrayAdapter(this,android.R.layout.simple_spinner_item,effect);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        binding.effect.setAdapter(aa);

        binding.speed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(i==0){
                    i=1;
                }
                if(i==100){
                    i=99;
                }
                int ri = 100-i;
                int speeds = ri*10;
                Log.d("TAG", "onProgressChanged: "+speeds);
                parallaxLayout.setTranslationUpdater(new AnimatedTranslationUpdater(0.5f),speeds);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        binding.frame.setOnClickListener(view1 -> {

            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), 2);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2) {

            if (resultCode == RESULT_OK) {
                Uri mImageUri = data.getData();
                BitmapDrawable bd = null;
                try {
                    bd = new BitmapDrawable(getResources(), uriToBitmap(mImageUri));
                    binding.ll2.setBackground(bd);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }}
    }

    public Bitmap uriToBitmap(Uri uri) throws IOException {
        return  MediaStore.Images.Media.getBitmap(getContentResolver() , uri);
    }
    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
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
