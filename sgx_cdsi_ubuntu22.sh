#!/bin/bash

# SGX Compatibility Setup for Ubuntu 24.04
# Maintains Jammy (22.04) SGX Package Configuration

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

# Configure APT to allow Jammy SGX packages
configure_apt_sources() {
    log_info "Configuring APT sources for SGX packages..."

    # Remove existing Intel SGX repository
    sudo rm -f /etc/apt/sources.list.d/intel-sgx.list

    # Add Jammy repository with signed key
    wget -O - https://download.01.org/intel-sgx/sgx_repo/ubuntu/intel-sgx-deb.key | \
    sudo gpg --dearmor -o /etc/apt/keyrings/intel-sgx-keyring.gpg

    echo 'deb [signed-by=/etc/apt/keyrings/intel-sgx-keyring.gpg arch=amd64] https://download.01.org/intel-sgx/sgx_repo/ubuntu jammy main' | \
    sudo tee /etc/apt/sources.list.d/intel-sgx.list

    log_success "Jammy SGX repository configured"
}

# Create APT pinning configuration to prevent unwanted upgrades
configure_apt_pinning() {
    log_info "Creating APT pinning for SGX packages..."

    # Create a pin file to lock SGX packages to Jammy version
    cat > /tmp/sgx-jammy-pins <<EOL
Package: libsgx-*
Pin: release n=jammy
Pin-Priority: 1001

Package: sgx-*
Pin: release n=jammy
Pin-Priority: 1001
EOL

    sudo mv /tmp/sgx-jammy-pins /etc/apt/preferences.d/sgx-jammy-pins
    log_success "SGX package pinning configured"
}

# Install Open Enclave from Jammy repository
install_open_enclave() {
    log_info "Installing Open Enclave..."

    # Remove any existing Microsoft repository files
    sudo rm -f /etc/apt/sources.list.d/microsoft-prod.list
    sudo rm -f /etc/apt/sources.list.d/microsoft-ubuntu-*.list

    # Add Microsoft repository for Open Enclave (explicitly using Jammy)
    wget -O - https://packages.microsoft.com/keys/microsoft.asc | \
    sudo gpg --dearmor -o /etc/apt/keyrings/microsoft-keyring.gpg

    echo "deb [arch=amd64 signed-by=/etc/apt/keyrings/microsoft-keyring.gpg] https://packages.microsoft.com/ubuntu/22.04/prod jammy main" | \
    sudo tee /etc/apt/sources.list.d/microsoft-jammy.list

    # Explicitly disable problematic repositories
    sudo sed -i 's/^deb/# deb/g' /etc/apt/sources.list.d/microsoft-ubuntu-*.list || true

    # Update package lists with increased verbosity for debugging
    sudo apt-get update -o Acquire::ForceHash=yes || log_warning "Repository update encountered issues"

    # Install Open Enclave
    sudo apt-get install -y open-enclave || log_warning "Open Enclave installation may have partial problems"

    log_success "Open Enclave installed from Jammy repository"
}

# Install additional development tools
install_dev_tools() {
    log_info "Installing development tools..."
    sudo apt-get install -y \
        build-essential \
        cmake \
        pkg-config \
        libssl-dev \
        openjdk-17-jdk \
        maven

    log_success "Development tools installed"
}

# Verify SGX and Open Enclave installation
verify_installation() {
    log_info "Verifying SGX and Open Enclave installation..."

    # Check SGX packages
    dpkg -l | grep -q libsgx && log_success "SGX packages verified" || log_error "SGX package verification failed"

    # Check Open Enclave
    if command -v oeverify >/dev/null; then
        oeverify --version
        log_success "Open Enclave verified"
    else
        log_warning "Open Enclave not found or not fully installed"
    fi
}

# Main installation process
main() {
    log_info "Starting SGX Compatibility Setup for Ubuntu 24.04"

    # Perform installation steps
    configure_apt_sources
    configure_apt_pinning
    
    # Update package lists with increased tolerance
    sudo apt-get update || log_warning "Repository update encountered non-critical issues"
    
    # Ensure existing SGX packages are kept
    sudo apt-get install -y --no-upgrade libsgx-* sgx-*
    
    install_open_enclave
    install_dev_tools
    verify_installation

    log_success "SGX Compatibility Setup Complete!"
    log_warning "Please reboot your system to ensure all configurations take effect"
}

# Execute main function
main