# Deployment

Before you deploy make sure you've fully completed the installation.

## Using Maven

When you have the source project and the maven build tool installed you can run the following command to rebuild and deploy.

	mvn clean package -Pdev

## Using the CLI

If you only have the `ddld.war` file, don't have the source project and maven installed, you can deploy via the CLI.

Put the `ddld.war` file on the server in your home folder or wherever. Once that's done you can SSH to the server, check for the existence of the `ddld.war` file in your home directory and then open the wildfly CLI with:

	sudo su - # you need to be either sudo or have credentials of a management user
	/u00/wildfly/bin/jboss-cli.sh

Then when in the cli:

	connect
	deploy /home/you/ddld.war

OR for redeployments / updates:

	connect
	undeploy ddld.war
	deploy /where-you-put-it/ddld.war
