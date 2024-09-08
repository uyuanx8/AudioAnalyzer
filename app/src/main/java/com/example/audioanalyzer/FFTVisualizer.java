package com.example.audioanalyzer;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

//fft 데이터 시각화
public class FFTVisualizer extends View {


    private static final int SamplingRate = 44100; // 44.1 kHz
    private double[] list;
    private int lineDistance = 10;
    private Paint barPaint;
    private Paint bgPaint;
    double baseMax = 130;

    public FFTVisualizer(Context context) {
        super(context);
        init(null, 0);
    }

    public FFTVisualizer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public FFTVisualizer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.ChartView, defStyle, 0);

        int bufferSize = AudioRecord.getMinBufferSize(SamplingRate, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT);
        list = new double[bufferSize];

        a.recycle();

        barPaint = new Paint();
        barPaint.setColor(Color.parseColor("#48ffac"));
        barPaint.setStrokeWidth(3f);

        bgPaint = new Paint();
        bgPaint.setColor(Color.parseColor("#484848"));
        bgPaint.setStrokeWidth(3f);

    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // TODO: consider storing these as member variables to reduce
        // allocations per draw cycle.
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();

        int contentWidth = getWidth() - paddingLeft - paddingRight;
        int contentHeight = getHeight() - paddingTop - paddingBottom;

//        canvas.drawRect(new RectF(paddingLeft,paddingTop, contentWidth + paddingLeft, contentHeight + paddingBottom), bgPaint);
        double max = baseMax;
        for(double i : list){
            if(max < i) { max = i; }
            else if (max < i * -1) { max = i * -1; }
        }
//        if(baseMax<max){ baseMax = max; }
        int baselineY = contentHeight / 2;

        for(int i=0;i<max;i+=lineDistance){
            int y = (int)(i*contentHeight/max);
            canvas.drawLine(paddingLeft,paddingTop + baselineY + y,
                    paddingLeft+contentWidth,paddingTop + baselineY+y,bgPaint);
            canvas.drawLine(paddingLeft,paddingTop + baselineY - y,
                    paddingLeft+contentWidth,paddingTop + baselineY-y,bgPaint);
        }

        int listSize = list.length/4;
        for(int i=0;i<listSize - 1;i+=1){
            double vf = list[i];
            int xf = contentWidth * i / listSize;
            int yf = baselineY + (int)(vf * contentHeight / max);
            double vs = list[i+1];
            int xs = contentWidth * i / listSize;
            int ys = baselineY + (int)(vs * contentHeight / max);
//            canvas.drawLine(x, contentHeight, x, 0, barPaint);

            canvas.drawLine(paddingLeft + xf, paddingTop + yf, paddingLeft + xs, paddingTop + ys, barPaint);
        }
//        canvas.drawLine(0,0,contentWidth,contentHeight,barPaint);
//        canvas.drawLine(0,baselineY,0, list[listSize/2] * 120 / max, barPaint);
        Log.d("audioData", max+ " ");
    }

    public double[] getList() {
        return list;
    }

    public void setList(double[] list) {
        this.list = list;
        invalidate();
        requestLayout();
    }
/**
     * Gets the example string attribute value.
     *
     * @return The example string attribute value.
     */
}