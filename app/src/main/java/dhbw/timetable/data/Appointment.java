package dhbw.timetable.data;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Created by Hendrik Ulbrich (C) 2017
 */
public class Appointment {
    private GregorianCalendar startDate, endDate;
    private String course, info;

    @Deprecated
    Appointment(String time, GregorianCalendar date, String course, String info) {
        startDate = (GregorianCalendar) date.clone();
        endDate = (GregorianCalendar) date.clone();

        String[] times = time.split("-");
        startDate.set(Calendar.HOUR_OF_DAY, Integer.parseInt(times[0].split(":")[0]));
        startDate.set(Calendar.MINUTE, Integer.parseInt(times[0].split(":")[1]));
        endDate.set(Calendar.HOUR_OF_DAY, Integer.parseInt(times[1].split(":")[0]));
        endDate.set(Calendar.MINUTE, Integer.parseInt(times[1].split(":")[1]));

        this.course = course;
        this.info = info;
    }

    Appointment(GregorianCalendar startDate, GregorianCalendar endDate,
                       String course, String info) {
        this.startDate = (GregorianCalendar) startDate.clone();
        this.endDate = (GregorianCalendar) endDate.clone();
        this.course = course;
        this.info = info;
    }

    public String getStartTime() {
       SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.GERMANY);
       sdf.setCalendar(startDate);
       return sdf.format(startDate.getTime());
    }

    public String getEndTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.GERMANY);
        sdf.setCalendar(endDate);
        return sdf.format(endDate.getTime());
    }

    public String getDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY);
        sdf.setCalendar(startDate);
        return sdf.format(startDate.getTime());
    }

    /**
     * Includes time
     * @return
     */
    public GregorianCalendar getStartDate() {
        return startDate;
    }

    /**
     * Includes time
     * @return
     */
    public GregorianCalendar getEndDate() {
        return endDate;
    }

    public String getCourse() {
        return course;
    }

    public String getInfo() {
        return info;
    }

    @Override
    public boolean equals(Object o) {
        if(o != null) {
            if (o instanceof Appointment) {
                Appointment that = (Appointment) o;
                return that.toString().equals(this.toString());
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return getDate() + "\t"+ getStartTime() + "-" + getEndTime() + "\t" + course + "\t" + info;
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }
}
