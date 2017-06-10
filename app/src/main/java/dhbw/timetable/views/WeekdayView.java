package dhbw.timetable.views;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.Calendar;

import dhbw.timetable.R;
import dhbw.timetable.data.Appointment;

/**
 * Created by Hendrik Ulbrich (C) 2017
 */
public class WeekdayView extends View {

    public final static int Y_GRID_SPACE = 30;
    private final static int X_OFFSET = 60;
    private final static int X_WIDTH = 30; // Minutes

    private Paint paint = new Paint();
    private TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private View parentLayout;
    private ArrayList<Appointment> dayAppointments;
    private float scale;
    private int min, max;
    private boolean isFriday;

    public WeekdayView(int min, int max, View parentLayout, ArrayList<Appointment> appointments, boolean isFriday) {
        super(parentLayout.getContext());
        this.min = min;
        this.max = max;
        this.isFriday = isFriday;
        this.parentLayout = parentLayout;
        this.dayAppointments= appointments;
        this.scale = getResources().getDisplayMetrics().density;
    }

    // TODO: Extract memory allocations where possible
    @Override
    public void onDraw(Canvas canvas) {
        drawGrid(canvas);

        for(Appointment a : dayAppointments) {
            textPaint.setColor(getResources().getColor(R.color.colorPrimary));
            int startOnMin = a.getStartDate().get(Calendar.HOUR_OF_DAY) * 60
                    + a.getStartDate().get(Calendar.MINUTE);
            int endOnMin = a.getEndDate().get(Calendar.HOUR_OF_DAY) * 60
                    + a.getEndDate().get(Calendar.MINUTE);
            int x1, x2, y1, y2, appointmentWidth;

            appointmentWidth = dp((int) (X_OFFSET * 1.75) + X_WIDTH);

            x1 = dp(X_OFFSET / 4);
            y1 = ((startOnMin - min) * parentLayout.getMeasuredHeight()) / (max - min);
            x2 = x1 + appointmentWidth;
            y2 = ((endOnMin - min) * parentLayout.getMeasuredHeight()) / (max - min);


            // Draw the appointment rectangle
            canvas.drawRoundRect(new RectF(new Rect(x1, y1, x2, y2)),
                    dp(7), dp(7), textPaint);

            // Draw the course title
            textPaint.setColor(Color.WHITE);
            textPaint.setTextSize(dp(14));
            Typeface currentTypeFace = textPaint.getTypeface();
            Typeface bold = Typeface.create(currentTypeFace, Typeface.BOLD);
            textPaint.setTypeface(bold);
            StaticLayout textLayout = new StaticLayout(
                    a.getCourse(),
                    textPaint,
                    appointmentWidth,
                    Layout.Alignment.ALIGN_CENTER,
                    1.0f,
                    0.0f,
                    false);
            canvas.save();
            canvas.translate(x1,
                    transpose(startOnMin + ((endOnMin - startOnMin) / 2))
                            - (textLayout.getHeight() / 2));
            textLayout.draw(canvas);
            canvas.restore();
        }
    }

    // For auto layout
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(dp(2 * X_OFFSET + X_WIDTH + (isFriday ? X_OFFSET / 4 : 0)), parentLayout.getMeasuredHeight());
        // Log.i("LAYOUT", dp(getMeasuredWidth()) + " x " + dp(getMeasuredHeight()));
        // Log.i("LAYOUT", getMeasuredWidth() + " x " + getMeasuredHeight());
    }

    private int dp(int px) {
        return (int) (px * scale + 0.5f);
    }

    private int transpose(int minValue) {
        return (minValue - min) * parentLayout.getMeasuredHeight() / (max - min);
    }

    private void drawGrid(Canvas canvas) {
        paint.setStrokeWidth(dp(1));
        paint.setColor(Color.parseColor("#E0E0E0"));

        final float height = parentLayout.getMeasuredHeight();
        final float width = parentLayout.getMeasuredWidth();
        final float k = (Y_GRID_SPACE * height) / (max - min);
        for(int i = 0; i * k < height; i++) {
            canvas.drawLine(0, i * k, (width / 5) + (isFriday ? X_OFFSET : 0), i * k, paint);
        }
    }
}
