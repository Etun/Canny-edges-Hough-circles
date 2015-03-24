package com.example1.edge1;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

public class CameraActivity extends Activity implements CvCameraViewListener2 {

	private static final String TAG = "OCVSample::Activity";

	public static final int VIEW_MODE_RGBA = 0;
	public static final int VIEW_MODE_CANNY = 2;
	public static final int VIEW_MODE_HOUGH = 3;

	private CameraBridgeViewBase mOpenCvCameraView;

	private Mat mIntermediateMat;

	public static int viewMode = VIEW_MODE_RGBA;

	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				Log.i(TAG, "OpenCV loaded successfully");
				mOpenCvCameraView.enableView();
			}
				break;
			default: {
				super.onManagerConnected(status);
			}
				break;
			}
		}
	};

	public CameraActivity() {
		Log.i(TAG, "Instantiated new " + this.getClass());
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "called onCreate");
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.camera_layout);

		mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.camera_view);
		mOpenCvCameraView.setCvCameraViewListener(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	@Override
	public void onResume() {
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this,
				mLoaderCallback);
	}

	public void onDestroy() {
		super.onDestroy();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	public void onCameraViewStarted(int width, int height) {
		mIntermediateMat = new Mat();

	}

	public void onCameraViewStopped() {
		if (mIntermediateMat != null)
			mIntermediateMat.release();

		mIntermediateMat = null;
	}

	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		Mat rgba = inputFrame.rgba();
		Size sizeRgba = rgba.size();

		Mat rgbaInnerWindow;

		int rows = (int) sizeRgba.height;
		int cols = (int) sizeRgba.width;

		int left = cols / 8;
		int top = rows / 8;

		int width = cols * 3 / 4;
		int height = rows * 3 / 4;

		switch (CameraActivity.viewMode) {

		case CameraActivity.VIEW_MODE_RGBA:
			break;
		case CameraActivity.VIEW_MODE_CANNY:
			rgbaInnerWindow = rgba
					.submat(top, top + height, left, left + width);
			Imgproc.Canny(rgbaInnerWindow, mIntermediateMat, 80, 90);
			Imgproc.cvtColor(mIntermediateMat, rgbaInnerWindow,
					Imgproc.COLOR_GRAY2BGRA, 4);
			rgbaInnerWindow.release();
			break;

		case CameraActivity.VIEW_MODE_HOUGH:
			rgbaInnerWindow = rgba
					.submat(top, top + height, left, left + width);
			Imgproc.cvtColor(rgbaInnerWindow, rgbaInnerWindow,
					Imgproc.COLOR_RGB2GRAY);
			Mat circles = rgbaInnerWindow.clone();
			rgbaInnerWindow = rgba
					.submat(top, top + height, left, left + width);
			Imgproc.GaussianBlur(rgbaInnerWindow, rgbaInnerWindow, new Size(5,
					5), 2, 2);
			Imgproc.Canny(rgbaInnerWindow, mIntermediateMat, 10, 90);
			Imgproc.HoughCircles(mIntermediateMat, circles,
					Imgproc.CV_HOUGH_GRADIENT, 1, 75, 50, 13, 35, 40);
			Imgproc.cvtColor(mIntermediateMat, rgbaInnerWindow,
					Imgproc.COLOR_GRAY2BGRA, 4);

			for (int x = 0; x < circles.cols(); x++) {
				double vCircle[] = circles.get(0, x);
				if (vCircle == null)
					break;
				Point pt = new Point(Math.round(vCircle[0]),
						Math.round(vCircle[1]));
				int radius = (int) Math.round(vCircle[2]);
				Log.d("cv", pt + " radius " + radius);
				Core.circle(rgbaInnerWindow, pt, 3, new Scalar(0, 0, 255), 5);
				Core.circle(rgbaInnerWindow, pt, radius, new Scalar(255, 0, 0),
						5);
			}
			rgbaInnerWindow.release();
			break;

		}

		return rgba;
	}

	public void Canny(View view) {
		if (viewMode != VIEW_MODE_CANNY) {
			viewMode = VIEW_MODE_CANNY;
		} else {
			viewMode = VIEW_MODE_RGBA;
		}
	}

	public void Hough(View view) {
		if (viewMode != VIEW_MODE_HOUGH) {
			viewMode = VIEW_MODE_HOUGH;
		} else {
			viewMode = VIEW_MODE_RGBA;
		}
	}

}
