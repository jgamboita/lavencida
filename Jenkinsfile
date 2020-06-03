pipeline {
    agent any
    stages {
        
       stage('Send Email') {
            steps {
            node ('master'){
                echo 'Send Email'
            }
        }
        }
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
                        
                        
               echo "SIN DESPLIEGUE"}
                   


             }
            }
        }
    }
    post {
         always { 
            echo 'I will always say Hello!'
        }
        aborted {
            echo 'I was aborted'
        }
        failure {
           echo 'falló'
        }
    }
}
