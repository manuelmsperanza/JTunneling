# JTunneling
#Create a new project
	mvn archetype:generate -Dfilter="org.apache.maven.archetypes:maven-archetype-quickstart" -DgroupId="com.hoffnungland" -DartifactId=JTunneling -Dpackage="com.hoffnungland.jTunneling" -Dversion="0.0.1-SNAPSHOT"
#Build settings
##Add prerequisites

	<prerequisites>
		<maven>3.0.5</maven>
	</prerequisites>

Update to java 1.8<br>
	
	<properties>
		<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
		<java.source.version>1.8</java.source.version>
		<java.target.version>1.8</java.target.version>
	</properties>

#Relationship
##Add dependencies
Add log4j<br>

	<dependencies>
		<dependency>
			<groupId>com.hoffnungland</groupId>
			<artifactId>Log4j</artifactId>
			<version>1.0.8</version>
		</dependency>
		<!-- https://mvnrepository.com/artifact/com.jcraft/jsch -->
		<dependency>
		    <groupId>com.jcraft</groupId>
		    <artifactId>jsch</artifactId>
		    <version>0.1.55</version>
		</dependency>
		<!-- https://mvnrepository.com/artifact/org.apache.commons/commons-lang3 -->
		<dependency>
			<groupId>org.apache.commons</groupId>
			<artifactId>commons-lang3</artifactId>
			<version>3.12.0</version>
		</dependency>
	</dependencies>

#Run with Maven
	
	start mvn exec:java -Dexec.mainClass="com.hoffnungland.jTunneling.App" -Dlog4j.configurationFile=src/main/resources/log4j2.xml
	
#Tunnel Properties

host=<target host>
user=<target user>
passwordType=[encrypt: encrypt the value and add the new entry to KeyStore |encrypted: password is stored and encrypted |oneTimePassword: password is not stored, you must enter it every time] 
password=<target password when passwordType is encrypt or encrypted>
lport=<comma separated value of local ports>
rport=<comma separated value of remote host ports>
rhost=<comma separated value of remote host name or ip>
