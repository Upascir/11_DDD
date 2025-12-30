# Use the official Microsoft Java development container as base
FROM mcr.microsoft.com/devcontainers/java:1-17-bullseye

# Install additional tools
RUN apt-get update && export DEBIAN_FRONTEND=noninteractive \
    && apt-get -y install --no-install-recommends \
        postgresql-client \
        curl \
        wget \
        unzip \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# Install Node.js for frontend development
RUN curl -fsSL https://deb.nodesource.com/setup_18.x | bash - \
    && apt-get install -y nodejs

# Set up Maven (already included in the base image, but ensure it's configured)
ENV MAVEN_HOME=/usr/share/maven
ENV PATH=$PATH:$MAVEN_HOME/bin

# Create workspace directory
WORKDIR /workspace

# Copy Maven wrapper files
COPY mvnw* ./
COPY .mvn .mvn/

# Make Maven wrapper executable
RUN chmod +x mvnw

# Set the default user
USER vscode