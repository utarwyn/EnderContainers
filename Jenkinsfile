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
            steps {
                withMaven(options: [artifactsPublisher(disabled: true)]) {
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
