package mbchallenge.cliapp.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import mbchallenge.cliapp.model.Endpoint;
import mbchallenge.cliapp.model.EndpointList;
import mbchallenge.cliapp.model.Output;
import org.hibernate.validator.internal.engine.valueextraction.ArrayElement;
import org.springframework.beans.factory.InitializingBean;

import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.spi.FileTypeDetector;
import java.util.*;


@Service
public class OutputService implements InitializingBean {

    /*
     ######################################################
     # VER https://www.youtube.com/watch?v=h6nMjjxJWjk&t= #
     ######################################################
    */

    private static final String OUTPUT_PATH = "src/main/resources/output.json";
    private static final String CONFIG_PATH = "src/main/resources/config.json";

    private Timer t = new Timer();
    private boolean run = false;

    public void poll(String only, String exclude) {
        /*
        -> Retrieves the status from of all configured services:
            -> Outputs results of all services
            -> Saves the result to local storage (don't use a database)

            -> Bonus: Pass an argument only to poll in order to retrieve a specific set of services (eg: --
                only=github,slack)
            -> Bonus: Pass an argument exclude to poll in order to exclude a specific set of services (eg: --
                exclude=slack)

         */

        System.out.println("This is the POLL shell command.");
        System.out.println();
        System.out.println();
        System.out.println("Poll output:");

        List<Output> output = getFromServices(only, exclude);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        // Saves data from output.json into the filePath

        saveToFile(OUTPUT_PATH, output);

    }


