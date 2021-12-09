pipeline {
    agent any
    tools {
        maven 'mvn'
        jdk 'jdk8'
    }
    options {
        buildDiscarder logRotator(
                numToKeepStr: '10'
        )
    }
    stages {
        stage('Build') {
            steps {
                withMaven {
                    sh 'mvn clean package -U'
                }
            }
        }

        stage('Deploy') {
            when {
                expression { env.BRANCH_NAME == 'master' || env.BRANCH_NAME == 'next' }
            }
            steps {
                withMaven(options: [artifactsPublisher(disabled: true), junitPublisher(disabled: true)]) {
                    sh "mvn -DskipTests deploy " +
                            "-DaltReleaseDeploymentRepository=utarwyn::default::https://repo.utarwyn.fr/releases/ " +
                            "-DaltSnapshotDeploymentRepository=utarwyn::default::https://repo.utarwyn.fr/snapshots/"
                }
            }
        }
    }
    post {
        always {
            cleanWs()
        }
    }
}
