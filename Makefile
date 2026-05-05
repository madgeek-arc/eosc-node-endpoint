IMAGE_NAME=$(shell ./mvnw help:evaluate -Dexpression=spring-boot.build-image.imageName -q -DforceStdout)
TARGET=$(shell find target -name "*.jar")
HOST_UID=$(shell id -u)
HOST_GID=$(shell id -g)

.PHONY: build run docker-build docker-push docker-compose docker-compose-down

build:
	./mvnw clean package

run:
	@trap 'exit 0' INT; java -jar $(TARGET)

docker-build:
	./mvnw clean -Pnative spring-boot:build-image

docker-push:
	docker image push $(IMAGE_NAME)

docker-compose:
	IMAGE_NAME=$(IMAGE_NAME) HOST_UID=$(HOST_UID) HOST_GID=$(HOST_GID) docker compose -f compose/docker-compose.yml up

docker-compose-down:
	IMAGE_NAME=$(IMAGE_NAME) HOST_UID=$(HOST_UID) HOST_GID=$(HOST_GID) docker compose -f compose/docker-compose.yml down

default: docker-build docker-push

.DEFAULT_GOAL := default
