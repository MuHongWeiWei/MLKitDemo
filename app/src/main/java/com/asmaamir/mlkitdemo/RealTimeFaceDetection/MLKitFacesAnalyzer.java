package com.asmaamir.mlkitdemo.RealTimeFaceDetection;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.util.Log;
import android.view.TextureView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.asmaamir.mlkitdemo.ToastUtils;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.common.FirebaseVisionPoint;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceContour;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

import java.util.ArrayList;
import java.util.List;

import static com.google.firebase.ml.vision.face.FirebaseVisionFaceContour.*;

public class MLKitFacesAnalyzer implements ImageAnalysis.Analyzer {
    private static final String TAG = "MLKitFacesAnalyzer";
    private FirebaseVisionFaceDetector faceDetector;
    private TextureView tv;
    private ImageView iv;
    private Bitmap bitmap;
    private Canvas canvas;
    private Paint dotPaint, linePaint;
    private float widthScaleFactor = 1.0f;
    private float heightScaleFactor = 1.0f;
    private FirebaseVisionImage fbImage;
    //搖頭
    float rightFace = 0;
    float leftFace = 0;
    float nose = 0;
    float turnRight = 0;
    float turnLeft = 0;
    //張嘴
    float upBottomLip = 0;
    float LowTopLip = 0;
    //微笑
    float upTopLipLeft = 0;
    float upTopLipRight = 0;
    //點頭
    float midFace = 0;
    float topFace = 0;
    float bottomFace = 0;
    float turnTop = 0;
    float turnBottom = 0;
    private CameraX.LensFacing lens;
    List<Float> myList = new ArrayList<>();

    MLKitFacesAnalyzer(TextureView tv, ImageView iv, CameraX.LensFacing lens) {
        this.tv = tv;
        this.iv = iv;
        this.lens = lens;
    }

    @Override
    public void analyze(ImageProxy image, int rotationDegrees) {
        if (image == null || image.getImage() == null) {
            return;
        }

        int rotation = degreesToFirebaseRotation(rotationDegrees);

        fbImage = FirebaseVisionImage.fromMediaImage(image.getImage(), rotation);
        initDrawingUtils();

        initDetector();
        detectFaces();
    }

    private void initDrawingUtils() {
        bitmap = Bitmap.createBitmap(tv.getWidth(), tv.getHeight(), Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        dotPaint = new Paint();
        dotPaint.setColor(Color.YELLOW);
        dotPaint.setStyle(Paint.Style.FILL);
        dotPaint.setStrokeWidth(4f);
        dotPaint.setAntiAlias(true);
        linePaint = new Paint();
        linePaint.setColor(Color.BLUE);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(4f);
        widthScaleFactor = canvas.getWidth() / (fbImage.getBitmap().getWidth() * 1.0f);
        heightScaleFactor = canvas.getHeight() / (fbImage.getBitmap().getHeight() * 1.0f);
    }

    private void initDetector() {
        FirebaseVisionFaceDetectorOptions detectorOptions = new FirebaseVisionFaceDetectorOptions.Builder()
                .setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS)
                .build();


        faceDetector = FirebaseVision
                .getInstance()
                .getVisionFaceDetector(detectorOptions);
    }

