# Scorekeep
Scorekeep is a RESTful web API implemented in Java that uses Spring to provide an HTTP interface for creating and managing game sessions and users. This project includes the Scorekeep API and a front-end web app that consumes it. The front end and API can run on the same server and domain or separately, with the API running in Elastic Beanstalk and the front end served statically by a CDN.

This branch shows the use of Spring, Angular, nginx, the [AWS SDK for Java](http://aws.amazon.com/sdkforjava), [Amazon DynamoDB](http://aws.amazon.com/dynamodb), Gradle, and [AWS ECS Fargate](http://aws.amazon.com/ecs) features that enable you to:

- Run both components in the same [Amazon ECS](http://aws.amazon.com/ecs) task definition behind an [Amazon Application Load Balancer](https://aws.amazon.com/elasticloadbalancing/)
- Create required DynamoDB and [Amazon SNS](http://aws.amazon.com/sns) resources through Cloudformation
- Publishes container logs to [Amazon Cloudwatch Logs](https://aws.amazon.com/cloudwatch)

**Sections**
- [Prerequisites](#prerequisites)
- [Repository Layout](#repository-layout)
- [Cloudformation Setup](#cloudformation-setup)
- [Building the Java application](#building-the-java-application)
- [How it works](#how-it-works)
- [Running the project locally](#running-the-project-locally)
- [Contributing](#contributing)

# Prerequisites
Install the following tools to create Docker images, upload them to ECR, and register task definitions with ECS.
- Docker
- AWS CLI v1.14.0a
- ECS CLI 2.0+
- AWS user with permission for IAM, DynamoDB, SNS, ECS, CloudWatch Logs, and ECR

# Repository Layout
The project contains two independent applications:

- An HTML and JavaScript front end in Angular 1.5 to be ran with Nginx
- A Java backend that uses Spring to provide a public API

The backend and frontend are both built using `docker` and `make`. Docker images are published to Amazon ECR.

| Directory | Contents                                        | Build           | Clean         |
|-----------|-------------------------------------------------|-----------------|---------------|
| `/scorekeep-api`       | Contains the Java Backend (aka `scorekeep-api`) | `make build && make package` | `make clean`   |
| `/scorekeep-frontend` | Contains the Angular+Nginx frontend |  `make build`  |  N/A         |
| `/cloudformation` | Contains the Cloudformation template for creating the dependent resources for the app (i.e. DynamoDB, SNS, CWL) | `make stack` | `make clean` |

# Cloudformation setup

The pre-requisite resources can be setup using Cloudformation. 

The Cloudformation template requires no paramaters and can be ran by executing `make stack` from the directory. It will use the `AWS_REGION` configured in `aws.env` in the root of the package, and the default credentials from the AWS CLI. IAM permissions are needed for the Cloudformation stack to run successfully.

# Building the Java application

The Java application is built using the gradle Docker container so it does not rely on your local Java or Gradle setup. The output of the build process appears in the `build/` folder of the project. After you build the application you will want to package it into a docker container so it can be executed. The docker container packaging takes the JAR produced from the build step and adds it to a Java base image. It then configures the environment, ports, and entry point. The docker can be ran locally with valid AWS credentials, or ran on ECS.

# Deploying the application

*To deploy the containers to your AWS Account*

1. Setup the Cloudformation stack to create the prerequisite resources by executing `make stack` in the `cloudformation/` folder
2. Build your API container `make build` in the `scorekeep-api` folder
3. In the root folder, run `archer init` and follow the wizard. Name the project scorekeep, environment test, app: scorekeep-api.
4. In the root folder, run `archer app deploy`. Go to the TG console, and fix the health check to use path as `/api/rules` and timeout as `60` and interval as `100`.

5. Build and Publish your Frontend container to the ECR repository created by Cloudformation executing `make publish` in the `scorekeep-frontend/` folder
4. Populate your Task Definition with the correct region and account id using the `generate-task-definition` script in the `task-definition` folder
5. Register your Task Definition to ECS with `aws ecs register-task-definition --cli-input-json file://scorekeep-task-definition.json`
6. Launch your Service or Task using the AWS CLI, ECS CLI, or AWS Management Console

# Configuring notifications
The API uses SNS to send a notification email when a game ends. To enable e-mail notifications, to an email address to your Task Definition in environment variable **NOTIFICATION_EMAIL**. To enable other notifications add the subscription to the topic through the SNS console.

# How it works

## Backend
The API runs at paths under /api that provide access to user, session, game, state, and move resources stored as JSON documents in DynamoDB. The API is RESTful, so you can create resources by sending HTTP POST requests to the resource path, for example /api/session. See the [test script](https://github.com/awslabs/eb-java-scorekeep/blob/fargate/bin/test-api.sh) for example requests with cURL.

The Cloudformation template creates a DynamoDB table for each resource type.

## Front end
The front end is an Angular 1.5 web app that uses `$resource` objects to perform CRUD operations on resources defined by the API. Users first encounter the [main view](https://github.com/awslabs/eb-java-scorekeep/blob/fargate/scorekeep-frontend/public/main.html) and [controller](https://github.com/awslabs/eb-java-scorekeep/blob/fargate/scorekeep-frontend/public/app/mainController.js) and progress through session and game views at routes that include the IDs of resources that the user creates.

The front end is served statically by an Nginx container. The nginx.conf file in the source code sets up Nginx to serve the frontend html pages from root, and forward requests starting with /api to the API backend running on port 5000.
