FROM centos:centos7
ENV SHELL /bin/bash
MAINTAINER yuanter


# set timezone
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/${TZ} /etc/localtime && \
    echo ${TZ} > /etc/timezone

RUN set -eux

ENV JAVA_HOME /usr/java/openjdk-8
ENV JRE_HOME=$JAVA_HOME/jre
ENV JAVA_PATH=$JAVA_HOME/bin:$JRE_HOME/bin
ENV CLASSPATH=.:$JAVA_HOME/lib:$JRE_HOME/lib:$CLASSPATH
ENV PATH=$PATH:$JAVA_HOME/bin

# Default to UTF-8 file.encoding
ENV LANG en_US.UTF-8

RUN arch="$(uname -m)"&& echo "$(uname -m)"; \
	mkdir -p "$JAVA_HOME";

ADD arm/openjdk-8 /arm/openjdk-8
ADD amd/openjdk-8 /amd/openjdk-8

RUN  arch="$(uname -m)"&& echo "$(uname -m)";\
	 case "$arch" in \
		'x86_64') \
#			tar -zxvf ~/OpenJDK8U-jdk_x64_linux_8u322b06.tar.gz -C $PWD/$arch/ \
			mv /amd/openjdk-8 /usr/java \
			;; \
		'aarch64') \
#			tar -zxvf ~/OpenJDK8U-jdk_aarch64_linux_8u322b06.tar.gz -C $PWD/$arch/ \
			mv /arm/openjdk-8 /usr/java \
			;; \
		'armv7l') \
#			tar -zxvf ~/OpenJDK8U-jdk_aarch64_linux_8u322b06.tar.gz -C $PWD/$arch/ \
			mv /arm/openjdk-8 /usr/java \
			;; \
		*) echo >&2 "error: unsupported architecture: '$arch'"; exit 1 ;; \
	esac; \
	echo "export JAVA_HOME=$JAVA_HOME"  >>  /etc/profile \
	&& echo "export JRE_HOME=\$JAVA_HOME/jre"  >>  /etc/profile \
	&& echo "export JAVA_PATH=\$JAVA_HOME/bin:\$JRE_HOME/bin"  >>  /etc/profile \
	&& echo "export CLASSPATH=.:\$JAVA_HOME/lib:\$JRE_HOME/lib:\$CLASSPATH"  >>  /etc/profile \
	&& echo "export PATH=\$PATH:\$JAVA_HOME/bin"  >>  /etc/profile \
	&& source /etc/profile

ADD demo-latest.jar demo.jar
ADD application.yml application.yml
EXPOSE 8080
ENTRYPOINT ["nohup","java","-server","-Xms64m","-Xmx128m","-Djava.security.egd=file:/dev/./urandom","-jar","-Dfile.encoding=UTF-8","demo.jar","&"]