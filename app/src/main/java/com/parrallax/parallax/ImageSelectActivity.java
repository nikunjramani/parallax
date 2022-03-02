package com.parrallax.parallax;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.parrallax.parallax.databinding.ActivityImageSelectBinding;
import com.parrallax.parallax.databinding.ActivitySpalshBinding;

import java.util.ArrayList;

public class ImageSelectActivity extends AppCompatActivity {

    ActivityImageSelectBinding binding;
    private final int PICK_IMAGE_MULTIPLE = 1;
    ArrayList<String> mArrayUri = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityImageSelectBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        binding.btnSelect.setOnClickListener(v->{
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_MULTIPLE);
        });
        binding.next.setOnClickListener(view1 -> {
            Intent i = new Intent(ImageSelectActivity.this,MainActivity.class);
            i.putStringArrayListExtra("images",mArrayUri);
            startActivity(i);
        });

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_MULTIPLE) {

            if (resultCode == RESULT_OK) {
                //data.getParcelableArrayExtra(name);
                //If Single image selected then it will fetch from Gallery
                if (data.getData() != null) {
                    Uri mImageUri = data.getData();
                    Toast.makeText(this, "You need to select only 3 images", Toast.LENGTH_SHORT).show();

                } else {
                    if (data.getClipData() != null) {
                        if (data.getClipData().getItemCount() < 3 || data.getClipData().getItemCount() > 3) {
                            Toast.makeText(this, "You need to select only 3 images", Toast.LENGTH_SHORT).show();
                        } else {
                            ClipData mClipData = data.getClipData();
                            mArrayUri.clear();
                            for (int i = 0; i < mClipData.getItemCount(); i++) {

                                ClipData.Item item = mClipData.getItemAt(i);
                                Uri uri = item.getUri();
                                mArrayUri.add(uri.toString());
                            }

                            binding.image1.setImageURI(Uri.parse(mArrayUri.get(0)));
                            binding.image2.setImageURI(Uri.parse(mArrayUri.get(1)));
                            binding.image3.setImageURI(Uri.parse(mArrayUri.get(2)));
                            binding.card1.setVisibility(View.VISIBLE);
                        }
                    }

                }

            }

        }


    }
}