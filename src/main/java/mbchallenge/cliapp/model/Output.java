package mbchallenge.cliapp.model;

import java.util.Date;

public class Output {

    String id, name, url, status;

    long time;

    public Output(String id, String name, String url, String status, long time) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.status = status;
        this.time = time;
    }

    public Output() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return id + " - " + name + " - " + url + " - "+status;
    }
}
