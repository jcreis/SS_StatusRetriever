package mbchallenge.cliapp.cli_setup;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mbchallenge.cliapp.model.EndpointList;
import mbchallenge.cliapp.service.OutputService;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;


@ShellComponent
public class Commands {

    // 5sec timer by default, goes in as (String) but is processed as (Long)
    private static final String DEFAULT_TIMER = "5";

    private final OutputService output;

    public Commands(OutputService output) {
        this.output = output;
    }



    /*
                ######################################
                ###### C O M M A N D    L I S T ######
                ######################################
     */

    @ShellMethod(value = "Status from all configured services", key = "poll")
    public void poll(@ShellOption(value = "--only", defaultValue = "") String only,
                     @ShellOption(value = "--exclude", defaultValue = "")String exclude){
        this.output.poll(only, exclude);
    }

    @ShellMethod(value = "Status from all configured services with a given interval", key = "fetch")
    public void fetch(@ShellOption(defaultValue = DEFAULT_TIMER) String timer,
                      @ShellOption(value = "--only", defaultValue = "") String only,
                      @ShellOption(value = "--exclude", defaultValue = "")String exclude){
        this.output.fetch(timer, only, exclude);
    }

    @ShellMethod(value = "Data from the local storage", key = "history")
    public void history(@ShellOption(value = "--only", defaultValue = "") String only){
        this.output.history(only);
    }

    @ShellMethod(value = "Saves local storage into given file", key = "backup")
    public void backup(String filePath){
        this.output.backup(filePath);
    }

    @ShellMethod(value = "Restores the data of a given file into local storage", key = "restore")
    public void restore(String filePath, boolean merge){
        this.output.restore(filePath, merge);
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
