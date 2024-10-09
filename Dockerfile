#Adapted from Luis' recommender system
# Pull OpenJDK, it uses Oracle Linux
FROM openjdk:11

# Update the environment and perform apt installations
RUN apt-get update -y \
    && apt-get install -y maven \
    && apt-get clean

# create a directory for app
WORKDIR /data-reconciliation

# Copy project directory into the container at /data-reconciliation
COPY  . /data-reconciliation

# Generate fat jar (uber jar)
RUN mvn package

# Expose port
EXPOSE 8080

# Production FAT jar
CMD java -jar target/data-reconciliation-1.0.0.jar server settings.yml