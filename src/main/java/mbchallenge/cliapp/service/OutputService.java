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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Stream;


@Service
public class OutputService implements InitializingBean {

    private static final String OUTPUT_PATH = "src/main/resources/output.json";
    private static final String CONFIG_PATH = "src/main/resources/config.json";

    private Timer t = new Timer();
    private boolean run = false;

    public void poll(String only, String exclude) {

        System.out.println();

        // Gets the config.json endpoints, filtered if asked
        List<Output> output = getFromServices(only, exclude);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        // Saves to output.json the endpoints from config.json
        saveToFile(OUTPUT_PATH, output);

    }


    public void fetch(String inputTimer, String only, String exclude){

        System.out.println();
        System.out.println("Write 'stop' to stop the fetch operation.");

        // Receive timer from shell (in seconds) and translate to milliseconds
        long timer = Long.valueOf(inputTimer).longValue() * 1000;
        System.out.println("Timer value = "+(timer/1000)+"s");
        System.out.println();

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                // Gets the config.json endpoints, filtered if asked
                List<Output> output = getFromServices(only, exclude);

                // Saves to output.json the endpoints from config.json
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

        System.out.println();

        // Read the output.json data, filtered if asked
        List<Output> output = readFromFile(OUTPUT_PATH, only);
        for(int i = 0; i<output.size();i++){
            System.out.println("["+output.get(i).getId()+"] "+output.get(i).getTime() + " - " + output.get(i).getStatus());

        }
    }


    public void backup(String path, String format){

        List<Output> outputInfo = new ArrayList<>();

        // Reads data from output.json file
        outputInfo = readFromFile(OUTPUT_PATH,"");

        // Checks for valid formats
        if(format.equals("txt")){
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            // Sends the output.json data as a String to save it into the backup.txt file
            String txtOutput = gson.toJson(outputInfo);
            saveBackup(path+"/backup."+format, txtOutput);
        }
        else if(format.equals("csv")){

            // Writes the output.json data as csv format
            String csvOutput = writeAsCSVFile(outputInfo);
            saveBackup(path+"/backup."+format, csvOutput);
        }
        System.out.println();
        System.out.println("Backup complete.");
    }


    public void restore(String path, boolean merge){

        System.out.println();

        // Checks if format is valid
        if(getFileExtension(path).equals("csv") || getFileExtension(path).equals("txt")) {

            if (!merge) {
                // Reads data from backup file
                List<Output> newOutputInfo = readFromFile(path, "");

                Gson gson = new GsonBuilder().setPrettyPrinting().create();

                // Saves data from file path into the local storage(output.json)
                String strOutput = gson.toJson(newOutputInfo);
                saveBackup(OUTPUT_PATH, strOutput);

                System.out.println("Restored data from " + path + " into local storage.");
            } else {
                mergeFiles(path, OUTPUT_PATH);
                System.out.println("Restored and merged data from " + path + " into local storage.");
            }
        }
        else {
            System.out.println("Can't restore file is invalid");
        }

    }

