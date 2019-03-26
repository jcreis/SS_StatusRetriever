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
import java.util.Timer;
import java.util.TimerTask;


@Service
public class OutputService implements InitializingBean {

    /*
     ######################################################
     # VER https://www.youtube.com/watch?v=h6nMjjxJWjk&t= #
     ######################################################
    */


    public void poll() {
        /*
        -> Retrieves the status from of all configured services:
            -> Outputs results of all services
            -> Saves the result to local storage (don't use a database)

            //TODO -> Bonus: Pass an argument only to poll in order to retrieve a specific set of services (eg: --
                only=github,slack)
            //TODO -> Bonus: Pass an argument exclude to poll in order to exclude a specific set of services (eg: --
                exclude=slack)
         */

        System.out.println("This is the POLL shell command.");

        String output = getFromServices();

        saveToFile(output);

    }

    public void fetch(){

        /*
        -> Retrieves the status from of all configured services with a given interval (default interval: 5 seconds):
            -> Saves the result to local storage (don't use a database)
            -> Outputs results of all services
            //TODO -> Configurable polling interval, with default of 5 seconds (eg: --refresh=60)

            //TODO -> Bonus: Pass the argument only to fetch in order to retrieve a specific set of services (eg: --
                only=github,slack)
            //TODO -> Bonus: Pass the argument *exclude to fetch in order to exclude a specific set of services (eg: --
                exclude=slack)
         */

        System.out.println("This is the FETCH shell command.");
        System.out.println("The recursion will end after 5 loops, or write 'exit' and run the program again.");

        long defaultTimer = 5000L;

        Timer t = new Timer();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                String output = getFromServices();

                saveToFile(output);
                System.out.println("ola");

            }

        };
        t.schedule(task,0L, defaultTimer);

        //TODO -> doesnt finish
    }

    //TODO
    public void history(){

        /*
        -> Outputs all the data from the local storage:
            -> Bonus: Pass the argument only to history in order to retrieve the history of a specific set of
                services (eg: --only=github)
         */

    }

    //TODO
    public void backup(){

        /*
        -> Takes an argument (path with the file name) and saves the currect local storage:
            -> Bonus: Save to a simple .txt file, with a custom format (eg: --format=txt)
            -> Bonus: Save to a simple .csv file, with row data separated by commas (eg: --format=csv)
         */

    }

    //TODO
    public void restore(){

        /*
        -> Takes an argument (path with the file name) and restores the data in the file into current local storage.
            -> Bonus: Pass an argument to restore in order to merge the content of the input file instead of
                replacing it (eg: --merge=true)
            ########
            # HINT #: Don't forget to validate the content of the import file before starting the import.
            ########
         */
    }

    //TODO
    public void services(){
        /*
        -> Outputs all services defined in the configuration file and their respective endpoint.
         */

    }

    //TODO
    public void help(){
        /*
        -> Outputs all available CLI commands
         */

    }

    //BONUS METHOD //TODO
    public void status(){
        /*
        -> Summarizes data and displays it in a table-like fashion:
            -> Print time since webservice has't been down
            -> Mean time to failure (MTTF)
         */
    }


    private String getFromServices(){
        Gson gson = new GsonBuilder().create();
        EndpointList list = null;
        URL url = null;
        String output = new String();

        try {
            list = gson.fromJson(new FileReader("src/main/resources/config.json"), EndpointList.class);


            // Fetch info from sites in the config.json

            for (int i = 0; i < list.getServices().size(); i++) {
                url = new URL(list.getServices().get(i).getUrl());
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");

                output = output + "[" + list.getServices().get(i).toString() + "] | status > "
                        + con.getResponseMessage() + "\n";

                System.out.println("Service > [" + list.getServices().get(i).toString() + "] | status > "
                        + con.getResponseMessage());
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

    private void saveToFile(String output) {
        try {
            File f = new File("src/main/resources/output.txt");

            // Clear output.txt if already writen
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


    @Override
    public void afterPropertiesSet() throws Exception {

        // VER https://www.youtube.com/watch?v=h6nMjjxJWjk&t=

    }
}
