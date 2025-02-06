#!/bin/bash

# SGX Readiness Verification Script for CDSI Deployment
# Version 1.0
# Checks system compatibility for Signal Contact Discovery Service

set -e

# Color codes for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

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

# Comprehensive system check
verify_sgx_support() {
    log_info "Checking SGX Support..."

    # Check CPU SGX capabilities
    if [ -f /proc/cpuinfo ]; then
        if grep -q sgx /proc/cpuinfo; then
            log_success "CPU supports SGX"
        else
            log_error "CPU does not support SGX"
            return 1
        fi
    else
        log_error "Cannot read CPU information"
        return 1
    fi

    # Check SGX driver
    if [ -c /dev/sgx/enclave ]; then
        log_success "SGX driver is loaded"
    elif [ -c /dev/sgx_vepc ]; then
        log_success "SGX driver is loaded (alternate path)"
    else
        log_error "SGX driver not loaded"
        return 1
    fi
}

verify_software_dependencies() {
    log_info "Checking Software Dependencies..."

    # Required packages for CDSI
    REQUIRED_PACKAGES=(
        "openjdk-17-jdk"
        "maven"
        "openssl"
        "pkg-config"
        "libsgx-dcap-ql"
        "libsgx-quote-ex"
        "open-enclave"
    )

    MISSING_PACKAGES=()

    for pkg in "${REQUIRED_PACKAGES[@]}"; do
        if ! dpkg -s "$pkg" >/dev/null 2>&1; then
            MISSING_PACKAGES+=("$pkg")
        fi
    done

    if [ ${#MISSING_PACKAGES[@]} -eq 0 ]; then
        log_success "All required packages are installed"
    else
        log_warning "Missing packages: ${MISSING_PACKAGES[*]}"
    fi
}

verify_azure_sgx_support() {
    log_info "Checking Azure SGX Specific Configuration..."

    # Azure DCsv3 and DCdsv3 series specific checks
    if command -v az >/dev/null 2>&1; then
        CURRENT_VM_SIZE=$(curl -s -H Metadata:true "http://169.254.169.254/metadata/instance/compute/vmSize?api-version=2021-02-01&format=text")
        
        SUPPORTED_VM_SIZES=(
            "Standard_DC1s_v3"
            "Standard_DC2s_v3"
            "Standard_DC4s_v3"
            "Standard_DC8s_v3"
            "Standard_DC1ds_v3"
            "Standard_DC2ds_v3"
            "Standard_DC4ds_v3"
            "Standard_DC8ds_v3"
        )

        VM_SUPPORTED=false
        for size in "${SUPPORTED_VM_SIZES[@]}"; do
            if [[ "$CURRENT_VM_SIZE" == "$size" ]]; then
                VM_SUPPORTED=true
                break
            fi
        done

        if [ "$VM_SUPPORTED" = true ]; then
            log_success "VM Size $CURRENT_VM_SIZE is SGX-enabled"
        else
            log_warning "VM Size $CURRENT_VM_SIZE may not fully support SGX"
        fi
    else
        log_warning "Azure CLI not installed. Cannot verify VM SKU"
    fi
}

verify_enclave_runtime() {
    log_info "Checking Enclave Runtime..."

    # Verify Open Enclave
    if command -v oeverify >/dev/null 2>&1; then
        if oeverify --version >/dev/null 2>&1; then
            log_success "Open Enclave runtime is correctly installed"
        else
            log_error "Open Enclave runtime verification failed"
            return 1
        fi
    else
        log_error "Open Enclave tools not found"
        return 1
    fi
}

verify_network_configuration() {
    log_info "Checking Network Configuration..."

    # Verify required network configurations
    REQUIRED_NETWORK_SERVICES=(
        "Azure Attestation Service"
        "Key Management Service"
    )

    FAILED_SERVICES=()

    # Example network check (replace with actual endpoints)
    AZURE_ATTESTATION_ENDPOINT="https://shareduks.uks.attest.azure.net"
    if ! curl -s --max-time 5 "$AZURE_ATTESTATION_ENDPOINT" >/dev/null; then
        FAILED_SERVICES+=("Azure Attestation Service")
    fi

    if [ ${#FAILED_SERVICES[@]} -eq 0 ]; then
        log_success "Network services are accessible"
    else
        log_warning "Some network services are not accessible: ${FAILED_SERVICES[*]}"
    fi
}

comprehensive_system_check() {
    log_info "Starting Comprehensive CDSI Deployment Readiness Check"
    
    verify_sgx_support
    verify_software_dependencies
    verify_azure_sgx_support
    verify_enclave_runtime
    verify_network_configuration

    log_info "System Readiness Check Complete"
}

# Optional: Generate a detailed report
generate_system_report() {
    log_info "Generating System Report..."
    
    {
        echo "CDSI Deployment Readiness Report"
        echo "================================"
        echo "Date: $(date)"
        echo "Hostname: $(hostname)"
        echo ""
        echo "CPU Information:"
        lscpu | grep -E "Architecture:|Vendor ID:|Model name:|CPU(s):|Thread(s) per core:|Core(s) per socket:|Socket(s):|SGX:"
        echo ""
        echo "Installed Java Version:"
        java --version
        echo ""
        echo "Maven Version:"
        mvn --version
        echo ""
        echo "Open Enclave Version:"
        oeverify --version
    } > cdsi_system_report.txt

    log_success "System report generated at cdsi_system_report.txt"
}

# Main execution
main() {
    comprehensive_system_check
    generate_system_report
}

# Run the main function
main