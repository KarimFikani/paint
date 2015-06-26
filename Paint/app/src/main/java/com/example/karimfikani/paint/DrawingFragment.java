package com.example.karimfikani.paint;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A placeholder fragment containing a simple view.
 */
public class DrawingFragment extends Fragment {

    private DrawingView drawingView;

    public DrawingFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_drawing, container, false);
        drawingView = (DrawingView) view.findViewById(R.id.drawing);

        return view;
    }

    public void clearDrawing() {
        drawingView.clearDrawing();
    }

    public void setColor(int color) {
        drawingView.setColor(color);
    }

    public void undo() {
        drawingView.undo();
    }

    public void redo() {
        drawingView.redo();
    }

    public void rotate(float degree) {
        drawingView.rotate(degree);
    }
}
