

def multiBranchJobs = [
        [
                name : 'microservices-sandbox',
                remote: 'https://github.com/entrofi/microservicesSandbox.git',
                includes: '*'
        ],
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