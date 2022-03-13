package com.parrallax.parallax;

import static android.content.res.Configuration.ORIENTATION_PORTRAIT;

import static com.hbisoft.hbrecorder.Constants.MAX_FILE_SIZE_REACHED_ERROR;
import static com.hbisoft.hbrecorder.Constants.SETTINGS_ERROR;

import static java.sql.Types.DATE;
import static java.sql.Types.TIME;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.LongDef;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.hbisoft.hbrecorder.HBRecorder;
import com.hbisoft.hbrecorder.HBRecorderCodecInfo;
import com.hbisoft.hbrecorder.HBRecorderListener;
import com.parrallax.parallax.databinding.ActivityChooseConversionBinding;
import com.parrallax.parallax.databinding.ActivityMainOvniBinding;
import com.parrallax.parallax.lib.AnimatedGifEncoder;
import com.parrallax.parallax.lib.AnimatedTranslationUpdater;
import com.parrallax.parallax.lib.ParallaxLayerLayout;
import com.parrallax.parallax.lib.SensorTranslationUpdater;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import nl.bravobit.ffmpeg.ExecuteBinaryResponseHandler;
import nl.bravobit.ffmpeg.FFmpeg;

public class MainActivity extends AppCompatActivity implements HBRecorderListener {

    private static final int SCREEN_RECORD_REQUEST_CODE = 100;
    private static final int PERMISSION_REQ_ID_RECORD_AUDIO = 101;
    private static final int PERMISSION_REQ_ID_WRITE_EXTERNAL_STORAGE = 102;
    ArrayList<String> mArrayUri = new ArrayList<String>();
    ActivityMainOvniBinding binding;
    ActivityChooseConversionBinding activityChooseConversionBinding;
    String[] effect = { "Sideways", "Up Down"};
    HBRecorder hbRecorder;
    boolean hasPermissions;
    ContentValues contentValues;
    ContentResolver resolver;
    Uri mUri;
    private ParallaxLayerLayout parallaxLayout;
    private SensorTranslationUpdater translationUpdater;
    private ArrayList<String> imagesPathList;
    private int speed = 1500;
    private int originalHeight;
    private boolean isFullScreen = false;
    private boolean isVertical = true;
    RecyclerViewAdapter mAdapter;
    ItemTouchHelper touchHelper;
    private static final String TAG = "MainActivity";
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

        checkPermission();
//        binding.smallImage1.setImageURI(Uri.parse(mArrayUri.get(0)));
//        binding.smallImage2.setImageURI(Uri.parse(mArrayUri.get(1)));
//        binding.smallImage3.setImageURI(Uri.parse(mArrayUri.get(2)));

        translationUpdater = new SensorTranslationUpdater(this);

        parallaxLayout.setTranslationUpdater(new AnimatedTranslationUpdater(0.5f),speed,true);

        // Resets orientation when clicked
        parallaxLayout.setOnClickListener(v -> translationUpdater.reset());

        ArrayAdapter aa = new ArrayAdapter(this,android.R.layout.simple_spinner_item,effect);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


