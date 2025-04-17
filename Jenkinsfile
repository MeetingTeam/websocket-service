def baseRepoUrl = 'https://github.com/MeetingTeam'
def mainBranch = 'main'

def appRepoName = 'websocket-service'
def appRepoUrl = "${baseRepoUrl}/${appRepoName}.git"
def appVersion = "1.0"

def k8SRepoName = 'k8s-repo'
def helmPath = "${k8SRepoName}/application/${appRepoName}"
def helmValueFile = "values.test.yaml"

def dockerhubAccount = 'dockerhub'
def githubAccount = 'github'
def kanikoAccount = 'kaniko'

def sonarCloudOrganization = 'meetingteam'

def trivyReportFile = 'trivy_report.html'

def imageVersion = "${appVersion}-${BUILD_NUMBER}"

pipeline{
         agent {
                    kubernetes {
                              inheritFrom 'springboot'
                    }
          }
          
          environment {
                    DOCKER_REGISTRY = 'registry-1.docker.io'
                    DOCKER_IMAGE_NAME = 'hungtran679/mt_websocket-service'
                    DOCKER_IMAGE = "${DOCKER_REGISTRY}/${DOCKER_IMAGE_NAME}:${imageVersion}"          
          }
          
          stages{
                      stage('Setup credentials for maven'){
                        steps {
                                    container('maven'){
                                                  withCredentials([
                                                            usernamePassword(
                                                                      credentialsId: githubAccount, 
                                                                      passwordVariable: 'GIT_PASS', 
                                                                      usernameVariable: 'GIT_USER'
                                                            )
                                                ]) {
                                                      script {
                                                          def settingsXml = """
                                                              <settings>
                                                                <servers>
                                                                  <server>
                                                                    <id>github</id>
                                                                    <username>${GIT_USER}</username>
                                                                    <password>${GIT_PASS}</password>
                                                                  </server>
                                                                </servers>
                                                              </settings>
                                                            """
                                                          writeFile file: 'settings.xml', text: settingsXml.trim()
                                                          sh 'mv settings.xml /root/.m2/settings.xml'
                                                      }
                                                }                                        
                                        }
                                }
                      }
                      stage('Unit test stage'){
                              steps{
                                        container('maven'){
                                            sh 'mvn clean test'                                     
                                        }
                              }
                    }
                    stage('Build jar file'){
                              steps{
                                        container('maven'){
                                                sh "mvn clean package -DskipTests=true"
                                        }
                              }
                    }
                    stage('Code analysis'){
                              steps{
                                        container('maven'){
                                                  withSonarQubeEnv('SonarCloud') {
                                                            sh "mvn sonar:sonar -Dsonar.organization=${sonarCloudOrganization}"
                                                  }
                                        }
                              }
                    }
                    stage('Quality gate check') {
                              steps {
                                        timeout(time: 5, unit: 'MINUTES') {
                                                  waitForQualityGate(abortPipeline: true)
                                        }
                              }
                    }
                    stage('Build and push docker image'){
                              when{ branch mainBranch }
                              steps{
                                        container('kaniko'){
                                                   withCredentials([
                                                            string(credentialsId: kanikoAccount, variable: 'KANIKO_AUTH')
                                                  ]) {
                                                      script {
                                                          def dockerConfig = """
                                                            {
                                                              "auths": {
                                                                "${DOCKER_REGISTRY}": {
                                                                  "auth": "${KANIKO_AUTH}"
                                                                }
                                                              }
                                                            }
                                                            """
                                                          writeFile file: 'config.json', text: dockerConfig.trim()
                                                          
                                                          sh """
                                                            mv config.json /kaniko/.docker/config.json
                                                            /kaniko/executor \
                                                              --context=. \
                                                              --dockerfile=Dockerfile \
                                                              --destination=${DOCKER_IMAGE}
                                                          """
                                                      }
                                                }
                                        }
                              }
                    }
                    stage('Scan built image'){
                              when{ branch mainBranch }
                              steps{
                                        container('trivy'){
                                                sh """
                                                    wget -O html.tpl https://raw.githubusercontent.com/aquasecurity/trivy/main/contrib/html.tpl
                                                    trivy image --format template --template \"@html.tpl\" -o ${trivyReportFile} \
                                                        --timeout 15m --scanners vuln ${DOCKER_IMAGE}
                                                """
                                        }
                              }
                    }
                    stage('Update k8s repo'){
                              when{ branch mainBranch }
                              steps {
				                                withCredentials([
                                                  usernamePassword(
                                                            credentialsId: githubAccount, 
                                                            passwordVariable: 'GIT_PASS', 
                                                            usernameVariable: 'GIT_USER'
                                                  )
                                        ]) {
                                                  sh """
                                                            git clone https://\${GIT_USER}:\${GIT_PASS}@github.com/MeetingTeam/${k8SRepoName}.git --branch ${mainBranch}
                                                            cd ${helmPath}
                                                            sed -i 's|  tag: .*|  tag: "${imageVersion}"|' ${helmValueFile}

                                                            git config --global user.email "jenkins@gmail.com"
                                                            git config --global user.name "Jenkins"
                                                            git add .
                                                            git commit -m "feat: update application image of helm chart '${appRepoName}' to version ${imageVersion}"
                                                            git push origin ${mainBranch}
                                                  """		
				                              }				
                              }
                    }
          }
          post {
                failure {
                      script {
                          try{
                              emailext(
                                    subject: "Build Failed: ${currentBuild.fullDisplayName}",
                                    body: "The build has failed. Please check the logs for more information.",
                                    to: '$DEFAULT_RECIPIENTS'
                              )
                          } catch (Exception e) {
                                echo "SMTP email configuration is not found or failed: ${e.getMessage()}. Skipping email notification."
                          }
                      }
                }
          }
}