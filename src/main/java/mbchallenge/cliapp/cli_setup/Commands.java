package mbchallenge.cliapp.cli_setup;

import mbchallenge.cliapp.service.OutputService;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent
public class Commands {

    private final OutputService output;

    public Commands(OutputService output) {
        this.output = output;
    }

    @ShellMethod(value = "Status from all configured services", key = "poll")
    public void poll(){
        this.output.poll();
    }

    @ShellMethod(value = "Status from all configured services with a given interval", key = "fetch")
    public void fetch(){
        this.output.fetch();
    }

    @ShellMethod(value = "Data from the local storage", key = "history")
    public void history(){
        this.output.history();
    }

    @ShellMethod(value = "Saves local storage into given file", key = "backup")
    public void backup(){
        this.output.backup();
    }

    @ShellMethod(value = "Restores the data of a given file into local storage", key = "restore")
    public void restore(){
        this.output.restore();
    }

    @ShellMethod(value = "Services defined in the configuration file and their respective endpoint", key = "services")
    public void services(){
        this.output.services();
    }

    @ShellMethod(value = "Available CLI commands", key = "help1")
    public void help(){
        this.output.help();
    }

    @ShellMethod(value = "Display of data", key = "status")
    public void status(){
        this.output.status();
    }



    @ShellMethod(value = "Add numbers.", key = "add")
    public int add(int a, int b) {
        return a + b;
    }
}
