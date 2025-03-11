def CONTAINER_NAME = "react"

node {
	stage('Checkout') {
			checkout scm
			def container_exists = sh ( script: "docker container inspect -f '{{.State.Status}}' ${CONTAINER_NAME}", returnStatus: true )
		}
		stage('Environment') {
			sh 'git --version'
			sh 'docker -v'
			sh 'printenv'
		}
		stage('Push to registry'){
				sh 'docker build -t react-app --no-cache .'
				sh 'docker tag react-app localhost:5000/react-app'
				sh 'docker push localhost:5000/react-app'
				sh 'docker rmi -f react-app localhost:5000/react-app'
		}
		stage('Deploy'){
			def container_exists = sh ( script: "docker container inspect -f '{{.State.Status}}' ${CONTAINER_NAME}", returnStatus: true )
			if (container_exists == 0) {
				def container_status = sh ( script: "docker container inspect -f '{{.State.Status}}' ${CONTAINER_NAME}", returnStdout: true )
				container_status = container_status - '\n'
				echo "Container status: ${container_status}"
				if ( container_status == 'running') {
					echo "Container ${CONTAINER_NAME} is already running. Stopping and removing container to start it again."
					sh "docker stop ${CONTAINER_NAME}"
				}
				sh "docker container rm ${CONTAINER_NAME}"
			}
			sh "docker run -d -p 3000:3000 --name ${CONTAINER_NAME} --restart unless-stopped localhost:5000/react-app:latest"
		}
}
