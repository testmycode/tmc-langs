FROM java:8u91


ENV MAVEN_VERSION 3.3.9

RUN mkdir -p /usr/share/maven \
  && curl -fsSL http://apache.osuosl.org/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz \
    | tar -xzC /usr/share/maven --strip-components=1 \
  && ln -s /usr/share/maven/bin/mvn /usr/bin/mvn

ENV MAVEN_HOME /usr/share/maven

RUN useradd -g users user && mkdir -p /home/user && chown -R user:users /home/user
RUN apt-get update && apt-get install -y ruby build-essential valgrind check pkg-config python3.4  && rm -rf /var/lib/apt/lists/*


#RUN chown -R user:users /app
#WORKDIR app

USER user

CMD ["/bin/bash"]