    private void detectFaces() {
        faceDetector.detectInImage(fbImage)
                .addOnSuccessListener(firebaseVisionFaces -> {
                    if (!firebaseVisionFaces.isEmpty()) {
                        processFaces(firebaseVisionFaces);
                    } else {
                        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.MULTIPLY);
                    }
                }).addOnFailureListener(e -> Log.i(TAG, e.toString()));
    }


    private void processFaces(List<FirebaseVisionFace> faces) {
        for (FirebaseVisionFace face : faces) {
            drawContours(face.getContour(FACE).getPoints(), FACE);
            drawContours(face.getContour(NOSE_BRIDGE).getPoints(), NOSE_BRIDGE);
            drawContours(face.getContour(LOWER_LIP_TOP).getPoints(), LOWER_LIP_TOP);
            drawContours(face.getContour(UPPER_LIP_TOP).getPoints(), UPPER_LIP_TOP);
            drawContours(face.getContour(UPPER_LIP_BOTTOM).getPoints(), UPPER_LIP_BOTTOM);
//            drawContours(face.getContour(LOWER_LIP_BOTTOM).getPoints(), 0);
//            drawContours(face.getContour(LEFT_EYEBROW_BOTTOM).getPoints(), 0);
//            drawContours(face.getContour(RIGHT_EYEBROW_BOTTOM).getPoints(), 0);
//            drawContours(face.getContour(LEFT_EYE).getPoints(), 0);
//            drawContours(face.getContour(RIGHT_EYE).getPoints(), 0);
//            drawContours(face.getContour(LEFT_EYEBROW_TOP).getPoints(), 0);
//            drawContours(face.getContour(RIGHT_EYEBROW_TOP).getPoints(), 0);
//            drawContours(face.getContour(UPPER_LIP_TOP).getPoints(), 0);
//            drawContours(face.getContour(NOSE_BOTTOM).getPoints(), 0);
        }
        iv.setImageBitmap(bitmap);
    }

    private void drawContours(List<FirebaseVisionPoint> points, int face) {
        int counter = 0;

        //搖頭
        if (face == FACE) {
            rightFace = points.get(27).getX();
            leftFace = points.get(9).getX();
        }

        if (face == NOSE_BRIDGE) {
            nose = points.get(1).getX();
        }

        if ((nose - rightFace) / (leftFace - nose) > 5) {
            ++turnLeft;
        } else if ((leftFace - nose) / (nose - rightFace) > 5) {
            ++turnRight;
        }
        if (turnRight > 1 && turnLeft > 1) {
            ToastUtils.showShort("搖頭");
            turnRight = 0;
            turnLeft = 0;
        }

        //點頭
        if (face == FACE) {
            midFace = points.get(9).getY();
            bottomFace = points.get(18).getY();
            topFace = points.get(0).getY();
            Paint paint = new Paint();
            paint.setColor(Color.BLUE);
            paint.setTextSize(50f);
        }

        if ((midFace - topFace) / (bottomFace - midFace) > 1.23) {
            ++turnTop;
        } else if ((midFace - topFace) / (bottomFace - midFace) < 0.88) {
            ++turnBottom;
        }
        if (turnTop > 1 && turnBottom > 1) {
            ToastUtils.showShort("點頭");
            turnTop = 0;
            turnBottom = 0;
        }

        //張嘴
//        if (face == UPPER_LIP_BOTTOM) {
//            upBottomLip = points.get(4).getY();
//        }
//        if (face == LOWER_LIP_TOP) {
//            LowTopLip = points.get(4).getY();
//        }
//        if (LowTopLip - upBottomLip > 15) {
//            myList.add(LowTopLip - upBottomLip);
//        } else {
//            myList.clear();
//        }
//        if (myList.size() > 10) {
//            ToastUtils.showShort("張嘴");
//            myList.clear();
//        }

        //微笑
        if (face == UPPER_LIP_TOP) {
            upTopLipLeft = points.get(0).getY();
            upTopLipRight = points.get(10).getY();
        }

        if (face == NOSE_BRIDGE) {
            nose = points.get(1).getY();
        }

        if (upTopLipLeft - nose < 30 && upTopLipRight - nose < 30) {
            myList.add(upTopLipLeft - nose);
        } else {
            myList.clear();
        }

        if (myList.size() > 30) {
            ToastUtils.showShort("微笑");
            myList.clear();
        }

        for (FirebaseVisionPoint point : points) {
            if (counter != points.size() - 1) {
                canvas.drawLine(translateX(point.getX()),
                        translateY(point.getY()),
                        translateX(points.get(counter + 1).getX()),
                        translateY(points.get(counter + 1).getY()),
                        linePaint);


            } else {
                canvas.drawLine(translateX(point.getX()),
                        translateY(point.getY()),
                        translateX(points.get(0).getX()),
                        translateY(points.get(0).getY()),
                        linePaint);
            }
            counter++;
            canvas.drawCircle(translateX(point.getX()), translateY(point.getY()), 6, dotPaint);
        }
    }

    private float translateY(float y) {
        return y * heightScaleFactor;
    }

    private float translateX(float x) {
        float scaledX = x * widthScaleFactor;
        if (lens == CameraX.LensFacing.FRONT) {
            return canvas.getWidth() - scaledX;
        } else {
            return scaledX;
        }
    }

    private int degreesToFirebaseRotation(int degrees) {
        switch (degrees) {
            case 0:
                return FirebaseVisionImageMetadata.ROTATION_0;
            case 90:
                return FirebaseVisionImageMetadata.ROTATION_90;
            case 180:
                return FirebaseVisionImageMetadata.ROTATION_180;
            case 270:
                return FirebaseVisionImageMetadata.ROTATION_270;
            default:
                throw new IllegalArgumentException("Rotation must be 0, 90, 180, or 270.");
        }
    }
}