linklander-ui
=============


Installation of development infrastructure
---------------------------------------------

- Download Eclipse 4.5.1 Java EE and unpack it somewhere (e.g. /opt/eclipse/eclipse-ee-4.5.1)
  - http://www.eclipse.org/downloads/packages/eclipse-ide-java-ee-developers/mars1
- Download Tomee Plus 1.7.2 and unpack it where you want to use it(e.g. /opt/tomee/apache-tomee-plus-1.7.2)
  - http://www.apache.org/dyn/closer.cgi/tomee/tomee-1.7.2/apache-tomee-1.7.2-plus.tar.gz
- Start Eclipse, choose workspace directory (e.g. ~/workspaces/linklander)
- Add Tomee as Server to your eclipse (see chapter 'Advanced': https://openejb.apache.org/tomee-and-eclipse.html)
  - (I used the tomee installation, not the workspace)
- Go to the server configuration, open the launch configuration and add the following to the VM arguments
  - -Dlinklander.base.directory="/home/<user>/.linklander/"
  -  This will tell the linklander application where its base directory is (where to store the database, find config files, ...)

Installation of linklander
------------------------------
- change to your eclipse workspace directory in a console
- 'git clone <linklanderui>'
- In eclipse, import the linklander project as 'existing maven project'
- Run 'mvn clean install' on the linklander project
- Publish the project to your server in eclipse
- Start the server from eclipse
- browse to http://localhost:8080/linklander-ui/linklander-ui/
