/**
 * Script to install build related tools like maven, jdk etc.
 */
import jenkins.model.*
import hudson.model.*
import hudson.tools.*
import hudson.tasks.Maven.MavenInstallation;
import hudson.util.DescribableList;

def instance = Jenkins.getInstance()

/**
 * Install jdk
 */
def javaDescriptor = instance.getDescriptor(hudson.model.JDK.class)
def javaInstallations = []
def javaVersions = [
        "jdk8": "jdk-8u102",
        "jdk11": "jdk-11.0.5"
]


for (version in javaVersions) {
    def installer = new JDKInstaller(version.value, true)
    def installerProps = new InstallSourceProperty([installer])
    def installation = new JDK(version.key, "", [installerProps])
    //installer.getDescriptor().doPostCredential('username', 'password')
    javaInstallations.push(installation)
}

javaDescriptor.setInstallations(javaInstallations.toArray(new JDK[0]))
javaDescriptor.save()


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