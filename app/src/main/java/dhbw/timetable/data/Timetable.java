package dhbw.timetable.data;

/**
 * Created by Hendrik Ulbrich (C) 2017
 */
public class Timetable {

    private String name, url;

    public Timetable(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public String getURL() {
        return url;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setURL(String url) {
        this.url = url;
    }

}
