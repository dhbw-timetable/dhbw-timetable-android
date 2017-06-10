package dhbw.timetable.data;

/**
 * Created by Hendrik Ulbrich (C) 2017
 */
public class Timetable {
    private String name, key;

    public Timetable(String name, String key) {
        this.name = name;
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public String getKey() {
        return key;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setKey(String key) {
        this.key = key;
    }

}
