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
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;


public class ImageProcessor extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    CameraBridgeViewBase cameraBridgeViewBase;
    BaseLoaderCallback blobLoaderCallback;
    Mat preview, mat2, mat3, grey, blurred, canny, warped, thresh, mask, black;

    List<MatOfPoint> contours = new ArrayList<>();
    List<MatOfPoint> bubbles = new ArrayList<>();
    List<Tuple<MatOfPoint, Rect>> questions = new ArrayList<>();
    Mat hierarchy, hierarchy2;
    MatOfPoint2f approxCurve;
    MatOfPoint2f docCnt;

    String key;

    HashMap<Integer, Integer> answers = new HashMap();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_processor);
        setTitle(R.string.scan);

        Intent intent = getIntent();

        key = intent.getStringExtra(MainActivity.EXTRA_KEY);

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
        preview = inputFrame.rgba();

        int width  = preview.width();
        int height = preview.height();

        //Log.d("rozmiar", preview.size().toString());

        /*if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            Core.transpose(mat1, mat2);

            float scale = (float) mat1.rows() / (float) mat1.cols();
            Imgproc.resize(mat2, mat3, new Size(), scale, scale, 0);

            int offset_x = (mat3.cols() - mat1.cols()) / 2;
            Core.flip(mat3.colRange(offset_x, offset_x + mat1.cols()).rowRange(0, mat1.rows()), mat1, 1);*/

        Imgproc.cvtColor(preview, grey, Imgproc.COLOR_RGB2GRAY);
        //Imgproc.GaussianBlur(grey, blurred, new Size(5, 5), 0);
        Imgproc.medianBlur(grey, blurred, 9);
        Imgproc.Canny(blurred, canny, 75, 200);

        Imgproc.dilate(canny, canny, new Mat(), new Point(-1, -1));

        // Imgproc.cvtColor(canny, preview, Imgproc.COLOR_GRAY2RGBA);

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
                    //Imgproc.drawContours(preview, Arrays.asList(new MatOfPoint(docCnt.toArray())), 0, new Scalar(255, 0, 0, 255), 5);

                    // order points

                    List<Double> sum  = new ArrayList<>();
                    List<Double> diff = new ArrayList<>();
                    List<Point> rect = Arrays.asList(docCnt.toArray());
                    // List<Point> clockwise = new ArrayList<>();

                    for (int i = 0; i < 4; i++) {
                        double x = rect.get(i).x;
                        double y = rect.get(i).y;

                        sum.add(x + y);
                        diff.add(x - y);
                    }

                    int minSumIndex  = 0;
                    int maxSumIndex  = 0;
                    int minDiffIndex = 0;
                    int maxDiffIndex = 0;

                    for (int i = 0; i < 4; i++) {
                        if (sum.get(minSumIndex) > sum.get(i)) {
                            minSumIndex = i;
                        }

                        if (sum.get(maxSumIndex) < sum.get(i)) {
                            maxSumIndex = i;
                        }

                        if (diff.get(minDiffIndex) > diff.get(i)) {
                            minDiffIndex = i;
                        }

                        if (diff.get(maxDiffIndex) < diff.get(i)) {
                            maxDiffIndex = i;
                        }
                    }

                    Point tl = rect.get(minSumIndex);
                    Point tr = rect.get(maxDiffIndex);
                    Point br = rect.get(maxSumIndex);
                    Point bl = rect.get(minDiffIndex);

                    //Imgproc.circle(preview, tl, 10, new Scalar(255,   0,   0, 255));
                    //Imgproc.circle(preview, tr, 10, new Scalar(255, 255,   0, 255));
                    //Imgproc.circle(preview, br, 10, new Scalar(  0, 255,   0, 255));
                    //Imgproc.circle(preview, bl, 10, new Scalar(  0,   0, 255, 255));

                    double widthA = Math.sqrt(Math.pow(tr.x - tl.x, 2) + Math.pow(tr.y - tl.y, 2));
                    double widthB = Math.sqrt(Math.pow(br.x - bl.x, 2) + Math.pow(br.y - bl.y, 2));
                    double maxWidth = Math.max(widthA, widthB);

                    // wypróbować średnią arytmetyczną ^^^

                    double heightA = Math.sqrt(Math.pow(tl.x - bl.x, 2) + Math.pow(tl.y - bl.y, 2));
                    double heightB = Math.sqrt(Math.pow(tr.x - br.x, 2) + Math.pow(tr.y - br.y, 2));
                    double maxHeight = Math.max(heightA, heightB);

                    Log.d("ratioxxx", String.format("%.2f", maxHeight / maxWidth));

                    MatOfPoint2f src = new MatOfPoint2f(tl, tr, br, bl);
                    MatOfPoint2f dst = new MatOfPoint2f(
                            new Point(0, 0),
                            new Point(maxWidth - 1, 0),
                            new Point(maxWidth - 1, maxHeight - 1),
                            new Point(0, maxHeight - 1)
                    );

                    Mat M = Imgproc.getPerspectiveTransform(src, dst);
                    Imgproc.warpPerspective(grey, warped, M, new Size(maxWidth, maxHeight));
                    Imgproc.threshold(warped, thresh, 0, 255, Imgproc.THRESH_BINARY_INV | Imgproc.THRESH_OTSU);
                    Imgproc.warpPerspective(preview, warped, M, new Size(maxWidth, maxHeight));

                    double ratio = (double)thresh.width() / (double)thresh.height();

                    // operowanie na thresh
                    Imgproc.findContours(thresh, bubbles, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

                    for (MatOfPoint b : bubbles) {
                        Rect box = Imgproc.boundingRect(b);

                        double ar = (double)box.width / (double)box.height;

                        if (box.width >= 10 && box.height >= 10 && ar >= 0.8 && ar <= 1.2) {
                            questions.add(new Tuple(b, box));
                        }
                    }

                    Collections.sort(questions, new Comparator<Tuple<MatOfPoint, Rect>>() {
                        @Override
                        public int compare(Tuple<MatOfPoint, Rect >q1, Tuple<MatOfPoint, Rect> q2) {
                            return Integer.valueOf(q1.second.y).compareTo(q2.second.y);
                        }
                    });

                    List<Scalar> colors = new ArrayList<Scalar>();

                    colors.add(new Scalar(255, 0, 0, 255));
                    colors.add(new Scalar(255, 255, 0, 255));
                    colors.add(new Scalar(0, 255, 0, 255));
                    colors.add(new Scalar(0, 0, 255, 255));
                    colors.add(new Scalar(255, 0, 255, 255));

                    if (questions.size() == 25) {
                        for (int i = 0; i < 5; i++) {
                            List<Tuple<MatOfPoint, Rect>> line = questions.subList(5 * i, 5 * i + 5);

                            Collections.sort(line, new Comparator<Tuple<MatOfPoint, Rect>>() {
                                @Override
                                public int compare(Tuple<MatOfPoint, Rect> a1, Tuple<MatOfPoint, Rect> a2) {
                                    return Integer.valueOf(a1.second.x).compareTo(a2.second.x);
                                }
                            });

                            int max_total = 0;
                            int total = 0;
                            int user_answer = 0;


                            //Core.countNonZero()

                            for (int j = 0; j < 5; j++) {
                                //mask.setTo(new Scalar(0));
                                Mat mask = new Mat(thresh.rows(), thresh.cols(), CvType.CV_8U, Scalar.all(0));

                                Imgproc.drawContours(mask, Arrays.asList(new MatOfPoint(line.get(j).first.toArray())), 0, new Scalar(1), -1);
                                Core.bitwise_and(thresh, thresh, mask, mask);
                                total = Core.countNonZero(mask);

                                if (total > max_total) {
                                    max_total = total;
                                    user_answer = j;
                                }
                            }

                            boolean correct = answers.get(i).equals(user_answer);

                            Imgproc.drawContours( warped, Arrays.asList(new MatOfPoint(line.get(answers.get(i).intValue()).first.toArray())), 0, new Scalar(0, 255, 0, 255), 2);

                            if (!correct) {
                                Imgproc.drawContours( warped, Arrays.asList(new MatOfPoint(line.get(user_answer).first.toArray())), 0, new Scalar(255, 0, 0, 255), 2);
                            }

                        }
                    }

                    //for (int i = 0; i < questions.size(); i++) {
                    //    Imgproc.drawContours(warped, Arrays.asList(new MatOfPoint(questions.get(i).first.toArray())), 0, colors.get(i % 25 / 5), 2);
                    //}

                    // rysowanie na warped
                    // https://stackoverflow.com/questions/29184697/opencv-android-copy-part-of-an-image-to-new-mat

                    Imgproc.resize(warped, preview, preview.size());

                    //black.setTo(new Scalar(128));
                    //Mat resized = new Mat(100, 100, CvType.CV_8U);
                    //Imgproc.resize(thresh, resized, new Size(100, 100));
                    //resized.copyTo(black.rowRange(0, resized.height()).colRange(0, resized.width()));
                    //resized.release();
                    //Imgproc.cvtColor(black, preview, Imgproc.COLOR_GRAY2RGB);
                    //preview.setTo(new Scalar(0, 255, 0, 255));
                    //Imgproc.resize(thresh, thresh, new Size(ratio * height - 1, height - 1));
                    //Imgproc.cvtColor(thresh, thresh, preview.type());
                    //thresh.copyTo(preview.rowRange(0, thresh.height()).colRange(0, thresh.width()));

                    break;
                }
            }
        }

        contours.clear();
        bubbles.clear();
        questions.clear();
        //}

        return preview;
    }

    @Override
    public void onCameraViewStopped() {
        preview.release();
        mat2.release();
        mat3.release();

        grey.release();
        blurred.release();
        canny.release();

        hierarchy.release();
        hierarchy2.release();

        approxCurve.release();
        docCnt.release();

        warped.release();

        thresh.release();
        mask.release();
        black.release();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        preview = new Mat(width, height, CvType.CV_8UC4);
        mat2 = new Mat(width, height, CvType.CV_8UC4);
        mat3 = new Mat(width, height, CvType.CV_8UC4);

        thresh = new Mat(width, height, CvType.CV_8U);
        mask = new Mat(width, height, CvType.CV_8U);
        black = new Mat(width, height, CvType.CV_8U);

        grey = new Mat(width, height, CvType.CV_8UC4);
        blurred = new Mat(width, height, CvType.CV_8UC4);
        canny = new Mat(width, height, CvType.CV_8UC4);

        hierarchy = new Mat();
        hierarchy2 = new Mat();
        approxCurve = new MatOfPoint2f();
        docCnt = new MatOfPoint2f();

        warped = new Mat(width, height, CvType.CV_8UC4);

        answers.put(0, 1);
        answers.put(1, 4);
        answers.put(2, 0);
        answers.put(3, 4);
        answers.put(4, 1);
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
