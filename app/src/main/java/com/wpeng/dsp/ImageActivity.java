package com.wpeng.dsp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.wpeng.dsp.FFT.FourierTransformer;

import org.w3c.dom.Text;

import java.io.FileNotFoundException;
import java.io.IOException;

public class ImageActivity extends AppCompatActivity {

    private static final int SELECT_IMAGE = 1;
    private Bitmap bitmap = null;
    private Bitmap yourSelectedImage = null;
    public String imagePath="";
    public String imageName="";

    ImageView selImg,conImg,afImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        Button selBtu=findViewById(R.id.sel_btu);
        Button confirm=findViewById(R.id.confirm);
        TextView detail=findViewById(R.id.detail);
        selImg=findViewById(R.id.sel_img);
        conImg=findViewById(R.id.con_img);
        afImg=findViewById(R.id.af_img);

        selBtu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_PICK);
                i.setType("image/*");
                startActivityForResult(i, SELECT_IMAGE);
            }
        });
        confirm.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                Toast.makeText(ImageActivity.this,"In the calculation...",Toast.LENGTH_SHORT).show();
                FourierTransformer fourierTransformer=new FourierTransformer();
                Bitmap co_bitmap=fourierTransformer.convert(bitmap);
                Bitmap af_bitmap=fourierTransformer.recover(bitmap,false);

                conImg.setImageBitmap(co_bitmap);
                afImg.setImageBitmap(af_bitmap);

                detail.setText("Image Detail\n" +
                        "select image:\n" +
                        "height:"+bitmap.getHeight()+"\n"+
                        "width:"+bitmap.getWidth()+ "\n"+
                        "ByteCount:\n"+bitmap.getByteCount()+ "\n"+
                        "af image:\n" +
                        "height:"+af_bitmap.getHeight()+"\n"+
                        "width:"+af_bitmap.getWidth()+ "\n"+
                        "ByteCount:\n"+af_bitmap.getByteCount());
            }
        });

    }

    /*
    图片回调函数
     */
    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();

            try
            {
                if (requestCode == SELECT_IMAGE) {
                    bitmap = decodeUri(selectedImage);
                    yourSelectedImage = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                    //imageView.setImageBitmap(bitmap);

                    String[] proj = {MediaStore.Images.Media.DATA};
                    Cursor cursor = managedQuery(selectedImage, proj, null, null, null);
                    //获得用户选择的图片的索引值
                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    //将光标移至开头 ，这个很重要，不小心很容易引起越界
                    cursor.moveToFirst();
                    //最后根据索引值获取图片路径
                    imagePath = cursor.getString(column_index);
                    imageName = imagePath.substring(imagePath.lastIndexOf("/")+1);

                    selImg.setImageBitmap(bitmap);
                }
            }
            catch (FileNotFoundException e)
            {
                Log.e("MainActivity", "FileNotFoundException");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private Bitmap decodeUri(Uri selectedImage) throws FileNotFoundException
    {
        // Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o);

        // The new size we want to scale to
        final int REQUIRED_SIZE = 640;

        // Find the correct scale value. It should be the power of 2.
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp / 2 < REQUIRED_SIZE
                    || height_tmp / 2 < REQUIRED_SIZE) {
                break;
            }
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o2);

        // Rotate according to EXIF
        int rotate = 0;
        try
        {
            ExifInterface exif = new ExifInterface(getContentResolver().openInputStream(selectedImage));
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }
        }
        catch (IOException e)
        {
            Log.e("MainActivity", "ExifInterface IOException");
        }

        Matrix matrix = new Matrix();
        matrix.postRotate(rotate);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }
}