pipelineJob('job-name') {
  definition {
    cps {
      script('''
        pipeline {
            agent any
                stages {        
                    stage('Clone repository') {
                        steps {                
                            git url: 'https://github.com/Rauler7/angular-app'
                        }
                    }

                    stage('Download dependencies') {
                        steps {
                        bat """
                            npm install
                        """
                        }
                    }

                    stage('SonarQube Static Code Test') {
                        steps {
                        bat """
                            sonar-scanner.bat -D"sonar.projectKey=angular-app" -D"sonar.sources=." -D"sonar.host.url=http://localhost:9000" -D"sonar.login=737a72b4847fd4eac8c6ad68dca36f9617ce6ea5"
                            """
                        }
                    }
                    
                    stage('Build & Deploy') {
                        steps {
                        bat """
                            call ng build
                            call powershell Copy-Item "C:/Windows/System32/config/systemprofile/AppData/Local/Jenkins.jenkins/workspace/test/dist/angular-app" -Destination "C:/prd" -Recurse -force;
                        """
                        }
                    }

                    stage('Automated test with Cypress') {
                        steps {
                        bat """
                            call npm run cy:run
                        """
                        }
                    }

                    stage('Notifications') {
                        steps {
                            echo 'Sending mail notifications...'
                        }
                    }
                }
                post {
                    always {
                        emailext body: 'Notification about a recent Job execution', recipientProviders: [[$class: 'DevelopersRecipientProvider'], [$class: 'RequesterRecipientProvider']], subject: 'Notifications of Status Jenkins Job'
                    }
                }
            }
        }
      '''.stripIndent())
      sandbox()     
    }
  }
}