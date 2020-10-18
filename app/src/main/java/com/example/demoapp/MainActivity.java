package com.example.demoapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.acos;
import static java.lang.Math.asin;
import static java.lang.Math.cos;
import static java.lang.Math.signum;
import static java.lang.Math.sin;

public class MainActivity extends AppCompatActivity {

    private TextView xAngleView;
    private TextView yAngleView;
    private TextView zAngleView;

    private SensorManager sensorManager;
    private Sensor sensor;
    private SensorEventListener sensorEventListener;

    private ConstraintLayout drawingPlace;

    private Point3D[] point3Ds = new Point3D[8];
    private Point2D[] point2Ds = new Point2D[8];
    private Angles angles;

    private final int w = 1080;
    private final int h = 1920;

    private final int dx0 = w / 2;
    private final int dy0 = h / 2;
    private final int size = 400;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_main);

        this.xAngleView = findViewById(R.id.xAngle);
        this.yAngleView = findViewById(R.id.yAngle);
        this.zAngleView = findViewById(R.id.zAngle);

        this.sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        this.sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        this.sensorEventListener = createSensorEventListener();

        this.drawingPlace = findViewById(R.id.canvas);

        this.initPoints();
        this.initAngles();

        compute3D(point3Ds);
        drawImage();
    }

    private void initAngles() {
        this.angles = new Angles(
                Math.toRadians(0f),
                Math.toRadians(120),
                Math.toRadians(270f)
        );
    }

    private void initPoints() {
        int offset = size / 2;
        this.point3Ds[0] = new Point3D( - offset,  - offset, -offset);
        this.point3Ds[1] = new Point3D( - offset,  + offset, -offset);
        this.point3Ds[2] = new Point3D( + offset,  - offset, -offset);
        this.point3Ds[3] = new Point3D( + offset,  + offset, -offset);

        this.point3Ds[4] = new Point3D( - offset,  - offset, offset);
        this.point3Ds[5] = new Point3D( - offset,  + offset, offset);
        this.point3Ds[6] = new Point3D( + offset,  - offset, offset);
        this.point3Ds[7] = new Point3D( + offset,  + offset, offset);
    }


    // Перевод точки 3д в точку 2д
    private void compute3D(Point3D[] newImage) {
        int i = 0;
        for (Point3D point3D : newImage) {
            float x = point3D.x;
            float y = point3D.y;
            point2Ds[i++] = new Point2D(x, y);
        }
    }

    private Point3D[] rotateImage(Point3D[] point3Ds, float[] angles) {
        Point3D[] result = new Point3D[point3Ds.length];
        for (int i = 0; i < result.length; i++) {
            Point3D p = point3Ds[i];
            result[i] = rotateByZ(rotateByY(rotateByX(p, angles[0]), angles[1]), angles[2]);
        }
        return result;
    }

    private Point3D rotateByX(Point3D point3D, float angle) {
        double[][] matrix = {
                {1, 0, 0},
                {0, cos(angle), -sin(angle)},
                {0, sin(angle), cos(angle)}
        };
        return rotateBy(point3D, matrix);
    }

    private Point3D rotateByY(Point3D point3D, float angle) {
        double[][] matrix = {
                {cos(angle), 0, sin(angle)},
                {0, 1, 0},
                {-sin(angle), 0, cos(angle)}
        };
        return rotateBy(point3D, matrix);
    }

    private Point3D rotateByZ(Point3D point3D, float angle) {
        double[][] matrix = {
                {cos(angle), -sin(angle), 0},
                {sin(angle), cos(angle), 0},
                {0, 0, 1}
        };
        return rotateBy(point3D, matrix);
    }

    private Point3D rotateBy(Point3D point3D, double[][] matrix) {
        float[] vector = {point3D.x, point3D.y, point3D.z};
        float[] result = new float[3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                result[i] += matrix[j][i] * vector[j];
            }
        }
        return new Point3D(result[0], result[1], result[2]);
    }

    private SensorEventListener createSensorEventListener() {
        return new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                xAngleView.setText(String.format("α: %.3f", event.values[0]));
                yAngleView.setText(String.format("β: %.3f", event.values[1]));
                zAngleView.setText(String.format("γ: %.3f", event.values[2]));

                float ax = event.values[0];
                float ay = event.values[1];
                float az = event.values[2];

                Point3D[] rotationZ = rotateImage(point3Ds, event.values);
                compute3D(rotationZ);
                drawImage();
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) { }
        };
    }

    private void drawImage() {
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(5);
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint yPaint = new Paint();
        yPaint.setColor(Color.RED);
        yPaint.setStrokeWidth(5);
        canvas.drawLine(dx0, dy0, dx0, 0, yPaint);
        canvas.drawLine(dx0 - 20, 20, dx0, 0, yPaint);
        canvas.drawLine(dx0 + 20, 20, dx0, 0, yPaint);

        canvas.drawLine(point2Ds[0].x + dx0, point2Ds[0].y + dy0, point2Ds[1].x + dx0, point2Ds[1].y + dy0, paint);
        canvas.drawLine(point2Ds[0].x + dx0, point2Ds[0].y + dy0, point2Ds[2].x + dx0, point2Ds[2].y + dy0, paint);
        canvas.drawLine(point2Ds[1].x + dx0, point2Ds[1].y + dy0, point2Ds[3].x + dx0, point2Ds[3].y + dy0, paint);
        canvas.drawLine(point2Ds[2].x + dx0, point2Ds[2].y + dy0, point2Ds[3].x + dx0, point2Ds[3].y + dy0, paint);
        // up square
        canvas.drawLine(point2Ds[4].x + dx0, point2Ds[4].y + dy0, point2Ds[5].x + dx0, point2Ds[5].y + dy0, paint);
        canvas.drawLine(point2Ds[4].x + dx0, point2Ds[4].y + dy0, point2Ds[6].x + dx0, point2Ds[6].y + dy0, paint);
        canvas.drawLine(point2Ds[5].x + dx0, point2Ds[5].y + dy0, point2Ds[7].x + dx0, point2Ds[7].y + dy0, paint);
        canvas.drawLine(point2Ds[6].x + dx0, point2Ds[6].y + dy0, point2Ds[7].x + dx0, point2Ds[7].y + dy0, paint);
        // connect up/down points
        canvas.drawLine(point2Ds[0].x + dx0, point2Ds[0].y + dy0, point2Ds[4].x + dx0, point2Ds[4].y + dy0, paint);
        canvas.drawLine(point2Ds[1].x + dx0, point2Ds[1].y + dy0, point2Ds[5].x + dx0, point2Ds[5].y + dy0, paint);
        canvas.drawLine(point2Ds[2].x + dx0, point2Ds[2].y + dy0, point2Ds[6].x + dx0, point2Ds[6].y + dy0, paint);
        canvas.drawLine(point2Ds[3].x + dx0, point2Ds[3].y + dy0, point2Ds[7].x + dx0, point2Ds[7].y + dy0, paint);

        Paint xPaint = new Paint();
        xPaint.setColor(Color.BLUE);
        xPaint.setStrokeWidth(5);
        canvas.drawLine(dx0, dy0, w, dy0, xPaint);
        canvas.drawLine(w - 20, dy0 - 20, w, dy0, xPaint);
        canvas.drawLine(w - 20, dy0 + 20, w, dy0, xPaint);

        drawingPlace.setBackgroundDrawable(new BitmapDrawable(bitmap));
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.sensorManager.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.sensorManager.unregisterListener(sensorEventListener);
    }

}