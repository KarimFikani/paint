package com.example.karimfikani.paint;

import android.graphics.Path;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by karimfikani on 6/25/15.
 */
public class DrawingPath extends Path implements Serializable {

    private ArrayList<PointFloat> pathPoints;
    private int color = 0;

    public DrawingPath() {
        super();
        pathPoints = new ArrayList<PointFloat>();
        color = 0;
    }

    public DrawingPath(DrawingPath p) {
        super(p);
        pathPoints = p.pathPoints;
        color = p.color;
    }

    public void addPathPoints(float pointX, float pointY, int color) {
        this.pathPoints.add(new PointFloat(pointX, pointY));
        this.color = color;
    }

    public void clear() {
        pathPoints.clear();
    }

    public ArrayList<PointFloat> getPathPoints() {
        return pathPoints;
    }
    public int getColor() { return color; }
    public void setColor(int color) { this.color = color; }
}
