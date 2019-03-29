package mbchallenge.cliapp;

import org.apache.catalina.startup.Bootstrap;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class CliAppApplication implements ApplicationRunner {

    public static void main(String[] args) {
        SpringApplication.run(CliAppApplication.class, args);
    }


    @Override
    public void run(ApplicationArguments args) throws Exception {
        Bootstrap.main(args.getSourceArgs());
    }
}
