
pipeline {
  agent none
  stages {
	  stage('Test') {
	    agent {
    		docker {
    		    image 'maven:3-jdk-11'
    		    args '-v $HOME/.m2:/root/.m2 --network docker-ci_default'
    		}
	    }
	    steps {
        sh 'mvn clean test sonar:sonar -Dsonar.host.url=http://sonarqube:9000/sonarqube -Dsonar.login=$SONAR_TOKEN'
	    }
	  }

  	stage('Build'){
  	  agent {
  		  docker {
  		    image 'maven:3-jdk-11'
  		    args '-v $HOME/.m2:/root/.m2 --network docker-ci_default'
  		  }
  	  }
  	  steps {
  		  sh 'mvn clean package'
  	  }
  
  	  post {
  		  failure {
  		    slackSend color: "danger", message: ":darth_maul: Build fail! :darth_maul:\nJob name: ${env.JOB_NAME}, Build number: ${env.BUILD_NUMBER}\nGit Author: ${env.CHANGE_AUTHOR}, Branch: ${env.GIT_BRANCH}, ${env.GIT_URL}\n"
  		  }
  		  success {
  		    script { 
            if(currentBuild.getPreviousBuild() &&
  			       currentBuild.getPreviousBuild().getResult().toString() != 'SUCCESS') {
  				    slackSend color: "good", message: ":baby-yoda: This is the way! :baby-yoda: \nJob name: ${env.JOB_NAME}, Build number: ${env.BUILD_NUMBER}\nGit Author: ${env.CHANGE_AUTHOR}, Branch: ${env.GIT_BRANCH}, ${env.GIT_URL}\n"
  			    }
  		    }
  		  }
  	  }
  	}
  }
}
