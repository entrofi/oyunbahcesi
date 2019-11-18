# Different (Possibly Stateless) Jenkins Use-cases
This initial form tackles only with a containerised version of Jenkins. The goal is to head towards a stateless form of a jenkins setup. In this initial version, we will only have a dockerized Jenkins instance, the state of which will be mapped to static folders in the host machine.

The advantage of this setup is to have the state, which includes history, configurations, etc,  preserved in the host machine and it's not that different from having Jenkins installed on a physical/virtual machine. You still need to setup the installation via Jenkins initial setup interface and configure your jobs, pipelines etc manually.  Nevertheless, it's the initial step towards the "... as a code" approach. 

## Container Configuration
Port mappings:
* Jenkins' default http port `8080` is mapped to `7080` on the host machine. 
* Jenkins' default jnlp port `50000` is mapped to `50005` on the host machine.  

Checkout docker-compose.yml for additional configuration. 


## How to run the sample
* Go to `ci_cd` root directory and run `docker-compose up`
> `jenkins_home` directory is mapped to the directory `./jenkins/jenkins_home` on the host machine. As soon as your container gets up-and-running, you will be able to see jenkins related files in this directory. For such kind of setups Jenkins creates a temporary admin password in the file `jenkins/jenkins_home/secrets/initialAdminPassword`. 
* Navivgate to `http://localhost:7080` and use the temporary password defined in `jenkins/jenkins_home/secrets/initialAdminPassword` to start configuring your Jenkins instance. 
* Follow the steps defined in the setup interface of Jenkins. 


