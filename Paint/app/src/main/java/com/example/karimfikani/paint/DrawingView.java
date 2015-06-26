package com.example.karimfikani.paint;

/**
 * Created by karimfikani on 6/24/15.
 */

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;

public class DrawingView extends SurfaceView {

    final private String saveFilename = "drawing";

    private int paintColor = Color.GREEN;
    private int strokeWidth = 10;
    private Paint drawPaint;
    private ArrayList<DrawingPath> drawingPathStack = new ArrayList();
    private ArrayList<DrawingPath> redoDrawingPathStack = new ArrayList();
    private DrawingPath currentDrawingPath;
    private Boolean toggleColor = false;
    private int screenSizeHalfWidth = 0;
    private int screenSizeHalfHeight = 0;
    private float rotationDegree = 0;

    /**
     *
     * @param context
     * @param attrs
     */
    public DrawingView(Context context, AttributeSet attrs){
        super(context, attrs);

        setupDrawing();
        loadDrawingFromMemory();

        // get the center of the screen so we can rotate the canvas according to that point
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point screenSize = new Point();
        display.getSize(screenSize);
        screenSizeHalfWidth = screenSize.x/2;
        screenSizeHalfHeight = screenSize.y/2;
    }

    /**
     * Every touch made it's drawn and the image is stored on the internal storage.
     *
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float[] pts = new float[2];
        pts[0] = event.getX();
        pts[1] = event.getY();

        // rotate those values if the phone is rotated
        Matrix rotationMat = new Matrix();
        rotationMat.setRotate(-rotationDegree, screenSizeHalfWidth, screenSizeHalfHeight);
        rotationMat.mapPoints(pts);

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            currentDrawingPath = new DrawingPath();
            drawingPathStack.add(currentDrawingPath);
            currentDrawingPath.addPathPoints(pts[0], pts[1], paintColor);
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            currentDrawingPath.addPathPoints(pts[0], pts[1], paintColor);
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            currentDrawingPath.addPathPoints(pts[0], pts[1], paintColor);
            save();
        }

        invalidate();
        return true;
    }

    /**
     *
     */
    public void clearDrawing() {
        // first clear the points in every DrawingPath object
        Iterator it = drawingPathStack.iterator();

        while(it.hasNext()) {
            DrawingPath drawingPath = (DrawingPath) it.next();
            drawingPath.clear();
        }

        drawingPathStack.clear();
        invalidate();

        save();
    }

    /**
     * 
     * @param color
     */
    public void setColor(int color) {
        paintColor = color;

        drawPaint.setColor(paintColor);

        Iterator it = drawingPathStack.iterator();

        while(it.hasNext()) {
            DrawingPath drawingPath = (DrawingPath) it.next();
            drawingPath.setColor(paintColor);
        }

        invalidate();

        save();
    }

    public void undo() {
        int size = drawingPathStack.size() - 1;
        if (size >= 0) {
            redoDrawingPathStack.add(drawingPathStack.remove(size));
            invalidate();
            save();
        }
    }

    public void redo() {
        int size = redoDrawingPathStack.size() - 1;
        if (size >= 0) {
            drawingPathStack.add(redoDrawingPathStack.remove(size));
            invalidate();
            save();
        }
    }

    public void rotate(float degree) {
        rotationDegree = degree;
        invalidate();
    }

    /**
     *
     * @param canvas
     */
    @Override
    public void onDraw(Canvas canvas) {
        canvas.rotate(rotationDegree, screenSizeHalfWidth, screenSizeHalfHeight);

        Iterator it = drawingPathStack.iterator();

        while(it.hasNext()) {
            final DrawingPath drawingPath = (DrawingPath) it.next();
            ArrayList<PointFloat> pathPoints = drawingPath.getPathPoints();

            paintColor = drawingPath.getColor();
            drawPaint.setColor(paintColor);

            Iterator ppIt = pathPoints.iterator();
            Path tmpPath = new Path();
            PointFloat point = null;

            if (ppIt.hasNext()) {
                point = (PointFloat) ppIt.next();
                tmpPath.moveTo(point.x, point.y);
                tmpPath.lineTo(point.x, point.y);
            }

            while(ppIt.hasNext()) {
                point = (PointFloat) ppIt.next();
                tmpPath.lineTo(point.x, point.y);
                canvas.drawPath(tmpPath, drawPaint);
            }
        }
    }

    /**
     *
     */
    private void setupDrawing() {
        drawPaint = new Paint();

        drawPaint.setColor(paintColor);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(strokeWidth);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    /**
     *
     */
    private void loadDrawingFromMemory() {
        try {
            File filePath = DrawingView.this.getContext().getFileStreamPath(saveFilename);
            FileInputStream fis = new FileInputStream(filePath);
            ObjectInputStream ois = new ObjectInputStream(fis);
            drawingPathStack = (ArrayList<DrawingPath>) ois.readObject();
            invalidate();
        } catch (IOException e) {
            Log.e("DrawingView", "Unable to load bitmap from internal storage with errorMsg:" + e.getMessage());
        } catch (ClassNotFoundException e) {
            Log.e("DrawingView", "Class not found with errorMsg:" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * save the bitmap to the internal storage
     */
    private void save() {
        new SaveDrawingsToFile(DrawingView.this.getContext(), drawingPathStack).execute();
    }

    private class SaveDrawingsToFile extends AsyncTask<Intent,Void,Boolean> {
        private Context context;
        private ArrayList<DrawingPath> drawings;

        public SaveDrawingsToFile(Context context, ArrayList<DrawingPath> drawings) {
            this.context = context;
            this.drawings = drawings;
        }

        @Override
        protected Boolean doInBackground(Intent... arg0) {
            try {
                FileOutputStream fos = DrawingView.this.getContext().openFileOutput(saveFilename, Context.MODE_PRIVATE);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(drawingPathStack);
                oos.flush();
                fos.close();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }
    }

}
