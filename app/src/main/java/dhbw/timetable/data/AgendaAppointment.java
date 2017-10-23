package dhbw.timetable.data;

import dhbw.timetable.rapla.data.event.Lecture;

/**
 * Created by Hendrik Ulbrich (C) 2017
 */
public class AgendaAppointment extends Lecture {

    private String startTime, endTime;
    private boolean isBreak;

    public AgendaAppointment(String startTime, String endTime, String title, String persons, String resources, boolean isBreak) {
        super(title, persons, resources);

        this.startTime = startTime;
        this.endTime = endTime;
        this.isBreak = isBreak;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public boolean isBreak() {
        return isBreak;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null) {
            if (obj instanceof AgendaAppointment) {
                return this.toString().equals(obj.toString());
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

    @Override
    public String toString() {
        return startTime + "|" + title + "|" + persons + " " + resources + "|" + endTime;
    }

}
