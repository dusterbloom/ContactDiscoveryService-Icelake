#!/bin/bash

# Open Enclave SDK Installation Script
# Compatible with Ubuntu 24.04 using Focal (20.04) repositories

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

# Configure Repositories
configure_repositories() {
    log_info "Configuring repositories..."

    # Clean existing repository configurations
    sudo rm -f /etc/apt/sources.list.d/intel-sgx.list
    sudo rm -f /etc/apt/sources.list.d/llvm-toolchain.list
    sudo rm -f /etc/apt/sources.list.d/msprod.list

    # Intel SGX Repository (Focal)
    echo 'deb [arch=amd64] https://download.01.org/intel-sgx/sgx_repo/ubuntu focal main' | \
    sudo tee /etc/apt/sources.list.d/intel-sgx.list
    wget -qO - https://download.01.org/intel-sgx/sgx_repo/ubuntu/intel-sgx-deb.key | \
    sudo apt-key add -

    # LLVM Toolchain (Focal, version 11)
    echo "deb http://apt.llvm.org/focal/ llvm-toolchain-focal-11 main" | \
    sudo tee /etc/apt/sources.list.d/llvm-toolchain-focal-11.list
    wget -qO - https://apt.llvm.org/llvm-snapshot.gpg.key | \
    sudo apt-key add -

    # Microsoft Repository (Focal)
    echo "deb [arch=amd64] https://packages.microsoft.com/ubuntu/20.04/prod focal main" | \
    sudo tee /etc/apt/sources.list.d/msprod.list
    wget -qO - https://packages.microsoft.com/keys/microsoft.asc | \
    sudo apt-key add -

    log_success "Repositories configured"
}

# Check SGX Driver
check_sgx_driver() {
    log_info "Checking SGX driver..."
    
    if dmesg | grep -q "sgx: intel_sgx: Intel SGX DCAP Driver"; then
        log_success "SGX driver already installed"
    else
        log_warning "SGX driver not found. Attempting installation..."
        
        # Install DKMS
        sudo apt-get update
        sudo apt-get install -y dkms
        
        # Download and install SGX driver
        wget https://download.01.org/intel-sgx/sgx-linux/2.17/distro/ubuntu20.04-server/sgx_linux_x64_driver_1.41.bin -O sgx_linux_x64_driver.bin
        chmod +x sgx_linux_x64_driver.bin
        sudo ./sgx_linux_x64_driver.bin
        
        log_success "SGX driver installed"
    fi
}

# Install SGX and Open Enclave Packages
install_packages() {
    log_info "Installing SGX and Open Enclave packages..."
    
    # Update package lists
    sudo apt-get update
    
    # Install required packages
    sudo apt-get install -y \
        clang-11 \
        libssl-dev \
        gdb \
        libsgx-enclave-common \
        libsgx-quote-ex \
        libprotobuf17 \
        libsgx-dcap-ql \
        libsgx-dcap-ql-dev \
        az-dcap-client \
        open-enclave \
        ninja-build
    
    # Install CMake via pip
    sudo apt-get install -y python3-pip
    sudo pip3 install cmake
    
    log_success "Packages installed"
}

# Verify Open Enclave Installation
verify_installation() {
    log_info "Verifying Open Enclave installation..."
    
    # Check Open Enclave version
    if command -v oeverify >/dev/null; then
        oeverify --version
        log_success "Open Enclave verified"
    else
        log_error "Open Enclave verification failed"
        return 1
    fi
    
    # Check SGX capabilities
    if dmesg | grep -q "sgx: intel_sgx: Intel SGX DCAP Driver"; then
        log_success "SGX driver verified"
    else
        log_error "SGX driver not detected"
        return 1
    fi
}

# Main installation process
main() {
    log_info "Starting Open Enclave SDK Installation"
    
    configure_repositories
    check_sgx_driver
    install_packages
    verify_installation
    
    log_success "Open Enclave SDK Installation Complete!"
    log_warning "Please reboot your system to ensure all configurations take effect"
}

# Execute main function
main