package com.example.android.opencvproject;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.InstallCallbackInterface;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    CameraBridgeViewBase cameraBridgeViewBase;
    BaseLoaderCallback blobLoaderCallback;
    Mat mat1, mat2, mat3, grey, blurred, canny;
    List<MatOfPoint> contours;
    Mat hierarchy;
    MatOfPoint2f approxCurve;
    MatOfPoint2f docCnt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraBridgeViewBase = (JavaCameraView) findViewById(R.id.my_camera_view);
        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        cameraBridgeViewBase.setCvCameraViewListener(this);

        blobLoaderCallback = new BaseLoaderCallback(this) {
            @Override
            public void onManagerConnected(int status) {
                switch (status) {
                    case BaseLoaderCallback.SUCCESS:
                        cameraBridgeViewBase.enableView();
                        break;
                    default:
                        super.onManagerConnected(status);
                        break;
                }
            }

            @Override
            public void onPackageInstall(int operation, InstallCallbackInterface callback) {
                super.onPackageInstall(operation, callback);
            }
        };
    }

    @Override
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        mat1 = inputFrame.rgba();

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            Core.transpose(mat1, mat2);

            float scale = (float) mat1.rows() / (float) mat1.cols();
            Imgproc.resize(mat2, mat3, new Size(), scale, scale, 0);

            int offset_x = (mat3.cols() - mat1.cols()) / 2;
            Core.flip(mat3.colRange(offset_x, offset_x + mat1.cols()).rowRange(0, mat1.rows()), mat1, 1);

            Imgproc.cvtColor(mat1, grey, Imgproc.COLOR_RGB2GRAY);

            Imgproc.GaussianBlur(grey, blurred, new Size(5, 5), 0);

            Imgproc.Canny(blurred, canny, 75, 200);

            Imgproc.cvtColor(canny, mat1, Imgproc.COLOR_GRAY2RGBA);

            Imgproc.findContours(canny, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

            if (contours.size() > 0) {
                Collections.sort(contours, new Comparator<MatOfPoint>() {
                    @Override
                    public int compare(MatOfPoint c1, MatOfPoint c2) {
                        return Double.valueOf(Imgproc.contourArea(c1)).compareTo(Imgproc.contourArea(c2));
                    }
                });

                Collections.reverse(contours);

                for (MatOfPoint c : contours) {
                    double peri = Imgproc.arcLength(new MatOfPoint2f(c.toArray()), true);
                    Imgproc.approxPolyDP(new MatOfPoint2f(c.toArray()), approxCurve, 0.02 * peri, true);

                    if (approxCurve.rows() == 4) {
                        approxCurve.copyTo(docCnt);
                        Imgproc.drawContours(mat1, Arrays.asList(new MatOfPoint(docCnt.toArray())), 0, new Scalar(255, 0, 0, 255), 5);
                        break;
                    }
                }
            }

            contours.clear();
        }

        return mat1;
    }

    @Override
    public void onCameraViewStopped() {
        mat1.release();
        mat2.release();
        mat3.release();

        grey.release();
        blurred.release();
        canny.release();

        hierarchy.release();

        approxCurve.release();
        docCnt.release();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mat1 = new Mat(width, height, CvType.CV_8UC4);
        mat2 = new Mat(width, height, CvType.CV_8UC4);
        mat3 = new Mat(width, height, CvType.CV_8UC4);

        grey = new Mat(width, height, CvType.CV_8UC4);
        blurred = new Mat(width, height, CvType.CV_8UC4);
        canny = new Mat(width, height, CvType.CV_8UC4);

        contours = new ArrayList<>();
        hierarchy = new Mat();

        approxCurve = new MatOfPoint2f();
        docCnt = new MatOfPoint2f();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (cameraBridgeViewBase != null) {
            cameraBridgeViewBase.disableView();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!OpenCVLoader.initDebug()) {
            Toast.makeText(getApplicationContext(), "There is a problem in OpenCV", Toast.LENGTH_SHORT).show();
        } else {
            blobLoaderCallback.onManagerConnected(BaseLoaderCallback.SUCCESS);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (cameraBridgeViewBase != null) {
            cameraBridgeViewBase.disableView();
        }
    }
}
