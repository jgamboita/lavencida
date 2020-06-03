pipeline {
    agent { label 'jenkins-did-slave' }
    stages {
        stage('Prepare') {
            steps {
                sh 'chmod +x ./mvnw'
            }
        }
        stage('Compile') {
            steps {
                withMaven(
                    mavenSettingsConfig: 'MavenJenkinsSettings') {
                    sh './mvnw clean package -DskipTests'
                 }
            }
        }
        stage('Test') {
            steps {
                withMaven(
                    mavenSettingsConfig: 'MavenJenkinsSettings') {
                    sh './mvnw verify'
                }
            }
        }
        stage('SonarQube analysis') {
            when {
                anyOf {
                    branch 'develop'; branch 'master'; branch 'feature/*'
                }
            }
            steps {
                withSonarQubeEnv('conexia-sonar') {
                      sh './mvnw sonar:sonar'
                }
            }
        }
        stage('Deploy') {
            when {
                anyOf {
                    branch 'develop'
                }
            }
            steps {
                withMaven(
                    mavenSettingsConfig: 'MavenJenkinsSettings') {
                        withCredentials([usernamePassword(credentialsId: 'emssa-dev-usrpass', usernameVariable: 'USRNM', passwordVariable: 'USRPASS')]){
                        sh './mvnw clean install -Pdeploy -Dusername.deploy=${USRNM} -Dpassword.deploy=${USRPASS} \
                        -Dhostname.deploy=192.168.2.31 -Dport.deploy=9990  -Dcas.server=https://desarrollo-emssanar.conexia.com.co:8443/cas \
                        -Dcas.server.verifier=https://desarrollo-emssanar.conexia.com.co:8443/cas -Dhost.verifier=192.168.2.31 \
                        -Dprincipal.server=https://desarrollo-emssanar.conexia.com.co:8443 -Dserver-web-service-negociacion=https://desarrollo-emssanar.conexia.com.co:8443'}
                    }
            }
        }
    }
    post {
        always{
          echo 'Run some clean steps and test reports'
          junit "**/**/**/target/surefire-reports/*.xml"
        }
    }
}
