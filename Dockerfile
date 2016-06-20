FROM maven:3-jdk-8

RUN apt-get update && apt-get install -y --no-install-recommends build-essential valgrind check pkg-config python3.4  && rm -rf /var/lib/apt/lists/*

RUN useradd -g users user && mkdir -p /home/user && chown -R user:users /home/user
#RUN chown -R user:users /app
WORKDIR /app

RUN chown -R user:users /app
USER user

CMD ["/bin/bash"]
