FROM centos:7
ENV LANG=en_US.utf8

RUN yum -y update && \
    yum install -y bzip2 fontconfig tar java-1.8.0-openjdk nmap-ncat psmisc gtk3 git \
      python-setuptools xorg-x11-xauth wget unzip which rh-maven33 \
      xorg-x11-server-Xvfb xfonts-100dpi libXfont GConf2 \
      xorg-x11-fonts-75dpi xfonts-scalable xfonts-cyrillic \
      ipa-gothic-fonts xorg-x11-utils xorg-x11-fonts-Type1 xorg-x11-fonts-misc && \
      yum -y clean all

COPY google-chrome.repo /etc/yum.repos.d/google-chrome.repo
RUN yum install -y xorg-x11-server-Xvfb google-chrome-stable

ENV DISPLAY=:99
ENV FABRIC8_USER_NAME=fabric8

RUN useradd --user-group --create-home --shell /bin/false ${FABRIC8_USER_NAME}

ENV HOME=/home/${FABRIC8_USER_NAME}
ENV WORKSPACE=$HOME/che
RUN mkdir $WORKSPACE

COPY . $WORKSPACE
RUN chown -R ${FABRIC8_USER_NAME}:${FABRIC8_USER_NAME} $HOME/*

USER ${FABRIC8_USER_NAME}
WORKDIR $WORKSPACE/
RUN chmod +rx /home/fabric8/che/docker-entrypoint.sh

VOLUME /dist

ENTRYPOINT ["/home/fabric8/che/docker-entrypoint.sh"]
