//--------Seperator - Implementation starts from here


/* Copyright 2017 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

package com.example.android.tflitecamerademo;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.HardwarePropertiesManager;
import android.os.Process;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v13.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v13.app.FragmentCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/** Basic fragments for the Camera. */
public class Camera2BasicFragment extends Fragment
        implements FragmentCompat.OnRequestPermissionsResultCallback {

    /** Tag for the {@link Log}. */
    private static final String TAG = "TfLiteCameraDemo";

    private static final String FRAGMENT_DIALOG = "dialog";

    private static final String HANDLE_THREAD_NAME = "CameraBackground";
    private static final String HANDLE_THREAD_NAME_2 = "CameraBackground2";
    private static final String HANDLE_THREAD_NAME_3 = "CameraBackground3";
    private static final String HANDLE_THREAD_NAME_4 = "CameraBackground4";

    private static final int PERMISSIONS_REQUEST_CODE = 1;

    int size_to_consider;

    Bitmap bmFrame;

    Bitmap resizedBitmap;


    private final Object lock = new Object();
    private boolean runClassifier = false;
    private boolean runClassifier2 = false;
    private boolean runClassifier3 = false;
    private boolean runClassifier4 = false;
    private boolean checkedPermissions = false;
    private TextView textView;
    private TextView textView2;
    private TextView textView3;
    private TextView textView4;
    private Button startButton;
    private Button startButton2;
    private NumberPicker np;
    private ImageClassifier classifier;
    private ImageClassifier classifier2;
    private ImageClassifier classifier3;
    private ImageClassifier classifier4;




    private ImageClassifier inceptionV1Cpu;
    private ImageClassifier inceptionV1Gpu;
    private ImageClassifier inceptionV1Dsp;

    private ImageClassifier inceptionV2Cpu;
    private ImageClassifier inceptionV2Gpu;
    private ImageClassifier inceptionV2Dsp;

    private ImageClassifier inceptionV3Cpu;
    private ImageClassifier inceptionV3Gpu;
    private ImageClassifier inceptionV3Dsp;

    private ImageClassifier mobilenetV1Cpu;
    private ImageClassifier mobilenetV1Gpu;
    private ImageClassifier mobilenetV1Dsp;

    private ImageClassifier mobilenetV2Cpu;
    private ImageClassifier mobilenetV2Gpu;
    private ImageClassifier mobilenetV2Dsp;



    private ListView deviceView;
    private ListView modelView;

    boolean acquired;


    /** Max preview width that is guaranteed by Camera2 API */
    private static final int MAX_PREVIEW_WIDTH = 1920;

    /** Max preview height that is guaranteed by Camera2 API */
    private static final int MAX_PREVIEW_HEIGHT = 1080;

    private MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
    String uri = "/storage/emulated/0/DCIM/Camera/VID_20210331_131355.mp4";

    /**
     * {@link TextureView.SurfaceTextureListener} handles several lifecycle events on a {@link
     * TextureView}.
     */
    private final TextureView.SurfaceTextureListener surfaceTextureListener =
            new TextureView.SurfaceTextureListener() {

                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
                    openCamera(width, height);
                }

                @Override
                public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
                    configureTransform(width, height);
                }

                @Override
                public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
                    return true;
                }

                @Override
                public void onSurfaceTextureUpdated(SurfaceTexture texture) {}
            };

    // Model parameter constants.
    private String gpu;
    private String cpu;
    private String nnApi;
    private String mobilenetV1Quant;
    private String mobilenetV1Float;



    /** ID of the current {@link CameraDevice}. */
    private String cameraId;

    /** An {@link AutoFitTextureView} for camera preview. */
    private AutoFitTextureView textureView;

    /** A {@link CameraCaptureSession } for camera preview. */
    private CameraCaptureSession captureSession;

    /** A reference to the opened {@link CameraDevice}. */
    private CameraDevice cameraDevice;

    /** The {@link android.util.Size} of camera preview. */
    private Size previewSize;

    /** {@link CameraDevice.StateCallback} is called when {@link CameraDevice} changes its state. */
    private final CameraDevice.StateCallback stateCallback =
            new CameraDevice.StateCallback() {

                @Override
                public void onOpened(@NonNull CameraDevice currentCameraDevice) {
                    // This method is called when the camera is opened.  We start camera preview here.
                    cameraOpenCloseLock.release();
                    cameraDevice = currentCameraDevice;
                    createCameraPreviewSession();
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice currentCameraDevice) {
                    cameraOpenCloseLock.release();
                    currentCameraDevice.close();
                    cameraDevice = null;
                }

                @Override
                public void onError(@NonNull CameraDevice currentCameraDevice, int error) {
                    cameraOpenCloseLock.release();
                    currentCameraDevice.close();
                    cameraDevice = null;
                    Activity activity = getActivity();
                    if (null != activity) {
                        activity.finish();
                    }
                }
            };

    private ArrayList<String> deviceStrings = new ArrayList<String>();
    private ArrayList<String> modelStrings = new ArrayList<String>();

    /** Current indices of device and model. */
    int currentDevice = -1;

    int currentModel = -1;

    int currentNumThreads = 4;


    Long thread1[] = new Long[15];
    int i1=0;
    Long total = 0l;



    Long thread2[] = new Long[15];
    int i2=0;
    Long total2 = 0l;


    Long thread3[] = new Long[15];
    int i3=0;
    Long total3 = 0l;



    Long thread4[] = new Long[15];
    int i4=0;
    Long total4 = 0l;



    /** An additional thread for running tasks that shouldn"t block the UI. */
    private HandlerThread backgroundThread;
    private HandlerThread backgroundThread2;
    private HandlerThread backgroundThread3;
    private HandlerThread backgroundThread4;
    private HandlerThread backgroundThreadInit;

    /** A {@link Handler} for running tasks in the background. */
    private Handler backgroundHandler;
    private Handler backgroundHandler2;
    private Handler backgroundHandler3;
    private Handler backgroundHandler4;
    private Handler backgroundHandlerInit;

    /** An {@link ImageReader} that handles image capture. */
    private ImageReader imageReader;

    /** {@link CaptureRequest.Builder} for the camera preview */
    private CaptureRequest.Builder previewRequestBuilder;

    /** {@link CaptureRequest} generated by {@link #previewRequestBuilder} */
    private CaptureRequest previewRequest;

    /** A {@link Semaphore} to prevent the app from exiting before closing the camera. */
    private Semaphore cameraOpenCloseLock = new Semaphore(1);

    /** A {@link CameraCaptureSession.CaptureCallback} that handles events related to capture. */
    private CameraCaptureSession.CaptureCallback captureCallback =
            new CameraCaptureSession.CaptureCallback() {

                @Override
                public void onCaptureProgressed(
                        @NonNull CameraCaptureSession session,
                        @NonNull CaptureRequest request,
                        @NonNull CaptureResult partialResult) {}

                @Override
                public void onCaptureCompleted(
                        @NonNull CameraCaptureSession session,
                        @NonNull CaptureRequest request,
                        @NonNull TotalCaptureResult result) {}
            };

    /**
     * Shows a {@link Toast} on the UI thread for the classification results.
     *
     * @param text The message to show
     */
    private void showToast(String s) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        SpannableString str1 = new SpannableString(s);
        builder.append(str1);
        showToast(builder);
    }

    private void showToast(SpannableStringBuilder builder) {
        final Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            textView.setText(builder, TextView.BufferType.SPANNABLE);
                        }
                    });
        }
    }

    /**
     * Resizes image.
     *
     * Attempting to use too large a preview size could  exceed the camera bus" bandwidth limitation,
     * resulting in gorgeous previews but the storage of garbage capture data.
     *
     * Given {@code choices} of {@code Size}s supported by a camera, choose the smallest one that is
     * at least as large as the respective texture view size, and that is at most as large as the
     * respective max size, and whose aspect ratio matches with the specified value. If such size
     * doesn"t exist, choose the largest one that is at most as large as the respective max size, and
     * whose aspect ratio matches with the specified value.
     *
     * @param choices The list of sizes that the camera supports for the intended output class
     * @param textureViewWidth The width of the texture view relative to sensor coordinate
     * @param textureViewHeight The height of the texture view relative to sensor coordinate
     * @param maxWidth The maximum width that can be chosen
     * @param maxHeight The maximum height that can be chosen
     * @param aspectRatio The aspect ratio
     * @return The optimal {@code Size}, or an arbitrary one if none were big enough
     */
    private static Size chooseOptimalSize(
            Size[] choices,
            int textureViewWidth,
            int textureViewHeight,
            int maxWidth,
            int maxHeight,
            Size aspectRatio) {

        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Size> bigEnough = new ArrayList<>();
        // Collect the supported resolutions that are smaller than the preview Surface
        List<Size> notBigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getWidth() <= maxWidth
                    && option.getHeight() <= maxHeight
                    && option.getHeight() == option.getWidth() * h / w) {
                if (option.getWidth() >= textureViewWidth && option.getHeight() >= textureViewHeight) {
                    bigEnough.add(option);
                } else {
                    notBigEnough.add(option);
                }
            }
        }

        // Pick the smallest of those big enough. If there is no one big enough, pick the
        // largest of those not big enough.
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else if (notBigEnough.size() > 0) {
            return Collections.max(notBigEnough, new CompareSizesByArea());
        } else {
            Log.e(TAG, "Couldn't find any suitable preview size");
            return choices[0];
        }
    }

    public static Camera2BasicFragment newInstance() {
        return new Camera2BasicFragment();
    }

    /** Layout the preview and buttons. */
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera2_basic, container, false);
    }


    private void loadModel(String model, String device ) {

        runClassifier = true;
        runClassifier2 = true;

        if ( model.equals("inception_v1")) {

            if ( device.equals("CPU") ) {
                if ( inceptionV1Cpu != null ) {
                    inceptionV1Cpu.close();
                    inceptionV1Cpu = null;
                }

                try {
                    inceptionV1Cpu = new ImageClassifierInceptionV1Quant(getActivity());
                    inceptionV1Cpu.setNumThreads(currentNumThreads);
                } catch (Exception e) {

                }
            }

            else if ( device.equals("GPU") ) {
                if ( inceptionV1Gpu != null ) {
                    inceptionV1Gpu.close();
                    inceptionV1Gpu = null;
                }

                try {
                    inceptionV1Gpu = new ImageClassifierInceptionV1Quant(getActivity());
                    inceptionV1Gpu.setNumThreads(currentNumThreads);

                    inceptionV1Gpu.useGpu();
                } catch (Exception e) {

                }
            }

            else if ( device.equals("DSP") ) {
                if ( inceptionV1Dsp != null ) {
                    inceptionV1Dsp.close();
                    inceptionV1Dsp = null;
                }

                try {
                    inceptionV1Dsp = new ImageClassifierInceptionV1Quant(getActivity());
                    inceptionV1Dsp.setNumThreads(currentNumThreads);

                    inceptionV1Dsp.useDSP();
                } catch (Exception e) {

                }
            }


        }


        else if ( model.equals("inception_v2")) {

            if ( device.equals("CPU") ) {
                if ( inceptionV2Cpu != null ) {
                    inceptionV2Cpu.close();
                    inceptionV2Cpu = null;
                }

                try {
                    inceptionV2Cpu = new ImageClassifierInceptionV2Quant(getActivity());
                    inceptionV2Cpu.setNumThreads(currentNumThreads);
                } catch (Exception e) {

                }
            }

            else if ( device.equals("GPU") ) {
                if ( inceptionV2Gpu != null ) {
                    inceptionV2Gpu.close();
                    inceptionV2Gpu = null;
                }

                try {
                    inceptionV2Gpu = new ImageClassifierInceptionV2Quant(getActivity());
                    inceptionV2Gpu.setNumThreads(currentNumThreads);

                    inceptionV2Gpu.useGpu();
                } catch (Exception e) {

                }
            }

            else if ( device.equals("DSP") ) {
                if ( inceptionV2Dsp != null ) {
                    inceptionV2Dsp.close();
                    inceptionV2Dsp = null;
                }

                try {
                    inceptionV2Dsp = new ImageClassifierInceptionV2Quant(getActivity());
                    inceptionV2Dsp.setNumThreads(currentNumThreads);

                    inceptionV2Dsp.useDSP();
                } catch (Exception e) {

                }
            }


        }

        else if ( model.equals("inception_v3")) {

            if ( device.equals("CPU") ) {
                if ( inceptionV3Cpu != null ) {
                    inceptionV3Cpu.close();
                    inceptionV3Cpu = null;
                }

                try {
                    inceptionV3Cpu = new ImageClassifierFloatInception(getActivity());
                    inceptionV3Cpu.setNumThreads(currentNumThreads);
                } catch (Exception e) {

                }
            }

            else if ( device.equals("GPU") ) {
                if ( inceptionV3Gpu != null ) {
                    inceptionV3Gpu.close();
                    inceptionV3Gpu = null;
                }

                try {
                    inceptionV3Gpu = new ImageClassifierFloatInception(getActivity());
                    inceptionV3Gpu.setNumThreads(currentNumThreads);

                    inceptionV3Gpu.useGpu();
                } catch (Exception e) {

                }
            }

            else if ( device.equals("DSP") ) {
                if ( inceptionV3Dsp != null ) {
                    inceptionV3Dsp.close();
                    inceptionV3Dsp = null;
                }

                try {
                    inceptionV3Dsp = new ImageClassifierFloatInception(getActivity());
                    inceptionV3Dsp.setNumThreads(currentNumThreads);

                    inceptionV3Dsp.useDSP();
                } catch (Exception e) {

                }
            }


        }

        else if ( model.equals("mobilenet_v1")) {

            if ( device.equals("CPU") ) {
                if ( mobilenetV1Cpu != null ) {
                    mobilenetV1Cpu.close();
                    mobilenetV1Cpu = null;
                }

                try {
                    mobilenetV1Cpu = new ImageClassifierFloatMobileNet(getActivity());
                    mobilenetV1Cpu.setNumThreads(currentNumThreads);
                } catch (Exception e) {

                }
            }

            else if ( device.equals("GPU") ) {
                if ( mobilenetV1Gpu != null ) {
                    mobilenetV1Gpu.close();
                    mobilenetV1Gpu = null;
                }

                try {
                    mobilenetV1Gpu = new ImageClassifierFloatMobileNet(getActivity());
                    mobilenetV1Gpu.setNumThreads(currentNumThreads);

                    mobilenetV1Gpu.useGpu();
                } catch (Exception e) {

                }
            }

            else if ( device.equals("DSP") ) {
                if ( mobilenetV1Dsp != null ) {
                    mobilenetV1Dsp.close();
                    mobilenetV1Dsp = null;
                }

                try {
                    mobilenetV1Dsp = new ImageClassifierFloatMobileNet(getActivity());
                    mobilenetV1Dsp.setNumThreads(currentNumThreads);

                    mobilenetV1Dsp.useDSP();
                } catch (Exception e) {

                }
            }


        }

        else if ( model.equals("mobilenet_v2")) {

            if ( device.equals("CPU") ) {
                if ( mobilenetV2Cpu != null ) {
                    mobilenetV2Cpu.close();
                    mobilenetV2Cpu = null;
                }

                try {
                    mobilenetV2Cpu = new ImageClassifierFloatMobileNetV2(getActivity());
                    mobilenetV2Cpu.setNumThreads(currentNumThreads);
                } catch (Exception e) {

                }
            }

            else if ( device.equals("GPU") ) {
                if ( mobilenetV2Gpu != null ) {
                    mobilenetV2Gpu.close();
                    mobilenetV2Gpu = null;
                }

                try {
                    mobilenetV2Gpu = new ImageClassifierFloatMobileNetV2(getActivity());
                    mobilenetV2Gpu.setNumThreads(currentNumThreads);

                    mobilenetV2Gpu.useGpu();
                } catch (Exception e) {

                }
            }

            else if ( device.equals("DSP") ) {
                if ( mobilenetV2Dsp != null ) {
                    mobilenetV2Dsp.close();
                    mobilenetV2Dsp = null;
                }

                try {
                    mobilenetV2Dsp = new ImageClassifierFloatMobileNetV2(getActivity());
                    mobilenetV2Dsp.setNumThreads(currentNumThreads);

                    mobilenetV2Dsp.useDSP();
                } catch (Exception e) {

                }
            }


        }




    }


    private void updateActiveModel() {
        // Get UI information before delegating to background
        final int modelIndex = modelView.getCheckedItemPosition();
        final int deviceIndex = deviceView.getCheckedItemPosition();
        final int numThreads = np.getValue();
        currentModel = modelIndex;
        currentDevice = deviceIndex;
        currentNumThreads = numThreads;

//        if ( classifier != null ) {
//            classifier.close();
//            classifier = null;
//        }
//
//        try {
//            classifier = new ImageClassifierFloatMobileNet(getActivity());
//            classifier.setNumThreads(currentNumThreads);
//        } catch (Exception e) {
//
//        }
//
//        if ( classifier2 != null ) {
//            classifier2.close();
//            classifier2 = null;
//        }
//
//        try {
//            classifier2 = new ImageClassifierQuantizedMobileNet(getActivity());
//            classifier2.setNumThreads(currentNumThreads);
//        } catch (Exception e) {
//
//        }
//
//        if ( classifier3 != null ) {
//            classifier3.close();
//            classifier3 = null;
//        }
//
//        try {
//            classifier3 = new ImageClassifierFloatInception(getActivity());
//            classifier3.setNumThreads(currentNumThreads);
//            classifier3.useGpu();
//        } catch (Exception e) {
//
//        }
//
//        if ( classifier4 != null ) {
//            classifier4.close();
//
//            classifier4 = null;
//        }
//
//        try {
//            classifier4 = new ImageClassifierInceptionV1Quant(getActivity());
//            classifier4.setNumThreads(currentNumThreads);
//            classifier4.useNNAPI();
//            // classifier4.useGpu();
//        } catch (Exception e) {
//
//        }


        if ( classifier != null ) {
            classifier.close();
            classifier = null;
        }

        try {
            classifier = new ImageClassifierInceptionV1Quant(getActivity());
            classifier.setNumThreads(currentNumThreads);
        } catch (Exception e) {

        }

        if ( classifier2 != null ) {
            classifier2.close();
            classifier2 = null;
        }

        try {
            classifier2 = new ImageClassifierQuantizedMobileNet(getActivity());
            classifier2.setNumThreads(currentNumThreads);
        } catch (Exception e) {

        }

        if ( classifier3 != null ) {
            classifier3.close();
            classifier3 = null;
        }

        try {
            classifier3 = new ImageClassifierFloatInception(getActivity());
            classifier3.setNumThreads(currentNumThreads);
            classifier3.useGpu();
        } catch (Exception e) {

        }

        if ( classifier4 != null ) {
            classifier4.close();

            classifier4 = null;
        }

        try {
            classifier4 = new ImageClassifierInceptionV1Quant(getActivity());
            classifier4.setNumThreads(currentNumThreads);
            classifier4.useNNAPI();
            // classifier4.useGpu();
        } catch (Exception e) {

        }



//        backgroundHandler.post(
//                () -> {
//                    if (modelIndex == currentModel
//                            && deviceIndex == currentDevice
//                            && numThreads == currentNumThreads) {
//                        return;
//                    }
//
//
//                    // Disable classifier while updating
//                    if (classifier != null) {
//                        classifier.close();
//                        classifier = null;
//                    }
//
//                    if (classifier2 != null) {
//                        classifier2.close();
//                        classifier2 = null;
//                    }
//
//                    if (classifier3 != null) {
//                        classifier3.close();
//                        classifier3 = null;
//                    }
//
//
//
//                    // Lookup names of parameters.
//                    String model = modelStrings.get(modelIndex);
//                    String device = deviceStrings.get(deviceIndex);
//
//                    Log.i(TAG, "Changing model to " + model + " device " + device);
//
//                    // Try to load model.
//                    try {
//                        if (model.equals(mobilenetV1Quant)) {
//                            classifier = new ImageClassifierQuantizedMobileNet(getActivity());
//                            classifier2 = new ImageClassifierFloatMobileNet(getActivity());
//                            classifier3 = new ImageClassifierFloatInception(getActivity());
//                        } else if (model.equals(mobilenetV1Float)) {
//                            classifier = new ImageClassifierFloatMobileNet(getActivity());
//                            classifier2 = new ImageClassifierFloatMobileNet(getActivity());
//                            classifier3 = new ImageClassifierFloatInception(getActivity());
//
//                        } else {
//                            showToast("Failed to load model");
//                        }
//                    } catch (IOException e) {
//                        Log.d(TAG, "Failed to load", e);
//                        classifier = null;
//                        classifier2 = null;
//                        classifier3 = null;
//                    }
//
//                    // Customize the interpreter to the type of device we want to use.
//                    if (classifier == null) {
//                        return;
//                    }
//
//                    if (classifier2 == null) {
//                        return;
//                    }
//
//                    if (classifier3 == null) {
//                        return;
//                    }
//
//                    classifier.setNumThreads(numThreads);
//                    classifier2.setNumThreads(numThreads);
//                    classifier3.setNumThreads(numThreads);
//                    if (device.equals(cpu)) {
//                    } else if (device.equals(gpu)) {
//                        classifier.useGpu();
//                        classifier2.useGpu();
//                        //classifier3.useGpu();
//                    } else if (device.equals(nnApi)) {
//                        classifier.useNNAPI();
//                        classifier2.useNNAPI();
//                        //classifier3.useNNAPI();
//                    }
//
//                    classifier3.useGpu();
//                });
    }

    /** Connect the buttons to their event handler. */
    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        gpu = getString(R.string.gpu);
        cpu = getString(R.string.cpu);
        nnApi = getString(R.string.nnapi);
        mobilenetV1Quant = getString(R.string.mobilenetV1Quant);
        mobilenetV1Float = getString(R.string.mobilenetV1Float);

        // Get references to widgets.
        textureView = (AutoFitTextureView) view.findViewById(R.id.texture);
        textView = (TextView) view.findViewById(R.id.text);
        textView2 = (TextView) view.findViewById(R.id.text2);
        textView3 = (TextView) view.findViewById(R.id.text3);
        textView4 = (TextView) view.findViewById(R.id.text4);
        startButton = (Button) view.findViewById(R.id.start_button);
        startButton2 = (Button) view.findViewById(R.id.start_button2);
        deviceView = (ListView) view.findViewById(R.id.device);
        modelView = (ListView) view.findViewById(R.id.model);

        int permissionCheck = ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE);

        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {


            // OPCIONAL(explicaciones de poque pedimos los permisos)
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

            } else {
                //pedir permisos
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        permissionCheck+1);
            }
        }

        System.out.println("Permission check" + "  " + permissionCheck);



        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (textureView.isAvailable()) {
                    //updateActiveModel();
                    //openCamera(textureView.getWidth(), textureView.getHeight());

                    //Load All models

                    loadModel("inception_v1","CPU");
                    loadModel("inception_v1","GPU");
                    loadModel("inception_v1","DSP");

                    loadModel("inception_v2","CPU");
                    loadModel("inception_v2","GPU");
                    loadModel("inception_v2","DSP");

                    loadModel("inception_v3","CPU");
                    loadModel("inception_v3","GPU");
                    loadModel("inception_v3","DSP");

                    loadModel("mobilenet_v1","CPU");
                    loadModel("mobilenet_v1","GPU");
                    loadModel("mobilenet_v1","DSP");

                    loadModel("mobilenet_v2","CPU");
                    loadModel("mobilenet_v2","GPU");
                    loadModel("mobilenet_v2","DSP");



                    mediaMetadataRetriever.setDataSource(uri);

                    bmFrame = mediaMetadataRetriever.getFrameAtTime(10000);

                    resizedBitmap = Bitmap.createScaledBitmap(
                            bmFrame, 224, 224, false);






                } else {
                    textureView.setSurfaceTextureListener(surfaceTextureListener);
                }
            }
        });

        startButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                execList.clear();
                start2();
            }
        });

        // Build list of models
        modelStrings.add(mobilenetV1Quant);
        modelStrings.add(mobilenetV1Float);

        // Build list of devices
        int defaultModelIndex = 0;
        deviceStrings.add(cpu);
        deviceStrings.add(gpu);
        deviceStrings.add(nnApi);

        deviceView.setAdapter(
                new ArrayAdapter<String>(
                        getContext(), R.layout.listview_row, R.id.listview_row_text, deviceStrings));
        deviceView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        deviceView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        updateActiveModel();
                    }
                });
        deviceView.setItemChecked(0, true);

        modelView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        ArrayAdapter<String> modelAdapter =
                new ArrayAdapter<>(
                        getContext(), R.layout.listview_row, R.id.listview_row_text, modelStrings);
        modelView.setAdapter(modelAdapter);
        modelView.setItemChecked(defaultModelIndex, true);
        modelView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        updateActiveModel();
                    }
                });

        np = (NumberPicker) view.findViewById(R.id.np);
        np.setMinValue(1);
        np.setMaxValue(10);
        np.setWrapSelectorWheel(true);
        np.setOnValueChangedListener(
                new NumberPicker.OnValueChangeListener() {
                    @Override
                    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                        updateActiveModel();
                    }
                });

        // Start initial model.
    }

    /** Load the model and labels. */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //startBackgroundThread();
    }

    @Override
    public void onResume() {
        super.onResume();
        //startBackgroundThread();

        // When the screen is turned off and turned back on, the SurfaceTexture is already
        // available, and "onSurfaceTextureAvailable" will not be called. In that case, we can open
        // a camera and start preview from here (otherwise, we wait until the surface is ready in
        // the SurfaceTextureListener).
        if (textureView.isAvailable()) {
            //openCamera(textureView.getWidth(), textureView.getHeight());
        } else {
            //textureView.setSurfaceTextureListener(surfaceTextureListener);
        }
    }

    @Override
    public void onPause() {
        closeCamera();
        stopBackgroundThread();
        stopBackgroundThread2();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (classifier != null) {
            classifier.close();
        }
        super.onDestroy();
    }

    /**
     * Sets up member variables related to camera.
     *
     * @param width The width of available size for camera preview
     * @param height The height of available size for camera preview
     */
    private void setUpCameraOutputs(int width, int height) {
        Activity activity = getActivity();
        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);

                // We don"t use a front facing camera in this sample.
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    continue;
                }

                StreamConfigurationMap map =
                        characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if (map == null) {
                    continue;
                }

                // // For still image captures, we use the largest available size.
                Size largest =
                        Collections.max(
                                Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)), new CompareSizesByArea());
                imageReader =
                        ImageReader.newInstance(
                                largest.getWidth(), largest.getHeight(), ImageFormat.JPEG, /*maxImages*/ 2);

                // Find out if we need to swap dimension to get the preview size relative to sensor
                // coordinate.
                int displayRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
                // noinspection ConstantConditions
                /* Orientation of the camera sensor */
                int sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
                boolean swappedDimensions = false;
                switch (displayRotation) {
                    case Surface.ROTATION_0:
                    case Surface.ROTATION_180:
                        if (sensorOrientation == 90 || sensorOrientation == 270) {
                            swappedDimensions = true;
                        }
                        break;
                    case Surface.ROTATION_90:
                    case Surface.ROTATION_270:
                        if (sensorOrientation == 0 || sensorOrientation == 180) {
                            swappedDimensions = true;
                        }
                        break;
                    default:
                        Log.e(TAG, "Display rotation is invalid: " + displayRotation);
                }

                Point displaySize = new Point();
                activity.getWindowManager().getDefaultDisplay().getSize(displaySize);
                int rotatedPreviewWidth = width;
                int rotatedPreviewHeight = height;
                int maxPreviewWidth = displaySize.x;
                int maxPreviewHeight = displaySize.y;

                if (swappedDimensions) {
                    rotatedPreviewWidth = height;
                    rotatedPreviewHeight = width;
                    maxPreviewWidth = displaySize.y;
                    maxPreviewHeight = displaySize.x;
                }

                if (maxPreviewWidth > MAX_PREVIEW_WIDTH) {
                    maxPreviewWidth = MAX_PREVIEW_WIDTH;
                }

                if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) {
                    maxPreviewHeight = MAX_PREVIEW_HEIGHT;
                }

                previewSize =
                        chooseOptimalSize(
                                map.getOutputSizes(SurfaceTexture.class),
                                rotatedPreviewWidth,
                                rotatedPreviewHeight,
                                maxPreviewWidth,
                                maxPreviewHeight,
                                largest);

                // We fit the aspect ratio of TextureView to the size of preview we picked.
                int orientation = getResources().getConfiguration().orientation;
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    textureView.setAspectRatio(previewSize.getWidth(), previewSize.getHeight());
                } else {
                    textureView.setAspectRatio(previewSize.getHeight(), previewSize.getWidth());
                }

                this.cameraId = cameraId;
                return;
            }
        } catch (CameraAccessException e) {
            Log.e(TAG, "Failed to access Camera", e);
        } catch (NullPointerException e) {
            // Currently an NPE is thrown when the Camera2API is used but not supported on the
            // device this code runs.
            ErrorDialog.newInstance(getString(R.string.camera_error))
                    .show(getChildFragmentManager(), FRAGMENT_DIALOG);
        }
    }

    private String[] getRequiredPermissions() {
        Activity activity = getActivity();
        try {
            PackageInfo info =
                    activity
                            .getPackageManager()
                            .getPackageInfo(activity.getPackageName(), PackageManager.GET_PERMISSIONS);
            String[] ps = info.requestedPermissions;
            if (ps != null && ps.length > 0) {
                return ps;
            } else {
                return new String[0];
            }
        } catch (Exception e) {
            return new String[0];
        }
    }

    /** Opens the camera specified by {@link Camera2BasicFragment#cameraId}. */
    private void openCamera(int width, int height) {
        if (!checkedPermissions && !allPermissionsGranted()) {
            FragmentCompat.requestPermissions(this, getRequiredPermissions(), PERMISSIONS_REQUEST_CODE);
            return;
        } else {
            checkedPermissions = true;
        }
        setUpCameraOutputs(width, height);
        configureTransform(width, height);
        Activity activity = getActivity();
        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {

            if (!cameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            manager.openCamera(cameraId, stateCallback, backgroundHandler);
        } catch (CameraAccessException e) {
            Log.e(TAG, "Failed to open Camera", e);
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
        }
    }

    private boolean allPermissionsGranted() {
        for (String permission : getRequiredPermissions()) {
            if (getActivity().checkPermission(permission, Process.myPid(), Process.myUid())
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public static float cpuTemperature1(String pathCommand) {
        java.lang.Process process;
        try {
            process = Runtime.getRuntime().exec(pathCommand);
            process.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = reader.readLine();
            if (line != null) {
                float temp = Float.parseFloat(line);
                return temp / 1000.0f;
            } else {
                return 51.0f;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0f;
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /** Closes the current {@link CameraDevice}. */
    private void closeCamera() {
        try {
            cameraOpenCloseLock.acquire();
            if (null != captureSession) {
                captureSession.close();
                captureSession = null;
            }
            if (null != cameraDevice) {
                cameraDevice.close();
                cameraDevice = null;
            }
            if (null != imageReader) {
                imageReader.close();
                imageReader = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            cameraOpenCloseLock.release();
        }
    }

    Timer mTimer1;
    TimerTask mTt1;
    Handler mTimerHandler = new Handler();

    Timer mTimer2;
    TimerTask mTt2;
    Handler mTimerHandler2 = new Handler();

    List<String> execList = new ArrayList<String>();


    Timer mTimer3;
    TimerTask mTt3;
    Handler mTimerHandler3 = new Handler();

    Timer mTimer4;
    TimerTask mTt4;
    Handler mTimerHandler4 = new Handler();

    private void stopTimer(){
        if(mTimer1 != null){
            mTimer1.cancel();
            mTimer1.purge();
        }
    }






    private void startTimer2(HandlerThread backgroundThread3){
        mTimer2 = new Timer();
        mTt2 = new TimerTask() {
            public void run() {
                mTimerHandler2.post(new Runnable() {
                    public void run(){
                        //TODO
                        Log.d("Thread 3","Started");

                        if( backgroundThread3.isAlive() ) {

                        } else {
                            backgroundThread3.start();

                        }

                        Log.d("Thread 3 Priority", Integer.toString(backgroundThread3.getPriority()));


                        backgroundHandler3 = new Handler(backgroundThread3.getLooper());
                        synchronized (lock) {
                            runClassifier3 = true;
                        }
                        //backgroundHandler3.post(periodicClassify3);

                    }
                });
            }
        };

        mTimer2.schedule(mTt2, 5);
    }

    private void startTimer3(HandlerThread backgroundThread4){
        mTimer3 = new Timer();
        mTt3 = new TimerTask() {
            public void run() {
                mTimerHandler3.post(new Runnable() {
                    public void run(){
                        //TODO
                        Log.d("Thread 4","Started");

                        if( backgroundThread4.isAlive() ) {

                        } else {
                            backgroundThread4.start();

                        }

                        Log.d("Thread 4 Priority", Integer.toString(backgroundThread4.getPriority()));

                        backgroundHandler4 = new Handler(backgroundThread4.getLooper());
                        synchronized (lock) {
                            runClassifier4 = true;
                        }
                        //backgroundHandler4.post(periodicClassify4);

                    }
                });
            }
        };

        mTimer3.schedule(mTt3, 0);
    }





    /** Starts a background thread and its {@link Handler}. */
    private boolean startBackgroundThread() {

        return true;

//
//        Random r = new Random();
//        int low = 0;
//        int high = 1000;
//
//        for ( int i = 1; i <=10; i++ ) {
//            int result = r.nextInt(high-low) + low;
//
//            System.out.println("Result is " + result);
//
//            //System.out.println(result);
//        }
//
//
//
//        backgroundThread = new HandlerThread(HANDLE_THREAD_NAME);
//        backgroundThread2 = new HandlerThread(HANDLE_THREAD_NAME_2);
//        backgroundThread3 = new HandlerThread(HANDLE_THREAD_NAME_3);
//        backgroundThread4 = new HandlerThread(HANDLE_THREAD_NAME_4);
//        backgroundThread.start();
//        backgroundThread2.start();
//        backgroundThread3.start();
//
//        //startTimerOne(backgroundThread);
//
//        backgroundHandler = new Handler(backgroundThread.getLooper());
//        backgroundHandler2 = new Handler(backgroundThread2.getLooper());
//        backgroundHandler3 = new Handler(backgroundThread3.getLooper());
//
//        // Start the classification train & load an initial model.
//        synchronized (lock) {
//            runClassifier = true;
//        }
//
//        for ( int i = 1; i < 1000; i++ ) {
//
//            System.out.println("Hi from main " + i);
//
//            if( i%2 == 0 )
//                System.out.println("Yaaayyyyy " + i);
//                backgroundHandler2.post(periodicClassify3);
//
//        }
//
//
//
//
//        //backgroundHandler.post(periodicClassify);
//
//
//        Log.d("Classifiers", "Initialized");

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void start2() {



        //Initializing threads

        backgroundThread = new HandlerThread(HANDLE_THREAD_NAME);
        backgroundThread2 = new HandlerThread(HANDLE_THREAD_NAME_2);
        backgroundThread3 = new HandlerThread(HANDLE_THREAD_NAME_3);
        backgroundThread4 = new HandlerThread(HANDLE_THREAD_NAME_4);
        backgroundThread.start();
        backgroundThread2.start();
        backgroundThread3.start();

        backgroundHandler = new Handler(backgroundThread.getLooper());
        backgroundHandler2 = new Handler(backgroundThread2.getLooper());
        backgroundHandler3 = new Handler(backgroundThread3.getLooper());

        // Start the classification train & load an initial model.


        try {

            updateActiveModel();

            List<String> supplierNames = new ArrayList<String>();

            int j = 0;


//            int[] timings = new int[]{0,
//                    14,
//                    33,
//                    129,
//                    155,
//                    170,
//                    194,
//                    224,
//                    227,
//                    248,
//                    252,
//                    256,
//                    268,
//                    302,
//                    315,
//                    334,
//                    362,
//                    384,
//                    405,
//                    435,
//                    464,
//                    478,
//                    487,
//                    508,
//                    522,
//                    536,
//                    705,
//                    735,
//                    740,
//                    751,
//                    758,
//                    791,
//                    863,
//                    864,
//                    869,
//                    888,
//                    900,
//                    908,
//                    914,
//                    932,
//                    954,
//                    975,
//                    1046,
//                    1054,
//                    1071,
//                    1080,
//                    1106,
//                    1124,
//                    1126,
//                    1130,
//                    1150,
//                    1151,
//                    1166,
//                    1171,
//                    1183,
//                    1198,
//                    1207,
//                    1221,
//                    1224,
//                    1229,
//                    1244,
//                    1274,
//                    1278,
//                    1354,
//                    1368,
//                    1369,
//                    1435,
//                    1435,
//                    1442,
//                    1533,
//                    1560,
//                    1580,
//                    1624,
//                    1699,
//                    1734,
//                    1780,
//                    1866,
//                    1870,
//                    1926,
//                    1983,
//                    1996,
//                    2015,
//                    2026,
//                    2032,
//                    2040,
//                    2042,
//                    2050,
//                    2078,
//                    2079,
//                    2096,
//                    2114,
//                    2138,
//                    2202,
//                    2206,
//                    2208,
//                    2209,
//                    2228,
//                    2233,
//                    2235,
//                    2253};
//
//
//
//            String[] models = new String[] {
//
//                    "inception_v3","mobilenet_v1","mobilenet_v2","inception_v3",
//                    "mobilenet_v1","inception_v3","mobilenet_v1","inception_v3",
//                    "inception_v1","inception_v3","inception_v3","inception_v3","inception_v1","inception_v1","mobilenet_v2","mobilenet_v2","mobilenet_v2","mobilenet_v1","inception_v3","mobilenet_v2","inception_v3","mobilenet_v1","inception_v3","inception_v3","inception_v1","mobilenet_v1","mobilenet_v2","mobilenet_v2","mobilenet_v1","inception_v3","inception_v1","mobilenet_v1","inception_v3","mobilenet_v1","inception_v3","inception_v1","inception_v1","mobilenet_v2","inception_v1","mobilenet_v1","mobilenet_v1","mobilenet_v1","inception_v1","inception_v3","inception_v3","inception_v3","inception_v3","mobilenet_v1","mobilenet_v2","mobilenet_v1","mobilenet_v1","mobilenet_v1","inception_v1","mobilenet_v2","inception_v1","mobilenet_v2","inception_v1","inception_v3","mobilenet_v1","inception_v3","mobilenet_v2","mobilenet_v2","inception_v1","inception_v3","inception_v1","inception_v1","mobilenet_v2","inception_v3","inception_v3","inception_v1","mobilenet_v1","inception_v3","mobilenet_v1","inception_v3","inception_v3","inception_v1","mobilenet_v2","inception_v1","mobilenet_v1","inception_v1","inception_v1","inception_v1","inception_v3","mobilenet_v2","mobilenet_v1","inception_v3","inception_v1","inception_v1","inception_v3","mobilenet_v1","mobilenet_v1","mobilenet_v2","inception_v3","inception_v1","mobilenet_v1","inception_v1","mobilenet_v2","inception_v3","inception_v3","mobilenet_v2"};
//


            int[] timings = new int[]{
                    20,50,50,50,50,50,50,50,50,50,50,70,100,100,100,100,100,100,100,100,100,100,120,150,150,150,150,150,150,150,150,150,150,170,200,200,200,200,200,200,200,200,200,200,220,250,250,250,250,250,250,250,250,250,250,270,300,300,300,300,300,300,300,300,300,300,300,350,350,350,350,350,350,350,350,350,350,370,400,400,400,400,400,400,400,400,400,400,420,450,450,450,450,450,450,450,450,450,450};

            String[] models = new String[] {"inception_v3","inception_v1","inception_v1","inception_v1","mobilenet_v1",
                    "mobilenet_v1","mobilenet_v1","mobilenet_v1", "mobilenet_v2","mobilenet_v2",
                    "mobilenet_v2","inception_v3","inception_v1","inception_v1","inception_v1",
                    "mobilenet_v1","mobilenet_v1","mobilenet_v1","mobilenet_v1","mobilenet_v2",
                    "mobilenet_v2","mobilenet_v2","inception_v3","inception_v1","inception_v1",
                    "inception_v1","mobilenet_v1","mobilenet_v1","mobilenet_v1","mobilenet_v1",
                    "mobilenet_v2","mobilenet_v2","mobilenet_v2","inception_v3","inception_v1",
                    "inception_v1","inception_v1","mobilenet_v1","mobilenet_v1","mobilenet_v1",
            "mobilenet_v1","mobilenet_v2","mobilenet_v2","mobilenet_v2","inception_v3","inception_v1","inception_v1","inception_v1","mobilenet_v1","mobilenet_v1","mobilenet_v1","mobilenet_v1","mobilenet_v2","mobilenet_v2","mobilenet_v2","inception_v3","inception_v1","inception_v1","inception_v1","mobilenet_v1","mobilenet_v1","mobilenet_v1","mobilenet_v1","mobilenet_v2","mobilenet_v2","mobilenet_v2","inception_v3","inception_v1","inception_v1","inception_v1","mobilenet_v1","mobilenet_v1","mobilenet_v1","mobilenet_v1","mobilenet_v2","mobilenet_v2","mobilenet_v2","inception_v3","inception_v1","inception_v1","inception_v1","mobilenet_v1","mobilenet_v1","mobilenet_v1","mobilenet_v1","mobilenet_v2","mobilenet_v2","mobilenet_v2","inception_v3","inception_v1","inception_v1","inception_v1","mobilenet_v1","mobilenet_v1","mobilenet_v1","mobilenet_v1","mobilenet_v2","mobilenet_v2","mobilenet_v2"};

            //Execution times of different batches


            int [] batch_execs_cpu_inception_v1 = new int[]{30886,35402,55537,70560,80590,490809,529548,698705,714818,830517};
            int [] batch_execs_cpu_inception_v3 = new int[] {70897,127705,171708,231012,300999,1154838,1641064,1579348,1934691,2814480};
            int [] batch_execs_cpu_mobilenet_v1 = new int[] {10458,16335,17468,23255,32833,225876,260722,284881,384387,465750};
            int [] batch_execs_cpu_mobilenet_v2 = new int[] {53545,93638,130618,170252,220943,398295,475733,490799,442993,563887};

            int [] batch_execs_gpu_inception_v1 = new int[]{29724,53484,64921,  76363,  90385,  114555, 133657, 153274, 172524, 190385};
            int [] batch_execs_gpu_inception_v3 = new int[] {27798, 38484, 50462, 63798, 76431, 410127, 478727, 549210, 616380, 686648};
            int [] batch_execs_gpu_mobilenet_v1 = new int[] {5199,  9172,  11627,  14135,  16110,  39800,  46295,  52916,  59414,  66000};
            int [] batch_execs_gpu_mobilenet_v2 = new int[] {19712,  30349,  40883,  50814,  60264,  34148,  39791,  45624,  51099,  56535};

            int [] batch_execs_dsp_inception_v1 = new int[]{11302,   17676,  24851,  31853,  40073,  41906,  48824,  55950,  62274,  69675};
            int [] batch_execs_dsp_inception_v3 = new int[] {11170968,11127043,11171067,11230627,11300122,104742,120341,135057,150335,165684};
            int [] batch_execs_dsp_mobilenet_v1 = new int[] {4991,1114026,1120159,1124359,1133427,1112559,11114644,16609,18355,20203};
            int [] batch_execs_dsp_mobilenet_v2 = new int[] {1155111,  11192374,   111133916,  111166095,  111220943,  17884,  20311,  22666,  24897,  27032};



//            Actual Load Times
//            int[] inception_v1_load_times = new int[] {12, 704,21};
//            int[] inception_v3_load_times = new int[] {11, 1162,117};
//            int[] inception_v2_load_times = new int[] {12, 824,23};
//            int[] mobilenet_v1_load_times = new int[] {9, 635,19};
//            int[] mobilenet_v2_load_times = new int[] {9, 934,34};

            int[] inception_v1_load_times = new int[] {0, 0,0};
            int[] inception_v3_load_times = new int[] {0, 0,0};
            int[] inception_v2_load_times = new int[] {0, 0,0};
            int[] mobilenet_v1_load_times = new int[] {0, 0,0};
            int[] mobilenet_v2_load_times = new int[] {0, 0,0};



            boolean[] isLoadedInception = new boolean[] {false,false,false};
            boolean[] isLoadedInceptionV3 = new boolean[] {false,false,false};
            boolean[] isLoadedInceptionV2 = new boolean[] {false,false,false};
            boolean[] isLoadedMobileNet = new boolean[] {false,false,false};
            boolean[] isLoadedMobileNetV2 = new boolean[] {false,false,false};








            int cpu_exec_current = 0;
            int gpu_exec_current = 0;
            int dsp_exec_current = 0;


            //Initializing separate queue for each model

            List<String> inception_v1_queue = new ArrayList<String>();
            List<String> inception_v3_queue = new ArrayList<String>();
            List<String> inception_v2_queue = new ArrayList<String>();
            List<String> mobilenet_v1_queue = new ArrayList<String>();
            List<String> mobilenet_v2_queue = new ArrayList<String>();
            List<String> q = new ArrayList<String>();

            List<Integer> arrivals = new ArrayList<>();
            List<Integer> arrivalsiv1 = new ArrayList<>();
            List<Integer> arrivalsiv3 = new ArrayList<>();
            List<Integer> arrivalsmv1 = new ArrayList<>();
            List<Integer> arrivalsmv2 = new ArrayList<>();

            int queueSize = 5; //Wait time before running a model's instance

            //Initializing queues related to models instances

            for ( int i = 0; i < queueSize; i++ ) {
                inception_v1_queue.add("no");
                inception_v3_queue.add("no");
                inception_v2_queue.add("no");
                mobilenet_v1_queue.add("no");
                mobilenet_v2_queue.add("no");
                q.add("no");
            }


            //baseline or online scheduler code start

            for ( int i = 0; i < timings[timings.length-1]+200; i++ ) { // online scheduler parallel execution

                System.out.println("Time now in ms " + SystemClock.uptimeMillis());

                TimeUnit.MILLISECONDS.sleep(1);

                //Printing current loads in the devices

                if( cpu_exec_current > 0 ) {

                    cpu_exec_current-=1;
                    System.out.println("Current time = " + i + " cpu remaining = " + cpu_exec_current + " gpu remaining = " + gpu_exec_current + " dsp remaining = " + dsp_exec_current);

                }

                if ( gpu_exec_current > 0 ) {
                    gpu_exec_current-=1;
                    System.out.println("Current time = " + i + " cpu remaining = " + cpu_exec_current + " gpu remaining = " + gpu_exec_current + " dsp remaining = " + dsp_exec_current);
                }

                if ( dsp_exec_current > 0 ) {
                    dsp_exec_current-=1;
                    System.out.println("Current time = " + i + " cpu remaining = " + cpu_exec_current + " gpu remaining = " + gpu_exec_current + " dsp remaining = " + dsp_exec_current);
                }

//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                    writeToFile(String.valueOf(inception_v1_queue.size())+"\n",getContext(),"queueSize.txt");
//                }


                String inception_queue_element = "";
                String inception_v3_queue_element = "";
                String inception_v2_queue_element = "";
                String mobilenet_queue_element = "";
                String mobilenet_v2_queue_element = "";

                if ( inception_v1_queue.size() != 0 ) {
                    inception_queue_element = inception_v1_queue.remove(0);
                }

                if ( inception_v3_queue.size() != 0 ) {
                    inception_v3_queue_element = inception_v3_queue.remove(0);
                }

                if ( inception_v2_queue.size() != 0 ) {
                    inception_v2_queue_element = inception_v2_queue.remove(0);
                }


                if ( mobilenet_v1_queue.size() != 0 ) {
                    mobilenet_queue_element = mobilenet_v1_queue.remove(0);
                }

                if ( mobilenet_v2_queue.size() != 0 ) {
                    mobilenet_v2_queue_element = mobilenet_v2_queue.remove(0);
                }

                //Getting an instance from the queue's beginning after it has completed its wait time
                if (!inception_queue_element.equals("no") && !inception_queue_element.equals("")) { //inception_v1 instance


                    int temp_wait_time = queueSize;

                    int first_arrival_latest = (i-queueSize);

                    //Printing the logs

//                    System.out.println("iModel = " + inception_queue_element + " Arrival_time = "+ first_arrival_latest + " Wait_time " + (temp_wait_time));

                    System.out.println("Queue element \t " + inception_queue_element);



                    int batchSize = 1;

                    //Finding other instances in the queue which came in the wait time

                    while (inception_v1_queue.contains(inception_queue_element)) {

                        int indexElement = inception_v1_queue.indexOf(inception_queue_element);

                        inception_v1_queue.remove(inception_queue_element);


//                        System.out.println("iModel = " + inception_queue_element + " Arrival_time = "+ (first_arrival_latest + indexElement) + " Wait_time " + (temp_wait_time - indexElement-1));

                        inception_v1_queue.add("no");
                        batchSize += 1;

                    }

                    String toWrite = "Timing = " + String.valueOf(i) + " Model = " + inception_queue_element + " Batch = " + String.valueOf(batchSize) + "  " + j + " Queue size = " + inception_v1_queue.size();



                    int modelArrival = arrivalsiv1.remove(0);

                    System.out.println("Eppudu ra arrival inception_v1 " + modelArrival);

                    int cpuTotal = (int)(batch_execs_cpu_inception_v1[batchSize-1]/1000);

                    int gpuTotal = (int)(batch_execs_gpu_inception_v1[batchSize-1]/1000);

                    int dspTotal = (int)(batch_execs_dsp_inception_v1[batchSize-1]/1000);

                    if ( !isLoadedInception[0] ) {
                        cpuTotal+=inception_v1_load_times[0];
                    }
                    if ( !isLoadedInception[1] ) {
                        gpuTotal+=inception_v1_load_times[1];
                    }

                    if ( !isLoadedInception[2]) {
                        dspTotal+=inception_v1_load_times[2];
                    }


                    int cpuTotalCopy = cpuTotal;
                    int gpuTotalCopy = gpuTotal;
                    int dspTotalCopy = dspTotal;

                    cpuTotal += cpu_exec_current;
                    gpuTotal += gpu_exec_current;
                    dspTotal += dsp_exec_current;

                    String processorNow = "";

                    //Execute on CPU



                    if ( cpuTotal <= gpuTotal && cpuTotal <= dspTotal) {
                        processorNow = processorNow.concat("CPU");

                        cpu_exec_current+=cpuTotalCopy;
                        System.out.println("iModel = " + inception_queue_element + " Batch size = " + batchSize + " Arrival_time = "+ modelArrival + " Wait_time " + (temp_wait_time-1) +" Execution time = " + cpu_exec_current + " Processor = " + processorNow);

                        isLoadedInception[0] = true;

                        switch (batchSize) {
                            case 1:backgroundHandler.post(periodicClassifyForThreadInceptionV1Cpu); // Run model
                                break;
                            case 2:backgroundHandler.post(periodicClassifyForThreadInceptionV1Cpu2); // Run model
                                break;
                            case 3:backgroundHandler.post(periodicClassifyForThreadInceptionV1Cpu3); // Run model
                                break;
                            case 4:backgroundHandler.post(periodicClassifyForThreadInceptionV1Cpu4); // Run model
                                break;
                            case 5:backgroundHandler.post(periodicClassifyForThreadInceptionV1Cpu5); // Run model
                                break;
                        }



                    }


                    //Execute on GPU
                    else if ( gpuTotal <= cpuTotal && gpuTotal <= dspTotal ) {
                        processorNow = processorNow.concat("GPU");
                        gpu_exec_current+=gpuTotalCopy;
                        System.out.println("iModel = " + inception_queue_element + " Batch size = " + batchSize + " Arrival_time = "+ first_arrival_latest + " Wait_time " + (temp_wait_time-1) +" Execution time = " + gpu_exec_current + " Processor = " + processorNow );

                        isLoadedInception[1] = true;

                        switch (batchSize ){
                            case 1:
                                backgroundHandler2.post(periodicClassifyForThreadinceptionV1Gpu);
                                break;
                            case 2:
                                backgroundHandler2.post(periodicClassifyForThreadinceptionV1Gpu2);
                                break;
                            case 3:
                                backgroundHandler2.post(periodicClassifyForThreadinceptionV1Gpu3);
                                break;
                            case 4:
                                backgroundHandler2.post(periodicClassifyForThreadinceptionV1Gpu4);
                                break;
                            case 5:
                                backgroundHandler2.post(periodicClassifyForThreadinceptionV1Gpu5);
                                break;
                        }

                        backgroundHandler.post(periodicClassifyForThreadinceptionV1Gpu);
                    }
//
                    //Execute on DSP
                    else {
                        processorNow = processorNow.concat("DSP");
                        dsp_exec_current+=dspTotalCopy;

                        System.out.println("iModel = " + inception_queue_element + " Batch size = " + batchSize + " Arrival_time = "+ first_arrival_latest + " Wait_time " + (temp_wait_time-1)+" Execution time = " + dsp_exec_current + " Processor = " + processorNow);
                        isLoadedInception[2] = true;

                        switch (batchSize) {
                            case 1:
                                backgroundHandler3.post(periodicClassifyForThreadinceptionV1Dsp);
                                break;
                            case 2:
                                backgroundHandler3.post(periodicClassifyForThreadinceptionV1Dsp2);
                                break;
                            case 3:
                                backgroundHandler3.post(periodicClassifyForThreadinceptionV1Dsp3);
                                break;
                            case 4:
                                backgroundHandler3.post(periodicClassifyForThreadinceptionV1Dsp4);
                                break;
                            case 5:
                                backgroundHandler3.post(periodicClassifyForThreadinceptionV1Dsp5);
                                break;

                        }


                    }


                    System.out.println("Timing = " + i + " CPU total = " + cpuTotal + " Gpu total = " + gpuTotal);


                    System.out.println("Timing = " + i + " Model = " + inception_queue_element + " Batch = " + batchSize + "Arrival = " + modelArrival + "Processor = " + processorNow + " Execs times = " + cpu_exec_current + " " + gpu_exec_current + " " + dsp_exec_current);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        writeToFile(toWrite+"\n",getContext(),"inceptionV1.txt");
                    }

                }

                //

                if (!inception_v2_queue_element.equals("no") && !inception_v2_queue_element.equals("")) {


                    System.out.println("Queue element \t " + inception_v2_queue_element);

                    int batchSize = 1;

                    while (inception_v2_queue.contains(inception_v2_queue_element)) {
                        inception_v2_queue.remove(inception_v2_queue_element);
                        inception_v2_queue.add("no");
                        batchSize += 1;
                    }

                    String toWrite = "Timing = " + String.valueOf(i) + " Model = " + inception_v2_queue_element + " Batch = " + String.valueOf(batchSize) + "  " + j + " Queue size = " + inception_v2_queue.size();



                    int modelArrival = arrivals.remove(0);



                    int cpuTotal = (int)(batch_execs_cpu_inception_v1[batchSize-1]/1000);

                    int gpuTotal = (int)(batch_execs_gpu_inception_v1[batchSize-1]/1000);

                    int dspTotal = (int)(batch_execs_dsp_inception_v1[batchSize-1]/1000);


                    /*if ( !batchLoadedInceptionV2Cpu[batchSize] ) {
                        cpuTotal+=inception_v2_load_times[0];
                        //isLoadedInception[0] = true;
                    }
                    if ( !batchLoadedInceptionV2Gpu[batchSize] ) {
                        gpuTotal+=inception_v2_load_times[1];
                        //isLoadedInception[0] = true;
                    }
                    if ( !batchLoadedInceptionV2Dsp[batchSize] ) {
                        dspTotal+=inception_v2_load_times[2];
                        //isLoadedInception[0] = true;
                    } */

                    int cpuTotalCopy = cpuTotal;
                    int gpuTotalCopy = gpuTotal;
                    int dspTotalCopy = dspTotal;

                    cpuTotal += cpu_exec_current;
                    gpuTotal += gpu_exec_current;
                    dspTotal += dsp_exec_current;

                    String processorNow = "";


                    //batchLoadedInceptionV2Cpu[batchSize] = true;

//                    if ( cpuTotal <= gpuTotal && cpuTotal <= dspTotal) {
//                        processorNow = processorNow.concat("CPU");
//                        cpu_exec_current+=cpuTotalCopy;
//                        System.out.println("Execution time = " + cpuTotal);
//
//
//                        //loadModel("inception_v2",processorNow);
//
//                        batchLoadedInceptionV2Cpu[batchSize] = true;
//                    }
//
//                    else if ( gpuTotal <= cpuTotal && gpuTotal <= dspTotal ) {
//                        processorNow = processorNow.concat("GPU");
//                        gpu_exec_current+=gpuTotalCopy;
//
//                        //loadModel("inception_v2",processorNow);
//                        batchLoadedInceptionV2Gpu[batchSize] = true;
//                    }
//
//                    else {
//                        processorNow = processorNow.concat("DSP");
//                        dsp_exec_current+=dspTotalCopy;
//
//                        //loadModel("inception_v2",processorNow);
//                        batchLoadedInceptionV2Dsp[batchSize] = true;
//                    }


                    System.out.println("Timing = " + i + " CPU total = " + cpuTotal + " Gpu total = " + gpuTotal);


                    System.out.println("Timing = " + i + " Model = " + inception_v2_queue_element + " Batch = " + batchSize + "Arrival = " + modelArrival + "Processor = " + processorNow + " Execs times = " + cpu_exec_current + " " + gpu_exec_current + " " + dsp_exec_current);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        writeToFile(toWrite+"\n",getContext(),"inceptionV1.txt");
                    }

                }


                if (!inception_v3_queue_element.equals("no") && !inception_v3_queue_element.equals("")) {

                    //inception_v1_queue.add("no");


                    int temp_wait_time = queueSize;

                    int first_arrival_latest2 = (i-queueSize);

//                    System.out.println("iModel = " + inception_v3_queue_element + " Arrival_time = "+ first_arrival_latest2 + " Wait_time " + (temp_wait_time));


                    System.out.println("Queue element \t " + inception_v3_queue_element);

                    int batchSize = 1;

                    //Finding other instances in the queue which came in the wait time

                    while (inception_v3_queue.contains(inception_v3_queue_element)) {
                        int indexElement = inception_v3_queue.indexOf(inception_v3_queue_element);
                        inception_v3_queue.remove(inception_v3_queue_element);

//                        System.out.println("iModel = " + inception_v3_queue_element + " Arrival_time = "+ (first_arrival_latest2 + indexElement ) + " Wait_time " + (temp_wait_time - indexElement-1));


                        inception_v3_queue.add("no");

                        batchSize += 1;
                    }

                    String toWrite = "Timing = " + String.valueOf(i) + " Model = " + inception_v3_queue_element + " Batch = " + String.valueOf(batchSize) + "  " + j + " Queue size = " + inception_v3_queue.size();



                    int modelArrival = arrivalsiv3.remove(0);

                    System.out.println("Eppudu ra arrival inception_v3 " + modelArrival);

                    int cpuTotal = (int)(batch_execs_cpu_inception_v3[batchSize-1]/1000);

                    int gpuTotal = (int)(batch_execs_gpu_inception_v3[batchSize-1]/1000);

                    int dspTotal = (int)(batch_execs_dsp_inception_v3[batchSize-1]/1000);

                    if ( !isLoadedInceptionV3[0] ) {
                        cpuTotal+=inception_v3_load_times[0];
                        //isLoadedInception[0] = true;
                    }
                    if ( !isLoadedInceptionV3[1] ) {
                        gpuTotal+=inception_v3_load_times[1];
                        //isLoadedInception[0] = true;
                    }

                    if ( !isLoadedInceptionV3[2] ) {
                        dspTotal+=inception_v3_load_times[2];
                        //isLoadedInception[0] = true;
                    }

                    int cpuTotalCopy = cpuTotal;
                    int gpuTotalCopy = gpuTotal;
                    int dspTotalCopy = dspTotal;

                    cpuTotal += cpu_exec_current;
                    gpuTotal += gpu_exec_current;
                    dspTotal += dsp_exec_current;

                    String processorNow = "";


                    if ( cpuTotal <= gpuTotal && cpuTotal <= dspTotal) { //Execute on CPU
                        processorNow = processorNow.concat("CPU");
                        cpu_exec_current+=cpuTotalCopy;


                        System.out.println("iModel = " + inception_v3_queue_element +" Batch size = " + batchSize + " Arrival_time = "+ first_arrival_latest2 + " Wait_time " + (temp_wait_time-1) +" Execution time = " + cpu_exec_current + " Processor = " + processorNow );


                        switch (batchSize){
                            case 1:
                                backgroundHandler.post(periodicClassifyForThreadInceptionV3Cpu);
                                break;
                            case 2:
                                backgroundHandler.post(periodicClassifyForThreadInceptionV3Cpu2);
                                break;
                            case 3:
                                backgroundHandler.post(periodicClassifyForThreadInceptionV3Cpu3);
                                break;
                            case 4:
                                backgroundHandler.post(periodicClassifyForThreadInceptionV3Cpu4);
                                break;
                            case 5:
                                backgroundHandler.post(periodicClassifyForThreadInceptionV3Cpu5);
                                break;
                        }
                        //loadModel("inception_v3",processorNow);
                        isLoadedInceptionV3[0] = true;

                    }

                    else if ( gpuTotal <= cpuTotal && gpuTotal <= dspTotal ) { //Execute on GPU
                        processorNow = processorNow.concat("GPU");
                        gpu_exec_current+=gpuTotalCopy;
                        size_to_consider = batchSize;

                        System.out.println("iModel = " + inception_v3_queue_element +" Batch size = " + batchSize + " Arrival_time = "+ first_arrival_latest2 + " Wait_time " + (temp_wait_time-1) +" Execution time = " + gpu_exec_current + " Processor = " + processorNow );


                        //loadModel("inception_v3",processorNow);

                        isLoadedInceptionV3[1] = true;

                        switch (batchSize) {
                            case 1:backgroundHandler2.post(periodicClassifyForThreadInceptionV3Gpu);
                                break;
                            case 2:
                                backgroundHandler2.post(periodicClassifyForThreadInceptionV3Gpu2);
                                break;
                            case 3:
                                backgroundHandler2.post(periodicClassifyForThreadInceptionV3Gpu3);
                                break;
                            case 4:
                                backgroundHandler2.post(periodicClassifyForThreadInceptionV3Gpu4);
                                break;
                            case 5:
                                backgroundHandler2.post(periodicClassifyForThreadInceptionV3Gpu5);
                                break;

                        }



                    }

                    else { //Execute on DSP
                        processorNow = processorNow.concat("DSP");
                        dsp_exec_current+=dspTotalCopy;
                        size_to_consider = batchSize;
                        System.out.println("iModel = " + inception_v3_queue_element + " Batch size = " + batchSize + " Arrival_time = "+ first_arrival_latest2 + " Wait_time " + (temp_wait_time-1) +" Execution time = " + dsp_exec_current + " Processor = " + processorNow );


                        //loadModel("inception_v3",processorNow);

                        isLoadedInceptionV3[2]  = true;

                        switch (batchSize) {
                            case 1:
                                backgroundHandler3.post(periodicClassifyForThreadinceptionV3Dsp);
                                break;
                            case 2:
                                backgroundHandler3.post(periodicClassifyForThreadinceptionV3Dsp2);
                                break;
                            case 3:
                                backgroundHandler3.post(periodicClassifyForThreadinceptionV3Dsp3);
                                break;
                            case 4:
                                backgroundHandler3.post(periodicClassifyForThreadinceptionV3Dsp4);
                                break;
                            case 5:
                                backgroundHandler3.post(periodicClassifyForThreadinceptionV3Dsp5);
                                break;

                        }



                    }


                    System.out.println("Timing = " + i + " CPU total = " + cpuTotal + " Gpu total = " + gpuTotal);


                    System.out.println("Timing = " + i + " Model = " + inception_v3_queue_element + " Batch = " + batchSize + "Arrival = " + modelArrival + "Processor = " + processorNow + " Execs times = " + cpu_exec_current + " " + gpu_exec_current + " " + dsp_exec_current);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        writeToFile(toWrite+"\n",getContext(),"inceptionV3.txt");
                    }

                }

                if (!mobilenet_queue_element.equals("no") && !mobilenet_queue_element.equals("")) {


                    int temp_wait_time = queueSize;

                    int first_arrival_latest3 = (i-queueSize);

//                    System.out.println("Model = " + mobilenet_queue_element + " Arrival_time = "+ first_arrival_latest3 + " Wait_time " + (temp_wait_time-1));

//                    System.out.println("iModel = " + mobilenet_queue_element + " Arrival_time = "+ first_arrival_latest3 + " Wait_time " + (temp_wait_time) + "  "+ i);

                    //inception_v1_queue.add("no");

                    System.out.println("Queue element \t " + mobilenet_queue_element);

                    int batchSize = 1;

                    while (mobilenet_v1_queue.contains(mobilenet_queue_element)) { //Finding other instances in the wait time

                        int indexElement = mobilenet_v1_queue.indexOf(mobilenet_queue_element);

                        mobilenet_v1_queue.remove(mobilenet_queue_element);
//                        System.out.println("iModel = " + mobilenet_queue_element + " Arrival_time = "+ (first_arrival_latest3 + indexElement ) + " Wait_time " + (temp_wait_time - indexElement-1));

                        mobilenet_v1_queue.add("no");
                        batchSize += 1;
                    }

                    int modelArrival = arrivalsmv1.remove(0);

                    System.out.println("Eppudu ra arrival mobilenet_v1 " + modelArrival);

                    int cpuTotal = (int)(batch_execs_cpu_mobilenet_v1[batchSize-1]/1000);

                    int gpuTotal = (int)(batch_execs_gpu_mobilenet_v1[batchSize-1]/1000);

                    int dspTotal = (int)(batch_execs_dsp_mobilenet_v1[batchSize-1]/1000);


                    if ( !isLoadedMobileNet[0]  ) {
                        cpuTotal+=mobilenet_v1_load_times[0];
                        //isLoadedInception[0] = true;
                    }
                    if ( !isLoadedMobileNet[1]  ) {
                        gpuTotal+=mobilenet_v1_load_times[1];
                        //isLoadedInception[0] = true;
                    }

                    if ( !isLoadedMobileNet[2]  ) {
                        dspTotal+=mobilenet_v1_load_times[2];
                        //isLoadedInception[0] = true;
                    }


                    int cpuTotalCopy = cpuTotal;
                    int gpuTotalCopy = gpuTotal;
                    int dspTotalCopy = dspTotal;


                    cpuTotal += cpu_exec_current;
                    gpuTotal += gpu_exec_current;
                    gpuTotal += dsp_exec_current;


                    String processorNow = "";


                    if ( cpuTotal <= gpuTotal && cpuTotal <= dspTotal ) { //Execute on CPU
                        processorNow = processorNow.concat("CPU");
                        cpu_exec_current+=cpuTotalCopy;
                        size_to_consider = batchSize;
                        System.out.println("iModel = " + mobilenet_queue_element + " Batch size = " + batchSize + " Arrival_time = "+ first_arrival_latest3 + " Wait_time " + (temp_wait_time-1) +" Execution time = " + cpu_exec_current + " Processor = " + processorNow);

                        isLoadedMobileNet[0]  = true;

                        switch (batchSize) {
                            case 1:
                                backgroundHandler.post(periodicClassifyForThreadMobilenetV1Cpu);
                                break;
                            case 2:
                                backgroundHandler.post(periodicClassifyForThreadMobilenetV1Cpu2);
                                break;
                            case 3:
                                backgroundHandler.post(periodicClassifyForThreadMobilenetV1Cpu3);
                                break;
                            case 4:
                                backgroundHandler.post(periodicClassifyForThreadMobilenetV1Cpu4);
                                break;
                            case 5:
                                backgroundHandler.post(periodicClassifyForThreadMobilenetV1Cpu5);
                                break;

                        }




                    }

                    else if ( gpuTotal <= cpuTotal && gpuTotal <= dspTotal ){ //Execute on GPU
                        processorNow = processorNow.concat("GPU");
                        gpu_exec_current+=gpuTotalCopy;
                        size_to_consider = batchSize;
                        System.out.println("iModel = " + mobilenet_queue_element + " Batch size = " + batchSize + " Arrival_time = "+ first_arrival_latest3 + " Wait_time " + (temp_wait_time-1) +" Execution time = " + gpu_exec_current + " Processor = " + processorNow );

                        isLoadedMobileNet[1] = true;
                        switch (batchSize) {
                            case 1:
                                backgroundHandler2.post(periodicClassifyForThreadMobilenetV1Gpu);
                                break;
                            case 2:
                                backgroundHandler2.post(periodicClassifyForThreadMobilenetV1Gpu2);
                                break;
                            case 3:
                                backgroundHandler2.post(periodicClassifyForThreadMobilenetV1Gpu3);
                                break;
                            case 4:
                                backgroundHandler2.post(periodicClassifyForThreadMobilenetV1Gpu4);
                                break;
                            case 5:
                                backgroundHandler2.post(periodicClassifyForThreadMobilenetV1Gpu5);
                                break;

                        }



                    }
                    else { //Execute on DSP
                        processorNow = processorNow.concat("DSP");
                        dsp_exec_current+=dspTotalCopy;
                        size_to_consider = batchSize;
                        System.out.println("iModel = " + mobilenet_queue_element + " Batch size = " + batchSize + " Arrival_time = "+ first_arrival_latest3 + " Wait_time " + (temp_wait_time-1) +" Execution time = " + dsp_exec_current + " Processor = " + processorNow);


                        //loadModel("mobilenet_v1",processorNow);

                        isLoadedMobileNet[2] = true;

                        switch (batchSize) {
                            case 1:
                                backgroundHandler3.post(periodicClassifyForThreadMobilenetV1Dsp);
                                break;
                            case 2:
                                backgroundHandler3.post(periodicClassifyForThreadMobilenetV1Dsp2);
                                break;
                            case 3:
                                backgroundHandler3.post(periodicClassifyForThreadMobilenetV1Dsp3);
                                break;
                            case 4:
                                backgroundHandler3.post(periodicClassifyForThreadMobilenetV1Dsp4);
                                break;
                            case 5:
                                backgroundHandler3.post(periodicClassifyForThreadMobilenetV1Dsp5);
                                break;

                        }



                    }

                    System.out.println("Timing = " + i + "CPU total = " + cpuTotal + " Gpu total = " + gpuTotal);


                    String toWrite = "Timing = " + String.valueOf(i) + " Model = " + mobilenet_queue_element + " Batch = " + String.valueOf(batchSize) + "  " + j + " Queue size = " + mobilenet_v1_queue.size();

                    System.out.println("Timing = " + i + " Model = " + mobilenet_queue_element + " Batch = " + batchSize + "Arrival = " + modelArrival+ " Processor = " + processorNow + " Execs times = " + cpu_exec_current + " " + gpu_exec_current + " " + dsp_exec_current);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        writeToFile(toWrite+"\n",getContext(),"mobilenetQueue.txt");
                    }

                }

                if (!mobilenet_v2_queue_element.equals("no") && !mobilenet_v2_queue_element.equals("")) {

                    //inception_v1_queue.add("no");

                    int temp_wait_time = queueSize;

                    int first_arrival_latest4 = (i-queueSize);

//                    System.out.println("Model = " + mobilenet_v2_queue_element + " Arrival_time = "+ first_arrival_latest4 + " Wait_time " + (temp_wait_time-1));

//                    System.out.println("iModel = " + mobilenet_v2_queue_element + " Arrival_time = "+ first_arrival_latest4 + " Wait_time " + (temp_wait_time));

                    System.out.println("Queue element \t " + mobilenet_v2_queue_element);

                    int batchSize = 1;

                    while (mobilenet_v2_queue.contains(mobilenet_v2_queue_element)) {

                        int indexElement = mobilenet_v2_queue.indexOf(mobilenet_v2_queue_element);
                        mobilenet_v2_queue.remove(mobilenet_v2_queue_element);
//                        System.out.println("iModel = " + mobilenet_v2_queue_element + " Arrival_time = "+ (first_arrival_latest4 + indexElement) + " Wait_time " + (temp_wait_time - indexElement-1));


                        mobilenet_v2_queue.add("no");
                        batchSize += 1;
                    }

                    runClassifier2 = true;



                    int modelArrival = arrivalsmv2.remove(0);

                    System.out.println("Eppudu ra arrival mobilenet_v2 " + modelArrival);

                    int cpuTotal = (int)(batch_execs_cpu_mobilenet_v2[batchSize-1]/1000);

                    int gpuTotal = (int)(batch_execs_gpu_mobilenet_v2[batchSize-1]/1000);

                    int dspTotal = (int)(batch_execs_dsp_mobilenet_v2[batchSize-1]/1000);

                    if ( !isLoadedMobileNetV2[0] ) {
                        cpuTotal+=mobilenet_v2_load_times[0];
                        //isLoadedInception[0] = true;
                    }
                    if ( !isLoadedMobileNetV2[1] ) {
                        gpuTotal+=mobilenet_v2_load_times[1];
                        //isLoadedInception[0] = true;
                    }
                    if ( !isLoadedMobileNetV2[2] ) {
                        dspTotal+=mobilenet_v2_load_times[2];
                        //isLoadedInception[0] = true;
                    }


                    int cpuTotalCopy = cpuTotal;
                    int gpuTotalCopy = gpuTotal;
                    int dspTotalCopy = dspTotal;


                    cpuTotal += cpu_exec_current;
                    gpuTotal += gpu_exec_current;
                    dspTotal += dsp_exec_current;


                    String processorNow = "";



                    if ( cpuTotal <= gpuTotal && cpuTotal <= dspTotal ) {
                        processorNow = processorNow.concat("CPU");

                        cpu_exec_current+=cpuTotalCopy;
                        size_to_consider = batchSize;
                        System.out.println("iModel = " + mobilenet_v2_queue_element + " Batch size = " + batchSize + " Arrival_time = "+ first_arrival_latest4 + " Wait_time " + (temp_wait_time-1) + " Execution time = " + cpu_exec_current + " Processor = " + processorNow );


                        //loadModel("mobilenet_v2",processorNow);
                        isLoadedMobileNetV2[0] = true;

                        switch (batchSize) {
                            case 1:
                                backgroundHandler.post(periodicClassifyForThreadMobilenetV2Cpu);
                                break;
                            case 2:
                                backgroundHandler.post(periodicClassifyForThreadMobilenetV2Cpu2);
                                break;
                            case 3:
                                backgroundHandler.post(periodicClassifyForThreadMobilenetV2Cpu3);
                                break;
                            case 4:
                                backgroundHandler.post(periodicClassifyForThreadMobilenetV2Cpu4);
                                break;
                            case 5:
                                backgroundHandler.post(periodicClassifyForThreadMobilenetV2Cpu5);
                                break;

                        }



                    }

                    else if ( gpuTotal <= cpuTotal && gpuTotal <= dspTotal ) {
                        processorNow = processorNow.concat("GPU");
                        gpu_exec_current+=gpuTotalCopy;
                        size_to_consider = batchSize;
                        System.out.println("iModel = " + mobilenet_v2_queue_element + " Batch size = " + batchSize + " Arrival_time = "+ first_arrival_latest4 + " Wait_time " + (temp_wait_time-1)+ " Execution time = " + gpu_exec_current + " Processor = " + processorNow );


                        //loadModel("mobilenet_v2",processorNow);

                        isLoadedMobileNetV2[1] = true;

                        switch (batchSize) {
                            case 1:
                                backgroundHandler2.post(periodicClassifyForThreadMobilenetV2Gpu);
                                break;
                            case 2:
                                backgroundHandler2.post(periodicClassifyForThreadMobilenetV2Gpu2);
                                break;
                            case 3:
                                backgroundHandler2.post(periodicClassifyForThreadMobilenetV2Gpu3);
                                break;
                            case 4:
                                backgroundHandler2.post(periodicClassifyForThreadMobilenetV2Gpu4);
                                break;
                            case 5:
                                backgroundHandler2.post(periodicClassifyForThreadMobilenetV2Gpu5);
                                break;

                        }


                    }
                    else {
                        processorNow = processorNow.concat("DSP");
                        dsp_exec_current+=dspTotalCopy;
                        size_to_consider = batchSize;

                        //loadModel("mobilenet_v2",processorNow);
                        System.out.println("iModel = " + mobilenet_v2_queue_element + " Batch size = " + batchSize + " Arrival_time = "+ first_arrival_latest4 + " Wait_time " + (temp_wait_time-1) + " Execution time = " + dsp_exec_current + " Processor = " + processorNow );


                        isLoadedMobileNetV2[2] = true;

                        switch (batchSize) {
                            case 1:
                                backgroundHandler3.post(periodicClassifyForThreadMobilenetV2Dsp);
                                break;
                            case 2:
                                backgroundHandler3.post(periodicClassifyForThreadMobilenetV2Dsp2);
                                break;
                            case 3:
                                backgroundHandler3.post(periodicClassifyForThreadMobilenetV2Dsp3);
                                break;
                            case 4:
                                backgroundHandler3.post(periodicClassifyForThreadMobilenetV2Dsp4);
                                break;
                            case 5:
                                backgroundHandler3.post(periodicClassifyForThreadMobilenetV2Dsp5);
                                break;

                        }


                    }

                    System.out.println("Timing = " + i + "CPU total = " + cpuTotal + " Gpu total = " + gpuTotal);


                    String toWrite = "Timing = " + String.valueOf(i) + " Model = " + mobilenet_v2_queue_element + " Batch = " + String.valueOf(batchSize) + "  " + j + " Queue size = " + mobilenet_v2_queue.size();

                    System.out.println("Timing = " + i + " Model = " + mobilenet_v2_queue_element + " Batch = " + batchSize + " Processor = " + processorNow );

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        writeToFile(toWrite+"\n",getContext(),"mobilenetV2Queue.txt");
                    }


                }



                if (j < models.length) {

                    System.out.println("reached here " + j + "  " + i);


                    if (timings[j] == i) {
                        while ( timings[j] == i) {

                            if ( models[j].equals("inception_v1")) {
                                if ( !inception_v1_queue.contains(models[j])) {
                                    arrivals.add(i);

                                    arrivalsiv1.add(i);
                                }

                                inception_v1_queue.add(models[j]);

                                if( mobilenet_v1_queue.size() < queueSize)
                                    mobilenet_v1_queue.add("no");

                                if( mobilenet_v2_queue.size() < queueSize)
                                    mobilenet_v2_queue.add("no");

                                if( inception_v3_queue.size() < queueSize)
                                    inception_v3_queue.add("no");

                                if( inception_v2_queue.size() < queueSize)
                                    inception_v2_queue.add("no");

                            }

                            else if ( models[j].equals("inception_v2")) {


                                if ( !inception_v2_queue.contains(models[j])) {
                                    arrivals.add(i);
                                    arrivalsiv1.add(i);
                                }

                                inception_v2_queue.add(models[j]);

                                if( mobilenet_v1_queue.size() < queueSize)
                                    mobilenet_v1_queue.add("no");

                                if( mobilenet_v2_queue.size() < queueSize)
                                    mobilenet_v2_queue.add("no");

                                if( inception_v3_queue.size() < queueSize)
                                    inception_v3_queue.add("no");

                                if( inception_v1_queue.size() < queueSize)
                                    inception_v1_queue.add("no");


                            }

                            else if ( models[j].equals("mobilenet_v1")) {

                                if ( !mobilenet_v1_queue.contains(models[j])) {
                                    arrivals.add(i);
                                    arrivalsmv1.add(i);
                                    mobilenet_v1_queue.add(models[j]);
                                }
                                else {
                                    mobilenet_v1_queue.add(models[j]);
                                }


                                //+";;"+cpu_execs[j] + ";;" + gpu_execs[j]
                                if ( inception_v1_queue.size() < queueSize )
                                    inception_v1_queue.add("no");

                                if( mobilenet_v2_queue.size() < queueSize)
                                    mobilenet_v2_queue.add("no");

                                if( inception_v3_queue.size() < queueSize)
                                    inception_v3_queue.add("no");
                                if( inception_v2_queue.size() < queueSize)
                                    inception_v2_queue.add("no");
                            }

                            else if ( models[j].equals("mobilenet_v2")) {

                                if ( !mobilenet_v2_queue.contains(models[j])) {
                                    arrivals.add(i);
                                    arrivalsmv2.add(i);
                                    mobilenet_v2_queue.add(models[j]);
                                }
                                else {
                                    mobilenet_v2_queue.add(models[j]);
                                }


                                //+";;"+cpu_execs[j] + ";;" + gpu_execs[j]
                                if ( inception_v1_queue.size() < queueSize )
                                    inception_v1_queue.add("no");

                                if( mobilenet_v1_queue.size() < queueSize)
                                    mobilenet_v1_queue.add("no");

                                if( inception_v3_queue.size() < queueSize)
                                    inception_v3_queue.add("no");
                                if( inception_v2_queue.size() < queueSize)
                                    inception_v2_queue.add("no");
                            }

                            else if ( models[j].equals("inception_v3")) {

                                if ( !inception_v3_queue.contains(models[j])) {
                                    arrivals.add(i);
                                    arrivalsiv3.add(i);
                                    inception_v3_queue.add(models[j]);
                                }
                                else {
                                    inception_v3_queue.add(models[j]);
                                }


                                //+";;"+cpu_execs[j] + ";;" + gpu_execs[j]
                                if ( inception_v1_queue.size() < queueSize )
                                    inception_v1_queue.add("no");

                                if( mobilenet_v1_queue.size() < queueSize)
                                    mobilenet_v1_queue.add("no");

                                if( mobilenet_v2_queue.size() < queueSize)
                                    mobilenet_v2_queue.add("no");

                                if( inception_v2_queue.size() < queueSize)
                                    inception_v2_queue.add("no");
                            }

                            System.out.println("iModel Exec " + models[j] + " " + i);

                            execList.add("iModel Exec " + models[j] + " " + i);


                            j += 1;
                            if ( j == models.length ){
                                break;
                            }

                        }

                        continue;

                    }

                    inception_v1_queue.add("no");
                    mobilenet_v1_queue.add("no");
                    mobilenet_v2_queue.add("no");
                    inception_v3_queue.add("no");
                    inception_v2_queue.add("no");


                }

            }



            //baseline or online scheduler code start
            System.out.println(q.size());
            System.out.println("All execs " + execList.size());

            String towrite = "";


            for ( String str : execList ) {
                towrite = towrite+str+"\n";

            }

            writeToFile(towrite+"\n",getContext(),"bokkalofile.txt");





        } catch (Exception e) {
            e.printStackTrace();
        }



//        //backgroundHandler.post(periodicClassify);
//
//
//        Log.d("Classifiers", "Initialized");
    }

    /** Stops the background thread and its {@link Handler}. */
    private void stopBackgroundThread() {
        backgroundThread.quitSafely();
        try {
            backgroundThread.join();
            backgroundThread = null;
            backgroundHandler = null;
            synchronized (lock) {
                runClassifier = false;
            }
        } catch (InterruptedException e) {
            Log.e(TAG, "Interrupted when stopping background thread", e);
        }
    }

    private void stopBackgroundThread2() {
        backgroundThread2.quitSafely();
        try {
            backgroundThread2.join();
            backgroundThread2 = null;
            backgroundHandler2 = null;
            synchronized (lock) {
                runClassifier2 = false;
            }
        } catch (InterruptedException e) {
            Log.e(TAG, "Interrupted when stopping background thread", e);
        }
    }




    private Runnable periodicClassifyForThreadInceptionV1Cpu =
            new Runnable() {
                @Override
                public void run() {
                    //synchronized (lock) {
                        if (runClassifier2) {
                            try {

                                if( inceptionV1Cpu == null ) {
                                    inceptionV1Cpu = new ImageClassifierInceptionV1Quant(getActivity());
                                    inceptionV1Cpu.setNumThreads(currentNumThreads);


                                }
                                System.out.println("inceptionV1Cpu" + " " + size_to_consider);

                                classifyFrame2("Thread 2 inception_v1 cpu ",inceptionV1Cpu,1);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    //}
                    //classifyFrame("Thread 2");
                    //backgroundHandler2.post(periodicClassifyForThread);
                }
            };

    private Runnable periodicClassifyForThreadInceptionV1Cpu2 =
            new Runnable() {
                @Override
                public void run() {
                    //synchronized (lock) {
                        if (runClassifier2) {
                            try {

                                if( inceptionV1Cpu == null ) {
                                    inceptionV1Cpu = new ImageClassifierInceptionV1Quant(getActivity());
                                    inceptionV1Cpu.setNumThreads(currentNumThreads);


                                }
                                System.out.println("inceptionV1Cpu" + " " + size_to_consider);

                                classifyFrame2("Thread 2 inception_v1 cpu ",inceptionV1Cpu,2);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        //}
                    }
                    //classifyFrame("Thread 2");
                    //backgroundHandler2.post(periodicClassifyForThread);
                }
            };

    private Runnable periodicClassifyForThreadInceptionV1Cpu3 =
            new Runnable() {
                @Override
                public void run() {
                    //synchronized (lock) {
                        if (runClassifier2) {
                            try {

                                if( inceptionV1Cpu == null ) {
                                    inceptionV1Cpu = new ImageClassifierInceptionV1Quant(getActivity());
                                    inceptionV1Cpu.setNumThreads(currentNumThreads);


                                }
                                System.out.println("inceptionV1Cpu" + " " + size_to_consider);

                                classifyFrame2("Thread 2 inception_v1 cpu ",inceptionV1Cpu,3);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    //}
                    //classifyFrame("Thread 2");
                    //backgroundHandler2.post(periodicClassifyForThread);
                }
            };

    private Runnable periodicClassifyForThreadInceptionV1Cpu4 =
            new Runnable() {
                @Override
                public void run() {
                    synchronized (lock) {
                        if (runClassifier2) {
                            try {

                                if( inceptionV1Cpu == null ) {
                                    inceptionV1Cpu = new ImageClassifierInceptionV1Quant(getActivity());
                                    inceptionV1Cpu.setNumThreads(currentNumThreads);


                                }
                                System.out.println("inceptionV1Cpu" + " " + size_to_consider);

                                classifyFrame2("Thread 2 inception_v1 cpu ",inceptionV1Cpu,4);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    //classifyFrame("Thread 2");
                    //backgroundHandler2.post(periodicClassifyForThread);
                }
            };

    private Runnable periodicClassifyForThreadInceptionV1Cpu5 =
            new Runnable() {
                @Override
                public void run() {
                    //synchronized (lock) {
                        if (runClassifier2) {
                            try {

                                if( inceptionV1Cpu == null ) {
                                    inceptionV1Cpu = new ImageClassifierInceptionV1Quant(getActivity());
                                    inceptionV1Cpu.setNumThreads(currentNumThreads);


                                }
                                System.out.println("inceptionV1Cpu" + " " + size_to_consider);

                                classifyFrame2("Thread 2 inception_v1 cpu ",inceptionV1Cpu,5);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    //}
                    //classifyFrame("Thread 2");
                    //backgroundHandler2.post(periodicClassifyForThread);
                }
            };






    private Runnable periodicClassifyForThreadinceptionV1Gpu =
            new Runnable() {
                @Override
                public void run() {
                    //synchronized (lock) {
                        if (runClassifier2) {



                            try {

                                if( inceptionV1Gpu == null ) {
                                    inceptionV1Gpu = new ImageClassifierInceptionV1Quant(getActivity());
                                    inceptionV1Gpu.setNumThreads(currentNumThreads);

                                    inceptionV1Gpu.useGpu();


                                }

                                System.out.println("inceptionV1Gpu" + " " + size_to_consider);


                                classifyFrame2("Thread 2 inception_v1 gpu ",inceptionV1Gpu,1);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    //}
                    //classifyFrame("Thread 2");
                    //backgroundHandler2.post(periodicClassifyForThread);
                }
            };

    private Runnable periodicClassifyForThreadinceptionV1Gpu2 =
            new Runnable() {
                @Override
                public void run() {
                    synchronized (lock) {
                        if (runClassifier2) {



                            try {

                                if( inceptionV1Gpu == null ) {
                                    inceptionV1Gpu = new ImageClassifierInceptionV1Quant(getActivity());
                                    inceptionV1Gpu.setNumThreads(currentNumThreads);

                                    inceptionV1Gpu.useGpu();


                                }

                                System.out.println("inceptionV1Gpu" + " " + size_to_consider);


                                classifyFrame2("Thread 2 inception_v1 gpu ",inceptionV1Gpu,2);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    //classifyFrame("Thread 2");
                    //backgroundHandler2.post(periodicClassifyForThread);
                }
            };

    private Runnable periodicClassifyForThreadinceptionV1Gpu3 =
            new Runnable() {
                @Override
                public void run() {
                    synchronized (lock) {
                        if (runClassifier2) {



                            try {

                                if( inceptionV1Gpu == null ) {
                                    inceptionV1Gpu = new ImageClassifierInceptionV1Quant(getActivity());
                                    inceptionV1Gpu.setNumThreads(currentNumThreads);

                                    inceptionV1Gpu.useGpu();


                                }

                                System.out.println("inceptionV1Gpu" + " " + size_to_consider);


                                classifyFrame2("Thread 2 inception_v1 gpu ",inceptionV1Gpu,3);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    //classifyFrame("Thread 2");
                    //backgroundHandler2.post(periodicClassifyForThread);
                }
            };

    private Runnable periodicClassifyForThreadinceptionV1Gpu4 =
            new Runnable() {
                @Override
                public void run() {
                    synchronized (lock) {
                        if (runClassifier2) {



                            try {

                                if( inceptionV1Gpu == null ) {
                                    inceptionV1Gpu = new ImageClassifierInceptionV1Quant(getActivity());
                                    inceptionV1Gpu.setNumThreads(currentNumThreads);

                                    inceptionV1Gpu.useGpu();


                                }

                                System.out.println("inceptionV1Gpu" + " " + size_to_consider);


                                classifyFrame2("Thread 2 inception_v1 gpu ",inceptionV1Gpu,4);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    //classifyFrame("Thread 2");
                    //backgroundHandler2.post(periodicClassifyForThread);
                }
            };

    private Runnable periodicClassifyForThreadinceptionV1Gpu5 =
            new Runnable() {
                @Override
                public void run() {
                    synchronized (lock) {
                        if (runClassifier2) {



                            try {

                                if( inceptionV1Gpu == null ) {
                                    inceptionV1Gpu = new ImageClassifierInceptionV1Quant(getActivity());
                                    inceptionV1Gpu.setNumThreads(currentNumThreads);

                                    inceptionV1Gpu.useGpu();


                                }

                                System.out.println("inceptionV1Gpu" + " " + size_to_consider);


                                classifyFrame2("Thread 2 inception_v1 gpu ",inceptionV1Gpu,5);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    //classifyFrame("Thread 2");
                    //backgroundHandler2.post(periodicClassifyForThread);
                }
            };

    private Runnable periodicClassifyForThreadinceptionV1Dsp =
            new Runnable() {
                @Override
                public void run() {
                    synchronized (lock) {
                        if (runClassifier2) {
                            try {

                                if( inceptionV1Dsp == null ) {
                                    inceptionV1Dsp = new ImageClassifierInceptionV1Quant(getActivity());
                                    inceptionV1Dsp.setNumThreads(currentNumThreads);

                                    inceptionV1Dsp.useDSP();



                                }
                                System.out.println("inceptionV1Dsp" + " " + size_to_consider);

                                classifyFrame2("Thread 2 inception_v1 dsp ",inceptionV1Dsp,1);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    //classifyFrame("Thread 2");
                    //backgroundHandler2.post(periodicClassifyForThread);
                }
            };

    private Runnable periodicClassifyForThreadinceptionV1Dsp2 =
            new Runnable() {
                @Override
                public void run() {
                    synchronized (lock) {
                        if (runClassifier2) {
                            try {

                                if( inceptionV1Dsp == null ) {
                                    inceptionV1Dsp = new ImageClassifierInceptionV1Quant(getActivity());
                                    inceptionV1Dsp.setNumThreads(currentNumThreads);

                                    inceptionV1Dsp.useDSP();



                                }
                                System.out.println("inceptionV1Dsp" + " " + size_to_consider);

                                classifyFrame2("Thread 2 inception_v1 dsp ",inceptionV1Dsp,2);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    //classifyFrame("Thread 2");
                    //backgroundHandler2.post(periodicClassifyForThread);
                }
            };

    private Runnable periodicClassifyForThreadinceptionV1Dsp3 =
            new Runnable() {
                @Override
                public void run() {
                    //synchronized (lock) {
                        if (runClassifier2) {
                            try {

                                if( inceptionV1Dsp == null ) {
                                    inceptionV1Dsp = new ImageClassifierInceptionV1Quant(getActivity());
                                    inceptionV1Dsp.setNumThreads(currentNumThreads);

                                    inceptionV1Dsp.useDSP();



                                }
                                System.out.println("inceptionV1Dsp" + " " + size_to_consider);

                                classifyFrame2("Thread 2 inception_v1 dsp ",inceptionV1Dsp,3);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    //}
                    //classifyFrame("Thread 2");
                    //backgroundHandler2.post(periodicClassifyForThread);
                }
            };

    private Runnable periodicClassifyForThreadinceptionV1Dsp4 =
            new Runnable() {
                @Override
                public void run() {
                    synchronized (lock) {
                        if (runClassifier2) {
                            try {

                                if( inceptionV1Dsp == null ) {
                                    inceptionV1Dsp = new ImageClassifierInceptionV1Quant(getActivity());
                                    inceptionV1Dsp.setNumThreads(currentNumThreads);

                                    inceptionV1Dsp.useDSP();



                                }
                                System.out.println("inceptionV1Dsp" + " " + size_to_consider);

                                classifyFrame2("Thread 2 inception_v1 dsp ",inceptionV1Dsp,4);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    //classifyFrame("Thread 2");
                    //backgroundHandler2.post(periodicClassifyForThread);
                }
            };



    private Runnable periodicClassifyForThreadinceptionV1Dsp5 =
            new Runnable() {
                @Override
                public void run() {
                    synchronized (lock) {
                        if (runClassifier2) {
                            try {

                                if( inceptionV1Dsp == null ) {
                                    inceptionV1Dsp = new ImageClassifierInceptionV1Quant(getActivity());
                                    inceptionV1Dsp.setNumThreads(currentNumThreads);

                                    inceptionV1Dsp.useDSP();



                                }
                                System.out.println("inceptionV1Dsp" + " " + size_to_consider);

                                classifyFrame2("Thread 2 inception_v1 dsp ",inceptionV1Dsp,5);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    //classifyFrame("Thread 2");
                    //backgroundHandler2.post(periodicClassifyForThread);
                }
            };

    private Runnable periodicClassifyForThreadInceptionV3Cpu =
            new Runnable() {
                @Override
                public void run() {
                    synchronized (lock) {
                        if (runClassifier2) {
                            try {

                                if( inceptionV3Cpu == null ) {
                                    inceptionV3Cpu = new ImageClassifierFloatInception(getActivity());
                                    inceptionV3Cpu.setNumThreads(currentNumThreads);


                                }
                                System.out.println("inceptionV3Cpu" + " " + size_to_consider);

                                classifyFrame2("Thread 2 inception_v3 cpu ",inceptionV3Cpu,1);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    //classifyFrame("Thread 2");
                    //backgroundHandler2.post(periodicClassifyForThread);
                }
            };
    private Runnable periodicClassifyForThreadInceptionV3Cpu2 =
            new Runnable() {
                @Override
                public void run() {
                    synchronized (lock) {
                        if (runClassifier2) {
                            try {

                                if( inceptionV3Cpu == null ) {
                                    inceptionV3Cpu = new ImageClassifierFloatInception(getActivity());
                                    inceptionV3Cpu.setNumThreads(currentNumThreads);


                                }
                                System.out.println("inceptionV3Cpu" + " " + size_to_consider);

                                classifyFrame2("Thread 2 inception_v3 cpu ",inceptionV3Cpu,2);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    //classifyFrame("Thread 2");
                    //backgroundHandler2.post(periodicClassifyForThread);
                }
            };

    private Runnable periodicClassifyForThreadInceptionV3Cpu3 =
            new Runnable() {
                @Override
                public void run() {
                    synchronized (lock) {
                        if (runClassifier2) {
                            try {

                                if( inceptionV3Cpu == null ) {
                                    inceptionV3Cpu = new ImageClassifierFloatInception(getActivity());
                                    inceptionV3Cpu.setNumThreads(currentNumThreads);


                                }
                                System.out.println("inceptionV3Cpu" + " " + size_to_consider);

                                classifyFrame2("Thread 2 inception_v3 cpu ",inceptionV3Cpu,3);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    //classifyFrame("Thread 2");
                    //backgroundHandler2.post(periodicClassifyForThread);
                }
            };
    private Runnable periodicClassifyForThreadInceptionV3Cpu4 =
            new Runnable() {
                @Override
                public void run() {
                    synchronized (lock) {
                        if (runClassifier2) {
                            try {

                                if( inceptionV3Cpu == null ) {
                                    inceptionV3Cpu = new ImageClassifierFloatInception(getActivity());
                                    inceptionV3Cpu.setNumThreads(currentNumThreads);


                                }
                                System.out.println("inceptionV3Cpu" + " " + size_to_consider);

                                classifyFrame2("Thread 2 inception_v3 cpu ",inceptionV3Cpu,4);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    //classifyFrame("Thread 2");
                    //backgroundHandler2.post(periodicClassifyForThread);
                }
            };

    private Runnable periodicClassifyForThreadInceptionV3Cpu5 =
            new Runnable() {
                @Override
                public void run() {
                    synchronized (lock) {
                        if (runClassifier2) {
                            try {

                                if( inceptionV3Cpu == null ) {
                                    inceptionV3Cpu = new ImageClassifierFloatInception(getActivity());
                                    inceptionV3Cpu.setNumThreads(currentNumThreads);


                                }
                                System.out.println("inceptionV3Cpu" + " " + size_to_consider);

                                classifyFrame2("Thread 2 inception_v3 cpu ",inceptionV3Cpu,5);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    //classifyFrame("Thread 2");
                    //backgroundHandler2.post(periodicClassifyForThread);
                }
            };
    private Runnable periodicClassifyForThreadInceptionV3Cpu6 =
            new Runnable() {
                @Override
                public void run() {
                    synchronized (lock) {
                        if (runClassifier2) {
                            try {

                                if( inceptionV3Cpu == null ) {
                                    inceptionV3Cpu = new ImageClassifierFloatInception(getActivity());
                                    inceptionV3Cpu.setNumThreads(currentNumThreads);


                                }
                                System.out.println("inceptionV3Cpu" + " " + size_to_consider);

                                classifyFrame2("Thread 2 inception_v3 cpu ",inceptionV3Cpu,6);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    //classifyFrame("Thread 2");
                    //backgroundHandler2.post(periodicClassifyForThread);
                }
            };

    private Runnable periodicClassifyForThreadInceptionV3Gpu =
            new Runnable() {
                @Override
                public void run() {
                    //synchronized (lock) {
                        if (runClassifier2) {



                            try {

                                if( inceptionV3Gpu == null ) {
                                    inceptionV3Gpu = new ImageClassifierFloatInception(getActivity());
                                    inceptionV3Gpu.setNumThreads(currentNumThreads);

                                    inceptionV3Gpu.useGpu();



                                }

                                System.out.println("inceptionV3Gpu" + " " + size_to_consider);


                                classifyFrame2("Thread 2 inception_v3 gpu ",inceptionV3Gpu,1);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    //}
                    //classifyFrame("Thread 2");
                    //backgroundHandler2.post(periodicClassifyForThread);
                }
            };

    private Runnable periodicClassifyForThreadInceptionV3Gpu2 =
            new Runnable() {
                @Override
                public void run() {
                    synchronized (lock) {
                        if (runClassifier2) {



                            try {

                                if( inceptionV3Gpu == null ) {
                                    inceptionV3Gpu = new ImageClassifierFloatInception(getActivity());
                                    inceptionV3Gpu.setNumThreads(currentNumThreads);

                                    inceptionV3Gpu.useGpu();



                                }

                                System.out.println("inceptionV3Gpu" + " " + size_to_consider);


                                classifyFrame2("Thread 2 inception_v3 gpu ",inceptionV3Gpu,2);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    //classifyFrame("Thread 2");
                    //backgroundHandler2.post(periodicClassifyForThread);
                }
            };

    private Runnable periodicClassifyForThreadInceptionV3Gpu3 =
            new Runnable() {
                @Override
                public void run() {
                    synchronized (lock) {
                        if (runClassifier2) {



                            try {

                                if( inceptionV3Gpu == null ) {
                                    inceptionV3Gpu = new ImageClassifierFloatInception(getActivity());
                                    inceptionV3Gpu.setNumThreads(currentNumThreads);

                                    inceptionV3Gpu.useGpu();



                                }

                                System.out.println("inceptionV3Gpu" + " " + size_to_consider);


                                classifyFrame2("Thread 2 inception_v3 gpu ",inceptionV3Gpu,3);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    //classifyFrame("Thread 2");
                    //backgroundHandler2.post(periodicClassifyForThread);
                }
            };

    private Runnable periodicClassifyForThreadInceptionV3Gpu4 =
            new Runnable() {
                @Override
                public void run() {
                    synchronized (lock) {
                        if (runClassifier2) {



                            try {

                                if( inceptionV3Gpu == null ) {
                                    inceptionV3Gpu = new ImageClassifierFloatInception(getActivity());
                                    inceptionV3Gpu.setNumThreads(currentNumThreads);

                                    inceptionV3Gpu.useGpu();



                                }

                                System.out.println("inceptionV3Gpu" + " " + size_to_consider);


                                classifyFrame2("Thread 2 inception_v3 gpu ",inceptionV3Gpu,4);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    //classifyFrame("Thread 2");
                    //backgroundHandler2.post(periodicClassifyForThread);
                }
            };

    private Runnable periodicClassifyForThreadInceptionV3Gpu5 =
            new Runnable() {
                @Override
                public void run() {
                    synchronized (lock) {
                        if (runClassifier2) {



                            try {

                                if( inceptionV3Gpu == null ) {
                                    inceptionV3Gpu = new ImageClassifierFloatInception(getActivity());
                                    inceptionV3Gpu.setNumThreads(currentNumThreads);

                                    inceptionV3Gpu.useGpu();



                                }

                                System.out.println("inceptionV3Gpu" + " " + size_to_consider);


                                classifyFrame2("Thread 2 inception_v3 gpu ",inceptionV3Gpu,5);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    //classifyFrame("Thread 2");
                    //backgroundHandler2.post(periodicClassifyForThread);
                }
            };

    private Runnable periodicClassifyForThreadinceptionV3Dsp =
            new Runnable() {
                @Override
                public void run() {
                    synchronized (lock) {
                        if (runClassifier2) {
                            try {

                                if( inceptionV3Dsp == null ) {
                                    inceptionV3Dsp = new ImageClassifierFloatInception(getActivity());
                                    inceptionV3Dsp.setNumThreads(currentNumThreads);

                                    inceptionV3Dsp.useDSP();

                                }
                                System.out.println("inceptionV3Dsp" + " " + size_to_consider);

                                classifyFrame2("Thread 2 inception_v3 dsp ",inceptionV3Dsp,1);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    //classifyFrame("Thread 2");
                    //backgroundHandler2.post(periodicClassifyForThread);
                }
            };

    private Runnable periodicClassifyForThreadinceptionV3Dsp2 =
            new Runnable() {
                @Override
                public void run() {
                    synchronized (lock) {
                        if (runClassifier2) {
                            try {

                                if( inceptionV3Dsp == null ) {
                                    inceptionV3Dsp = new ImageClassifierFloatInception(getActivity());
                                    inceptionV3Dsp.setNumThreads(currentNumThreads);

                                    inceptionV3Dsp.useDSP();

                                }
                                System.out.println("inceptionV3Dsp" + " " + size_to_consider);

                                classifyFrame2("Thread 2 inception_v3 dsp ",inceptionV3Dsp,2);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    //classifyFrame("Thread 2");
                    //backgroundHandler2.post(periodicClassifyForThread);
                }
            };

    private Runnable periodicClassifyForThreadinceptionV3Dsp3 =
            new Runnable() {
                @Override
                public void run() {
                    synchronized (lock) {
                        if (runClassifier2) {
                            try {

                                if( inceptionV3Dsp == null ) {
                                    inceptionV3Dsp = new ImageClassifierFloatInception(getActivity());
                                    inceptionV3Dsp.setNumThreads(currentNumThreads);

                                    inceptionV3Dsp.useDSP();

                                }
                                System.out.println("inceptionV3Dsp" + " " + size_to_consider);

                                classifyFrame2("Thread 2 inception_v3 dsp ",inceptionV3Dsp,3);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    //classifyFrame("Thread 2");
                    //backgroundHandler2.post(periodicClassifyForThread);
                }
            };

    private Runnable periodicClassifyForThreadinceptionV3Dsp4 =
            new Runnable() {
                @Override
                public void run() {
                    synchronized (lock) {
                        if (runClassifier2) {
                            try {

                                if( inceptionV3Dsp == null ) {
                                    inceptionV3Dsp = new ImageClassifierFloatInception(getActivity());
                                    inceptionV3Dsp.setNumThreads(currentNumThreads);

                                    inceptionV3Dsp.useDSP();

                                }
                                System.out.println("inceptionV3Dsp" + " " + size_to_consider);

                                classifyFrame2("Thread 2 inception_v3 dsp ",inceptionV3Dsp,4);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    //classifyFrame("Thread 2");
                    //backgroundHandler2.post(periodicClassifyForThread);
                }
            };

    private Runnable periodicClassifyForThreadinceptionV3Dsp5 =
            new Runnable() {
                @Override
                public void run() {
                    synchronized (lock) {
                        if (runClassifier2) {
                            try {

                                if( inceptionV3Dsp == null ) {
                                    inceptionV3Dsp = new ImageClassifierFloatInception(getActivity());
                                    inceptionV3Dsp.setNumThreads(currentNumThreads);

                                    inceptionV3Dsp.useDSP();

                                }
                                System.out.println("inceptionV3Dsp" + " " + size_to_consider);

                                classifyFrame2("Thread 2 inception_v3 dsp ",inceptionV3Dsp,5);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    //classifyFrame("Thread 2");
                    //backgroundHandler2.post(periodicClassifyForThread);
                }
            };

    private Runnable periodicClassifyForThreadMobilenetV1Cpu =
            new Runnable() {
                @Override
                public void run() {
                    //synchronized (lock) {
                        if (runClassifier2) {
                            try {

                                if( mobilenetV1Cpu == null ) {
                                    mobilenetV1Cpu = new ImageClassifierFloatMobileNet (getActivity());
                                    mobilenetV1Cpu.setNumThreads(currentNumThreads);


                                }
                                System.out.println("mobilenetV1Cpu" + " " + size_to_consider);

                                classifyFrame2("Thread 2 mobilenet_v1 cpu ",mobilenetV1Cpu,1);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    //}
                    //classifyFrame("Thread 2");
                    //backgroundHandler2.post(periodicClassifyForThread);
                }
            };


    private Runnable periodicClassifyForThreadMobilenetV1Cpu2 =
            new Runnable() {
                @Override
                public void run() {
                   // synchronized (lock) {
                        if (runClassifier2) {
                            try {

                                if( mobilenetV1Cpu == null ) {
                                    mobilenetV1Cpu = new ImageClassifierFloatMobileNet (getActivity());
                                    mobilenetV1Cpu.setNumThreads(currentNumThreads);


                                }
                                System.out.println("mobilenetV1Cpu" + " " + size_to_consider);

                                classifyFrame2("Thread 2 mobilenet_v1 cpu ",mobilenetV1Cpu,2);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    //}
                    //classifyFrame("Thread 2");
                    //backgroundHandler2.post(periodicClassifyForThread);
                }
            };

    private Runnable periodicClassifyForThreadMobilenetV1Cpu3 =
            new Runnable() {
                @Override
                public void run() {
                    //synchronized (lock) {
                        if (runClassifier2) {
                            try {

                                if( mobilenetV1Cpu == null ) {
                                    mobilenetV1Cpu = new ImageClassifierFloatMobileNet (getActivity());
                                    mobilenetV1Cpu.setNumThreads(currentNumThreads);


                                }
                                System.out.println("mobilenetV1Cpu" + " " + size_to_consider);

                                classifyFrame2("Thread 2 mobilenet_v1 cpu ",mobilenetV1Cpu,3);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    //}
                    //classifyFrame("Thread 2");
                    //backgroundHandler2.post(periodicClassifyForThread);
                }
            };
    private Runnable periodicClassifyForThreadMobilenetV1Cpu4 =
            new Runnable() {
                @Override
                public void run() {
                    //synchronized (lock) {
                        if (runClassifier2) {
                            try {

                                if( mobilenetV1Cpu == null ) {
                                    mobilenetV1Cpu = new ImageClassifierFloatMobileNet (getActivity());
                                    mobilenetV1Cpu.setNumThreads(currentNumThreads);


                                }
                                System.out.println("mobilenetV1Cpu" + " " + size_to_consider);

                                classifyFrame2("Thread 2 mobilenet_v1 cpu ",mobilenetV1Cpu,4);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    //}
                    //classifyFrame("Thread 2");
                    //backgroundHandler2.post(periodicClassifyForThread);
                }
            };

    private Runnable periodicClassifyForThreadMobilenetV1Cpu5 =
            new Runnable() {
                @Override
                public void run() {
                    synchronized (lock) {
                        if (runClassifier2) {
                            try {

                                if( mobilenetV1Cpu == null ) {
                                    mobilenetV1Cpu = new ImageClassifierFloatMobileNet (getActivity());
                                    mobilenetV1Cpu.setNumThreads(currentNumThreads);


                                }
                                System.out.println("mobilenetV1Cpu" + " " + size_to_consider);

                                classifyFrame2("Thread 2 mobilenet_v1 cpu ",mobilenetV1Cpu,5);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    //classifyFrame("Thread 2");
                    //backgroundHandler2.post(periodicClassifyForThread);
                }
            };

    private Runnable periodicClassifyForThreadMobilenetV1Gpu =
            new Runnable() {
                @Override
                public void run() {
                    synchronized (lock) {
                        if (runClassifier2) {



                            try {

                                if( mobilenetV1Gpu == null ) {
                                    mobilenetV1Gpu = new ImageClassifierFloatMobileNet(getActivity());
                                    mobilenetV1Gpu.setNumThreads(currentNumThreads);

                                    mobilenetV1Gpu.useGpu();




                                }

                                System.out.println("mobilenetV1Gpu" + " " + size_to_consider);

                                classifyFrame2("Thread 2 mobilenet_v1 gpu ",mobilenetV1Gpu,1);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    //classifyFrame("Thread 2");
                    //backgroundHandler2.post(periodicClassifyForThread);
                }
            };

    private Runnable periodicClassifyForThreadMobilenetV1Gpu2 =
            new Runnable() {
                @Override
                public void run() {
                    synchronized (lock) {
                        if (runClassifier2) {



                            try {

                                if( mobilenetV1Gpu == null ) {
                                    mobilenetV1Gpu = new ImageClassifierFloatMobileNet(getActivity());
                                    mobilenetV1Gpu.setNumThreads(currentNumThreads);

                                    mobilenetV1Gpu.useGpu();




                                }

                                System.out.println("mobilenetV1Gpu" + " " + size_to_consider);

                                classifyFrame2("Thread 2 mobilenet_v1 gpu ",mobilenetV1Gpu,2);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    //classifyFrame("Thread 2");
                    //backgroundHandler2.post(periodicClassifyForThread);
                }
            };

    private Runnable periodicClassifyForThreadMobilenetV1Gpu3 =
            new Runnable() {
                @Override
                public void run() {
                    synchronized (lock) {
                        if (runClassifier2) {



                            try {

                                if( mobilenetV1Gpu == null ) {
                                    mobilenetV1Gpu = new ImageClassifierFloatMobileNet(getActivity());
                                    mobilenetV1Gpu.setNumThreads(currentNumThreads);

                                    mobilenetV1Gpu.useGpu();




                                }

                                System.out.println("mobilenetV1Gpu" + " " + size_to_consider);

                                classifyFrame2("Thread 2 mobilenet_v1 gpu ",mobilenetV1Gpu,3);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    //classifyFrame("Thread 2");
                    //backgroundHandler2.post(periodicClassifyForThread);
                }
            };

    private Runnable periodicClassifyForThreadMobilenetV1Gpu4 =
            new Runnable() {
                @Override
                public void run() {
                    //synchronized (lock) {
                        if (runClassifier2) {



                            try {

                                if( mobilenetV1Gpu == null ) {
                                    mobilenetV1Gpu = new ImageClassifierFloatMobileNet(getActivity());
                                    mobilenetV1Gpu.setNumThreads(currentNumThreads);

                                    mobilenetV1Gpu.useGpu();




                                }

                                System.out.println("mobilenetV1Gpu" + " " + size_to_consider);

                                classifyFrame2("Thread 2 mobilenet_v1 gpu ",mobilenetV1Gpu,4);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    //}
                    //classifyFrame("Thread 2");
                    //backgroundHandler2.post(periodicClassifyForThread);
                }
            };

    private Runnable periodicClassifyForThreadMobilenetV1Gpu5 =
            new Runnable() {
                @Override
                public void run() {
                    synchronized (lock) {
                        if (runClassifier2) {



                            try {

                                if( mobilenetV1Gpu == null ) {
                                    mobilenetV1Gpu = new ImageClassifierFloatMobileNet(getActivity());
                                    mobilenetV1Gpu.setNumThreads(currentNumThreads);

                                    mobilenetV1Gpu.useGpu();




                                }

                                System.out.println("mobilenetV1Gpu" + " " + size_to_consider);

                                classifyFrame2("Thread 2 mobilenet_v1 gpu ",mobilenetV1Gpu,5);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    //classifyFrame("Thread 2");
                    //backgroundHandler2.post(periodicClassifyForThread);
                }
            };

    private Runnable periodicClassifyForThreadMobilenetV1Dsp =
            new Runnable() {
                @Override
                public void run() {
                    synchronized (lock) {
                        if (runClassifier2) {
                            try {

                                if( mobilenetV1Dsp == null ) {
                                    mobilenetV1Dsp = new ImageClassifierFloatMobileNetV2(getActivity());
                                    mobilenetV1Dsp.setNumThreads(currentNumThreads);

                                    mobilenetV1Dsp.useDSP();



                                }
                                System.out.println("mobilenetV1Dsp" + " " + size_to_consider);

                                classifyFrame2("Thread 2 mobilenet_v1 dsp ",mobilenetV1Dsp,1);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    //classifyFrame("Thread 2");
                    //backgroundHandler2.post(periodicClassifyForThread);
                }
            };

    private Runnable periodicClassifyForThreadMobilenetV1Dsp2 =
            new Runnable() {
                @Override
                public void run() {
                    synchronized (lock) {
                        if (runClassifier2) {
                            try {

                                if( mobilenetV1Dsp == null ) {
                                    mobilenetV1Dsp = new ImageClassifierFloatMobileNetV2(getActivity());
                                    mobilenetV1Dsp.setNumThreads(currentNumThreads);

                                    mobilenetV1Dsp.useDSP();



                                }
                                System.out.println("mobilenetV1Dsp" + " " + size_to_consider);

                                classifyFrame2("Thread 2 mobilenet_v1 dsp ",mobilenetV1Dsp,2);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    //classifyFrame("Thread 2");
                    //backgroundHandler2.post(periodicClassifyForThread);
                }
            };

    private Runnable periodicClassifyForThreadMobilenetV1Dsp3 =
            new Runnable() {
                @Override
                public void run() {
                    synchronized (lock) {
                        if (runClassifier2) {
                            try {

                                if( mobilenetV1Dsp == null ) {
                                    mobilenetV1Dsp = new ImageClassifierFloatMobileNetV2(getActivity());
                                    mobilenetV1Dsp.setNumThreads(currentNumThreads);

                                    mobilenetV1Dsp.useDSP();



                                }
                                System.out.println("mobilenetV1Dsp" + " " + size_to_consider);

                                classifyFrame2("Thread 2 mobilenet_v1 dsp ",mobilenetV1Dsp,3);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    //classifyFrame("Thread 2");
                    //backgroundHandler2.post(periodicClassifyForThread);
                }
            };

    private Runnable periodicClassifyForThreadMobilenetV1Dsp4 =
            new Runnable() {
                @Override
                public void run() {
                    synchronized (lock) {
                        if (runClassifier2) {
                            try {

                                if( mobilenetV1Dsp == null ) {
                                    mobilenetV1Dsp = new ImageClassifierFloatMobileNetV2(getActivity());
                                    mobilenetV1Dsp.setNumThreads(currentNumThreads);

                                    mobilenetV1Dsp.useDSP();



                                }
                                System.out.println("mobilenetV1Dsp" + " " + size_to_consider);

                                classifyFrame2("Thread 2 mobilenet_v1 dsp ",mobilenetV1Dsp,4);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    //classifyFrame("Thread 2");
                    //backgroundHandler2.post(periodicClassifyForThread);
                }
            };

    private Runnable periodicClassifyForThreadMobilenetV1Dsp5 =
            new Runnable() {
                @Override
                public void run() {
                    synchronized (lock) {
                        if (runClassifier2) {
                            try {

                                if( mobilenetV1Dsp == null ) {
                                    mobilenetV1Dsp = new ImageClassifierFloatMobileNetV2(getActivity());
                                    mobilenetV1Dsp.setNumThreads(currentNumThreads);

                                    mobilenetV1Dsp.useDSP();



                                }
                                System.out.println("mobilenetV1Dsp" + " " + size_to_consider);

                                classifyFrame2("Thread 2 mobilenet_v1 dsp ",mobilenetV1Dsp,5);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    //classifyFrame("Thread 2");
                    //backgroundHandler2.post(periodicClassifyForThread);
                }
            };


    private Runnable periodicClassifyForThreadMobilenetV2Cpu =
            new Runnable() {
                @Override
                public void run() {
                    synchronized (lock) {
                        if (runClassifier2) {
                            try {

                                if( mobilenetV2Cpu == null ) {
                                    mobilenetV2Cpu = new ImageClassifierFloatMobileNet (getActivity());
                                    mobilenetV2Cpu.setNumThreads(currentNumThreads);


                                }
                                System.out.println("mobilenetV2Cpu" + " " + size_to_consider);

                                classifyFrame2("Thread 2 mobilenet_v2 cpu ",mobilenetV2Cpu,1);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    //classifyFrame("Thread 2");
                    //backgroundHandler2.post(periodicClassifyForThread);
                }
            };

    private Runnable periodicClassifyForThreadMobilenetV2Cpu2 =
            new Runnable() {
                @Override
                public void run() {
                    synchronized (lock) {
                        if (runClassifier2) {
                            try {

                                if( mobilenetV2Cpu == null ) {
                                    mobilenetV2Cpu = new ImageClassifierFloatMobileNet (getActivity());
                                    mobilenetV2Cpu.setNumThreads(currentNumThreads);


                                }
                                System.out.println("mobilenetV2Cpu" + " " + size_to_consider);

                                classifyFrame2("Thread 2 mobilenet_v2 cpu ",mobilenetV2Cpu,2);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    //classifyFrame("Thread 2");
                    //backgroundHandler2.post(periodicClassifyForThread);
                }
            };

    private Runnable periodicClassifyForThreadMobilenetV2Cpu3 =
            new Runnable() {
                @Override
                public void run() {
                    //synchronized (lock) {
                        if (runClassifier2) {
                            try {

                                if( mobilenetV2Cpu == null ) {
                                    mobilenetV2Cpu = new ImageClassifierFloatMobileNet (getActivity());
                                    mobilenetV2Cpu.setNumThreads(currentNumThreads);


                                }
                                System.out.println("mobilenetV2Cpu" + " " + size_to_consider);

                                classifyFrame2("Thread 2 mobilenet_v2 cpu ",mobilenetV2Cpu,3);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    //}
                    //classifyFrame("Thread 2");
                    //backgroundHandler2.post(periodicClassifyForThread);
                }
            };

    private Runnable periodicClassifyForThreadMobilenetV2Cpu4 =
            new Runnable() {
                @Override
                public void run() {
                    synchronized (lock) {
                        if (runClassifier2) {
                            try {

                                if( mobilenetV2Cpu == null ) {
                                    mobilenetV2Cpu = new ImageClassifierFloatMobileNet (getActivity());
                                    mobilenetV2Cpu.setNumThreads(currentNumThreads);


                                }
                                System.out.println("mobilenetV2Cpu" + " " + size_to_consider);

                                classifyFrame2("Thread 2 mobilenet_v2 cpu ",mobilenetV2Cpu,4);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    //classifyFrame("Thread 2");
                    //backgroundHandler2.post(periodicClassifyForThread);
                }
            };


    private Runnable periodicClassifyForThreadMobilenetV2Cpu5 =
            new Runnable() {
                @Override
                public void run() {
                    synchronized (lock) {
                        if (runClassifier2) {
                            try {

                                if( mobilenetV2Cpu == null ) {
                                    mobilenetV2Cpu = new ImageClassifierFloatMobileNet (getActivity());
                                    mobilenetV2Cpu.setNumThreads(currentNumThreads);


                                }
                                System.out.println("mobilenetV2Cpu" + " " + size_to_consider);

                                classifyFrame2("Thread 2 mobilenet_v2 cpu ",mobilenetV2Cpu,5);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    //classifyFrame("Thread 2");
                    //backgroundHandler2.post(periodicClassifyForThread);
                }
            };

    private Runnable periodicClassifyForThreadMobilenetV2Gpu =
            new Runnable() {
                @Override
                public void run() {
                    //synchronized (lock) {
                        if (runClassifier2) {



                            try {

                                if( mobilenetV2Gpu == null ) {
                                    mobilenetV2Gpu = new ImageClassifierFloatMobileNetV2(getActivity());
                                    mobilenetV2Gpu.setNumThreads(currentNumThreads);

                                    mobilenetV2Gpu.useGpu();
                                    System.out.println("mobilenetV2Gpu" + " " + size_to_consider);

                                }



                                classifyFrame2("Thread 2 mobilenet 2 gpu ",mobilenetV2Gpu,1);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    //}
                    //classifyFrame("Thread 2");
                    //backgroundHandler2.post(periodicClassifyForThread);
                }
            };

    private Runnable periodicClassifyForThreadMobilenetV2Gpu2 =
            new Runnable() {
                @Override
                public void run() {
                    synchronized (lock) {
                        if (runClassifier2) {



                            try {

                                if( mobilenetV2Gpu == null ) {
                                    mobilenetV2Gpu = new ImageClassifierFloatMobileNetV2(getActivity());
                                    mobilenetV2Gpu.setNumThreads(currentNumThreads);

                                    mobilenetV2Gpu.useGpu();
                                    System.out.println("mobilenetV2Gpu" + " " + size_to_consider);

                                }



                                classifyFrame2("Thread 2 mobilenet 2 gpu ",mobilenetV2Gpu,2);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    //classifyFrame("Thread 2");
                    //backgroundHandler2.post(periodicClassifyForThread);
                }
            };

    private Runnable periodicClassifyForThreadMobilenetV2Gpu3 =
            new Runnable() {
                @Override
                public void run() {
                    //synchronized (lock) {
                        if (runClassifier2) {



                            try {

                                if( mobilenetV2Gpu == null ) {
                                    mobilenetV2Gpu = new ImageClassifierFloatMobileNetV2(getActivity());
                                    mobilenetV2Gpu.setNumThreads(currentNumThreads);

                                    mobilenetV2Gpu.useGpu();
                                    System.out.println("mobilenetV2Gpu" + " " + size_to_consider);

                                }



                                classifyFrame2("Thread 2 mobilenet 2 gpu ",mobilenetV2Gpu,3);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    //}
                    //classifyFrame("Thread 2");
                    //backgroundHandler2.post(periodicClassifyForThread);
                }
            };

    private Runnable periodicClassifyForThreadMobilenetV2Gpu4 =
            new Runnable() {
                @Override
                public void run() {
                    synchronized (lock) {
                        if (runClassifier2) {



                            try {

                                if( mobilenetV2Gpu == null ) {
                                    mobilenetV2Gpu = new ImageClassifierFloatMobileNetV2(getActivity());
                                    mobilenetV2Gpu.setNumThreads(currentNumThreads);

                                    mobilenetV2Gpu.useGpu();
                                    System.out.println("mobilenetV2Gpu" + " " + size_to_consider);

                                }



                                classifyFrame2("Thread 2 mobilenet 2 gpu ",mobilenetV2Gpu,4);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    //classifyFrame("Thread 2");
                    //backgroundHandler2.post(periodicClassifyForThread);
                }
            };

    private Runnable periodicClassifyForThreadMobilenetV2Gpu5 =
            new Runnable() {
                @Override
                public void run() {
                    synchronized (lock) {
                        if (runClassifier2) {



                            try {

                                if( mobilenetV2Gpu == null ) {
                                    mobilenetV2Gpu = new ImageClassifierFloatMobileNetV2(getActivity());
                                    mobilenetV2Gpu.setNumThreads(currentNumThreads);

                                    mobilenetV2Gpu.useGpu();
                                    System.out.println("mobilenetV2Gpu" + " " + size_to_consider);

                                }



                                classifyFrame2("Thread 2 mobilenet 2 gpu ",mobilenetV2Gpu,5);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    //classifyFrame("Thread 2");
                    //backgroundHandler2.post(periodicClassifyForThread);
                }
            };

    private Runnable periodicClassifyForThreadMobilenetV2Dsp =
            new Runnable() {
                @Override
                public void run() {
                    synchronized (lock) {
                        if (runClassifier2) {
                            try {

                                if( mobilenetV2Dsp == null ) {
                                    mobilenetV2Dsp = new ImageClassifierFloatMobileNetV2(getActivity());
                                    mobilenetV2Dsp.setNumThreads(currentNumThreads);

                                    mobilenetV2Dsp.useDSP();



                                }

                                System.out.println("mobilenetV2Dsp" + " " + size_to_consider);


                                classifyFrame2("Thread 2 mobilenet_v2 dsp ",mobilenetV2Dsp,1);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    //classifyFrame("Thread 2");
                    //backgroundHandler2.post(periodicClassifyForThread);
                }
            };

    private Runnable periodicClassifyForThreadMobilenetV2Dsp2 =
            new Runnable() {
                @Override
                public void run() {
                    synchronized (lock) {
                        if (runClassifier2) {
                            try {

                                if( mobilenetV2Dsp == null ) {
                                    mobilenetV2Dsp = new ImageClassifierFloatMobileNetV2(getActivity());
                                    mobilenetV2Dsp.setNumThreads(currentNumThreads);

                                    mobilenetV2Dsp.useDSP();



                                }

                                System.out.println("mobilenetV2Dsp" + " " + size_to_consider);


                                classifyFrame2("Thread 2 mobilenet_v2 dsp ",mobilenetV2Dsp,2);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    //classifyFrame("Thread 2");
                    //backgroundHandler2.post(periodicClassifyForThread);
                }
            };

    private Runnable periodicClassifyForThreadMobilenetV2Dsp3 =
            new Runnable() {
                @Override
                public void run() {
                    synchronized (lock) {
                        if (runClassifier2) {
                            try {

                                if( mobilenetV2Dsp == null ) {
                                    mobilenetV2Dsp = new ImageClassifierFloatMobileNetV2(getActivity());
                                    mobilenetV2Dsp.setNumThreads(currentNumThreads);

                                    mobilenetV2Dsp.useDSP();



                                }

                                System.out.println("mobilenetV2Dsp" + " " + size_to_consider);


                                classifyFrame2("Thread 2 mobilenet_v2 dsp ",mobilenetV2Dsp,3);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    //classifyFrame("Thread 2");
                    //backgroundHandler2.post(periodicClassifyForThread);
                }
            };

    private Runnable periodicClassifyForThreadMobilenetV2Dsp4 =
            new Runnable() {
                @Override
                public void run() {
                    synchronized (lock) {
                        if (runClassifier2) {
                            try {

                                if( mobilenetV2Dsp == null ) {
                                    mobilenetV2Dsp = new ImageClassifierFloatMobileNetV2(getActivity());
                                    mobilenetV2Dsp.setNumThreads(currentNumThreads);

                                    mobilenetV2Dsp.useDSP();



                                }

                                System.out.println("mobilenetV2Dsp" + " " + size_to_consider);


                                classifyFrame2("Thread 2 mobilenet_v2 dsp ",mobilenetV2Dsp,4);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    //classifyFrame("Thread 2");
                    //backgroundHandler2.post(periodicClassifyForThread);
                }
            };

    private Runnable periodicClassifyForThreadMobilenetV2Dsp5 =
            new Runnable() {
                @Override
                public void run() {
                    synchronized (lock) {
                        if (runClassifier2) {
                            try {

                                if( mobilenetV2Dsp == null ) {
                                    mobilenetV2Dsp = new ImageClassifierFloatMobileNetV2(getActivity());
                                    mobilenetV2Dsp.setNumThreads(currentNumThreads);

                                    mobilenetV2Dsp.useDSP();



                                }

                                System.out.println("mobilenetV2Dsp" + " " + size_to_consider);


                                classifyFrame2("Thread 2 mobilenet_v2 dsp ",mobilenetV2Dsp,5);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    //classifyFrame("Thread 2");
                    //backgroundHandler2.post(periodicClassifyForThread);
                }
            };


    private Runnable periodicClassify3 =
            new Runnable() {
                @Override
                public void run() {

                    for ( int i = 1; i <= 3; i++ ) {
                        System.out.println("Hi from thread " + i);
                    }
                    //classifyFrame("Thread 2");
                    //backgroundHandler.post(periodicClassify);
                }
            };
//
//    private Runnable periodicClassify4 =
//            new Runnable() {
//                @Override
//                public void run() {
//                    synchronized (lock) {
//                        if (runClassifier4) {
//                            try {
//
//                                classifyFrame("Thread 4");
//                                classifyFrame("Thread 4");
//                                classifyFrame("Thread 4");
//
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }
//                    //classifyFrame("Thread 2");
//                    backgroundHandler4.post(periodicClassify);
//                }
//            };


    /** Creates a new {@link CameraCaptureSession} for camera preview. */
    private void createCameraPreviewSession() {
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;

            // We configure the size of default buffer to be the size of camera preview we want.
            texture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());

            // This is the output Surface we need to start preview.
            Surface surface = new Surface(texture);

            // We set up a CaptureRequest.Builder with the output Surface.
            previewRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            previewRequestBuilder.addTarget(surface);

            // Here, we create a CameraCaptureSession for camera preview.
            cameraDevice.createCaptureSession(
                    Arrays.asList(surface),
                    new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            // The camera is already closed
                            if (null == cameraDevice) {
                                return;
                            }

                            // When the session is ready, we start displaying the preview.
                            captureSession = cameraCaptureSession;
                            try {
                                // Auto focus should be continuous for camera preview.
                                previewRequestBuilder.set(
                                        CaptureRequest.CONTROL_AF_MODE,
                                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

                                // Finally, we start displaying the camera preview.
                                previewRequest = previewRequestBuilder.build();
                                captureSession.setRepeatingRequest(
                                        previewRequest, captureCallback, backgroundHandler);
                            } catch (CameraAccessException e) {
                                Log.e(TAG, "Failed to set up config to capture Camera", e);
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                            showToast("Failed");
                        }
                    },
                    null);
        } catch (CameraAccessException e) {
            Log.e(TAG, "Failed to preview Camera", e);
        }
    }

    /**
     * Configures the necessary {@link android.graphics.Matrix} transformation to `textureView`. This
     * method should be called after the camera preview size is determined in setUpCameraOutputs and
     * also the size of `textureView` is fixed.
     *
     * @param viewWidth The width of `textureView`
     * @param viewHeight The height of `textureView`
     */
    private void configureTransform(int viewWidth, int viewHeight) {
        Activity activity = getActivity();
        if (null == textureView || null == previewSize || null == activity) {
            return;
        }
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, previewSize.getHeight(), previewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale =
                    Math.max(
                            (float) viewHeight / previewSize.getHeight(),
                            (float) viewWidth / previewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        textureView.setTransform(matrix);
    }


    private void writeToFile(String data,Context context, String filename) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(filename, Context.MODE_APPEND));

            System.out.println(context.getFilesDir());

            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    double avg1 = 0.0;
    double avg2 = 0.0;
    double avg3 = 0.0;
    double avg4 = 0.0;


    /** Classifies a frame from the preview stream. */


    private void classifyFrame2(String thread_name, ImageClassifier classifierNow, int batchSize) throws IOException {





        if (classifierNow == null || getActivity() == null) {
            // It"s important to not call showToast every frame, or else the app will starve and
            // hang. updateActiveModel() already puts an error message up with showToast.
            // showToast("Uninitialized Classifier or invalid context.");

            if (cameraDevice == null) {
                Log.d("Null Camera", "True");
            }

            Log.d("Null Classifier", "True");
            return;
        }
        SpannableStringBuilder textToShow2 = new SpannableStringBuilder();




        //Bitmap bitmap = textureView.getBitmap(classifier.getImageSizeX(), classifier.getImageSizeY());




//        Bitmap bitmap2 = textureView.getBitmap(classifierNow.getImageSizeX(), classifierNow.getImageSizeY());



        Log.d("Thread Name ", thread_name + "  " + SystemClock.uptimeMillis() + " batch " );


        Long l2 = classifierNow.classifyFrame(resizedBitmap, textToShow2,thread_name,batchSize,SystemClock.uptimeMillis());
        //resizedBitmap.recycle();


        System.out.println(thread_name + " batchsize " + batchSize + " response time " + SystemClock.uptimeMillis() );


        total2+=l2;
        i2+=1;



        showToast(textToShow2);

        String text2 = "Thread 2 " + Double.toString(avg2);
        //textView2.setText(text2);

//
//        Float f1 = cpuTemperature1("cat /sys/class/thermal/thermal_zone1/temp");
//        float f2 = cpuTemperature1("cat /sys/class/thermal/thermal_zone2/temp");
//        Float f3 = cpuTemperature1("cat /sys/class/thermal/thermal_zone3/temp");
//        Float f4 = cpuTemperature1("cat /sys/class/thermal/thermal_zone4/temp");
//        Float f5 = cpuTemperature1("cat /sys/class/thermal/thermal_zone7/temp");
//        Float f6 = cpuTemperature1("cat /sys/class/thermal/thermal_zone8/temp");
//        Float f7 = cpuTemperature1("cat /sys/class/thermal/thermal_zone9/temp");
//        Float f8 = cpuTemperature1("cat /sys/class/thermal/thermal_zone10/temp");
//
//        System.out.println(f1);
//
//        String temps = f1.toString() + "\t" + Float.toString(f2) + "\t" + f3.toString() + "\t" + f4.toString() + "\t" +
//                f5.toString() + "\t" + f6.toString() + "\t" + f7.toString() + "\t" + f8.toString() + "\t";



    }

    /** Compares two {@code Size}s based on their areas. */
    private static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won"t overflow
            return Long.signum(
                    (long) lhs.getWidth() * lhs.getHeight() - (long) rhs.getWidth() * rhs.getHeight());
        }
    }

    /** Shows an error message dialog. */
    public static class ErrorDialog extends DialogFragment {

        private static final String ARG_MESSAGE = "message";

        public static ErrorDialog newInstance(String message) {
            ErrorDialog dialog = new ErrorDialog();
            Bundle args = new Bundle();
            args.putString(ARG_MESSAGE, message);
            dialog.setArguments(args);
            return dialog;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Activity activity = getActivity();
            return new AlertDialog.Builder(activity)
                    .setMessage(getArguments().getString(ARG_MESSAGE))
                    .setPositiveButton(
                            android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    activity.finish();
                                }
                            })
                    .create();
        }
    }
}
