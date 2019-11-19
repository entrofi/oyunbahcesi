#!groovy
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