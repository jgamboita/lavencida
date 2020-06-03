pipeline {
    agent any
    stages {       
       
        stage('Prepare') {
            steps {
                sh 'chmod +x ./mvnw'
            }
        }
        stage('package') {
            steps {
                withMaven(
                    mavenSettingsConfig: 'apache-maven-3.6.3') {
                    sh './mvn clean package -DskipTests'
                 }
            }
        }
        
        stage('Compile') {
            steps {
                withMaven(
                    mavenSettingsConfig: 'apache-maven-3.6.3') {
                    sh './mvn clean compile'
                 }
            }
        }
        stage('Test') {
            steps {
                withMaven(
                    mavenSettingsConfig: 'apache-maven-3.6.3') {
                    sh './mvn verify'
                }
            }
        }
        
        stage('Deploy') {
            steps {
                withMaven(
                    mavenSettingsConfig: 'apache-maven-3.6.3') {
                    sh './mvn deploy'
                }
            }
        }
        
    }
}
