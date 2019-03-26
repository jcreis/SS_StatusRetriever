package mbchallenge.cliapp.model;

import java.util.List;

public class EndpointList {
    List<Endpoint> services;

    public EndpointList(List<Endpoint> services) {
        this.services = services;
    }

    public EndpointList() {
    }

    public List<Endpoint> getServices() {
        return services;
    }

    public void setServices(List<Endpoint> services) {
        this.services = services;
    }

    public void addService(Endpoint service){
        this.services.add(service);
    }
}
