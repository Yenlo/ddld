#!/bin/bash
#
# this script installs elasticsearch and wildfly ( jboss application server 8 )
#
# requirements
#     java ( sudo zypper --non-interactive install java-1_7_0-openjdk-devel )
#     mysql 5+ ( with ddld db )
#
# put the startup script in place and make it autorun on startup:
#     cp startup.sh /etc/init.d/ddld
#     chown +x /etc/init.d/ddld
# after installation, place the java keystore file in wildfly/standalone/configuration/
#
# run as sudo:

PRGHOME=/u00
PRGUSER=pgayret

# elasticsearch
cd $PRGHOME
wget https://download.elasticsearch.org/elasticsearch/elasticsearch/elasticsearch-0.90.9.tar.gz
tar -zxf elasticsearch-0.90.9.tar.gz
rm elasticsearch-0.90.9.tar.gz
mv elasticsearch-0.90.9 elasticsearch
./elasticsearch/bin/plugin -install mobz/elasticsearch-head
chown -R $PRGUSER elasticsearch
sudo sh ./elasticsearch/bin/elasticsearch > /dev/null &

# wildfly
cd $PRGHOME
wget http://download.jboss.org/wildfly/8.0.0.CR1/wildfly-8.0.0.CR1.tar.gz
tar -zxf wildfly-8.0.0.CR1.tar.gz
rm wildfly-8.0.0.CR1.tar.gz
mv wildfly-8.0.0.CR1 wildfly

# configuration of ports and allowed ips on wildfly itself
cp ./wildfly/standalone/configuration/standalone.xml ./wildfly/standalone/configuration/standalone.xml.bak
# management console (port 999[90]): firewalls should never open this port to internet
sed -i 's|<inet-address value="${jboss.bind.address.management:127.0.0.1}"/>|<any-ipv4-address/>|g' ./wildfly/standalone/configuration/standalone.xml
# thes http(s) ports should be open to internet
sed -i 's|<inet-address value="${jboss.bind.address:127.0.0.1}"/>|<any-ipv4-address/>|g' ./wildfly/standalone/configuration/standalone.xml
sed -i 's|jboss.http.port:8080|jboss.http.port:80|g' ./wildfly/standalone/configuration/standalone.xml
sed -i 's|jboss.https.port:8443|jboss.https.port:443|g' ./wildfly/standalone/configuration/standalone.xml

# ssl configuration on wildfly itself, this is configured to use the wso2carbon.jks
sed -i 's|<security-realms>|<security-realms>\
    <security-realm name="UndertowRealm">\
        <server-identities>  \
            <ssl>\
                <keystore path="wso2carbon.jks" relative-to="jboss.server.config.dir" keystore-password="wso2carbon" alias="wso2carbon" key-password="wso2carbon" />\
            </ssl>\
        </server-identities>\
    </security-realm>\
|g' ./wildfly/standalone/configuration/standalone.xml

sed -i 's|<server name="default-server">|<server name="default-server">\
    <https-listener name="https" socket-binding="https" security-realm="UndertowRealm" />\
|g' ./wildfly/standalone/configuration/standalone.xml

mkdir -p /var/log/ddld/

chown -R $PRGUSER /var/log/ddld/
chown -R $PRGUSER wildfly

# the reason we use the wso2carbon.jks is because it is a keystore for which we know everything, for anything non-development server you must never use the default jks
# you can make jks files with $JAVA_HOME/jre/bin/keytool
# now, this is where you place the keystore file, probably with "cp wso2/products/wso2esb/repository/resources/security/wso2carbon.jks wildfly/standalone/configuration/"
# and then run wildfly with "sudo sh wildfly/bin/standalone.sh > /dev/null &"
