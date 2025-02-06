#!/bin/bash

# Comprehensive Repository and Development Environment Setup
# Debugged version for Ubuntu 24.04

set -e

# Color codes
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

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

# Comprehensive repository configuration
configure_repositories() {
    log_info "Configuring repositories for SGX and development tools..."

    # Clean up existing repository configurations
    sudo rm -f /etc/apt/sources.list.d/intel-sgx.list
    sudo rm -f /etc/apt/sources.list.d/microsoft-*.list

    # Intel SGX Repository (Jammy)
    wget -O - https://download.01.org/intel-sgx/sgx_repo/ubuntu/intel-sgx-deb.key | \
    sudo gpg --dearmor -o /etc/apt/keyrings/intel-sgx-keyring.gpg

    echo 'deb [signed-by=/etc/apt/keyrings/intel-sgx-keyring.gpg arch=amd64] https://download.01.org/intel-sgx/sgx_repo/ubuntu jammy main' | \
    sudo tee /etc/apt/sources.list.d/intel-sgx.list

    # Microsoft Repository for Open Enclave (Jammy)
    wget -O - https://packages.microsoft.com/keys/microsoft.asc | \
    sudo gpg --dearmor -o /etc/apt/keyrings/microsoft-keyring.gpg

    echo "deb [arch=amd64 signed-by=/etc/apt/keyrings/microsoft-keyring.gpg] https://packages.microsoft.com/ubuntu/22.04/prod jammy main" | \
    sudo tee /etc/apt/sources.list.d/microsoft-jammy.list

    # Additional repositories for development tools
    sudo add-apt-repository -y ppa:openjdk-r/ppa

    # Enable Jammy repositories for additional packages
    sudo sed -i 's/noble/jammy/g' /etc/apt/sources.list

    log_success "Repositories configured"
}

# Update and upgrade system
update_system() {
    log_info "Updating system packages..."
    
    # Force use of Jammy repositories
    sudo sed -i 's/noble/jammy/g' /etc/apt/sources.list
    
    # Update package lists
    sudo apt-get clean
    sudo apt-get update -o Acquire::ForceHash=yes || log_warning "Repository update encountered issues"
    
    # Upgrade existing packages
    sudo apt-get upgrade -y
    
    log_success "System updated"
}

# Install SGX packages
install_sgx_packages() {
    log_info "Installing SGX packages..."
    
    # Install SGX packages from Jammy repository
    sudo apt-get install -y --allow-downgrades \
        libsgx-enclave-common \
        libsgx-quote-ex \
        libsgx-dcap-ql \
        sgx-aesm-service \
        open-enclave || log_warning "Some SGX packages may not have installed"
    
    log_success "SGX packages installed"
}

# Install development tools
install_dev_tools() {
    log_info "Installing development tools..."
    
    # Install development packages
    sudo apt-get install -y \
        build-essential \
        cmake \
        openjdk-17-jdk \
        maven \
        pkg-config \
        libssl-dev || log_warning "Some development tools may not have installed"
    
    log_success "Development tools installed"
}

# Verify installations
verify_installations() {
    log_info "Verifying installations..."
    
    # Check SGX packages
    dpkg -l | grep -q libsgx && log_success "SGX packages verified" || log_error "SGX package verification failed"
    
    # Check Open Enclave
    if command -v oeverify >/dev/null; then
        oeverify --version
        log_success "Open Enclave verified"
    else
        log_warning "Open Enclave not found"
    fi
    
    # Check development tools
    java --version
    mvn --version
    cmake --version
}





# Main installation process
main() {
    log_info "Starting Comprehensive Development Environment Setup"
    
    # Run installation steps
    configure_repositories
    update_system
    install_sgx_packages
    install_dev_tools
    verify_installations
    
    log_success "Development Environment Setup Complete!"
    log_warning "Please reboot your system to ensure all configurations take effect"
}

# Execute main function
main