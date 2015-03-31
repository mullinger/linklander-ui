linklander-ui
=============

UI for Link Lander


Vaadin Frontend Implementation

Vaadin Eclipse Plugin for Luna
https://vaadin.com/eclipse

Note: Use Luna 4.4.0, Vaadin seems to have a Bug with Luna 4.4 SR1
@see https://bugs.eclipse.org/bugs/show_bug.cgi?id=445122

Build: mvn clean install<br>
Run on Jetty: mvn jetty:run

Installation
--------------

- Eclipse EE im System entpacken, starten, workspace anlegen
- Tomee Plus 1.7.1 irgendwo im System entpacken (DL: http://www.apache.org/dyn/closer.cgi/tomee/tomee-1.7.1/apache-tomee-1.7.1-plus.tar.gz)
- Tomee als Server integrieren (see advanced: https://openejb.apache.org/tomee-and-eclipse.html)
- linklander-ui Git Code auschecken in neuen Ordner
- Projekt als Maven Projekt in Eclipse importieren
- linklander-ui: mvn install
- linklander-ui: publish auf dem Tomee (via eclipse: run as: run on server)
- Eclipse: Auf Projekt im Package Explorer: refresh
- Browser: http://localhost:8080/linklander-ui/