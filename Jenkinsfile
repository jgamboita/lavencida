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
               echo "PARA DESPLEGAR"
        }
    }
    post {
        always{
          echo 'Run some clean steps and test reports'
          junit "**/**/**/target/surefire-reports/*.xml"
        }
    }
}
