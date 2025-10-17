def call (Closure... contexts) {
    pipeline {
        agent any
        environment {
            CREDENTIALS_ID = 'MACLEOHSECOURS'
            URL_GIT = 'https://github.com/BlitzcriegGGH/jenkins-du-prof.git'
        }

        post {
            always {
                echo 'Pipeline finished.'
            }
            success {
                echo 'Pipeline succeeded!'
            }
            failure {
                echo 'Pipeline failed.'
            }
        }

        stages {
            stage('Clone repository') {
                steps {
                    checkout([
                        $class: 'GitSCM',
                        branches: [[name: '*/main']],
                        userRemoteConfigs: [[
                            credentialsId: "${CREDENTIALS_ID}",
                            url: "${URL_GIT}"
                        ]]
                    ])
                    echo 'Cloning repository...'
                    sh "printenv"
                }
            }

            stage('Readfile and print its content') {
                steps {
                    echo 'Reading file... and Ready to print its content'
                    script {
                        def content = readFile 'servers.csv'
                        def lines = content.split(/\r?\n/)
                        lines.each { line ->
                            echo line
                        }
                    }
                }
            }

            stage('Clean') {
                steps {
                    echo 'Maven clean...'
                    sh "chmod '+x' mvnw"
                    sh "./mvnw clean"
                }
            }

            stage('Test') {
                steps {
                    echo 'Maven test...'
                    sh 'cat servers.csv'
                    sh './mvnw test'
                }
            }

            stage('Install') {
                steps {
                    echo 'Maven install...'
                    sh './mvnw install'
                }
            }

            stage('Deploy') {
                steps {
                    echo 'Deploying...'
                    sshPublisher(publishers: [
                        sshPublisherDesc(
                            configName: 'SERVEUR',
                            transfers: [
                                sshTransfer(
                                    sourceFiles: 'target/*.jar',
                                    removePrefix: '',
                                    remoteDirectory: '/tmp',
                                    execCommand: 'ls -la /tmp',
                                    execTimeout: 120000
                                )
                            ],
                            usePromotionTimestamp: false,
                            verbose: true
                        )
                    ])
                }
            }
        }
    }
}