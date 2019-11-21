GRADLE_DOCKER_IMAGE=gradle:4.3

build:
	docker run --rm -v ${CURDIR}:/home/gradle/project -w /home/gradle/project $(GRADLE_DOCKER_IMAGE) gradle build

package: build
	docker build -t scorekeep-api .

run-local: package
	docker run -d -v ~/.aws/:/root/.aws/:ro --net=host -e AWS_REGION=$(AWS_REGION) -e NOTIFICATION_TOPIC=arn:aws:sns:$(AWS_REGION):$(ACCOUNT_ID):scorekeep-notifications scorekeep-api

clean: build
	find -d build | xargs rm -fd
