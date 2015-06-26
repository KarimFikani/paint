package com.example.karimfikani.paint;

import java.io.Serializable;

/**
 * Created by karimfikani on 6/25/15.
 */
public class PointFloat implements Serializable {

    public float x;
    public float y;

    public PointFloat(float x, float y) {
        this.x = x;
        this.y = y;
    }
}
