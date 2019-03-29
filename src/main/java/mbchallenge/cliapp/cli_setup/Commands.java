package mbchallenge.cliapp.cli_setup;


import mbchallenge.cliapp.service.OutputService;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.shell.standard.commands.Help;
import org.springframework.shell.standard.commands.Quit;


@ShellComponent
public class Commands implements Help.Command, Quit.Command{

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
    public void fetch(@ShellOption(value = "--refresh", defaultValue = DEFAULT_TIMER) String timer,
                      @ShellOption(defaultValue = "") String only,
                      @ShellOption(defaultValue = "") String exclude){
        this.output.fetch(timer, only, exclude);
    }

    @ShellMethod(value = "Data from the local storage", key = "history")
    public void history(@ShellOption(value = "--only", defaultValue = "") String only){
        this.output.history(only);
    }

    @ShellMethod(value = "Saves local storage into given file", key = "backup")
    public void backup(String filePath, @ShellOption(value = "--format", defaultValue = "txt")String type){
        this.output.backup(filePath, type);
    }

    @ShellMethod(value = "Restores the data of a given file into local storage", key = "restore")
    public void restore(String filePath, boolean merge){
        this.output.restore(filePath, merge);
    }

    @ShellMethod(value = "Services defined in the configuration file and their respective endpoint", key = "services")
    public void services(){
        this.output.services();
    }

    @ShellMethod(value = "Available CLI commands", key = "help")
    public void help(){
        this.output.help();
    }

    @ShellMethod(value = "Display of data", key = "status")
    public void status(){
        this.output.status();
    }

    @ShellMethod(value = "Stop fetch command.", key = "stop")
    public void stop() {
        this.output.stopFetch();
    }

    @ShellMethod(value = "Ends program", key="quit")
    public void quit(){
        this.output.quit();
    }

    // is there any way to make two different commands do the same?
    @ShellMethod(value = "Ends program", key="exit")
    public void exit(){
        this.output.quit();
    }
}
