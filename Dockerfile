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

# Default command to keep the container running
ENTRYPOINT ["top", "-b"]
LABEL authors="alexdeturris"