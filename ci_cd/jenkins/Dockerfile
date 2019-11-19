FROM jenkins/jenkins:lts


USER jenkins
ADD init.groovy/ /usr/share/jenkins/ref/init.groovy.d/
COPY configs/plugins /var/jenkins_init_config/plugins
RUN /usr/local/bin/install-plugins.sh < /var/jenkins_init_config/plugins

