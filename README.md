## 사전준비
- JDK 17
- GRADLE 7.2+
- IDEA
- [docker](https://docs.docker.com/desktop/mac/install/)
- docker-compose 설치 확인

## local에서 실행
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

## docker없이 실행(cloud)




