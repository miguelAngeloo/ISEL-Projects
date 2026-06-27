FROM eclipse-temurin:22
WORKDIR /usr/app
COPY ./static-content ./static-content
COPY ./build/libs ./libs
CMD ["java", "-jar", "./libs/LS.jar", "--db"]