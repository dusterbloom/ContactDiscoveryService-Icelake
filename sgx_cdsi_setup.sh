#!/bin/bash

# Universal SGX and CDSI Deployment Preparation Script
# Supports Ubuntu 20.04, 22.04, and 24.04 LTS

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

# Global variables
UBUNTU_CODENAME=""
UBUNTU_VERSION=""

# Check Ubuntu version
check_ubuntu_version() {
    log_info "Checking Ubuntu Version..."
    if [ -f /etc/os-release ]; then
        . /etc/os-release
        UBUNTU_CODENAME=$UBUNTU_CODENAME
        UBUNTU_VERSION=$VERSION_ID

        case "$VERSION_ID" in
            20.04|22.04|24.04)
                log_success "Supported Ubuntu version: $VERSION_ID ($UBUNTU_CODENAME)"
                ;;
            *)
                log_error "Unsupported Ubuntu version: $VERSION_ID. Requires 20.04, 22.04, or 24.04."
                exit 1
                ;;
        esac
    else
        log_error "Cannot determine Ubuntu version"
        exit 1
    fi
}

# Update and install base dependencies
prepare_system() {
    log_info "Updating system and installing base dependencies..."
    sudo apt-get update
    sudo apt-get upgrade -y
    sudo apt-get install -y \
        wget \
        curl \
        software-properties-common \
        gnupg2 \
        build-essential \
        libssl-dev \
        ca-certificates
    log_success "System updated and base dependencies installed"
}

# Install Intel SGX repositories
install_sgx_repositories() {
    log_info "Installing Intel SGX repositories..."
    
    # Remove existing SGX repositories
    sudo rm -f /etc/apt/sources.list.d/intel-sgx.list
    
    # Install Intel SGX Repository Key
    wget -qO - https://download.01.org/intel-sgx/sgx_repo/ubuntu/intel-sgx-deb.key | sudo gpg --dearmor -o /usr/share/keyrings/intel-sgx-keyring.gpg
    
    # Add SGX repository with signed-by option
    echo "deb [arch=amd64 signed-by=/usr/share/keyrings/intel-sgx-keyring.gpg] https://download.01.org/intel-sgx/sgx_repo/ubuntu focal main" | sudo tee /etc/apt/sources.list.d/intel-sgx.list
    
    # Update package lists
    sudo apt-get update
    log_success "Intel SGX repositories added"
}

# Install SGX driver and libraries
install_sgx_components() {
    log_info "Installing SGX components..."
    
    # Determine appropriate package versions
    case "$UBUNTU_VERSION" in
        20.04)
            SGX_PACKAGES=(
                "libsgx-enclave-common=2.23.100.2-focal1"
                "libsgx-quote-ex=2.23.100.2-focal1"
                "libsgx-dcap-ql=1.20.100.2-focal1"
                "libsgx-dcap-default-qpl=1.20.100.2-focal1"
                "sgx-aesm-service=2.23.100.2-focal1"
            )
            ;;
        22.04)
            SGX_PACKAGES=(
                "libsgx-enclave-common=2.23.100.2-jammy1"
                "libsgx-quote-ex=2.23.100.2-jammy1"
                "libsgx-dcap-ql=1.20.100.2-jammy1"
                "libsgx-dcap-default-qpl=1.20.100.2-jammy1"
                "sgx-aesm-service=2.23.100.2-jammy1"
            )
            ;;
        24.04)
            log_warning "Using fallback packages for Ubuntu 24.04"
            SGX_PACKAGES=(
                "libsgx-enclave-common"
                "libsgx-quote-ex"
                "libsgx-dcap-ql"
                "libsgx-dcap-default-qpl"
                "sgx-aesm-service"
            )
            ;;
    esac

    # Install SGX packages
    sudo apt-get install -y "${SGX_PACKAGES[@]}"
    log_success "SGX components installed"
}

# Install Open Enclave
install_open_enclave() {
    log_info "Installing Open Enclave..."
    
    # Remove existing Microsoft repository
    sudo rm -f /etc/apt/sources.list.d/microsoft-prod.list
    
    # Add Microsoft repository with signed-by option
    wget -qO- https://packages.microsoft.com/keys/microsoft.asc | sudo gpg --dearmor -o /usr/share/keyrings/microsoft-keyring.gpg
    
    # Determine appropriate repository based on Ubuntu version
    case "$UBUNTU_VERSION" in
        20.04)
            REPO_URL="https://packages.microsoft.com/ubuntu/20.04/prod"
            ;;
        22.04)
            REPO_URL="https://packages.microsoft.com/ubuntu/22.04/prod"
            ;;
        24.04)
            log_warning "Using 22.04 repository for Open Enclave on Ubuntu 24.04"
            REPO_URL="https://packages.microsoft.com/ubuntu/24.04/prod/dists/noble/"
            ;;
    esac
    
    # Add repository
    echo "deb [arch=amd64 signed-by=/usr/share/keyrings/microsoft-keyring.gpg] $REPO_URL $(lsb_release -cs) main" | sudo tee /etc/apt/sources.list.d/microsoft-prod.list
    
    # Update and install Open Enclave
    sudo apt-get update
    sudo apt-get install -y open-enclave
    
    # Source Open Enclave environment
    source /opt/openenclave/share/openenclave/openenclaverc
    log_success "Open Enclave installed"
}

# Install Java and Maven
install_java_maven() {
    log_info "Installing Java 17 and Maven..."
    
    # Remove any existing Java-related repositories
    sudo add-apt-repository -r ppa:openjdk-r/ppa
    
    # Install Java 17
    sudo apt-get install -y openjdk-17-jdk
    
    # Install Maven
    sudo apt-get install -y maven
    
    # Verify installations
    java --version
    mvn --version
    
    log_success "Java 17 and Maven installed"
}

# Install Azure CLI with modern method
install_azure_cli() {
    log_info "Installing Azure CLI..."
    
    # Remove existing Azure CLI repositories
    sudo rm -f /etc/apt/sources.list.d/azure-cli.list
    
    # Import Microsoft GPG key
    curl -sL https://packages.microsoft.com/keys/microsoft.asc | sudo gpg --dearmor -o /usr/share/keyrings/microsoft-archive-keyring.gpg
    
    # Add Azure CLI repository
    AZ_REPO=$(lsb_release -cs)
    echo "deb [arch=amd64 signed-by=/usr/share/keyrings/microsoft-archive-keyring.gpg] https://packages.microsoft.com/repos/azure-cli/ $AZ_REPO main" | sudo tee /etc/apt/sources.list.d/azure-cli.list
    
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
    echo 'export PATH=$PATH:/opt/openenclave/bin' | sudo tee /etc/profile.d/openenclave.sh
    
    # Set environment variables for SGX
    echo 'export SGX_AESM_ADDR=1' | sudo tee /etc/profile.d/sgx.sh
    
    log_success "System configuration complete"
}

# Cleanup function
cleanup() {
    log_info "Cleaning up temporary files and updating package cache..."
    sudo apt-get autoremove -y
    sudo apt-get clean
    sudo apt-get update
}

# Main installation process
main() {
    log_info "Starting Universal SGX and CDSI Deployment Preparation"
    
    # Run installation steps
    check_ubuntu_version
    prepare_system
    install_sgx_repositories
    install_sgx_components
    install_open_enclave
    install_java_maven
    install_azure_cli
    configure_system
    cleanup
    
    log_success "SGX and CDSI Deployment Preparation Complete!"
    log_warning "Please reboot your system to ensure all configurations take effect"
}

# Execute main function
main