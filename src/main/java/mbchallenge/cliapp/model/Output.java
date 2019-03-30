package mbchallenge.cliapp.model;


public class Output {

    String id, name, url, status;

    String time;

    public Output(String id, String name, String url, String status, String time) {
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

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return id + " - " + name + " - " + url + " - "+status + " - " +time;
    }

}
