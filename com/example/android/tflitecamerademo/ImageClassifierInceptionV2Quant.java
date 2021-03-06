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

import android.app.Activity;
import android.os.SystemClock;

import org.tensorflow.lite.Tensor;

import java.io.IOException;

/**
 * This classifier works with the quantized MobileNet model.
 */
public class ImageClassifierInceptionV2Quant extends ImageClassifier {

    /**
     * An array to hold inference results, to be feed into Tensorflow Lite as outputs.
     * This isn't part of the super class, because we need a primitive array here.
     */
    private byte[][] labelProbArray = null;

    /**
     * Initializes an {@code ImageClassifier}.
     *
     * @param activity
     */
    ImageClassifierInceptionV2Quant(Activity activity) throws IOException {
        super(activity);
    }

    @Override
    protected String getModelPath() {
        // you can download this file from
        // see build.gradle for where to obtain this file. It should be auto
        // downloaded into assets.
        return "inception_v2_224_quant.tflite";
    }

    @Override
    protected String getLabelPath() {
        return "imagenet_labels.txt";
    }

    @Override
    protected int getImageSizeX() {
        return 224;
    }

    @Override
    protected int getImageSizeY() {
        return 224;
    }

    @Override
    protected int getNumBytesPerChannel() {
        // the quantized model uses a single byte only
        return 1;
    }

    @Override
    protected void addPixelValue(int pixelValue) {
        imgData.put((byte) ((pixelValue >> 16) & 0xFF));
        imgData.put((byte) ((pixelValue >> 8) & 0xFF));
        imgData.put((byte) (pixelValue & 0xFF));
    }

    @Override
    protected float getProbability(int labelIndex) {
        return labelProbArray[0][labelIndex];
    }

    @Override
    protected void setProbability(int labelIndex, Number value) {
        labelProbArray[0][labelIndex] = value.byteValue();
    }

    @Override
    protected float getNormalizedProbability(int labelIndex) {
        return (labelProbArray[0][labelIndex] & 0xff) / 255.0f;
    }

    @Override
    protected void runInference() {

        Tensor t1 = tflite.getInputTensor(0);

        int[] dims = t1.shape();

        for ( int dim : dims ){
            System.out.println("Dimension " + dim + getModelPath());
        }

        long startModel = SystemClock.uptimeMillis();


        int[] timings2 = new int[] {2,224,224,3};

        tflite.resizeInput(0,timings2);

        long endModel = SystemClock.uptimeMillis();

        System.out.println("Resize time " + (endModel-startModel));




        tflite.run(imgData, labelProbArray);
    }


    @Override
    protected void runInference2(int tentative) {

        Tensor t1 = tflite.getOutputTensor(0);

        int[] dims = t1.shape();

        for ( int dim : dims ){
            System.out.println("Dimension " + dim + getModelPath());
        }

        long startModel = SystemClock.uptimeMillis();


        int[] timings2 = new int[] {tentative,224,224,3};

        tflite.resizeInput(0,timings2);

        long endModel = SystemClock.uptimeMillis();

        System.out.println("Resize time " + (endModel-startModel));

        if (labelProbArray == null)
            labelProbArray = new byte[2][getNumLabels()];




        tflite.run(imgData, labelProbArray);
    }

}
