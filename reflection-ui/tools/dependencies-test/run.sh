 #!/bin/bash
 
 mvn dependency:tree
 mvn dependency:purge-local-repository
 mvn -e exec:java -Dexec.mainClass="xy.reflect.ui.ReflectionUI" -Dexec.args="xy.reflect.ui.util.ScheduledExit"
 