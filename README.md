# JTunneling
# Create a new project
	mvn archetype:generate -Dfilter="org.apache.maven.archetypes:maven-archetype-quickstart" -DgroupId="com.hoffnungland" -DartifactId=JTunneling -Dpackage="com.hoffnungland.jTunneling" -Dversion="0.0.1-SNAPSHOT"
# Build settings
## Add prerequisites

	<prerequisites>
		<maven>3.0.5</maven>
	</prerequisites>

Update to java 1.8<br>
	
	<properties>
		<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
		<java.source.version>1.8</java.source.version>
		<java.target.version>1.8</java.target.version>
	</properties>

# Relationship
## Add dependencies
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

# Run with Maven
	
	start mvn exec:java -Dexec.mainClass="com.hoffnungland.jTunneling.App" -Dlog4j.configurationFile=src/main/resources/log4j2.xml

# Create Jar with dependencies

## Configure the pom.xml

	<plugin>
		<artifactId>maven-assembly-plugin</artifactId>
		<configuration>
			<descriptorRefs>
				<descriptorRef>jar-with-dependencies</descriptorRef>
			</descriptorRefs>
			<appendAssemblyId>false</appendAssemblyId>
			<finalName>${project.artifactId}</finalName>
			<archive>
				<manifest>
					<mainClass>com.hoffnungland.jTunneling.App</mainClass>
				</manifest>
			</archive>
		</configuration>
	</plugin>

## Execute the maven assembly single

	mvn assembly:single
	
# Tunnel Properties

* **host**=<target host>
* **user**=<target user>
* **passwordType**=[encrypt: encrypt the value and add the new entry to keyStore |encrypted: keystore entry password |oneTimePassword: password is not stored, you must enter it every time you try to connect] 
* **password**=<target password when passwordType is encrypt or keystore entry password when passwordType is encrypted>
* **lport**=<comma separated value of local ports>
* **rport**=<comma separated value of remote host ports>
* **rhost**=<comma separated value of remote host name or ip>

# KeyStore
For more information, please refer to official documentation [keytool - Oracle Help Center](https://docs.oracle.com/javase/8/docs/technotes/tools/windows/keytool.html)

## Create KeyStore and Private Key (with user prompt)
keytool -genkeypair -keysize 2048 -keyalg RSA -alias <name.surname> -keystore ~/OneDrive/JTunnelingKStore.jks

	Enter keystore password: <your keystore password>
	Re-enter new password: <your keystore password previously set>
	What is your first and last name?
		[Unknown]:  <name.surname>
	What is the name of your organizational unit?
		[Unknown]:  NA
	What is the name of your organization?
		[Unknown]:  NA
	What is the name of your City or Locality?
		[Unknown]:  <your city or locality>
	What is the name of your State or Province?
		[Unknown]:  <state or province of your city>
	What is the two-letter country code for this unit?
		[Unknown]:  <[ISO 3166-1 alpha-2](https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2)>
	Is CN=<name.surname>, OU=NA, O=NA, L=<city or locality>, ST=<state or province>, C=<country code> correct?
		[no]:  yes

	Enter key password for <name.surname>
	        (RETURN if same as keystore password):
	Re-enter new password:

## Create KeyStore and Private Key (Silent mode)
_Different store and key passwords not supported for PKCS12 KeyStores_

	keytool -genkeypair -keysize 2048 -keyalg RSA -alias <name.surname> -keystore ~/OneDrive/JTunnelingKStore.jks -dname "CN=<name.surname>, OU=NA, O=NA, L=<city or locality>, ST=<state or province>, C=<country code>" -validity 365 -storepass <your keystore password>