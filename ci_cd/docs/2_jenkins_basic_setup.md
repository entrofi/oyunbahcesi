# Complete basic setup of Jenkins via Code
Recall that, our final goal is to get a stateless CI/CD environment which does not require any user interface interaction for configuration. In this step we are going to alter our containerised Jenkins so that we can get a basic setup of Jenkins without referring to user interface (setup wizard).  

In summary this basic setup will handle the following on behalf of an actual human being: 
1. Creation of the user(s) on jenkins programmatically.
2. Installation of basic plugins to get up-and-running with Jenkins. 

## Creating the initial admin user for Jenkins programmatically
Jenkins provides a set of scripted execution mechanisms to allow automation in delivery processes using certain key Jenkins events. This mechanism is called "[hook scripts](https://wiki.jenkins.io/display/JENKINS/Groovy+Hook+Script)". In this step, we are going to introduce  ["Post-initialization scripts"](https://wiki.jenkins.io/display/JENKINS/Post-initialization+script) from this set to our container. 

In a standard Jenkins setup, "post-initialization" scripts are located at `/usr/share/jenkins/ref/init.groovy.d/`. In order to use this feature in our container, let's add a folder named `init.groovy` to our code base and map the contents of this folder to our container's post-initialization scripts directory: 
> Add the following command to your Dockerfile
>
>`ADD init.groovy/ /usr/share/jenkins/ref/init.groovy.d/`

Next step is to implement the script, which will create the initial admin user for us. Create a groovy script file under `init.groovy` named "0-disable-login.groovy" directory and paste the following code: 

```groovy
import hudson.security.*
import jenkins.model.*

def instance = Jenkins.getInstance()
def username = 'admin_groovy'

def hudsonRealm = new HudsonPrivateSecurityRealm(false)
hudsonRealm.createAccount(username,'123456')
instance.setSecurityRealm(hudsonRealm)

def strategy = new FullControlOnceLoggedInAuthorizationStrategy()
strategy.setAllowAnonymousRead(false)
instance.setAuthorizationStrategy(strategy)
instance.save()
```

> _Note that, post-initialization scripts are executed in alphabetical order by Jenkins. This is why the prefix "0" is used for user creation script._

## Adding initial set of necessary jenkins plugins programmatically
In this step, we need to collect a list of necessary jenkins plugins for our setup. For the purpose of keeping the example simple, you can use recommended plugins in a standard jenkins installation. In order to get this list, run your container created in the previous step and complete the setup via user interface. When the setup is finished, you can either use jenkins scripting interface located at "http://yourhost:port/script" or use the api provided by Jenkins.  

   >_Getting the lis using a script:_

```groovy
Jenkins.instance.pluginManager.plugins.each{
    plugin -> println("${plugin.getShortName()}")
}
```

   >_Getting the list using the api:_
```shell script
curl --user admin_groovy:123456  "http://localhost:7080/pluginManager/api/json?depth=1"
```

After getting the list of plugins, create a file named `plugins` under `configs` directory and paste the shortnames of the plugins to this file. 

   >_Excerpt of the plugins file:_
```text
ace-editor
ant
antisamy-markup-formatter
apache-httpcomponents-client-4-api
authentication-tokens
bouncycastle-api
branch-api
build-timeout
cloudbees-folder
...
```

Now that we have our list of necessary plugins, it's time to install them within our container. Add the following lines to your Dockerfile. 

```dockerfile
COPY configs/plugins /var/jenkins_init_config/plugins
RUN /usr/local/bin/install-plugins.sh < /var/jenkins_init_config/plugins
```

The container can be tested after completing this step. Build your container, run it and check if everything works as expected from the user interface. 


