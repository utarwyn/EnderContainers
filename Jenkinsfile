pipeline {
    agent any
    tools {
        maven 'mvn'
        jdk 'jdk8'
    }
    options {
        timeout(time: 15, unit: 'MINUTES')
        buildDiscarder logRotator(
                numToKeepStr: env.BRANCH_NAME ==~ /next|master/ ? '10' : ''
        )
    }
    stages {
        stage('Build') {
            steps {
                withGradle {
                    sh 'chmod +x gradlew && ./gradlew build --stacktrace'
                }
            }
        }

        stage('Deploy') {
            environment {
                PERSONAL_TOKEN = credentials('maven-repository-token-utarwyn')
            }
            when {
                anyOf { branch 'master'; branch 'next' }
            }
            steps {
                withGradle {
                    sh './gradlew publishToMavenLocal publishApiPublicationToPersonalRepository publishPluginPublicationToPersonalRepository --stacktrace'
                }
            }
        }
    }
    post {
        always {
            archiveArtifacts artifacts: 'api/build/libs/*.jar,plugin/build/libs/EnderContainers-*.jar', fingerprint: true
            junit '**/test-results/**/*.xml'
            cleanWs()
        }
    }
}
