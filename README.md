## 사전준비
- 필수 : JDK 17
- 필수 : GRADLE 7.2+
- 필수 : IDEA
- 선택 : [docker](https://docs.docker.com/desktop/mac/install/)
- 선택 : docker-compose 설치 확인

## 실행
```bash
git clone https://github.com/chk386/webflux2021
cd webflux2021
gradle clean assemble
gradle bootRun -Dspring.profiles.active=cloud
```
## 로컬에서 kafka, mysql, mongodb, redis 띄우고 실행하는 법
1. /docker/docker-compose up
2. 몽고 - db, 계정 생성
    ```bash
    docker exec -it mongo /bin/sh
    mongo -u nhn -p nhn
    use webflux
    db.createUser({user: "webflux", pwd: "webflux", roles: ["readWrite"]})
    ```
3. mysql 스키마 생성
   1. r2dbc:mysql://localhost:3306/webflux (webflux/webflux) 접속
   2. [schema-mysql.sql](/src/main/resources/schema-mysql.sql) 실행
4. gradle clean build
5. gradle bootRun

## image로 실행 (설정하기 싫다면..)
```bash
docker run -e spring.profiles.active=cloud -p 8080:8080 -m=1G chk386/webflux
```
## dockerizing 참고
```bash
gradle bootBuildImage --imageName=chk386/webflux:latest # container 만들기

# image upload to docker hub
docker login # docker hub 계정 필요
docker tag chk386/webflux:latest chk386/webflux:latest # docker hub repository로 변경필요
docker push chk386/webflux:latest

docker run -e spring.profiles.active=local -p 8080:8080 -m=2G chk386/webflux # 실행
```

