# Stateless Jenkins - Job as Code Part II
[In the previous section of these examples](3_introduce_scripted_jobs.md), we created a stateless jenkins  container which can be initialised solely by using scripts. In that example, a seed job for jobdsl plugin, which was later on used to create a simple jenkins job, was also implemented.  In our current example, we will extend previous seed job implementation further to create more complicated jobs so that it will be able to poll remote repositories and scan for groovy based jenkins job descriptions. 

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
def jobsScriptFiles = "ci_cd/jenkins/configs/jobdsl/*.groovy"
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
    targets = jobsScriptFiles
}
advancedSeedProject.getBuildersList().add(advancedJobDslBuildStep)

jenkinsInstance.reload()
```
Let's summarize what this script is doing: 
1. It will poll the master branch of the repository 'https://github.com/entrofi/oyunbahcesi.git'
2. Check for groovy scripts under the folder `"ci_cd/jenkins/configs/jobdsl/"` and execute them to create new jenkins jobs. The scripts in this remote repository repository can poll any number of repositories and can define different types of jenkins jobs or views. 

## 2. Implementing Job Descriptions
In this section we are going to implement some example job descriptions in the remote repository. Go to `jenkins/configs/jobdsl` folder, and add a groovy script file called `createJobs.groovy`:

```groovy
//https://jenkinsci.github.io/job-dsl-plugin/#path/multibranchPipelineJob-branchSources
//https://javadoc.jenkins.io/plugin/workflow-multibranch/index.html?org/jenkinsci/plugins/workflow/multibranch/WorkflowBranchProjectFactory.html
def multiBranchJobs = [
        [
                name : 'spring-sandbox',
                remote: 'https://github.com/entrofi/spring.git',
                jenkinsFilePath: 'restassured-asciidoctor/Jenkinsfile',
                includes: '*'
        ]
]

multiBranchJobs.each {
    currentJob ->
        multibranchPipelineJob(currentJob.name) {
            branchSources {
                git {
                    id(UUID.randomUUID().toString())
                    remote(currentJob.remote)
                    includes(currentJob.includes)
                }
            }
            factory {
                workflowBranchProjectFactory {
                    scriptPath(currentJob.jenkinsFilePath ?: 'Jenkinsfile')
                }
            }
            orphanedItemStrategy {
                discardOldItems {
                    numToKeep(100)
                    daysToKeep(10)
                }
            }
        }
}
```
This groovy script uses [jobdsl api](https://jenkinsci.github.io/job-dsl-plugin/#path/multibranchPipelineJob-branchSources) to create multibranch jobs for the remote repositories which are defined in the multipbranchJobs array. The the `factory` section of the implementation checks if the remote repositories have `Jenkinsfile`s and creates the pipelines for each accordingly. 

This snippet demonstrates how to create a multi branch job using jobdsl api; however we are not limited in terms of the variety of the job descriptions. We can define any kind of  Jenkins jobs, including build monitors, list views, free style jobs, simple pipeline jobs etc., using this api. 

## 3. Installing Tools 

Every build jobhas it's own tooling requirements and in order to keep our "stateless CI/CD" goal, we should be able to automate such kind of tool installations. The example projects that we defined in the previous section are java projects with maven build support. Let's showcase how to install these tools via Jenkins initialization scripts: 

The `2-install-tools.groovy` script installs two different versions of JDK and maven for us.

__Java Installation__
```groovy
/**
 * Install jdk
 */
def javaDescriptor = instance.getDescriptor(hudson.model.JDK.class)
def javaInstallations = []
def javaVersions = [
        "jdk8": "jdk-8u102",
        "jdk11": "jdk-11.0.5"
]
//9dHgTtyL@HPvqE@

for (version in javaVersions) {
    def installer = new JDKInstaller(version.value, true)
    def installerProps = new InstallSourceProperty([installer])
    def installation = new JDK(version.key, "", [installerProps])
    //installer.getDescriptor().doPostCredential('username', 'password')
    javaInstallations.push(installation)
}

javaDescriptor.setInstallations(javaInstallations.toArray(new JDK[0]))
javaDescriptor.save()
```

__Maven Installation__

```groovy

/**
 * Install maven: https://stackoverflow.com/questions/55353804/how-to-automate-maven-and-java-jdk8-installation-with-groovy-for-jenkins
 */

mavenName = "maven3"
mavenVersion = "3.6.0"
println("Checking Maven installations...")

// Grab the Maven "task" (which is the plugin handle).
mavenPlugin = Jenkins.instance.getExtensionList(hudson.tasks.Maven.DescriptorImpl.class)[0]

// Check for a matching installation.
maven3Install = mavenPlugin.installations.find {
    install -> install.name.equals(mavenName)
}

// If no match was found, add an installation.
if(maven3Install == null) {
    println("No Maven install found. Adding...")

    newMavenInstall = new hudson.tasks.Maven.MavenInstallation('maven3', null,
            [new hudson.tools.InstallSourceProperty([new hudson.tasks.Maven.MavenInstaller(mavenVersion)])]
    )

    mavenPlugin.installations += newMavenInstall
    mavenPlugin.save()

    println("Maven installation added.")
} else {
    println("Maven installation found. Done.")
}
```

## Adding Jenkinsfile to the target projects
The rest is easy, in section two we have defined some target projects which was [entrofi/spring/restassured-asciidoctor](https://github.com/entrofi/spring/tree/master/restassured-asciidoctor) project for this specific example. Define your project as done in section 2. Add you Jenkinsfile to that project and run your Jenkins instance if everything is working as expected. 


