# GamesRuined
Backend to the gamesruined website.

## Prerequisites
* Install [Docker](https://www.docker.com/products/docker-desktop).
Note that **Docker for Windows** requires **Windows Pro Edition** and to have **Hyper-V** enabled. Otherwise, you will
need to install [Docker Toolbox](https://github.com/docker/toolbox/releases).
* Install [docker-compose](https://docs.docker.com/compose/install/).
This generally comes included with both **Docker Desktop** and **Docker Toolbox**.
Test installation via ```docker-compose --version```.
* Install [JDK](https://www.oracle.com/technetwork/java/javase/downloads/jdk13-downloads-5672538.html) version 1.8 or
above.
* If using **Docker Toolbox**, be sure to set the environment variable **DOCKER_IP** to the output of the
command ```docker-machine ip default```, likely **192.168.99.100**.
This **DOCKER_IP** defaults to "localhost", otherwise.
Afterwards, start the program **Docker Quickstart Terminal**.
This creates a Linux VM named **default** using **Virtual Box**, which will handle all docker-related commands used by
gradle.
You may need to rerun **Docker Quickstart Terminal** to start the VM for every restart.

## Running Via Command-Line
First, navigate to **${project_dir}/backend** in the terminal.
If this is the first time running the application, the database will need to be initialized.
This can be done by running the command:
```
./gradlew updateDB
```
For Windows:
```
gradlew updateDB
```
This will:
1) Stop/delete the database container if it exists.
2) Create a new database container and start it.
3) Run all database migrations using **Flyway**.

Once this is done, the application can be run via:
```
./gradlew startApp
```
For Windows:
```
gradlew startApp
```
This will first start the containers in **docker-compose.yml** if they're not already running, then run the application
in redploy mode.
Whenever the application is rebuilt, it will automatically be redeployed.
To rebuild the application, you can run the command:
```
./gradlew build
```
For Windows:
```
gradlew build
```
which both compiles and runs unit tests on your code.

Two java processes will start from running the task **startApp**:
1) The main java process that handles the initial deployment/redeployment.
2) Deployed application.

The deployed application (2) will have its output redirected to the main java process (1).
Killing the main java process via **Ctrl+C** will leave the deployed application orphaned and still running.
You will likely need to kill the deployed application with the following command:
```
./gradlew stopApp
```  
For Windows:
```
gradlew stopApp
```

## Debugging
When in a pinch, it might be useful to set breakpoints in your code in an IDE.
The task **startApp** runs the application in debug mode and hosts a JPDA server on port 5005.
The running application can be debugged in **IntelliJ**/**Android Studio** by typiong **Ctrl+Alt+F5** and selecting
**io.vertx.core.Launcher (5005) from the dropdown.
This can also be triggered by selecting **Run** -> **Attach to Process...**

## Database
The latest version of **Postgresql** is the database of choice.
Whenever the gradle tasks **startApp** or **debugApp** are invoked, the containerized database will be started unless
already running.
Developers need not install Postgresql manually.
These commands also start a helpful **adminer** app hosted on **${DOCKER_IP}:8081** for viewing/manipulating tables in a
browser.
The database is also exposed to **${DOCKER_IP}:5432**, so alternative database clients may be used by developers as
well.

## Database Migrations
SQL scripts are located in **src/main/resources/db.migration**.
This project uses the **Flyway** plugin to handle database migrations.
Whenever you wish to test a new SQL script, you will need to run:
```
./gradlew updateDB
``` 
For Windows:
```
gradlew updateDB
```
as was done when starting the application for the first time.
The application does not need to be restarted for this.
Keep in mind that all data will be deleted in this process as the old database container is discarded and a new one is
spun up followed by the execution of all migrations scripts.

The filename for the migrations scripts should be in the format:
**V${major_no}_${minor_no}__${script_name}.sql**.
It should be noted that there can only be a single migration script per migration.
There cannot be, for example, two files with the names:
* V1_0__foo.sql
* V1_0__bar.sql
as the version string **V1_0**, AKA **1.0** is encountered more than once.

However, these filenames will not cause a conflict:
* V1_0_foo.sql
* V1_1_bar.sql
* V2_0_foobar.sql
as they all have a different version string.

To keep scripts modular, DML and DDL scripts for a particular version should be kept separate for a single major
version.
Here are some examples:
* V1_0__DDL.sql
* V1_1__DML.sql
* V2_0__DDL_add_cookies_table.sql
* V2_1__DDL_add_milk_table.sql
* V2_2__DML_add_cookies_seed_data.sql
* V2_3__DML_add_milk_seed_data.sql

For every deployment to a production system, there will likely only ever need to be a single DDL and DML script pair.
That being said, as this project grows in size, there may need to be further splitting of scripts into multiple files
to keep the script size sane.