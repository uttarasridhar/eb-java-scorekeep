build:
	docker build -t scorekeep-frontend .

run-local: build
	docker run -d --net=host scorekeep-frontend
