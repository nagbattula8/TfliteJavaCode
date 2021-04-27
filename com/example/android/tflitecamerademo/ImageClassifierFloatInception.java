/* Copyright 2018 The TensorFlow Authors. All Rights Reserved.

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

/** This classifier works with the float MobileNet model. */
public class ImageClassifierFloatInception extends ImageClassifier {

  /** The mobile net requires additional normalization of the used input. */
  private static final float IMAGE_MEAN = 127.5f;

  private static final float IMAGE_STD = 127.5f;

  /**
   * An array to hold inference results, to be feed into Tensorflow Lite as outputs. This isn't part
   * of the super class, because we need a primitive array here.
   */
  private float[][] labelProbArray = null;
  private float[][] labelProbArray2 = null;
  private float[][] labelProbArray3= null;
  private float[][] labelProbArray4 = null;
  private float[][] labelProbArray5 = null;

  /**
   * Initializes an {@code ImageClassifierFloatMobileNet}.
   *
   * @param activity
   */
  ImageClassifierFloatInception(Activity activity) throws IOException {
    super(activity);
    labelProbArray = new float[1][getNumLabels()];
    labelProbArray2 = new float[2][getNumLabels()];
    labelProbArray3 = new float[3][getNumLabels()];
    labelProbArray4 = new float[4][getNumLabels()];
    labelProbArray5 = new float[5][getNumLabels()];
  }

  @Override
  protected String getModelPath() {
    // you can download this file from
    // see build.gradle for where to obtain this file. It should be auto
    // downloaded into assets.
    return "nasnet_mobile.tflite";
  }

  @Override
  protected String getLabelPath() {
    return "labels_nas.txt";
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
    return 4; // Float.SIZE / Byte.SIZE;
  }

  @Override
  protected void addPixelValue(int pixelValue) {
    imgData.putFloat((((pixelValue >> 16) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
    imgData.putFloat((((pixelValue >> 8) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
    imgData.putFloat(((pixelValue & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
  }

  @Override
  protected float getProbability(int labelIndex) {
    return labelProbArray[0][labelIndex];
  }

  @Override
  protected void setProbability(int labelIndex, Number value) {
    labelProbArray[0][labelIndex] = value.floatValue();
  }

  @Override
  protected float getNormalizedProbability(int labelIndex) {
    return labelProbArray[0][labelIndex];
  }

  @Override
  protected void runInference() {
    tflite.run(imgData, labelProbArray);
  }

  @Override
  protected void runInference2(int tentative) {


    Tensor t1 = tflite.getInputTensor(0);

    int[] dims = t1.shape();

    for ( int dim : dims ){
      System.out.println("Dimension " + dim + getModelPath());
    }

    long startModel = SystemClock.uptimeMillis();


    int[] timings2 = new int[] {tentative,224,224,3};

    tflite.resizeInput(0,timings2);

    long endModel = SystemClock.uptimeMillis();

    System.out.println("Resize time " + (endModel-startModel));

    for ( int dim : dims ){
      System.out.println("Dimension " + dim + getModelPath());
    }


    if( tentative == 1 ) {
      tflite.run(imgData, labelProbArray);
    }

    else if( tentative == 2 ) {
      tflite.run(imgData, labelProbArray2);
    }

    else if( tentative == 3 ) {
      tflite.run(imgData, labelProbArray3);
    }

    else if( tentative == 4 ) {
      tflite.run(imgData, labelProbArray4);
    }

    else if( tentative == 5 ) {
      tflite.run(imgData, labelProbArray5);
    }


  }

}
