package com.example.rollingball;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

  private SurfaceView surfaceView;
  private Ball ball;
  private int speedMultiplier = 3;
  private SensorManager sensorManager;
  private Sensor accelerometer;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    surfaceView = findViewById(R.id.surfaceView);
    ball = new Ball(speedMultiplier);
    try{
      sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
      accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

      sensorManager.registerListener(
          sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_GAME);
      surfaceView.getHolder().addCallback(surfaceCallback);
    } catch (NullPointerException e) {
      Log.getStackTraceString(e);
      Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
    }
  }

  private final SensorEventListener sensorEventListener =
      new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
          float x = event.values[0];
          float y = event.values[1];

          ball.updatePosition(x, y);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
      };

  private final SurfaceHolder.Callback surfaceCallback =
      new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
          new DrawThread(surfaceView.getHolder()).start();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
          sensorManager.unregisterListener(sensorEventListener);
        }
      };

  private class Ball {

    private static final float RADIUS = 100f;
    private float x, y;
    private Paint paint;
    private int speedMultiplier;

    Ball(int speedMultiplier) {
      x = surfaceView.getWidth() / 2f;
      y = surfaceView.getHeight() / 2f;

      this.speedMultiplier = speedMultiplier;

      paint = new Paint();
      paint.setColor(Color.RED);
    }

    void updatePosition(float deltaX, float deltaY) {
      x -= deltaX * speedMultiplier;
      y += deltaY * speedMultiplier;

      if (x < RADIUS) {
        x = RADIUS;
      } else if (x > surfaceView.getWidth() - RADIUS) {
        x = surfaceView.getWidth() - RADIUS;
      }

      if (y < RADIUS) {
        y = RADIUS;
      } else if (y > surfaceView.getHeight() - RADIUS) {
        y = surfaceView.getHeight() - RADIUS;
      }
    }

    void draw(Canvas canvas) {
      canvas.drawCircle(x, y, RADIUS, paint);
    }
  }

  private class DrawThread extends Thread {

    private final SurfaceHolder surfaceHolder;

    DrawThread(SurfaceHolder holder) {
      this.surfaceHolder = holder;
    }

    @Override
    public void run() {
      while (!Thread.interrupted()) {
        Canvas canvas = surfaceHolder.lockCanvas();
        if (canvas != null) {
          draw(canvas);
          surfaceHolder.unlockCanvasAndPost(canvas);
        }
      }
    }

    private void draw(Canvas canvas) {
      canvas.drawColor(Color.DKGRAY);

      ball.draw(canvas);
    }
  }
}
