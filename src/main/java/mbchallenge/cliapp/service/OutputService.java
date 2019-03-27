package mbchallenge.cliapp.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import mbchallenge.cliapp.model.EndpointList;
import org.springframework.beans.factory.InitializingBean;

import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


@Service
public class OutputService implements InitializingBean {

    /*
     ######################################################
     # VER https://www.youtube.com/watch?v=h6nMjjxJWjk&t= #
     ######################################################
    */

    private static final String OUTPUT_PATH = "src/main/resources/output.txt";
    private static final String CONFIG_PATH = "src/main/resources/config.json";


    public void poll(String only, String exclude) {
        /*
        -> Retrieves the status from of all configured services:
            -> Outputs results of all services
            -> Saves the result to local storage (don't use a database)

            -> Bonus: Pass an argument only to poll in order to retrieve a specific set of services (eg: --
                only=github,slack)
            -> Bonus: Pass an argument exclude to poll in order to exclude a specific set of services (eg: --
                exclude=slack)
                //TODO: Can only receive 1 argument in --only / --exclude command
         */

        System.out.println("This is the POLL shell command.");

        System.out.println("--only input > "+only + " size "+only.length());
        System.out.println("--exclude input > "+exclude+ " size "+exclude.length());

        System.out.println("onlyEndpoints set to: "+only+" and exludeEndpoints set to: "+exclude);

        String output = getFromServices(only, exclude);

        saveToFile(OUTPUT_PATH, output);

    }


