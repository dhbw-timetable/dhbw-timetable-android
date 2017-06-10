package dhbw.timetable.data;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Created by Hendrik Ulbrich (C) 2017
 */
public class TimelessDate extends GregorianCalendar {

    public TimelessDate() {
        this((GregorianCalendar) Calendar.getInstance());
    }

    public TimelessDate(GregorianCalendar cal) {
        super();
        this.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
    }


    @Override
    public boolean equals(Object obj) {
        if(obj != null && obj instanceof  GregorianCalendar) {
            GregorianCalendar otherObj = (GregorianCalendar) obj;
            return otherObj.get(Calendar.DAY_OF_MONTH) == this.get(Calendar.DAY_OF_MONTH)
                    && otherObj.get(Calendar.MONTH) == this.get(Calendar.MONTH)
                    && otherObj.get(Calendar.YEAR) == this.get(Calendar.YEAR);

        }
        return false;
    }

    @Override
    public int hashCode() {
        return new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY).format(this.getTime()).hashCode();
    }
}
