def CONTAINER_NAME = "human-factors-java"

node {
    stage('SCM Checkout') {
        checkout scm
    }
    stage('Build docker image') {
        sh 'docker build -t human-factors-java --no-cache .'
    }
    stage('Push to registry') {
        sh 'docker tag human-factors-java localhost:5000/human-factors-java'
        sh 'docker push localhost:5000/human-factors-java'
        sh 'docker rmi -f human-factors-java localhost:5000/human-factors-java'
    }
    stage('Deploy') {
        def container_exists = sh ( script: "docker container inspect -f '{{.State.Status}}' ${CONTAINER_NAME}", returnStatus: true )
		if (container_exists == 0) {
            echo "Container ${CONTAINER_NAME} exists already. It will be removed to create it again."
			def container_status = sh ( script: "docker container inspect -f '{{.State.Status}}' ${CONTAINER_NAME}", returnStdout: true )
			container_status = container_status - '\n'
			echo "Container status: ${container_status}"
			if ( container_status == 'running') {
				echo "Container ${CONTAINER_NAME} is running. Stopping it."
				sh "docker stop ${CONTAINER_NAME}"
			}
			sh "docker container rm ${CONTAINER_NAME}"
		}
		sh "docker run -d -p 8081:8081 --name ${CONTAINER_NAME} --restart unless-stopped localhost:5000/human-factors-java:latest"
    }
}
