FROM ubuntu:latest

LABEL authors="alexdeturris"

# Install dependencies required for Fly CLI
RUN apt-get update && apt-get install -y wget curl zip && \
    curl -L https://fly.io/install.sh | sh

# Add Fly CLI to PATH
ENV PATH="/root/.fly/bin:$PATH"

# Set the working directory
WORKDIR /app

# Copy application files
COPY . .

ENTRYPOINT ["java",     "-cp",     "/app",     "org.example.dnsservice.DnsServiceApplication"]

LABEL authors="alexdeturris"