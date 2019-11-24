# First Steps to "job as code"
[Checkout this example from here](https://github.com/entrofi/oyunbahcesi/tree/jenkins_as_code_0.1.0)

We are going to introduce necessary plugins to achieve the goal of programmatic job descriptions in this step and configure them accordingly via programmatic means. 

The first plugin that we are going to introcude is [jobDsl plugin](https://github.com/jenkinsci/job-dsl-plugin). The most common job creation mechanism in Jenkins is that users usually  create jobs by cloning/copying an existing project.  As experienced by most of us, when the number of jobs grows in Jenkins or the job description gets complicated, use of user interface oriented method becomes more tedious. This is where jobDsl plugin comes in handy. It provides the programmatic bridge to create jobs using scripts or configuration files.

In order to serve it's purpose,  jobDsl plugin uses a free style jenkins job, is called **"Seed Job"**. This job is a standard free style job to which you add a "Process Job DSLs" step. This step uses the configured DSL and generates jobs configured in it. (For further information please visit the [official tutorial](https://github.com/jenkinsci/job-dsl-plugin/wiki/Tutorial---Using-the-Jenkins-Job-DSL))

That's enough talking. Let's move to the practical part. 


## 1. Adding jobDsl support and creating a job using dslScript
We introduced automatic plugin installation support to our stateless jenkins instance in the previous step(s). Now we can use our plugin installation file to add our new plugin. 

Open `configs/plugins` file and add `job-dsl` as a new line to this file

The next step is to configure the seed job by programmatic means. Since we already have the support for ["Post-initialization scripts"](2_jenkins_basic_setup.md) in our container, we can add a groovy script to create our **"Seed Job"**. Let's add a new groovy script file named `1-init-dsl-seed-job.groovy` to our init scripts (init.groovy/1-dsl-seed-job.groovy). _Please remember that the reason a numbered prefix is used in the script file names is that these scripts are executed by Jenkins in alphabetical order._ 

As mentioned earlier, the primary goal of this script is to create the seed job for the plugin. Additionally, we will also add a simple job dsl to the seed job using the _"**Use the provided DSL script** (`useScriptText=true`)"_ option. This job, which is going to be created by our seed job, will only print "Hello World!" to the stdout. 

>Note that, there are more advanced ways to create jobs using the seed job, but simple job definition serves the purpose of this step. 

The example job dsl is as follows: 

```groovy
job('example') {
  steps {
    shell('echo Hello World!')
  }
}
```  

The content of the `init.groovy/1-dsl-seed-job.groovy` script will be as follows: 

```groovy
import hudson.plugins.git.*

import jenkins.*
import jenkins.model.*
import hudson.*
import javaposse.jobdsl.plugin.*
import hudson.model.*

/**
 * Api documentation for job dsl https://jenkins.io/doc/pipeline/steps/job-dsl/
 */
def jenkinsInstance = Jenkins.getInstance()
def jobName = "SeedJob"
// jobDsl plugin uses a free style project in order to seed jobs, let's initialise it.
def seedProdject = new FreeStyleProject(jenkinsInstance, jobName);
seedProdject.save()

def jobDslBuildStep = new ExecuteDslScripts()
jobDslBuildStep.with {
    ignoreExisting = true
    lookupStrategy = LookupStrategy.JENKINS_ROOT
    removedJobAction = RemovedJobAction.DELETE
    removedViewAction = RemovedViewAction.DELETE
    useScriptText = true
    scriptText = "job('example') {\n" +
            "  steps {\n" +
            "    shell('echo Hello World!')\n" +
            "  }\n" +
            "}"
}
seedProdject.getBuildersList().add(jobDslBuildStep)
jenkinsInstance.reload()
```
### Test what has been done so far
1. run the docker-compose up --build command against your docker-compose file. 
2. Browse to the Jenkins instance http://localhost:7080 and login.
3. Run the **SeedJob** job. 
    >The job will fail first time you run it. It's because the jobDsl plugin [limits execution of the scripts without admin approval](https://github.com/jenkinsci/job-dsl-plugin/wiki/Script-Security). In order to approve the execution, go to configuration page of the job and click save.
4. Check if a job named **example** is created. 
5. Run the example job and check the output of the job using the console output link on the interface. 

