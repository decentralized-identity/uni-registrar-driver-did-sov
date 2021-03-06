# Dockerfile for universalregistrar/driver-did-sov

FROM ubuntu:18.04
MAINTAINER Markus Sabadello <markus@danubetech.com>

RUN apt-get -y update && \
    apt-get install -y --no-install-recommends openjdk-11-jdk-headless maven git && \
    apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

ENV JAVA_HOME /usr/lib/jvm/java-11-openjdk-amd64/
ENV PATH $JAVA_HOME/bin:$PATH

# install dependencies

RUN apt-get -y update && \
    apt-get install -y --no-install-recommends software-properties-common gnupg libsodium23 wget && \
    apt-key adv --keyserver keyserver.ubuntu.com --recv-keys 68DB5E88 && \
    add-apt-repository "deb https://repo.sovrin.org/sdk/deb xenial stable" && \
    apt-get install -y libindy=1.14.0 && \
    apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

# build driver-did-sov

ADD . /opt/driver-did-sov
RUN cd /opt/driver-did-sov && mvn clean install package -N -DskipTests

# variables

ENV uniregistrar_driver_did_sov_libIndyPath=
ENV uniregistrar_driver_did_sov_poolConfigs=_;./sovrin/mainnet.txn;staging;./sovrin/stagingnet.txn;builder;./sovrin/buildernet.txn;danube;./sovrin/danube.txn
ENV uniregistrar_driver_did_sov_poolVersions=_;2;staging;2;builder;2;danube;2
ENV uniregistrar_driver_did_sov_walletName=default
ENV uniregistrar_driver_did_sov_trustAnchorSeed=

# done

EXPOSE 9080

RUN chmod a+rx /opt/driver-did-sov/docker/run-driver-did-sov.sh
CMD "/opt/driver-did-sov/docker/run-driver-did-sov.sh"
