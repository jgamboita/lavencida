pipeline {
    agent any
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

               echo "SIN DESPLIEGUE"

            }
        }
    }
    post {
        always{
          echo 'Run some clean steps and test reports'
          
        }
    }
}
