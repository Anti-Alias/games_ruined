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
To run the application in the command-line, navigate to
**${project_dir}/backend** and type:
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

Two java processes will start from running this:
1) Main java process that handles the initial deployment/redeployment.
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

## Database Migrations
