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
import java.util.Timer;
import java.util.TimerTask;
import android.os.Handler;
import android.support.design.widget.Snackbar;


public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_CODE = 100;

    Button previousButton;
    Button startStopButton;
    Button nextButton;

    Uri [] imageUriArray; //取得したURIを配列に格納
    int currentImageIndex = 0; //表示している画像のimageUriArray配列におけるインデックス

    Timer mTimer;

    Handler mHandler = new Handler();

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        previousButton = (Button)findViewById(R.id.previousButton);
        startStopButton = (Button)findViewById(R.id.startStopButton);
        nextButton = (Button)findViewById(R.id.nextButton);

        // permission Android 6.0以降の場合
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

        //戻るボタン
        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //permissionが拒否された場合はsnackbarでメッセージを出す。nextButton,startStopButtonについても同様。
                if (imageUriArray != null) {
                    getPreviousImage();
                } else {
                    snackbarMessage(v);
                }
            }
        });

        //進むボタン
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageUriArray != null) {
                    getNextImage();
                } else {
                    snackbarMessage(v);
                }
            }
        });

        //再生・停止ボタン
        startStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageUriArray != null) {
                    startStopSlideshow();
                } else {
                    snackbarMessage(v);
                }
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
                null,
                null,
                null,
                null
        );

        int numberOfImages = cursor.getCount();  //データ数＝カーソルの行数（imageUriArrayの宣言用）
        imageUriArray = new Uri[numberOfImages];
        int arraySubstIndex = -1; //取得したURIを配列へ代入するためのインデックス

        if (cursor.moveToFirst()) {
            //URIを取得し、順次配列に代入
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
        imageView.setImageURI(imageUriArray[currentImageIndex]);
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

    private void getPreviousImage() {
        //現在の画像のインデックスが0なら、最後の画像を表示する。それ以外は画像のインデックスを1戻して表示する。
        if (currentImageIndex  == 0) {
            currentImageIndex = imageUriArray.length -1;
            ImageView imageView = (ImageView) findViewById(R.id.imageView);
            imageView.setImageURI(imageUriArray[currentImageIndex]);
        } else {
            currentImageIndex -= 1;
            ImageView imageView = (ImageView) findViewById(R.id.imageView);
            imageView.setImageURI(imageUriArray[currentImageIndex]);
        }
    }

    private void startStopSlideshow() {
        if (mTimer == null) {
            startStopButton.setText("停止");
            previousButton.setEnabled(false);
            nextButton.setEnabled(false);
            // タイマーの作成
            mTimer = new Timer();
            // タイマーの始動
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            getNextImage();
                        }
                    });
                }
            }, 2000, 2000);
        } else {
            startStopButton.setText("再生");
            previousButton.setEnabled(true);
            nextButton.setEnabled(true);

            //タイマーを破棄
            mTimer.cancel();
            mTimer = null;
        }
    }

    private void snackbarMessage(View v) {
        Snackbar.make(v, "画像の使用を承認してください。", Snackbar.LENGTH_LONG).show();
    }
}
