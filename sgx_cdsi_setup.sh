#!/bin/bash

# Comprehensive SGX and CDSI Deployment Preparation Script
# Supports Ubuntu 20.04 LTS and 22.04 LTS

set -e

# Color codes
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

# Logging functions
log_success() {
    echo -e "${GREEN}[✓] $1${NC}"
}

log_warning() {
    echo -e "${YELLOW}[!] $1${NC}"
}

log_error() {
    echo -e "${RED}[✗] $1${NC}"
}

log_info() {
    echo -e "[*] $1"
}

# Check Ubuntu version
check_ubuntu_version() {
    log_info "Checking Ubuntu Version..."
    if [ -f /etc/os-release ]; then
        . /etc/os-release
        if [[ "$ID" == "ubuntu" && ("$VERSION_ID" == "20.04" || "$VERSION_ID" == "22.04") ]]; then
            log_success "Supported Ubuntu version: $VERSION_ID"
        else
            log_error "Unsupported Ubuntu version: $VERSION_ID. Requires 20.04 or 22.04."
            exit 1
        fi
    else
        log_error "Cannot determine Ubuntu version"
        exit 1
    fi
}

# Update and install base dependencies
prepare_system() {
    log_info "Updating system and installing base dependencies..."
    sudo apt-get update
    sudo apt-get install -y \
        wget \
        curl \
        software-properties-common \
        gnupg2 \
        build-essential \
        libssl-dev
    log_success "System updated and base dependencies installed"
}

# Install Intel SGX repositories
install_sgx_repositories() {
    log_info "Installing Intel SGX repositories..."
    
    # Install Intel SGX Repository Key
    wget -qO - https://download.01.org/intel-sgx/sgx_repo/ubuntu/intel-sgx-deb.key | sudo apt-key add -
    
    # Add SGX repository
    echo 'deb [arch=amd64] https://download.01.org/intel-sgx/sgx_repo/ubuntu focal main' | sudo tee /etc/apt/sources.list.d/intel-sgx.list
    
    # Update package lists
    sudo apt-get update
    log_success "Intel SGX repositories added"
}

# Install SGX driver and libraries
install_sgx_components() {
    log_info "Installing SGX components..."
    sudo apt-get install -y \
        libsgx-enclave-common \
        libsgx-quote-ex \
        libsgx-dcap-ql \
        libsgx-dcap-default-qpl \
        sgx-aesm-service
    log_success "SGX components installed"
}

# Install Open Enclave
install_open_enclave() {
    log_info "Installing Open Enclave..."
    
    # Add Microsoft repository
    echo "deb [arch=amd64] https://packages.microsoft.com/ubuntu/20.04/prod focal main" | sudo tee /etc/apt/sources.list.d/msprod.list
    wget -qO - https://packages.microsoft.com/keys/microsoft.asc | sudo apt-key add -
    
    # Install Open Enclave
    sudo apt-get update
    sudo apt-get install -y open-enclave
    
    # Source Open Enclave environment
    source /opt/openenclave/share/openenclave/openenclaverc
    log_success "Open Enclave installed"
}

# Install Java and Maven
install_java_maven() {
    log_info "Installing Java 17 and Maven..."
    
    # Add OpenJDK repository
    sudo add-apt-repository -y ppa:openjdk-r/ppa
    sudo apt-get update
    
    # Install Java 17
    sudo apt-get install -y openjdk-17-jdk
    
    # Install Maven
    sudo apt-get install -y maven
    
    # Verify installations
    java --version
    mvn --version
    
    log_success "Java 17 and Maven installed"
}

# Install Azure CLI
install_azure_cli() {
    log_info "Installing Azure CLI..."
    
    # Import Microsoft GPG key
    curl -sL https://packages.microsoft.com/keys/microsoft.asc | gpg --dearmor | sudo tee /etc/apt/trusted.gpg.d/microsoft.gpg > /dev/null
    
    # Add Azure CLI repository
    AZ_REPO=$(lsb_release -cs)
    echo "deb [arch=amd64] https://packages.microsoft.com/repos/azure-cli/ $AZ_REPO main" | sudo tee /etc/apt/sources.list.d/azure-cli.list
    
    # Install Azure CLI
    sudo apt-get update
    sudo apt-get install -y azure-cli
    
    log_success "Azure CLI installed"
}

# Final system configuration
configure_system() {
    log_info "Performing final system configuration..."
    
    # Set Java 17 as default
    sudo update-alternatives --set java /usr/lib/jvm/java-17-openjdk-amd64/bin/java
    
    # Configure PATH for Open Enclave
    echo 'export PATH=$PATH:/opt/openenclave/bin' | sudo tee -a /etc/profile.d/openenclave.sh
    
    log_success "System configuration complete"
}

# Main installation process
main() {
    log_info "Starting Comprehensive SGX and CDSI Deployment Preparation"
    
    # Run installation steps
    check_ubuntu_version
    prepare_system
    install_sgx_repositories
    install_sgx_components
    install_open_enclave
    install_java_maven
    install_azure_cli
    configure_system
    
    log_success "SGX and CDSI Deployment Preparation Complete!"
    log_warning "Please reboot your system to ensure all configurations take effect"
}

# Execute main function
main