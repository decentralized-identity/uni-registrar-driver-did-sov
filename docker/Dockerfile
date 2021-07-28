# Dockerfile for universalregistrar/driver-did-sov

FROM maven:3-jdk-11 AS build

# build driver-did-sov

ADD . /opt/driver-did-sov
RUN cd /opt/driver-did-sov && mvn clean install package -N -DskipTests


FROM jetty:9.4-jre11
MAINTAINER Markus Sabadello <markus@danubetech.com>

# install dependencies
USER root

RUN export DEBIAN_FRONTEND=noninteractive && \
    apt-get -y update && \
    apt-get install -y --no-install-recommends software-properties-common gnupg libsodium23 wget && \
    apt-key adv --keyserver keyserver.ubuntu.com --recv-keys 68DB5E88 && \
    add-apt-repository "deb https://repo.sovrin.org/sdk/deb bionic stable" && \
    apt-get -y update && \
    apt-get install -y libindy=1.16.0-bionic && \
    apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

USER 1001
# variables

ENV uniregistrar_driver_did_sov_libIndyPath=
ENV uniregistrar_driver_did_sov_poolConfigs=_;./sovrin/mainnet.txn;staging;./sovrin/stagingnet.txn;builder;./sovrin/buildernet.txn;danube;./sovrin/danube.txn
ENV uniregistrar_driver_did_sov_poolVersions=_;2;staging;2;builder;2;danube;2
ENV uniregistrar_driver_did_sov_walletName=default
ENV uniregistrar_driver_did_sov_trustAnchorSeed=

# copy from build stage

COPY --from=build /opt/driver-did-sov/target/*.war /var/lib/jetty/webapps/ROOT.war

# done

EXPOSE 9080
CMD java -Djetty.http.port=9080 -jar /usr/local/jetty/start.jar
