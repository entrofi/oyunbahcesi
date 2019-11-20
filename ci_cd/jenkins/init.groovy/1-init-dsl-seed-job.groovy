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