    public void fetch(String inputTimer, String only, String exclude){


        /*-> Retrieves the status from of all configured services with a given interval (default interval: 5 seconds):
            -> Saves the result to local storage (don't use a database)
            -> Outputs results of all services
            -> Configurable polling interval, with default of 5 seconds (eg: --refresh=60)

            -> Bonus: Pass the argument only to fetch in order to retrieve a specific set of services (eg: --
                only=github,slack)
            -> Bonus: Pass the argument *exclude to fetch in order to exclude a specific set of services (eg: --
                exclude=slack)
         */

        System.out.println("This is the FETCH shell command.");
        System.out.println("Write 'stop' to stop the fetch operation.");

        // Receive timer from shell (in seconds) and translate to milliseconds
        long timer = Long.valueOf(inputTimer).longValue() * 1000;
        System.out.println("Timer value = "+(timer/1000)+"s");

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                List<Output> output = getFromServices(only, exclude);

                // Saves data from output.json into the filePath
                saveToFile(OUTPUT_PATH, output);
                run=true;
            }

        };
        if(!run)
            t.schedule(task,0L, timer);

    }

    // Auxiliar command to stop fetch from executing
    public void stopFetch(){
        t.cancel();
        run=false;
    }

    public void history(String only){

        /*
        -> Outputs all the data from the local storage:
            -> Bonus: Pass the argument only to history in order to retrieve the history of a specific set of
                services (eg: --only=github)
         */
        System.out.println("This is the HISTORY shell command.");
        System.out.println("History from output.json >>>>> \n");

        List<Output> output = readFromFile(OUTPUT_PATH, only);
        System.out.println(output);
    }


    public void backup(String path, String format){
/*
        -> Takes an argument (path with the file name) and saves the correct local storage:
            -> Bonus: Save to a simple .txt file, with a custom format (eg: --format=txt)
            -> Bonus: Save to a simple .csv file, with row data separated by commas (eg: --format=csv)
*/
        System.out.println("This is the BACKUP shell command.");
        List<Output> outputInfo = new ArrayList<>();

        // Reads data from output.json file
        outputInfo = readFromFile(OUTPUT_PATH,"");
        if(format.equals("txt")){

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            // Saves data from output.json into the filePath
            String txtOutput = gson.toJson(outputInfo);

            saveBackup(path+"/backup."+format, txtOutput);
        }
        else if(format.equals("csv")){

            String csvOutput = writeAsCSVFile(outputInfo);
            saveBackup(path+"/backup."+format, csvOutput);
        }

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

        getFileExtension(path);
        System.out.println("Extension of the file: "+getFileExtension(path));
        //String filePath = "/home/joaoreis/Desktop/challenge_mb/backup_test/backup.txt";

        if(!merge) {
            // Reads data from backup.txt file
            List<Output> newOutputInfo = readFromFile(path, "");

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            // Saves data from output.json into the filePath
            String strOutput = gson.toJson(newOutputInfo);

            // Saves data from filePath into the output.txt
            saveBackup(OUTPUT_PATH, strOutput);
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
                        +" | " + list.getServices().get(i).getUrl());
            }
            //String output = list.getServices().toString();
            //System.out.println("Output > \n" + output);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void help(){
        /*
        -> Outputs all available CLI commands
        */
        // TODO: acho que tiro os predefinidos???

        System.out.println("Available Commands:");
        System.out.println();
        System.out.println(":>poll [--only] onlyArg1,onlyArg2 [--exclude] excludeArg1,excludeArg2");
        System.out.println(":>fetch [--refresh] seconds [--only] onlyArg1,onlyArg2 [--exclude] excludeArg1,excludeArg2");
        System.out.println(":>stop");
        System.out.println(":>history [--only] onlyArg1,onlyArg2");
        System.out.println(":>backup pathToFile [--format] formatType (txt or csv)");
        System.out.println(":>restore pathToFile [--merge]");
        System.out.println(":>services");
        System.out.println(":>help");
        System.out.println(":>status");
        System.out.println(":>quit/exit");
        System.out.println();
        System.out.println("Predefined commands of Spring Shell");
        System.out.println(":>clear");
        System.out.println(":>script");
        System.out.println(":>stacktrace");
        System.out.println();
        System.out.println("poll: Retrieves the status from of all configured services.");
        System.out.println("fetch: Retrieves the status from of all configured services with a given interval. (default timer = 5sec)");
        System.out.println("stop: Stops fetch command");
        System.out.println("history: Outputs all the data from the local storage.");
        System.out.println("backup: Saves the local storage into a given file.");
        System.out.println("restore: Restores data from a file into the local storage.");
        System.out.println("services: Outputs all services defined in the configuration file and their respective endpoint.");
        System.out.println("help: Outputs all available CLI commands.");
        System.out.println("status: Displays data in a table.");
        System.out.println("quit/exit: Ends the program.");
        System.out.println("clear: Clear the shell screen.");
        System.out.println("script: Read and execute commands from a file.");
        System.out.println("stacktrace: Display the full stacktrace of the last error.");
        System.out.println();

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
    private List<Output> getFromServices(String only, String exclude){

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        EndpointList list;
        URL url;
        List<Output> outputJSON = new ArrayList<>();

        try {
            list = gson.fromJson(new FileReader(CONFIG_PATH), EndpointList.class);

            // Fetch info from sites in the config.json
            // ex: poll
            if(only.equals("") && exclude.equals("")) {

                for (int i = 0; i < list.getServices().size(); i++) {
                    url = new URL(list.getServices().get(i).getUrl());
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");

                    Output out = new Output(list.getServices().get(i).getId(),list.getServices().get(i).getName(),
                            list.getServices().get(i).getUrl(),con.getResponseMessage());

                    outputJSON.add(out);
                    System.out.println("Service > [" + list.getServices().get(i).toString() + "] | status > "
                            + con.getResponseMessage());

                }

            }
            else{
                for (int i = 0; i < list.getServices().size(); i++) {

                    url = new URL(list.getServices().get(i).getUrl());
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");

                    if(only.length()>0) {
                        String[] onlyParts=only.split(",");

                        for(int j=0; j<onlyParts.length; j++){
                            if(list.getServices().get(i).getId().equals(onlyParts[j])){
                                Output out = new Output(list.getServices().get(i).getId(),list.getServices().get(i).getName(),
                                        list.getServices().get(i).getUrl(),con.getResponseMessage());
                                outputJSON.add(out);
                                System.out.println("Service > [" + list.getServices().get(i).toString() + "] | status > "
                                        + con.getResponseMessage());
                            }
                        }

                    }
                    else if(exclude.length()>0){
                        String[] excludeParts=exclude.split(",");

                        // Only prints if (e.g.) github is different than both exclude arguments
                        int differenceCounter=0;
                        for(int j=0; j<excludeParts.length; j++){
                            if(!list.getServices().get(i).getId().equals(excludeParts[j])){
                                differenceCounter++;
                            }
                        }
                        if(differenceCounter==excludeParts.length){
                            Output out = new Output(list.getServices().get(i).getId(),list.getServices().get(i).getName(),
                                    list.getServices().get(i).getUrl(),con.getResponseMessage());
                            outputJSON.add(out);
                            System.out.println("Service > [" + list.getServices().get(i).toString() + "] | status > "
                                    + con.getResponseMessage());
                        }
                    }
                    con.disconnect();
                }
            }
            //System.out.println(gson.toJson(outputJSON));

        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (ProtocolException e1) {
            e1.printStackTrace();
        } catch (MalformedURLException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        return outputJSON;
    }

    private void saveToFile(String filePath, List<Output> output) {
        try {
            File f = new File(filePath);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            List<Output> oldOutput = readFromFile(filePath, "");
            BufferedWriter out = new BufferedWriter(new FileWriter(f));

            String str = "";

            if(oldOutput.isEmpty()){
                str = gson.toJson(output);
            }
            else{

                for(int i=0; i<output.size();i++){
                    oldOutput.add(output.get(i));
                }
                str = gson.toJson(oldOutput);
            }
            out.write(str);
            out.close();


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void saveBackup(String filePath, String output){
        try {
            File f = new File(filePath);

            BufferedWriter out = new BufferedWriter(new FileWriter(f));
            out.write(output);
            out.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public String writeAsCSVFile(List<Output> outputs){
        StringBuilder sb = new StringBuilder();
        sb.append("id,");
        sb.append("name,");
        sb.append("url,");
        sb.append("status");

        for(int i=0; i<outputs.size(); i++){
            sb.append("\n");
            sb.append(outputs.get(i).getId()+",");
            sb.append(outputs.get(i).getName()+",");
            sb.append(outputs.get(i).getUrl()+",");
            sb.append(outputs.get(i).getStatus());
        }
        return sb.toString();
    }

    // Shows the info in the output file
    private List<Output> readFromFile(String filePath, String only) {

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        List<Output> readJSON;
        List<Output> outputJSON = new ArrayList<>();

        try {

            // Reads info from file
            String fileExtension = getFileExtension(filePath);
            if(fileExtension.equals("csv")){
                FileReader reader = new FileReader(new File(filePath));
                Scanner scan = new Scanner(reader);

                // Ignores header line
                scan.next();
                Output o = new Output();
                while(scan.hasNext()){
                    // id, name, url, status
                    String[] line = scan.next().split(",");

                    o.setId(line[0]);
                    o.setName(line[1]);
                    o.setUrl(line[2]);
                    o.setStatus(line[3]);

                    outputJSON.add(o);
                }
            }
            else {

                Output[] aux = gson.fromJson(new FileReader(filePath), Output[].class);
                readJSON = Arrays.asList(aux);

                if (only.equals("")) {
                    // Stores every endpoint at json into outputJson
                    for (int i = 0; i < readJSON.size(); i++) {
                        Output output = new Output(readJSON.get(i).getId(), readJSON.get(i).getName(),
                                readJSON.get(i).getUrl(), readJSON.get(i).getStatus());
                        outputJSON.add(output);
                    }
                } else {
                    for (int i = 0; i < readJSON.size(); i++) {
                        if (only.equals(readJSON.get(i).getId())) {
                            Output output = new Output(readJSON.get(i).getId(), readJSON.get(i).getName(),
                                    readJSON.get(i).getUrl(), readJSON.get(i).getStatus());
                            outputJSON.add(output);
                        }
                    }
                }
            }
            //System.out.println("Output from saveToFile >>>>> \n"+ output);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputJSON;
    }

    public static String getFileExtension(String path) {
        if(path!=null){
            String fileName = new File(path).getName();
            int dotPosition = fileName.lastIndexOf('.');
            return (dotPosition == -1) ? "" : fileName.substring(dotPosition + 1);
        }
        return "Invalid fileType.";
    }

    public void quit(){
        System.exit(0);
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        // VER https://www.youtube.com/watch?v=h6nMjjxJWjk&t=

    }
}
