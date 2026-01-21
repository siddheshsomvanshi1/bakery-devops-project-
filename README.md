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
sudo usermod -aG docker ubuntu
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
docker login -u siddheshsomvanshi27

# 2. Build and Push Images
docker build -t siddheshsomvanshi27/bakery-frontend:latest .
docker push siddheshsomvanshi27/bakery-frontend:latest

cd backend-java
docker build -t siddheshsomvanshi27/bakery-backend:latest .
docker push siddheshsomvanshi27/bakery-backend:latest
```

---

## Phase 4: Kubernetes Orchestration (EKS)
### Section 1: Connecting to the Cluster
#### H4 – Linking Kubectl to AWS
```bash
aws eks update-kubeconfig --region ca-central-1 --name BakeryProject-EKS
kubectl get nodes # Verifies that your worker nodes are 'Ready'
```

### Section 2: Deployment with bakery-deployment.yaml
#### H4 – The Manifest Source Code
**Why?** This file defines your entire cloud infrastructure (Replicas, LoadBalancers, and Database connections).

Create a file named `bakery-deployment.yaml` and paste this code:

```yaml
apiVersion: apps/v1 
kind: Deployment 
metadata: 
  name: bakery-backend 
spec: 
  replicas: 2 
  selector: 
    matchLabels: 
      app: bakery-backend 
  template: 
    metadata: 
      labels: 
        app: bakery-backend 
    spec: 
      containers: 
      - name: backend 
        image: siddheshsomvanshi27/bakery-backend:latest 
        ports: 
        - containerPort: 8080 
        env: 
        - name: DB_URL 
          value: "jdbc:mysql://bakery-db-instance.crm022wwqd6k.ca-central-1.rds.amazonaws.com:3306/bakery_db" 
        - name: DB_USER 
          value: "admin" 
        - name: DB_PASS 
          value: "admin123" 

--- 
apiVersion: v1 
kind: Service 
metadata: 
  name: backend-service 
spec: 
  selector: 
    app: bakery-backend 
  ports: 
  - port: 8080 
    targetPort: 8080 
  type: LoadBalancer 

--- 
apiVersion: apps/v1 
kind: Deployment 
metadata: 
  name: bakery-frontend 
spec: 
  replicas: 2 
  selector: 
    matchLabels: 
      app: bakery-frontend 
  template: 
    metadata: 
      labels: 
        app: bakery-frontend 
    spec: 
      containers: 
      - name: frontend 
        image: siddheshsomvanshi27/bakery-frontend:latest 
        ports: 
        - containerPort: 80 

--- 
apiVersion: v1 
kind: Service 
metadata: 
  name: frontend-service 
spec: 
  selector: 
    app: bakery-frontend 
  ports: 
  - port: 80 
    targetPort: 80 
  type: LoadBalancer 
```

#### H4 – Applying the Manifest
```bash
kubectl apply -f bakery-deployment.yaml
```

---

## Phase 5: Verification & Output
### Section 1: Final Networking & Fixes
#### H4 – The Critical main.js Fix
**Why?** You must tell the JavaScript where the new Backend Load Balancer is.
1. Get Backend URL: `kubectl get svc backend-service`.
2. Update the URL in `js/main.js`.
3. Re-push and Restart:
```bash
docker build -t siddheshsomvanshi27/bakery-frontend:latest .
docker push siddheshsomvanshi27/bakery-frontend:latest
kubectl rollout restart deployment bakery-frontend
```

#### H4 – RDS Security Group Fix
1. Add Inbound Rule to RDS Security Group: **MySQL (3306)**.
2. **Source**: Select your **EKS Node Security Group ID**.

### Section 2: Accessing the App
#### H4 – Get Your Public URL
```bash
kubectl get svc frontend-service # Copy the EXTERNAL-IP
```

###### H6 – End of DevOps Documentation
