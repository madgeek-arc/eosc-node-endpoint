IMAGE_NAME=$(shell mvn help:evaluate -Dexpression=spring-boot.build-image.imageName -q -DforceStdout)
TARGET=$(shell find target -name "*.jar")

.PHONY: build run docker-build docker-push docker-compose

build:
	mvn clean package

run:
	@trap 'exit 0' INT; java -jar $(TARGET)

docker-build:
	mvn -Pnative spring-boot:build-image

docker-push:
	docker image push $(IMAGE_NAME)

docker-compose:
	docker compose -f compose/docker-compose.yml up

default: docker-build docker-push

.DEFAULT_GOAL := default