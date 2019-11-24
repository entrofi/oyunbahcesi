# Stateless Jenkins - Job as Code Part II
[In the previous section of these examples](3_introduce_scripted_jobs.md), we created a stateless jenkins  container which can be initialised solely by using scripts. In that example, a seed job for jobdsl plugin, which was later on used to create a simple jenkins job, was also implemented.  In our current example, we will extend previous seed job implementation further to create more complicated jobs by polling external repositories.

## Summary of the steps: 
1. Extend `jenkins/init.groovy/1-init-dsl-seed-job.groovy` script to enable polling of remote repositories, so that it can check these repositories for new job descriptions. 
2. Implement some job description scripts in the repository (folder), which will be polled by the seed job initialisation script. 
3. Automate installation of the tools, which will be needed by the builds (e.g. maven, jdk, etc.)
4. Add Jenkinsfile(s) to the target projects which are referenced in the job definition script(s). 
5. Run and test the example. 

## 1. Extend jobDsl script
The jobDsl script `1-init-dsl-seed.job.groovy` in the previos example is implemented to create a simple _"Hello world"_ job, by passing  a dsl job script to the  seed jobs `scriptText` attribute. As we stated earlier, in this step we are going to extend this script to define more advanced jobs via groovy scripts. The new seed job will poll a remote repository and scan the contents for groovy based job descriptions. To achieve this goal, we need to define a scm and pass this to the freestyle job (the seed job):
```groovy
def jobScriptsRepository = "https://github.com/entrofi/oyunbahcesi.git"
def branch = "*/master"
def jobsScriptFile = "ci_cd/jenkins/configs/jobdsl/*.groovy"
def scm = new GitSCM(GitSCM.createRepoList(jobScriptsRepository, "jenkins"), [new BranchSpec(branch)], false, [], null, null, [])

def advancedJobName = "AdvancedSeedJob"
def advancedSeedProject = new FreeStyleProject(jenkinsInstance, advancedJobName)
advancedSeedProject.scm = scm
advancedSeedProject.save()
``` 
The rest of the jobdsl initialisation script defines a jobDsl executor (`ExecuteDslScripts`)instance and passes `jobScriptFile` variable to the `targets` attribute of this instance: 

```groovy
def advancedJobDslBuildStep = new ExecuteDslScripts()
advancedJobDslBuildStep.with {
    ignoreExisting = true
    lookupStrategy = LookupStrategy.JENKINS_ROOT
    removedJobAction = RemovedJobAction.DELETE
    removedViewAction = RemovedViewAction.DELETE
    scriptText = ""
    useScriptText = false
    //create jobs using the scripts in a remote repository
    targets = jobsScriptFile
}
advancedSeedProject.getBuildersList().add(advancedJobDslBuildStep)

jenkinsInstance.reload()
```
The final form of the script is as follows: 

```groovy
import hudson.plugins.git.*

import jenkins.*
import jenkins.model.*
import hudson.*
import javaposse.jobdsl.plugin.*
import hudson.model.*

/**
 * Api documentation for job dsl https://jenkins.io/doc/pipeline/steps/job-dsl/
 * Create a simple seed job and execute a job dsl script to create a job
 */
def jenkinsInstance = Jenkins.getInstance()

/**
 * Advanced job creation
 */

def jobScriptsRepository = "https://github.com/entrofi/oyunbahcesi.git"
def branch = "*/master"
def jobsScriptFile = "ci_cd/jenkins/configs/jobdsl/*.groovy"
def scm = new GitSCM(GitSCM.createRepoList(jobScriptsRepository, "jenkins"), [new BranchSpec(branch)], false, [], null, null, [])

def advancedJobName = "AdvancedSeedJob"
// jobDsl plugin uses a free style project in order to seed jobs, let's initialise it.
def advancedSeedProject = new FreeStyleProject(jenkinsInstance, advancedJobName)
advancedSeedProject.scm = scm
advancedSeedProject.save()

def advancedJobDslBuildStep = new ExecuteDslScripts()
advancedJobDslBuildStep.with {
    ignoreExisting = true
    lookupStrategy = LookupStrategy.JENKINS_ROOT
    removedJobAction = RemovedJobAction.DELETE
    removedViewAction = RemovedViewAction.DELETE
    scriptText = ""
    useScriptText = false
    //create jobs using the scripts in a remote repository
    targets = jobsScriptFile
}
advancedSeedProject.getBuildersList().add(advancedJobDslBuildStep)

jenkinsInstance.reload()
```
Let's summarize what this script will be doing: 
1. It will poll the master branch of the repository 'https://github.com/entrofi/oyunbahcesi.git'
2. Check for groovy scripts under the folder `"ci_cd/jenkins/configs/jobdsl/"` and execute them to create new jenkins jobs. The scripts in this repository can poll any number of repositories and can define different types of jenkins jobs or views. 

