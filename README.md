# CIRCEExtractors


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
			<version>1.0.7</version>
		</dependency>
	</dependencies>

#Run with Maven
	
	start mvn exec:java -Dexec.mainClass="com.hoffnungland.jTunneling.App" -Dlog4j.configurationFile=src/main/resources/log4j2.xml