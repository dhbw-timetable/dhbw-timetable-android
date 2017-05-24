package dhbw.timetable.views;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.view.View;

import dhbw.timetable.R;

public class SideTimesView extends View {

    private static final int TIME_WIDTH = 38;
    private static final int Y_TEXT_LAYOUT = 31;
    private static final int Y_LINE_LAYOUT = 26;

    private TextPaint paint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private View pLayout, sLayout;
    int min, max;
    private float scale;


    public SideTimesView(int min, int max, View parentLayout, View siblingLayout) {
        super(parentLayout.getContext());
        this.pLayout = parentLayout;
        this.sLayout = siblingLayout;
        this.min = min;
        this.max = max;
        scale = getResources().getDisplayMetrics().density;
    }

    // For auto layout
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(dp(TIME_WIDTH), heightMeasureSpec);
    }

    private int dp(int px) {
        return (int) (px * scale + 0.5f);
    }

    @Override
    public void onDraw(Canvas canvas) {
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setTextSize(dp(13));
        paint.setStrokeWidth(dp(1));

        final float height = sLayout.getMeasuredHeight();
        final float k = (WeekdayView.Y_GRID_SPACE * height) / (max - min);
        for(int i = 0; i * k < height; i++) {
            if(i % 2 == 0) {
                paint.setColor(getResources().getColor(R.color.textColorSecondary));
                int iM = min%60;
                int iH = min/60 + i/2;
                String sM = iM < 10 ? "0" + iM : "" + iM;
                String sH = iH < 10 ? "0" + iH : "" + iH;
                String time = sH + ":" + sM;
                canvas.drawText(time, 0, time.length(), dp(2), (int) (i * k) + dp(Y_TEXT_LAYOUT), paint);
            } else {
                paint.setColor(Color.parseColor("#E0E0E0"));
                canvas.drawLine(0, (int) (i * k) + dp(Y_LINE_LAYOUT), dp(TIME_WIDTH), (int) (i * k) + dp(Y_LINE_LAYOUT), paint);
            }
        }
    }
}
