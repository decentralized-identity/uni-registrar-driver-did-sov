# Dockerfile for universalregistrar/driver-did-sov

FROM maven:3-jdk-11 AS build
MAINTAINER Markus Sabadello <markus@danubetech.com>

# build driver-did-sov

ADD . /opt/driver-did-sov
RUN cd /opt/driver-did-sov && mvn clean install package -P war -N -DskipTests

FROM jetty:9.4-jre11-slim
MAINTAINER Markus Sabadello <markus@danubetech.com>

# install dependencies

USER root

RUN export DEBIAN_FRONTEND=noninteractive && \
    apt-get -y update && \
    apt-get install -y --no-install-recommends software-properties-common gnupg && \
    apt-key adv --keyserver keyserver.ubuntu.com --recv-keys 68DB5E88 && \
    add-apt-repository "deb https://repo.sovrin.org/sdk/deb bionic stable" && \
    apt-get -y update && \
    apt-get install -y libindy=1.16.0-bionic && \
    apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

USER jetty

# variables

ENV uniregistrar_driver_did_sov_libIndyPath=
ENV uniregistrar_driver_did_sov_poolConfigs=_;./sovrin/_.txn;test;./sovrin/test.txn;builder;./sovrin/builder.txn;danube;./sovrin/danube.txn;idunion;./sovrin/idunion.txn;idunion:test;./sovrin/idunion-test.txn;indicio;./sovrin/indicio.txn;indicio:test;./sovrin/indicio-test.txn;indicio:demo;./sovrin/indicio-demo.txn;nxd;./sovrin/nxd.txn;findy:test;./sovrin/findy-test.txn;bcovrin;./sovrin/bcovrin.txn;bcovrin:test;./sovrin/bcovrin-test.txn;bcovrin:dev;./sovrin/bcovrin-dev.txn;candy;./sovrin/candy.txn;candy:test;./sovrin/candy-test.txn;candy:dev;./sovrin/candy-dev.txn
ENV uniregistrar_driver_did_sov_poolVersions=_;2;test;2;builder;2;danube;2;idunion;2;idunion:test;2;indicio;2;indicio:test;2;indicio:demo;2;nxd;2;findy:test;2;bcovrin;2;bcovrin:test;2;bcovrin:dev;2;candy;2;candy:test;2;candy:dev;2
ENV uniregistrar_driver_did_sov_walletNames=_;w1;test;w2;builder;w3;danube;w4;idunion;w5;idunion:test;w6;indicio;w7;indicio:test;w8;indicio:demo;w9;nxd;w11;findy:test;w12;bcovrin;w13;bcovrin:test;w14;bcovrin:dev;w15;candy;w16;candy:test;w17;candy:dev;w18
ENV uniregistrar_driver_did_sov_submitterDidSeeds=_;_;test;_;builder;_;danube;_;idunion;_;idunion:test;_;indicio;_;indicio:test;_;indicio:demo;_;nxd;_;findy:test;_;bcovrin;_;bcovrin:test;_;bcovrin:dev;candy;_;candy:test;_;candy:dev;_

# copy from build stage

COPY --from=build --chown=jetty /opt/driver-did-sov/target/*.war /var/lib/jetty/webapps/ROOT.war
COPY --from=build --chown=jetty /opt/driver-did-sov/sovrin/ /var/lib/jetty/sovrin/

# done

EXPOSE 9080
CMD java -Djetty.http.port=9080 -jar /usr/local/jetty/start.jar
