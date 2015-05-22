# Install

## Java & MySQL

Install Java and MySQL manuaully.

    create database ddld;
    GRANT ALL PRIVILEGES ON ddld.* To 'ddld'@'localhost' IDENTIFIED BY '***********';

The DDLD webapplication will auto-create the tables when they do not exist.

## ElasticSearch & Wildfly

ElasticSearch and Wildfly are part of the `provision.sh` script. ElasticSearch does not need to be configured, `IndexMissingException` warnings can be ignored, they will automatically be resolved as soon as data is put into the ElasticSearch database. For Wildfly, you should run `wildfly/bin/add-user.sh` if you want to enable remote deployment over the management ports (999[90]). Add a `Management User` with username `ddld-admin`, `yes` to all as follows:

	What type of user do you wish to add? 
	 a) Management User (mgmt-users.properties) 
	 b) Application User (application-users.properties)
	(a): a

	Enter the details of the new user to add.
	Using realm 'ManagementRealm' as discovered from the existing property files.
	Username : ddld-admin
	Password requirements are listed below. To modify these restrictions edit the add-user.properties configuration file.
	 - The password must not be one of the following restricted values {root, admin, administrator}
	 - The password must contain at least 8 characters, 1 alphanumeric character(s), 1 digit(s), 1 non-alphanumeric symbol(s)
	 - The password must be different from the username
	Password : 
	Re-enter Password : 
	What groups do you want this user to belong to? (Please enter a comma separated list, or leave blank for none)[  ]: 
	About to add user 'ddld-admin' for realm 'ManagementRealm'
	Is this correct yes/no? yes
	Added user 'ddld-admin' to file '/u00/wildfly/standalone/configuration/mgmt-users.properties'
	Added user 'ddld-admin' to file '/u00/wildfly/domain/configuration/mgmt-users.properties'
	Added user 'ddld-admin' with groups  to file '/u00/wildfly/standalone/configuration/mgmt-groups.properties'
	Added user 'ddld-admin' with groups  to file '/u00/wildfly/domain/configuration/mgmt-groups.properties'
	Is this new user going to be used for one AS process to connect to another AS process? 
	e.g. for a slave host controller connecting to the master or for a Remoting connection for server to server EJB calls.
	yes/no? yes
	To represent the user add the following to the server-identities definition <secret value="***************" />

Once that's done you can use automatic deployment, see `deploy.md`.