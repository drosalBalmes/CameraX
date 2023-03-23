package com.example.suitemultimedia;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.core.VideoCapture;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

public class Fotos extends AppCompatActivity implements View.OnClickListener {

    PreviewView previewView;
    private ListenableFuture<ProcessCameraProvider> cameraProviderListenableFuture;
    Button takePicture, recording;
    private ImageCapture imageCapture;
    private VideoCapture videoCapture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fotos);
        takePicture = findViewById(R.id.image_capture_button);
        recording = findViewById(R.id.video_capture_button);
        previewView = findViewById(R.id.viewFinder);

        takePicture.setOnClickListener(this);
        recording.setOnClickListener(this);

        cameraProviderListenableFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderListenableFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderListenableFuture.get();
                startCameraX(cameraProvider);
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, getExecutor());
    }

    private Executor getExecutor() {
        return ContextCompat.getMainExecutor(this);
    }

    @SuppressLint("RestrictedApi")
    private void startCameraX(ProcessCameraProvider cameraProvider) {
        cameraProvider.unbindAll();

        // Camera Selector use case
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        Preview preview = new Preview.Builder().build();

        // Preview use case
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        // Image capture use case
        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build();

        //Video capture use case
        videoCapture = new VideoCapture.Builder()
                .setVideoFrameRate(30)
                .build();
        cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageCapture, videoCapture);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_capture_button:
                capturePhoto();
                break;
            case R.id.video_capture_button:
                if (recording.getText().equals("Start recording")) {
                    recording.setText("Stop recording");
                    recordVideo();
                } else {
                    recording.setText("Start recording");
                    videoCapture.stopRecording();
                }
                break;
        }
    }

    @SuppressLint("RestrictedApi")
    private void recordVideo() {
        //El video es guardara en el directori segons les variables d'entorn
        if (videoCapture != null) {
            File vidDir = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                vidDir = getExternalFilesDir(Environment.DIRECTORY_RECORDINGS);
            }

            if (!vidDir.exists()) {
                vidDir.mkdir();
            }
            Date date = new Date();
            String timestamp = String.valueOf(date.getTime());
            String vidFilePath = vidDir.getAbsolutePath() + "/" + timestamp + ".mp4";

            File vidFile = new File(vidFilePath);

            //Uri test = Uri.parse(vidFilePath);

            // Agrega la foto a la galería de medios
            //ContentResolver resolver = getContentResolver();
            //ContentValues values = new ContentValues();
            //values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
            //values.put(MediaStore.Images.Media.MIME_TYPE, "video/mp4");
            //values.put(MediaStore.MediaColumns.DATA, String.valueOf(test));
            //values.put(MediaStore.Images.Media.IS_PENDING,0);
            //getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            //checkPerms
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            videoCapture.startRecording(
                    new VideoCapture.OutputFileOptions.Builder(vidFile).build(),
                    getExecutor(),
                    new VideoCapture.OnVideoSavedCallback() {
                        @Override
                        public void onVideoSaved(@NonNull VideoCapture.OutputFileResults outputFileResults) {
                            Toast.makeText(Fotos.this, "Video has been saved succesfuly", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(int videoCaptureError, @NonNull String message, @Nullable Throwable cause) {
                            Toast.makeText(Fotos.this, "Error saving the video: " + message, Toast.LENGTH_SHORT).show();
                        }
                    }
            );
        }
    }

    private void capturePhoto() {
        // La foto es guardará en el directorio segons les variables d'entorn
        // File photoDir = new File("/storage/emulated/0/Pictures");
        File photoDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (!photoDir.exists()) {
            photoDir.mkdir();
        }
        Date date = new Date();
        String timestamp = String.valueOf(date.getTime());
        String photoFilePath = photoDir.getAbsolutePath() + "/" + timestamp + ".jpg";
        File photoFile = new File(photoFilePath);
        Uri test = Uri.parse(photoFilePath);

        // Agrega la foto a la galería de medios
        ContentResolver resolver = getContentResolver();
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.MediaColumns.DATA, String.valueOf(test));
        values.put(MediaStore.Images.Media.IS_PENDING,0);
        getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        // Hace la foto
        imageCapture.takePicture(
                new ImageCapture.OutputFileOptions.Builder(resolver,MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values).build(),
                getExecutor(),
                new ImageCapture.OnImageSavedCallback() {
                    // Si la guarda:
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {

                        Toast.makeText(Fotos.this, "Photo saved", Toast.LENGTH_SHORT).show();
                    }

                    // Si da error:
                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.d("aaaaaaaaa",exception.getMessage());
                        Toast.makeText(Fotos.this, "Error: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }


    }