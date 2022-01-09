mvn package -DskipTests

cp  target/file-server.jar docker/api/

cd docker || exit

docker-compose up --build -d

rm -r api/file-server.jar