        hbRecorder = new HBRecorder(this, this);
        hbRecorder.setVideoEncoder("H264");
        hbRecorder.isAudioEnabled(false);
        //Setting the ArrayAdapter data on the Spinner
        binding.effect.setAdapter(aa);
        binding.effect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position==0){
                    isVertical=true;
                    parallaxLayout.setTranslationUpdater(new AnimatedTranslationUpdater(0.5f),speed,true);
                }else {
                    isVertical=false;
                    parallaxLayout.setTranslationUpdater(new AnimatedTranslationUpdater(0.5f),speed,false);

                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

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
                speed=speeds;
                parallaxLayout.setTranslationUpdater(new AnimatedTranslationUpdater(0.5f),speeds,isVertical);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        binding.frame.setOnClickListener(view1 -> {

//            startRecordingScreen();
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), 2);
        });

        binding.fullscreen.setOnClickListener(v -> {
            Log.d("TAG", "onClick: Clicked");
            if(!isFullScreen){
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN);
                isFullScreen = true;
                binding.fullscreen.setBackground(getDrawable(R.drawable.ic_fullscreen_exit));
                binding.ll1.setVisibility(View.GONE);
                getSupportActionBar().hide();
                originalHeight = binding.ll2.getHeight();
                binding.ll2.getLayoutParams().height = ParallaxLayerLayout.LayoutParams.MATCH_PARENT;

            }else {
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                isFullScreen = false;
                binding.fullscreen.setBackground(getDrawable(R.drawable.ic_fullscreen));
                binding.ll1.setVisibility(View.VISIBLE);
                getSupportActionBar().show();
                binding.ll2.getLayoutParams().height =originalHeight;

            }
        });
        binding.btn.setOnClickListener(v -> {
            showDialog();
        });
        populateRecyclerView();
    }
    private void populateRecyclerView() {
        ItemMoveCallback.ItemListner itemListner = new ItemMoveCallback.ItemListner() {
            @Override
            public void onMove(ArrayList<String> arrayList) {
                try {
                    binding.image1.setImageBitmap(uriToBitmap(Uri.parse(arrayList.get(0))));
                    binding.image2.setImageBitmap(uriToBitmap(Uri.parse(arrayList.get(1))));
                    binding.image3.setImageBitmap(uriToBitmap(Uri.parse(arrayList.get(2))));

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        mAdapter = new RecyclerViewAdapter(this,mArrayUri,itemListner);

        ItemTouchHelper.Callback callback =
                new ItemMoveCallback(mAdapter);
        touchHelper  = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(binding.recyclerView);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.HORIZONTAL);
        binding.recyclerView.setLayoutManager(llm);
        binding.recyclerView.setAdapter(mAdapter);

    }
    void checkPermission(){
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO, PERMISSION_REQ_ID_RECORD_AUDIO) && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, PERMISSION_REQ_ID_WRITE_EXTERNAL_STORAGE)) {
            hasPermissions = true;
        }
    }

    private boolean checkSelfPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
            return false;
        }
        return true;
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
        if (requestCode == SCREEN_RECORD_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                //Start screen recording
                hbRecorder.startScreenRecording(data, resultCode, this);

            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (requestCode == SCREEN_RECORD_REQUEST_CODE) {
                if (resultCode == RESULT_OK) {
                    //Set file path or Uri depending on SDK version
                    setOutputPath();
                    //Start screen recording
                    hbRecorder.startScreenRecording(data, resultCode, this);

                }
            }
        }
    }

    public Bitmap uriToBitmap(Uri uri) throws IOException {
        return  MediaStore.Images.Media.getBitmap(getContentResolver() , uri);
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


    @Override
    public void HBRecorderOnStart() {

    }

    @Override
    public void HBRecorderOnComplete() {
        Toast.makeText(this, "Completed", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "HBRecorderOnComplete: "+mUri);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //Update gallery depending on SDK Level
            if (hbRecorder.wasUriSet()) {
                updateGalleryUri();
            }else{
                refreshGalleryFile();
            }
        }
    }
    private void startRecordingScreen() {
        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        Intent permissionIntent = mediaProjectionManager != null ? mediaProjectionManager.createScreenCaptureIntent() : null;
        startActivityForResult(permissionIntent, SCREEN_RECORD_REQUEST_CODE);
    }

    @Override
    public void HBRecorderOnError(int errorCode, String reason) {
        Log.d("TAG", "HBRecorderOnError: "+reason);

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }


    public void showDialog(){
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.activity_choose_conversion);
        dialog.findViewById(R.id.pro).setOnClickListener(v -> {
            hbRecorder.recordHDVideo(false);
            startStopRecording(false);
           dialog.dismiss();
       });
        dialog.findViewById(R.id.gif).setOnClickListener(v -> {
            hbRecorder.recordHDVideo(false);
            startStopRecording(false);
            dialog.dismiss();

        });
        dialog.findViewById(R.id.video).setOnClickListener(v -> {
            hbRecorder.recordHDVideo(false);
            startStopRecording(false);
            dialog.dismiss();

        });

        dialog.show();

    }

    void startStopRecording(boolean isGif){

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        binding.fullscreen.setVisibility(View.GONE);
        binding.ll1.setVisibility(View.GONE);
        getSupportActionBar().hide();
        originalHeight = binding.ll2.getHeight();
        binding.ll2.getLayoutParams().height = ParallaxLayerLayout.LayoutParams.MATCH_PARENT;
        startRecordingScreen();

        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> {
            hbRecorder.stopScreenRecording();
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            isFullScreen = false;
            binding.fullscreen.setVisibility(View.VISIBLE);
            binding.fullscreen.setBackground(getDrawable(R.drawable.ic_fullscreen));
            binding.ll1.setVisibility(View.VISIBLE);
            getSupportActionBar().show();
            binding.ll2.getLayoutParams().height =originalHeight;
//            convertToGIF();

        }, 15000);
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setOutputPath() {
        String filename = generateFileName();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            resolver = getContentResolver();
            contentValues = new ContentValues();
            contentValues.put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/" );
            contentValues.put(MediaStore.Video.Media.TITLE, filename);
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, filename);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");
            mUri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues);

            //FILE NAME SHOULD BE THE SAME
            hbRecorder.setFileName(filename);
            hbRecorder.setOutputUri(mUri);
        }else{
            createFolder();
            hbRecorder.setOutputPath(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) +"/Movies");
        }
    }
    private void updateGalleryUri(){
        contentValues.clear();
        contentValues.put(MediaStore.Video.Media.IS_PENDING, 0);
        getContentResolver().update(mUri, contentValues, null, null);
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void refreshGalleryFile() {
        MediaScannerConnection.scanFile(this,
                new String[]{hbRecorder.getFilePath()}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    }
                });
    }
    //Generate a timestamp to be used as a file name
    private String generateFileName() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault());
        Date curDate = new Date(System.currentTimeMillis());
        return formatter.format(curDate).replace(" ", "");
    }
    //drawable to byte[]
    private byte[] drawable2ByteArray(@DrawableRes int drawableId) {
        Bitmap icon = BitmapFactory.decodeResource(getResources(), drawableId);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        icon.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }
    //Create Folder
    //Only call this on Android 9 and lower (getExternalStoragePublicDirectory is deprecated)
    //This can still be used on Android 10> but you will have to add android:requestLegacyExternalStorage="true" in your Manifest
    private void createFolder() {
        File f1 = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "SpeedTest");
        if (!f1.exists()) {
            if (f1.mkdirs()) {
                Log.i("Folder ", "created");
            }
        }
    }
    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }
    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        if (cursor != null) {
            // HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
            // THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } else
            return null;
    }
