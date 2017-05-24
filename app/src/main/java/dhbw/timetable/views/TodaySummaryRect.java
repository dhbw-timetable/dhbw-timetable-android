package dhbw.timetable.views;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.View;

import java.util.ArrayList;
import java.util.Calendar;

import dhbw.timetable.R;
import dhbw.timetable.data.Appointment;

public class TodaySummaryRect extends View {
    private Paint paint = new Paint();
    private View pLayout;
    private ArrayList<ArrayList<Appointment>> wData;
    private int min, max;

    public TodaySummaryRect(int min, int max, View parentLayout, ArrayList<ArrayList<Appointment>> appointments) {
        super(parentLayout.getContext());
        pLayout = parentLayout;
        this.wData = appointments;
        this.min = min;
        this.max = max;
    }

    @Override
    public void onDraw(Canvas canvas) {
        paint.setStrokeWidth(0);
        paint.setColor(getResources().getColor(R.color.colorPrimary));
        drawWeek(canvas);
    }

    private int dp(int px) {
        return (int) (px * getResources().getDisplayMetrics().density + 0.5f);
    }

    private void drawWeek(Canvas canvas) {

        float aWidth = pLayout.getMeasuredWidth() / 5;
        for(int i = 0; i < wData.size(); i++) {
            for (Appointment a : wData.get(i)) {
                float startOnMin = a.getStartDate().get(Calendar.HOUR_OF_DAY) * 60
                        + a.getStartDate().get(Calendar.MINUTE);
                float endOnMin = a.getEndDate().get(Calendar.HOUR_OF_DAY) * 60
                        + a.getEndDate().get(Calendar.MINUTE);
                float startY = ((startOnMin - min) * pLayout.getMeasuredHeight()) / (max - min);
                float startX = aWidth * i;
                float aHeight = ((endOnMin - min) * pLayout.getMeasuredHeight()) / (max - min);

                canvas.drawRoundRect(new RectF(new Rect(
                        (int) startX,
                        (int) startY,
                        (int) (startX + aWidth),
                        (int) aHeight)),
                        dp(3),dp(3), paint);
            }
        }
    }

}
