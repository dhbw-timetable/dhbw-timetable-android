package dhbw.timetable.data;

/**
 * Created by Hendrik Ulbrich (C) 2017
 */
public class AgendaAppointment {
    private String startTime, endTime, course;
    private boolean isBreak;

    public AgendaAppointment(String startTime, String endTime, String course, boolean isBreak) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.course = course;
        this.isBreak = isBreak;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() { return endTime; }

    public String getCourse() {
        return course;
    }

    public boolean isBreak() {
        return isBreak;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj != null) {
            if(obj instanceof AgendaAppointment) {
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
        return startTime + "|" + course + "|" + endTime;
    }
}
