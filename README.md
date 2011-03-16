Remote Sensor Control
=====================

The Remote Sensor Control (RSC) is a fork of the device drivers from the [testbed-runtime][] project. It add new features like:
   * Concurrent access to connected devices
   * Remote control of devices via the Internet
   * Centralized metadata management
   * Easy to use console applications for device programming and using
	
The project was developed by students from the Insitute for Telemactics (University of L�beck).

What do I need?
---------------
   * Git
   * JDK >= 1.6
   * Maven 2.2 or 3.0

All library dependencies are downloaded by Maven.

Git Workflow
------------

The following commands show how to clone the RSC repository and how to make first changes.

    $ git clone git@github.com:itm/rsc.git
    $ cd rsc
    $ (edit files)
    $ git add (files)
    $ git commit -a -m "Explain what I changed"
    $ git status
    $ git push origin master

Build and Start the RSC with Maven
----------------------------------

On the command-line go to the Remote Sensor Control directory. Perform a clean build on the project to make sure, that all Maven
artifacts are installed in your local Maven repository (~/.m2/repository). If you are running Maven for the first time,
this will take a while as Maven downloads all project dependencies from the internet.

    $ cd rsc
    $ mvn clean install

More Documentation
==================
Take a look at our [wiki][].


[wiki]:https://github.com/itm/rsc/wiki
[testbed-runtime]:https://github.com/itm/testbed-runtime