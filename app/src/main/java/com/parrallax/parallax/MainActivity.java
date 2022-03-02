package com.parrallax.parallax;

import android.Manifest;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
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

        parallaxLayout.setTranslationUpdater(new AnimatedTranslationUpdater(0.5f));

        // Resets orientation when clicked
        parallaxLayout.setOnClickListener(v -> translationUpdater.reset());

        ArrayAdapter aa = new ArrayAdapter(this,android.R.layout.simple_spinner_item,effect);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        binding.effect.setAdapter(aa);

        binding.speed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
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