    public void services(){

        System.out.println();

        Gson gson = new GsonBuilder().create();
        EndpointList list = null;
        try {
            list = gson.fromJson(new FileReader(CONFIG_PATH), EndpointList.class);

            for(int i=0; i<list.getServices().size(); i++){
                System.out.println("Service: "+list.getServices().get(i).getName()
                        +" | " + list.getServices().get(i).getUrl());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void help(){
        /*
        -> Outputs all available CLI commands
        */
        System.out.println();
        System.out.println("Available Commands:");
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
        System.out.println("Predefined commands of Spring Shell:");
        System.out.println(":>clear");
        System.out.println(":>script");
        System.out.println(":>stacktrace");
        System.out.println();
        System.out.println("Description of the commands:");
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
        System.out.println("Not implemented.");

    }











    private void mergeFiles(String fromPath, String destPath){
        try {
            Gson gson = new GsonBuilder().create();

            // backup.txt or backup.csv
            File from = new File(fromPath);
            // output.json
            File dest = new File(destPath);

            List<Output>fromOutputs;

            Output[] fromOutputsArray = gson.fromJson(new FileReader(from), Output[].class);
            fromOutputs = Arrays.asList(fromOutputsArray);

            Output[] destOutputsArray = gson.fromJson(new FileReader(dest), Output[].class);
            List<Output> destOutputs = new ArrayList<>(Arrays.asList(destOutputsArray));

            // Static size so the destination file size value doesn't increment as the method adds Outputs
            long staticDestSize = destOutputs.size();

            for(int i=0; i<fromOutputs.size();i++){
                boolean match = false;
                for(int j=0; j<staticDestSize;j++){
                    if(fromOutputs.get(i).toString().equals(destOutputs.get(j).toString())){
                        match = true;
                        break;
                    }
                }
                if(!match) {
                    destOutputs.add(fromOutputs.get(i));
                }
            }
            String outputToSave = gson.toJson(destOutputs);

            // Saves the merged data into the output.json (destination file)
            saveBackup(destPath, outputToSave);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // Reads the config.json, sends GETs to urls and prints the info
    private List<Output> getFromServices(String only, String exclude){

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        EndpointList list;
        URL url;
        List<Output> outputJSON = new ArrayList<>();

        try {
            list = gson.fromJson(new FileReader(CONFIG_PATH), EndpointList.class);

            // Fetch info from sites in the config.json with no extra commands (only nor exclude)
            if(only.equals("") && exclude.equals("")) {

                for (int i = 0; i < list.getServices().size(); i++) {
                    url = new URL(list.getServices().get(i).getUrl());
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");

                    // Initial time when the request was made
                    String init = calculateInitTime();

                    // Adds into a List<Output>
                    prepareServicesOutput(list, i, init, con, outputJSON);
                    System.out.println("[" + list.getServices().get(i).getId() + "] " +init+ " - " + con.getResponseMessage());

                }

            }
            else{
                for (int i = 0; i < list.getServices().size(); i++) {

                    url = new URL(list.getServices().get(i).getUrl());
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");

                    // Initial time when the request was made
                    String init = calculateInitTime();

                    if(only.length()>0) {
                        // Separates the --only String args
                        String[] onlyParts=only.split(",");

                        for(int j=0; j<onlyParts.length; j++){
                            // Checks if Endpoint from config.json is equal to one of the --only argument
                            if(list.getServices().get(i).getId().equals(onlyParts[j])){

                                // Adds into a List<Output>
                                prepareServicesOutput(list, i, init, con, outputJSON);
                                System.out.println("[" + list.getServices().get(i).getId() + "] " +init+ " - " + con.getResponseMessage());

                            }
                        }
                    }
                    else if(exclude.length()>0){
                        String[] excludeParts=exclude.split(",");

                        // Only adds if (e.g.) github is different than both exclude arguments
                        int differenceCounter=0;
                        for(int j=0; j<excludeParts.length; j++){
                            if(!list.getServices().get(i).getId().equals(excludeParts[j])){
                                differenceCounter++;
                            }
                        }

                        if(differenceCounter==excludeParts.length){
                            prepareServicesOutput(list, i, init, con, outputJSON);
                            System.out.println("[" + list.getServices().get(i).getId() + "] " +init+ " - " + con.getResponseMessage());
                        }
                    }
                    con.disconnect();
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

        return outputJSON;
    }

    // Format the time value to a Day:Month:Year:Hour:Minute:Second format
    private String calculateInitTime(){
        long init = System.currentTimeMillis();
        Date resultdate = new Date(init);
        DateFormat df = new SimpleDateFormat("dd:MM:yyyy:HH:mm:ss");
        String initTime = df.format(resultdate);
        return initTime;
    }

    private void prepareServicesOutput(EndpointList list, int i, String init, HttpURLConnection con, List<Output> outputJSON){

        try {
            Output out = new Output(list.getServices().get(i).getId(),list.getServices().get(i).getName(),
                    list.getServices().get(i).getUrl(),con.getResponseMessage(), init);
            outputJSON.add(out);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // Saves a List<Output> into a file path
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

    // Saves a String into a file (to save in .txt files)
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

    // Shows the info from a file in filePath
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
                                readJSON.get(i).getUrl(), readJSON.get(i).getStatus(), readJSON.get(i).getTime());
                        outputJSON.add(output);
                    }
                } else {
                    // Checks who are equal to --only arguments
                    for (int i = 0; i < readJSON.size(); i++) {
                        if (only.equals(readJSON.get(i).getId())) {
                            Output output = new Output(readJSON.get(i).getId(), readJSON.get(i).getName(),
                                    readJSON.get(i).getUrl(), readJSON.get(i).getStatus(), readJSON.get(i).getTime());
                            outputJSON.add(output);
                        }
                    }
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputJSON;
    }

    // Returns extension of a file (without the dot .) -> eg: txt/csv/json
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
