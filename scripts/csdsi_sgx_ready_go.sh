#!/bin/bash

# Comprehensive SGX System Inspection Script

echo "🔍 SGX System Configuration Inspection"
echo "====================================="

echo -e "\n📋 Current Ubuntu Version:"
cat /etc/os-release | grep -E "PRETTY_NAME|VERSION_ID"

echo -e "\n🗂️ Current SGX Repository Configuration:"
cat /etc/apt/sources.list.d/intel-sgx.list 2>/dev/null || echo "No SGX repository configured"

echo -e "\n📦 Installed SGX Packages:"
dpkg -l | grep -E "sgx|libsgx"

echo -e "\n🔑 Configured Repository Keys:"
apt-key list | grep -E "SGX|Intel"

echo -e "\n🖥️ CPU SGX Capabilities:"
if [ -f /proc/cpuinfo ]; then
    grep -q sgx /proc/cpuinfo && echo "SGX Supported" || echo "SGX Not Supported"
else
    echo "Cannot read CPU information"
fi

echo -e "\n⚙️ SGX Driver Status:"
[ -c /dev/sgx/enclave ] && echo "SGX driver loaded (enclave)" || 
[ -c /dev/sgx_vepc ] && echo "SGX driver loaded (vepc)" || 
echo "No SGX driver detected"

# Open Enclave Check
echo -e "\n🔐 Open Enclave Installation:"
which oeverify && oeverify --version || echo "Open Enclave not installed"