    public void fetch(String inputTimer, String only, String exclude){

        /*
        -> Retrieves the status from of all configured services with a given interval (default interval: 5 seconds):
            -> Saves the result to local storage (don't use a database)
            -> Outputs results of all services
            -> Configurable polling interval, with default of 5 seconds (eg: --refresh=60)

            -> Bonus: Pass the argument only to fetch in order to retrieve a specific set of services (eg: --
                only=github,slack)
            -> Bonus: Pass the argument *exclude to fetch in order to exclude a specific set of services (eg: --
                exclude=slack)
                //TODO: Can only receive 1 argument in --only / --exclude command
                //TODO: Task doesn't finish
         */

        System.out.println("This is the FETCH shell command.");
        System.out.println("The recursion will end after 5 loops, or write 'exit' and run the program again.");

        // Receive timer from shell (in seconds) and translate to milliseconds
        long timer = Long.valueOf(inputTimer).longValue() * 1000;
        System.out.println("timer value = "+(timer/1000)+"s");

        Timer t = new Timer();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                String output = getFromServices(only, exclude);

                saveToFile(OUTPUT_PATH, output);
                System.out.println("ola");

            }

        };
        t.schedule(task,0L, timer);


    }


    public void history(String only){

        /*
        -> Outputs all the data from the local storage:
            -> Bonus: Pass the argument only to history in order to retrieve the history of a specific set of
                services (eg: --only=github)
            //TODO: historico so guarda 1x o output (github, bitbucket, slack),
                    não guarda [(github, bitbucket, slack)(github, bitbucket, slack)]

            //TODO: ver no readFromFile();
         */
        System.out.println("This is the HISTORY shell command.");

        String output = readFromFile(OUTPUT_PATH, only);
        System.out.println("History from output.txt >>>>> \n"+ output);

    }


    public void backup(String path){
        /*
        -> Takes an argument (path with the file name) and saves the correct local storage:
            // TODO:-> Bonus: Save to a simple .txt file, with a custom format (eg: --format=txt)
            // TODO:-> Bonus: Save to a simple .csv file, with row data separated by commas (eg: --format=csv)
         */
        System.out.println("This is the BACKUP shell command.");

        //String filePath = "/home/joaoreis/Desktop/challenge_mb/backup_test/backup.txt";

        // Reads data from output.txt file
        String outputInfo = readFromFile(OUTPUT_PATH,"");

        // Saves data from output.txt into the filePath
        saveToFile(path, outputInfo);

        System.out.println("backup file written into: "+path);

    }


    public void restore(String path, boolean merge){

        /*
        -> Takes an argument (path with the file name) and restores the data in the file into current local storage.
            -> Bonus: Pass an argument to restore in order to merge the content of the input file instead of
                replacing it (eg: --merge=true)
            ########
            # HINT #: Don't forget to validate the content of the import file before starting the import.
            ########
         */

        // TODO: validate to .txt, .csv

        System.out.println("This is the RESTORE shell command.");

        //String filePath = "/home/joaoreis/Desktop/challenge_mb/backup_test/backup.txt";

        if(!merge) {
            // Reads data from backup.txt file
            String newOutputInfo = readFromFile(path, "");
            // Saves data from filePath into the output.txt
            saveToFile(OUTPUT_PATH, newOutputInfo);
        }
        else{
            // TODO: Overwrite the file, merging the previous content with the new one (on the filePath)
        }
    }


    public void services(){
        /*
        -> Outputs all services defined in the configuration file and their respective endpoint.
         */
        System.out.println("This is the SERVICES shell command.");
        Gson gson = new GsonBuilder().create();
        EndpointList list = null;

        try {
            list = gson.fromJson(new FileReader(CONFIG_PATH), EndpointList.class);

            for(int i=0; i<list.getServices().size(); i++){
                System.out.println("Service: "+list.getServices().get(i).getName()
                        +" | Endpoint: " + list.getServices().get(i).getUrl());
            }
            //String output = list.getServices().toString();
            //System.out.println("Output > \n" + output);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    //TODO -> igual ao help Build-In Command ???
    public void help(){
        /*
        -> Outputs all available CLI commands
        */
        System.out.println("Available CLI commands: \n" +
                " poll\n fetch\n history\n backup\n restore\n services\n help1\n status");

    }


    //BONUS METHOD //TODO
    public void status(){
        /*
        -> Summarizes data and displays it in a table-like fashion:
            -> Print time since webservice has't been down
            -> Mean time to failure (MTTF)
         */
    }


    // Reads the config.json, sends GETs to urls and prints the info
    private String getFromServices(String only, String exclude){

        Gson gson = new GsonBuilder().create();
        EndpointList list = null;
        URL url = null;
        String output = new String();

        try {
            list = gson.fromJson(new FileReader(CONFIG_PATH), EndpointList.class);


            // Fetch info from sites in the config.json
            // ex: poll
            if(only.equals("") && exclude.equals("")) {

                for (int i = 0; i < list.getServices().size(); i++) {
                    url = new URL(list.getServices().get(i).getUrl());
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");

                    output = output + "[" + list.getServices().get(i).toString() + "] | status > "
                            + con.getResponseMessage() + "\n";

                    System.out.println("Service > [" + list.getServices().get(i).toString() + "] | status > "
                            + con.getResponseMessage());

                }
            }
            else{
                for (int i = 0; i < list.getServices().size(); i++) {

                    url = new URL(list.getServices().get(i).getUrl());
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");

                    if(only.length()>0 && list.getServices().get(i).getId().equals(only)) {
                        output = output + "[" + list.getServices().get(i).toString() + "] | status > "
                                + con.getResponseMessage() + "\n";

                        System.out.println("Service > [" + list.getServices().get(i).toString() + "] | status > "
                                + con.getResponseMessage());
                    }
                    else if(exclude.length()>0 && !list.getServices().get(i).getId().equals(exclude)){
                        output = output + "[" + list.getServices().get(i).toString() + "] | status > "
                                + con.getResponseMessage() + "\n";

                        System.out.println("Service > [" + list.getServices().get(i).toString() + "] | status > "
                                + con.getResponseMessage());
                    }
                }
            }

        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (ProtocolException e1) {
            e1.printStackTrace();
        } catch (MalformedURLException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        return output;
    }

    // Saves info into output.txt file as a String
    private void saveToFile(String filePath, String output) {
        try {
            File f = new File(filePath);

            // Clear output.txt if already written
            if (f.exists()) {
                f.delete();
            }

            // Writes output into output.txt
            FileWriter out = new FileWriter(f);
            out.write(output);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // Shows the info in the output.txt file
    private String readFromFile(String filePath, String only) {
        String output = new String();
        try {
            if(only.equals("")){
                output = new String(Files.readAllBytes(Paths.get(filePath)));
            }
            else{
                //TODO: 1- pôr output em JSON em vez de .txt
                //      2- filtrar pelo id dos JSON nodes
            }

            //System.out.println("Output from saveToFile >>>>> \n"+ output);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return output;
    }


    @Override
    public void afterPropertiesSet() throws Exception {

        // VER https://www.youtube.com/watch?v=h6nMjjxJWjk&t=

    }
}
