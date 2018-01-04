package dhbw.timetable.views;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashSet;

import dhbw.timetable.ActivityHelper;
import dhbw.timetable.DayDetailsActivity;
import dhbw.timetable.R;
import dhbw.timetable.data.TimetableManager;
import dhbw.timetable.rapla.data.event.BackportAppointment;

/**
 * Created by Hendrik Ulbrich (C) 2017
 */
public class WeekdayView extends View {

    public final static int Y_GRID_SPACE = 30;
    private final static int X_OFFSET = 60;
    private final static int X_WIDTH = 30; // Minutes

    private HashMap<BackportAppointment, RectF> eventRectangles = new HashMap<>();
    private Paint paint = new Paint();
    private TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private View parentLayout;
    private LinkedHashSet<BackportAppointment> dayAppointments;
    private float scale;
    private int min, max, shiftX_max = 0;
    private boolean isFriday, fit = false;

    public WeekdayView(int min, int max, final View parentLayout, final ArrayList<BackportAppointment> appointments, boolean isFriday, final String detailsDate, final String dayName) {
        super(parentLayout.getContext());
        this.min = min;
        this.max = max;
        this.isFriday = isFriday;
        this.parentLayout = parentLayout;
        dayAppointments = new LinkedHashSet<>();
        dayAppointments.addAll(appointments);
        this.scale = getResources().getDisplayMetrics().density;
        this.setOnClickListener(v -> {
            Activity activity = ActivityHelper.getActivity();
            if (activity != null && !TimetableManager.getInstance().isRunning()) {
                StringBuilder sb = new StringBuilder("");
                for (BackportAppointment ap : dayAppointments) {
                    Log.i("DEBUG", "" + ap);
                    sb.append(ap.getStartTime())
                            .append("\n")
                            .append(ap.getTitle())
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
        });
        generateRectangles();
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Recalculate rectangle positions on device rotation
        eventRectangles.clear();
        shiftX_max = 0;
        fit = false;
        generateRectangles();
    }

    private RectF layoutRectangle(final BackportAppointment a) {
        float startOnMin = a.getStartDate().get(Calendar.HOUR_OF_DAY) * 60
                + a.getStartDate().get(Calendar.MINUTE);
        float endOnMin = a.getEndDate().get(Calendar.HOUR_OF_DAY) * 60
                + a.getEndDate().get(Calendar.MINUTE);
        float x1, x2, y1, y2, appointmentWidth;

        appointmentWidth = dp((int) (X_OFFSET * 1.75) + X_WIDTH);

        x1 = dp(X_OFFSET / 4);
        y1 = (startOnMin - min) / (max - min);
        x2 = x1 + appointmentWidth;
        y2 = (endOnMin - min) / (max - min);

        return new RectF(x1, y1, x2, y2);
    }

    private void generateRectangles() {
        for (BackportAppointment a : dayAppointments) {
            // Default, left aligned (not shifted) rectangle
            RectF a_rect = layoutRectangle(a);

            // Check for each rects if intersection is present
            boolean check_intersect = true;
            int i = 0;
            start_intersection_check:
            while (check_intersect) {
                for (RectF r : eventRectangles.values()) {
                    if (r.contains(a_rect) || RectF.intersects(r, a_rect)) {
                        if (++i > shiftX_max) shiftX_max = i;
                        // shift one unit to the right and start again
                        a_rect.offset(dp(2 * X_OFFSET + X_WIDTH), 0);
                        continue start_intersection_check;
                    } // else check next
                }

                // finish
                check_intersect = false;
            }

            eventRectangles.put(a, a_rect);
        }
    }

    private void fitRectsToParent() {
        for (RectF rf : eventRectangles.values()) {
            rf.set(rf.left, (int) (rf.top * parentLayout.getMeasuredHeight()), rf.right, (int) (rf.bottom * parentLayout.getMeasuredHeight()));
        }
        fit = true;
    }

    @Override
    public void onDraw(Canvas canvas) {
        paint.reset();
        textPaint.reset();
        drawGrid(canvas);
        if (!fit) fitRectsToParent();
        for (BackportAppointment a : eventRectangles.keySet()) {
            RectF rect = eventRectangles.get(a);

            textPaint.setColor(getResources().getColor(R.color.colorPrimary));
            // Draw the appointment rectangle
            canvas.drawRoundRect(new RectF(rect), dp(7), dp(7), textPaint);

            textPaint.setColor(Color.WHITE);
            textPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 14, getResources().getDisplayMetrics()));

            // Draw the course time
            canvas.save();
            Typeface currentTypeFace1 = textPaint.getTypeface();
            textPaint.setTypeface(Typeface.create(currentTypeFace1, Typeface.NORMAL));

            canvas.drawText(a.getStartTime() + " - " + a.getEndTime(), rect.left + 16, rect.top + dp(16), textPaint);

            if (a.getTitle().equals("Klausur")) {
                Log.i("1337", "" + (rect.bottom - rect.top));
            }

            // Draw info
            if (rect.bottom - rect.top > 180) {
                canvas.drawText(TextUtils.ellipsize(a.getInfo(), textPaint,
                        dp((int) (X_OFFSET * 1.75) + X_WIDTH) - 32, TextUtils.TruncateAt.END).toString(),
                        rect.left + 16, rect.bottom - 16, textPaint);
            }

            // Draw course title
            if (rect.bottom - rect.top > 60) {
                StaticLayout courseTitleLayout = new StaticLayout(
                        TextUtils.ellipsize(a.getTitle().trim(), textPaint, dp((int) (X_OFFSET * 1.75) + X_WIDTH) - 32, TextUtils.TruncateAt.END),
                        textPaint,
                        dp((int) (X_OFFSET * 1.75) + X_WIDTH) - 32,
                        Layout.Alignment.ALIGN_NORMAL,
                        1.0f,
                        0.0f,
                        false);
                Typeface currentTypeFace = textPaint.getTypeface();
                Typeface bold = Typeface.create(currentTypeFace, Typeface.BOLD);
                textPaint.setTypeface(bold);
                canvas.save();
                canvas.translate(rect.left + 16, rect.top + 16 + dp(16));
                courseTitleLayout.draw(canvas);
            }

            // Reset
            canvas.restore();
        }
    }



    // For auto layout: standard appointment width + margin (if is friday also end margin)
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(dp((2 * X_OFFSET + X_WIDTH) * (shiftX_max + 1) + (isFriday ? X_OFFSET / 4 : 0)), parentLayout.getMeasuredHeight());
    }

    private int dp(int px) {
        return (int) (px * scale + 0.5f);
    }

    private void drawGrid(Canvas canvas) {
        paint.setColor(Color.parseColor("#E0E0E0"));

        final float height = parentLayout.getMeasuredHeight();
        final float k = (Y_GRID_SPACE * height) / (max - min);
        for (int i = 0; i * k < height; i++) {
            paint.setStrokeWidth(dp(((i % 2 == 0) == (min % 60 == 0)) ? 2 : 1));
            canvas.drawLine(0, i * k, getMeasuredWidth(), i * k, paint);
        }
    }
}
