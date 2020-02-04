# GamesRuined
Backend to the gamesruined website.

## Prerequisites
* Install [Docker](https://www.docker.com/products/docker-desktop).
Note that **Docker for Windows** requires **Windows Pro Edition** and to
have **Hyper-V** enabled. Otherwise, you will need to install
[Docker Toolbox](https://github.com/docker/toolbox/releases).
* Install [docker-compose](https://docs.docker.com/compose/install/).
This generally comes included with both **Docker Desktop** and
**Docker Toolbox**. Test installation via ```docker-compose --version```.
* Install [JDK](https://www.oracle.com/technetwork/java/javase/downloads/jdk13-downloads-5672538.html) version 1.8 or above.
* If using **Docker Toolbox**, start the program
**Docker Quickstart Terminal** before attempting to run any gradle
commands.
This creates a Linux VM named **default** using **Virtual Box**, which
will handle all docker-related commands.
You may need to rerun this application to start the VM for every
restart.
Also note that applications normally exposed to **127.0.0.1** (When
using Docker Desktop) will likely be exposed to **192.168.99.100**.
This can be confirmed by running the command
```docker-machine ip default```.
The ip of exposed containers will be referred to as **docker_ip**
henceforth.

## Running Via Command-Line
First, navigate to ${project_dir}/backend in the terminal.

If this is the first time running the application, the database will
need to be initialized.
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
3) Run database migration using **Flyway**.

Once this is done, the application can be run via:
```
./gradlew startApp
```
For Windows:
```
gradlew startApp
```
This will first start the containers in **docker-compose.yml**, then
run the vertx app in redploy mode.
Whenever the application is rebuilt, it will automatically be
redeployed.
To rebuild the application, you can run the command:
```
./gradlew build
```
For Windows:
```
gradlew build
```
which both compiles and runs unit tests on your code.

Two java processes will start from running this:
1) The main java process that handles the initial deployment/redeployment.
2) Deployed application.

The deployed application (2) will have its output redirected to the main
java process (1).
Killing the main java process via **Ctrl+C** will leave the deployed
application orphaned and still running. You will likely need to kill the
deployed application with the following command:
```
./gradlew stopApp
```  
For Windows:
```
gradlew stopApp
```

## Debugging
When in a pinch, it might be useful to set breakpoints in your code in
an IDE.
Because the task **startApp** runs the deployed application (2) in a
separate process, it is not possible to debug it without manually
attaching to it.
This is not ideal.
Luckily, the task **debugApp** does not start the application in
redeploy mode and, as such, only starts a single process.
Running the task **debugApp** in debug mode will allow for breakpoints
to be hit.
Be sure to stop the application before doing so.

## Database
The latest version of **Postgresql** is the database of choice. Whenever
the gradle tasks **startApp** or **debugApp** are invoked, the
containerized database will be started unless already running.
Developers need not install Postgresql manually.
These commands also start a helpful **adminer** app hosted on
**${docker_ip}:8081** for viewing/manipulating tables in a browser.
The database is also exposed to **${docker_ip}:5432**, so alternative
database clients may be used by developers as well.

# Database Migrations
SQL scripts are located in **src/main/resources/db.migration**.
This application uses **Flyway** plugin to handle database migrations.
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
Keep in mind that all data will be deleted in this process as the old
database container is discarded and a new one is spun up followed by
the execution of all migrations scripts.
The filename for the migrations scripts should be in the format:
**V${migration_no}__${script_name}.sql**
It should be noted that there can only be a single migration script per
migration. There cannot be, for example two files with the names:
* V1__foo.sql
* V1__bar.sql

However, this is perfectly fine:
* V1__foo.sql
* V2__bar.sql