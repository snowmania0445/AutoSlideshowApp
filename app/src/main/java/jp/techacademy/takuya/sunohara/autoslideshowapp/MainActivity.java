package jp.techacademy.takuya.sunohara.autoslideshowapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.util.Log;


public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_CODE = 100;

    Button previousButton;
    Button startStopButton;
    Button nextButton;

    Uri [] imageUriArray; //取得したURIを配列に格納

    int currentImageIndex = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        previousButton = (Button)findViewById(R.id.previousButton);
        startStopButton = (Button)findViewById(R.id.startStopButton);
        nextButton = (Button)findViewById(R.id.nextButton);

        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContents();
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
            }
            // Android 5系以下の場合
        } else {
            getContents();
        }

        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //getPreviousImage();
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNextImage();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContents();
                }
                break;
            default:
                break;
        }
    }

    private void getContents() {

        // 画像の情報を取得する
        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
                null, // 項目(null = 全項目)
                null, // フィルタ条件(null = フィルタなし)
                null, // フィルタ用パラメータ
                null // ソート (null ソートなし)
        );

        int cursorLength = cursor.getCount();
        int arraySubstIndex = -1; //配列への代入用のインデックス
        imageUriArray = new Uri[cursorLength];

        if (cursor.moveToFirst()) {
            do {
                int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                Long id = cursor.getLong(fieldIndex);
                Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

                arraySubstIndex += 1;
                imageUriArray[arraySubstIndex] = imageUri;

            } while (cursor.moveToNext());
        }
        cursor.close();

        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setImageURI(imageUriArray[0]);
    }

    private void getNextImage () {
        //現在の画像のインデックスがURI（画像）の総数と等しい（=最後の画像を表示している）時、先頭の画像を表示する。それ以外の時は画像のインデックスを1進めて表示する。
        if ((currentImageIndex + 1) == imageUriArray.length) {
            currentImageIndex = 0;
            ImageView imageView = (ImageView) findViewById(R.id.imageView);
            imageView.setImageURI(imageUriArray[currentImageIndex]);
        } else {
            currentImageIndex += 1;
            ImageView imageView = (ImageView) findViewById(R.id.imageView);
            imageView.setImageURI(imageUriArray[currentImageIndex]);
        }
    }

    /*private void getPreviousImage(Cursor cursor) {

        if (cursor.moveToPrevious()) {
            int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
            Long id = cursor.getLong(fieldIndex);
            Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

            ImageView imageView = (ImageView) findViewById(R.id.imageView);
            imageView.setImageURI(imageUri);
            Log.d("DEBUG", "前の画像へ");
        } else if (cursor.moveToFirst()) {
            int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
            Long id = cursor.getLong(fieldIndex);
            Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

            ImageView imageView = (ImageView) findViewById(R.id.imageView);
            imageView.setImageURI(imageUri);
            Log.d("DEBUG", "最後の画像へ");
        }
        cursor.close();
    }*/
}
