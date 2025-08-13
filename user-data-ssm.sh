#!/bin/bash
set -eux
dnf install -y amazon-ssm-agent
systemctl enable --now amazon-ssm-agent
