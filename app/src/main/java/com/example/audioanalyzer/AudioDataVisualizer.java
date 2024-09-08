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

//기본 오디오 데이터 시각화
public class AudioDataVisualizer extends View {


    private static final int SamplingRate = 44100; // 44.1 kHz
    private short[] list;
    private int baseMax = 16000;
    private int lineDistance = 2000;
    private Paint barPaint;
    private Paint bgPaint;

    public AudioDataVisualizer(Context context) {
        super(context);
        init(null, 0);
    }

    public AudioDataVisualizer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public AudioDataVisualizer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.ChartView, defStyle, 0);

        int bufferSize = AudioRecord.getMinBufferSize(SamplingRate, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT);
        list = new short[bufferSize];

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
        int max = getBaseMax();
        for(int i : list){
            if(max < i) { max = i; }
            else if (max < i * -1) { max = i * -1; }
        }

        int baselineY = contentHeight / 2;

        for(int i=0;i<max;i+=lineDistance){
            int y = i*contentHeight/max;
            canvas.drawLine(paddingLeft,paddingTop + baselineY + y,
                    paddingLeft+contentWidth,paddingTop + baselineY+y,bgPaint);
            canvas.drawLine(paddingLeft,paddingTop + baselineY - y,
                    paddingLeft+contentWidth,paddingTop + baselineY-y,bgPaint);
        }

        int listSize = list.length;
        for(int i=0;i<listSize - 1;i+=1){
            int vf = list[i];
            int xf = contentWidth * i / listSize;
            int yf = baselineY + (vf * contentHeight / (max * 2));
            int vs = list[i+1];
            int xs = contentWidth * i / listSize;
            int ys = baselineY + (vs * contentHeight / (max * 2));
//            canvas.drawLine(x, contentHeight, x, 0, barPaint);

            canvas.drawLine(paddingLeft + xf, paddingTop + yf, paddingLeft + xs, paddingTop + ys, barPaint);
        }
//        canvas.drawLine(0,0,contentWidth,contentHeight,barPaint);
//        canvas.drawLine(0,baselineY,0, list[listSize/2] * 120 / max, barPaint);
//        Log.d("audioData", baselineY + " " + listSize + " " + paddingLeft + " " + paddingTop);
    }

    public short[] getList() {
        return list;
    }

    public void setList(short[] list) {
        this.list = list;
        invalidate();
        requestLayout();
    }

    public int getBaseMax() {
        return baseMax;
    }

    public void setBaseMax(int baseMax) {
        this.baseMax = baseMax;
        invalidate();
        requestLayout();
    }

    public int getLineDistance() {
        return lineDistance;
    }

    public void setLineDistance(int lineDistance) {
        this.lineDistance = lineDistance;
        invalidate();
        requestLayout();
    }

    /**
     * Gets the example string attribute value.
     *
     * @return The example string attribute value.
     */
}