//    void convertToGIF(){
//        Log.d(TAG, "convertToGIF: "+getRealPathFromURI(mUri));
//
//
//        String[] list = getPath(mUri).split("/");
//        String s = "SD"+list[list.length-1];
//        list[list.length-1] =s;
//        StringBuilder finalpath = new StringBuilder();
//        for(int i=0;i<list.length;i++){
//            if(i+1<list.length){
//                finalpath.append(list[i]).append("/");
//            }else {
//                finalpath.append(list[i]);
//            }
//        }
//
//        Log.d(TAG, "convertToGIF: "+finalpath);
//        // Get your video file
//        File myVideo = new File(String.valueOf(finalpath));
//        Log.d(TAG, "convertToGIF: "+myVideo.getPath());
//
//// URI to your video file
//        Uri myVideoUri = Uri.parse(myVideo.toString());
//
//// MediaMetadataRetriever instance
//        MediaMetadataRetriever mmRetriever = new MediaMetadataRetriever();
//
//            mmRetriever.setDataSource(finalpath.toString());
//        ArrayList<Bitmap> frames = new ArrayList<Bitmap>();
//        Bitmap bitmap = mmRetriever.getFrameAtTime();
//        frames.add(bitmap);
//        if(bitmap!=null){
//            binding.image1.setImageBitmap(bitmap);
//        }else {
//            Log.d(TAG, "convertToGIF: issues"+frames);
//        }
//
//        saveGif(frames);
//
//    }

//    public byte[] generateGIF(ArrayList<Bitmap> bitmaps) {
//        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//        AnimatedGifEncoder encoder = new AnimatedGifEncoder();
//        encoder.start(bos);
//        for (Bitmap bitmap : bitmaps) {
//            encoder.addFrame(bitmap);
//        }
//        encoder.finish();
//        return bos.toByteArray();
//    }
//
//    public void saveGif(ArrayList<Bitmap> bitmaps) {
//        try {
//            FileOutputStream outStream = new FileOutputStream("/sdcard/test.gif");
//            outStream.write(generateGIF(bitmaps));
//            outStream.close();
//        } catch(Exception e) {
//            e.printStackTrace();
//        }
//    }


}
