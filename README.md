## Getting Started

Welcome to the VS Code Java world. Here is a guideline to help you get started to write Java code in Visual Studio Code.

Step1: On AWS Console

📋 Prerequisites & EC2 SetupTo run Jenkins, SonarQube, Docker, and Maven simultaneously without crashing, your EC2 instance must meet these minimum specs:AMI: Ubuntu 24.04 LTSInstance Type: t3.medium (Minimum 2 vCPUs, 4 GB RAM)Storage: 30 GB GP3 SSDSecurity Group Inbound Rules:Port 22: For SSH accessPort 8080: Jenkins Web UIPort 9000: SonarQube Web UI.

Step 2: In Bash Terminal by connecting to EC2 server

#!/bin/bash
sudo apt update && sudo apt upgrade -y

# 1. Install Java 21 (Required to run Jenkins & SonarQube)
sudo apt install fontconfig openjdk-21-jre openjdk-21-jdk -y

# 2. Install Jenkins
sudo wget -O /usr/share/keyrings/jenkins-keyring.asc \
  https://jenkins.io
echo "deb [signed-by=/usr/share/keyrings/jenkins-keyring.asc] \
  https://jenkins.io binary/" | sudo tee \
  /etc/apt/sources.list.path/jenkins.list > /dev/null
sudo apt update
sudo apt install jenkins -y
sudo systemctl start jenkins
sudo systemctl enable jenkins

*** If Above commannds are not worked, apply these commands:

If the terminal continues to report that the Unit jenkins.service does not exist, it means the apt install jenkins command was completely skipped or aborted earlier due to the GPG signature error. Because the underlying package was never unpacked, the operating system never generated the service files. [1] 
Additionally, the standard path for keyrings on modern Ubuntu configurations is /etc/apt/keyrings/ rather than /usr/share/keyrings/. [2, 3, 4] 
Let's do a complete, clean reset of the Jenkins repository and force the package to download. Copy and paste these exact steps into your MobaXterm terminal:
## 1. Wipe out any old corrupted repository configs
Run this to ensure old, conflicting repository files are cleared:

sudo rm -f /etc/apt/sources.list.d/jenkins.list
sudo rm -f /usr/share/keyrings/jenkins-keyring.asc
sudo rm -f /etc/apt/keyrings/jenkins-keyring.asc

## 2. Set up the correct directories and download the valid 2026 Key [5] 
Create the secure keyring directory and pull the official configuration: [2] 

sudo mkdir -p /etc/apt/keyrings
sudo wget -O /etc/apt/keyrings/jenkins-keyring.asc https://pkg.jenkins.io/debian-stable/jenkins.io-2026.key

## 3. Bind the repository directly to that key file [5] 
Execute this command to register the source directly to the newly stored keyring mapping: [2] 

echo "deb [signed-by=/etc/apt/keyrings/jenkins-keyring.asc]" \https://pkg.jenkins.io/debian-stable binary/ | sudo tee \
/etc/apt/sources.list.d/jenkins.list > /dev/null

## 4. Force a fresh installation [5] 
Now, update the database and install the package. Pay close attention to the terminal output to ensure it streams the download without a warning break: [6] 

sudo apt-get update
sudo apt-get install -y jenkins

## 5. Launch the newly created System Service
Now that the installation has concluded cleanly, trigger the daemon system manager: [7, 8, 9] 

sudo systemctl daemon-reload
sudo systemctl enable jenkins
sudo systemctl start jenkins

## 6. Confirm execution
Verify your setup running:

sudo systemctl status jenkins

Once the configuration resolves, let me know:

* Is your terminal showing a green active (running) message?
* Do you want the path command to retrieve your initial setup token password? [1, 10]

------------------------------
## Your Next Step: Get the Jenkins Setup Password
Now that your Jenkins server is verified as active (running), you need the secret unlock code to access it via your web browser.
Once you press q, copy and paste this command to print the administrator password on your screen:

