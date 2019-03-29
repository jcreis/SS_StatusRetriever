## _Code Challenge for Mercedes-Benz.io_



To make this project I used the framework Spring Shell, 
which is an extension of the Spring Framework that provides 
an interactive shell that allows the program to receive custom 
made commands while using a Spring based project. It can be 
extended to make an API capable of making HTTP requests via shell.

It's kind of a recent tool, so it's still quiet limited, but I
though it would be a nice choice to deepen my knowledge in a 
framework I've been using a lot lately - Spring. Even so, I was
still able to make the whole commands work as shown in the 
Challenge description file, but some with a few adjustments due
to some limitations from the framework, but nothing special.

As the language chosen, the program it's written in Java, as 
expected due to the usage of any Spring framework.



## To run the program:

1- Must have:
- java-8 (1.8.0_201)
- Apache Maven (latest patch eg: version 3.5.2)

2- Open command prompt

3- Go to root directory _/something/something_else/**proj**_ 
where the project is saved locally

4- Run the command:

> /proj$ _mvn -DskipTests=true clean install && java -jar target/cli-app-0.0.1-SNAPSHOT.jar_

or simply:

> /proj$ _mvn spring-boot:run_ 

and wait for the _shell:>_ print to appear, where you can now use commands

5- Use the command **help**, so you know what commands are available,
what is the exact command and, finally, what each command does exactly.

It will show something like this:

>Available Commands:

>:>poll [--only] onlyArg1,onlyArg2 [--exclude] excludeArg1,excludeArg2

>:>(...)

>poll: Retrieves the status from of all configured services.

Where the commands inside brackets (eg: [--only]) are optional 
commands (or flags) that receives arguments as it is shown in the display 
(in the **poll** case, receives a String where the commas 
separate the different arguments)

- the flag [--only] filters what is displayed into just the given Endpoint(s)
send as arguments (eg: :> poll --only bitbucket,slack) will display only 
the information regarding these two endpoints;

- the flag [--exclude] excludes the information of the given endpoints
from the display shown;
 
- the flag [--refresh] (from the **fetch** command) sets the timer
to the value given (in seconds) as argument; 

- the flag [--format] (from the **backup** command) indicates the
format that the backup file will have (either txt or csv);

- the flag [--merge] (from the **restore** command) is a boolean
that will merge the backup file to the output file, or not, 
depending if it's used or not in the **restore** command.

## **Special attention to:**

-> in the **backup** command, the argument passed as pathToFile
must refer to the directory where the backup file will be saved
**(with no '/' at the end)**
>_(eg: /home/user/desktop/backup_folder)_ 

-> still in the **backup** command, the format given must not have
the dot '.' 
>_(eg: :> (...) --format csv)_

-> in the **restore** command, the argument passed as pathToFile
must refer to the file itself 
>_(eg: /home/user/desktop/backup_folder/backup.csv)_

## Myself

    João Carlos Cristo Reis - jcreiswork@gmail.com
    Faculdade de Ciências e Tecnologia da Universidade Nova de Lisboa (FCT-UNL)
    Mestrado Integrado em Engenharia Informática
    5th year student


##########################################################
Don't forget to provide a README.md file with:
Your name, e-mail address, college name, degree and attendance year;
Descrive your solution and

relevant design decisions.
