# Bakery Web Application: End-to-End DevOps Deployment
## Cloud Architecture on AWS: EC2, RDS, Docker, and Kubernetes (EKS)

This project transforms a standard Java web application into a high-availability cloud system. Below is the step-by-step roadmap to reproduce the entire deployment.

---

## Phase 1: Infrastructure Setup (The Foundation)
### Section 1: AWS RDS (Managed Database)
#### H4 – Setup Managed MySQL
**Why?** Using RDS ensures our data is safe even if our servers crash. It handles backups and scaling automatically.

1. Create a **MySQL 8.0** instance in RDS (Free Tier).
2. **Database Name**: `bakery_db`.
3. **Connectivity**: Public Access: **No** (for security).
4. **Initialization**: Run the following SQL to create tables:

```sql
CREATE DATABASE IF NOT EXISTS bakery_db;
USE bakery_db;
CREATE TABLE products (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(255), price DECIMAL(10,2), image VARCHAR(255));
CREATE TABLE team (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(255), role VARCHAR(255), image VARCHAR(255));
CREATE TABLE contact_messages (id INT AUTO_INCREMENT PRIMARY KEY, payload JSON, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP);
```

---

## Phase 2: EC2 Management Server Setup
### Section 1: Installing DevOps Tools
#### H4 – Command Sequence for Ubuntu EC2
Run these commands to turn your EC2 into a DevOps management station.

##### H5 – 1. Install Docker
**Why?** To package our application code into portable containers.
```bash
sudo apt update -y && sudo apt install docker.io -y
sudo usermod -aG docker ubuntu # Allows running docker without 'sudo'
```

##### H5 – 2. Install AWS CLI v2
**Why?** To allow our server to talk to AWS services like EKS and RDS.
```bash
sudo apt install unzip curl -y
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip awscliv2.zip && sudo ./aws/install
aws configure # Connects your server to your AWS account
```

##### H5 – 3. Install kubectl
**Why?** The command-line tool used to send instructions to the Kubernetes cluster.
```bash
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
sudo install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl
```

---

## Phase 3: Containerization (Docker)
### Section 1: Build & Registry Management
#### H4 – Pushing to Docker Hub
**Why?** We store images in Docker Hub so Kubernetes can pull them from any server in the cluster.

```bash
# 1. Login to your registry
docker login -u your_username

# 2. Build and Tag Frontend
docker build -t your_username/bakery-frontend:latest .
docker tag containerizedbakerywebapplication-frontend your_username/bakery-frontend:latest

# 3. Build and Tag Backend
cd backend-java
docker build -t your_username/bakery-backend:latest .
docker tag containerizedbakerywebapplication-backend your_username/bakery-backend:latest

# 4. Push to Cloud
docker push your_username/bakery-frontend:latest
docker push your_username/bakery-backend:latest
```

---

## Phase 4: Kubernetes Orchestration (EKS)
### Section 1: Connecting to the Cluster
#### H4 – Linking Kubectl to AWS
**Why?** This tells your management server which cluster in which region it should control.

```bash
aws eks update-kubeconfig --region ca-central-1 --name BakeryProject-EKS
kubectl get nodes # Verifies that your worker nodes are 'Ready'
```

### Section 2: Deployment with bakery-deployment.yaml
#### H4 – Applying the Manifest
**Why?** This is the master plan. It tells Kubernetes to run 2 replicas of each service and creates the Load Balancers.

1. Create `bakery-deployment.yaml` with the Deployment and Service definitions.
2. Run the deployment:
```bash
kubectl apply -f bakery-deployment.yaml
```

#### H4 – The Critical main.js Fix
**Why?** In EKS, the Frontend and Backend have different URLs. You must tell the JavaScript where the new Backend Load Balancer is.

1. Get Backend URL: `kubectl get svc backend-service`.
2. Update the URL in `js/main.js`.
3. Re-push and Restart:
```bash
docker build -t your_username/bakery-frontend:latest .
docker push your_username/bakery-frontend:latest
kubectl rollout restart deployment bakery-frontend # Pulls the new fix
```

---

## Phase 5: Verification & Output
### Section 1: Final Networking
#### H4 – RDS Security Group Fix
**Why?** RDS blocks all traffic by default. We must explicitly allow the EKS Nodes to talk to it.
1. Add Inbound Rule to RDS Security Group: **MySQL (3306)**.
2. **Source**: Select your **EKS Node Security Group**.

### Section 2: Accessing the App
#### H4 – Get Your URL
```bash
kubectl get svc frontend-service # Copy the EXTERNAL-IP
```
**Paste that URL in your browser to see the live project!**

###### H6 – End of DevOps Documentation