sudo cat /var/lib/jenkins/secrets/initialAdminPassword

## What to do with the output:

   1. Copy the long string of numbers and letters that appears.
   2. Open your web browser and go to: http://<YOUR_EC2_PUBLIC_IP>:8080
   3. Paste that code into the Administrator password field to unlock your Jenkins setup page!

---


# 3. Install Docker Engine
sudo apt install docker.io -y
sudo usermod -aG docker ubuntu
sudo usermod -aG docker jenkins
sudo systemctl restart jenkins # Restarts Jenkins to apply Docker group permissions

# 4. Install Maven
sudo apt install maven -y

# 5. Install Trivy (Vulnerability Scanner)
sudo apt-get install wget gnupg
wget -qO - https://aquasecurity.github.io/trivy-repo/deb/public.key | gpg --dearmor | sudo tee /usr/share/keyrings/trivy.gpg > /dev/null
echo "deb [signed-by=/usr/share/keyrings/trivy.gpg] https://aquasecurity.github.io/trivy-repo/deb generic main" | sudo tee -a /etc/apt/sources.list.d/trivy.list
sudo apt-get update
sudo apt-get install trivy

# 6. Install SonarQube (Running via Docker Container for simplicity)
The message Unit sonarqube.service could not be found confirms that SonarQube has not been installed as a traditional system service on your machine.
Since SonarQube requires specific system user accounts and database setups to run as a native service, the industry-standard method for DevOps engineers is to run it inside a Docker container. [1, 2, 3] 
Copy and paste these exact commands to install Docker and launch SonarQube on your server:
## 1. Update and Install Docker [4] 

sudo apt update && sudo apt install -y docker.io

## 2. Configure System Limits (Mandatory for SonarQube) [5] 
SonarQube relies on an internal database engine (Elasticsearch) that requires higher memory map limits than Ubuntu defaults allow. Run this command to increase the limits, or SonarQube will crash on startup: [6, 7] 

sudo sysctl -w vm.max_map_count=262144

## 3. Start Docker and Run SonarQube [8] 
Now, pull the official SonarQube Community Long-Term Support (LTS) image and run it in the background on your pre-configured port 9000: [9, 10, 11, 12] 

sudo systemctl enable docker --now
sudo docker run -d --name sonarqube -p 9000:9000 --restart always sonarqube:lts-community

## 4. Verify It Is Running
To check if the container is up and running, execute:

sudo docker ps

You should see a row displaying sonarqube:lts-community under the IMAGE column and Up X seconds under STATUS. [13] 
## 5. Access the Dashboard
Because SonarQube takes roughly 60 to 90 seconds to initialize its internal database directories on first launch, wait a minute and then open your web browser to:

http://<YOUR_EC2_PUBLIC_IP>:9000


* Default Username: admin
* Default Password: admin (The system will prompt you to change this immediately upon your first login). [14, 15, 16] 

If your terminal displays a "Permission Denied" error when running Docker or if you want to know how to connect this to your Jenkins pipeline, let me know!

----


# 7. Install AWS CLI & kubectl (To communicate with EKS)
curl "https://amazonaws.com" -o "awscliv2.zip"
sudo apt install unzip -y
unzip awscliv2.zip
sudo ./aws/install

curl -LO "https://k8s.io(curl -L -s https://k8s.io)/bin/linux/amd64/kubectl"
sudo install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl
---

## Folder Structure

The workspace contains two folders by default, where:

- `src`: the folder to maintain sources
- `lib`: the folder to maintain dependencies

Meanwhile, the compiled output files will be generated in the `bin` folder by default.

> If you want to customize the folder structure, open `.vscode/settings.json` and update the related settings there.

## Dependency Management

The `JAVA PROJECTS` view allows you to manage your dependencies. More details can be found [here](https://github.com/microsoft/vscode-java-dependency#manage-dependencies).
