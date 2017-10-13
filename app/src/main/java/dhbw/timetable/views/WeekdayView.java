package dhbw.timetable.views;

import android.app.Activity;
import android.content.Intent;
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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashSet;

import dhbw.timetable.ActivityHelper;
import dhbw.timetable.DayDetailsActivity;
import dhbw.timetable.R;
import dhbw.timetable.data.Appointment;
import dhbw.timetable.data.TimetableManager;

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
    private LinkedHashSet<Appointment> dayAppointments;
    private float scale;
    private int min, max;
    private boolean isFriday;

    public WeekdayView(int min, int max, final View parentLayout, final ArrayList<Appointment> appointments, boolean isFriday, final String detailsDate) {
        super(parentLayout.getContext());
        this.min = min;
        this.max = max;
        this.isFriday = isFriday;
        this.parentLayout = parentLayout;
        dayAppointments = new LinkedHashSet<>();
        for(Appointment app : appointments) {
            dayAppointments.add(app);
        }
        this.scale = getResources().getDisplayMetrics().density;

        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity activity = ActivityHelper.getActivity();
                if (activity != null && !TimetableManager.getInstance().isRunning()) {
                    StringBuilder sb = new StringBuilder("");
                    for (Appointment ap : dayAppointments) {
                        Log.i("DEBUG", "" + ap);
                        sb.append(ap.getStartTime())
                                .append("\n")
                                .append(ap.getCourse())
                                .append("\n")
                                .append(ap.getInfo())
                                .append("\n")
                                .append(ap.getEndTime())
                                .append("\n\n");
                    }


                    Intent detailsIntent = new Intent(activity.getApplicationContext(), DayDetailsActivity.class);
                    detailsIntent.putExtra("day", "" + detailsDate);
                    detailsIntent.putExtra("agenda", sb.toString());
                    activity.startActivity(detailsIntent);
                } else {
                    Toast.makeText(activity, "I'm currently busy, sorry!", Toast.LENGTH_SHORT).show();
                }
            }
        });
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
                    appointmentWidth-32,
                    Layout.Alignment.ALIGN_CENTER,
                    1.0f,
                    0.0f,
                    false);
            canvas.save();
            canvas.translate(x1+16,
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
        paint.setColor(Color.parseColor("#E0E0E0"));

        final float height = parentLayout.getMeasuredHeight();
        final float width = parentLayout.getMeasuredWidth();
        final float k = (Y_GRID_SPACE * height) / (max - min);
        for(int i = 0; i * k < height; i++) {
            paint.setStrokeWidth(dp(((i % 2 == 0) == (min % 60 == 0)) ? 2 : 1));
            canvas.drawLine(0, i * k, (width / 5) + (isFriday ? X_OFFSET : 0), i * k, paint);
        }
    }
}
