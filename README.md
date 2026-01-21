# Bakery Web Application: End-to-End DevOps Deployment
## Cloud Architecture on AWS: EC2, RDS, Docker, and Kubernetes (EKS)

This project demonstrates a full production-grade deployment of a Java-based Bakery Web Application. It covers containerization with Docker, managed database setup with RDS, and orchestration with AWS EKS.

---

## Phase 1: AWS Infrastructure Setup
### Section 1: AWS RDS (Database) Setup
#### H4 – What is AWS RDS?
**Amazon Relational Database Service (RDS)** is a managed database service. Instead of installing MySQL on a server manually, RDS handles backups, security patches, and scaling for you, making it more reliable for production.

#### H4 – Initial RDS Configuration
1. Go to **RDS Console** -> **Create Database**.
2. Select **MySQL 8.0** (Free Tier).
3. **DB Instance Identifier**: `bakery-db`.
4. **Master Username**: `admin`.
5. **Master Password**: `YourSecurePassword123`.
6. **Public Access**: No (Secure).
7. **Initial Database Name**: `bakery_db`.

#### H4 – Database Initialization (SQL Commands)
Once your RDS is active, connect to it and run these commands to set up your tables:

##### H5 – Create Schema
```sql
CREATE DATABASE IF NOT EXISTS bakery_db;
USE bakery_db;

-- Table for Bakery Products
CREATE TABLE products (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    price DECIMAL(10,2),
    image VARCHAR(255)
);

-- Table for Team Members
CREATE TABLE team (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    role VARCHAR(255),
    image VARCHAR(255)
);

-- Table for Contact Form Submissions
CREATE TABLE contact_messages (
    id INT AUTO_INCREMENT PRIMARY KEY,
    payload JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

##### H5 – Insert Sample Data
```sql
INSERT INTO products (name, price, image) VALUES 
('Chocolate Cake', 49.99, 'img/product-1.jpg'),
('French Bread', 14.99, 'img/product-2.jpg');

INSERT INTO team (name, role, image) VALUES 
('Ganesh Jadhav', 'Master Chef', 'img/team-1.jpg');
```

---

## Phase 2: EC2 Management Server Setup
### Section 1: Tool Installation Sequence
#### H4 – Installing Docker, AWS CLI, and Kubectl
Connect to your Ubuntu EC2 instance and run these commands in order:

##### H5 – Step 1: Install Docker
```bash
sudo apt update -y
sudo apt install docker.io -y
sudo systemctl start docker
sudo usermod -aG docker ubuntu
# Note: Log out and log back in for group changes to take effect
```

##### H5 – Step 2: Install AWS CLI v2
```bash
sudo apt install unzip curl -y
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip awscliv2.zip
sudo ./aws/install
aws --version
aws configure # Enter your Access Key, Secret Key, and Region (ca-central-1)
```

##### H5 – Step 3: Install kubectl
```bash
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
sudo install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl
kubectl version --client
```

---

## Phase 3: Dockerization & Registry Management
### Section 1: Building and Pushing Images
#### H4 – Docker Login and Build
```bash
# Log in to Docker Hub
docker login -u siddheshsomvanshi27

# Build and Tag Frontend
docker build -t siddheshsomvanshi27/bakery-frontend:latest .
docker tag containerizedbakerywebapplication-frontend siddheshsomvanshi27/bakery-frontend:latest

# Build and Tag Backend
cd backend-java
docker build -t siddheshsomvanshi27/bakery-backend:latest .
docker tag containerizedbakerywebapplication-backend siddheshsomvanshi27/bakery-backend:latest
```

#### H4 – Pushing to Docker Hub
```bash
docker push siddheshsomvanshi27/bakery-frontend:latest
docker push siddheshsomvanshi27/bakery-backend:latest
```

---

## Phase 4: Kubernetes Orchestration on AWS EKS
### Section 1: EKS Cluster Connection
#### H4 – Connecting to the Cluster
```bash
aws eks update-kubeconfig --region ca-central-1 --name BakeryProject-EKS
kubectl get nodes # Verify your nodes are joined and 'Ready'
```

### Section 2: Deployment and Scaling
#### H4 – Applying Manifests
Create a `bakery-deployment.yaml` and apply it:
```bash
kubectl apply -f bakery-deployment.yaml
kubectl get pods # Ensure all pods are 'Running'
```

#### H4 – Updating Code and Rolling Restart
If you change your JavaScript code, rebuild and restart the pods:
```bash
docker build -t siddheshsomvanshi27/bakery-frontend:latest .
docker push siddheshsomvanshi27/bakery-frontend:latest
kubectl rollout restart deployment bakery-frontend
```

---

## Phase 5: Verification and Output
### Section 1: Accessing the Application
#### H4 – Get LoadBalancer URLs
```bash
kubectl get svc
```
1. Copy the **EXTERNAL-IP** of `frontend-service` to access the website.
2. Copy the **EXTERNAL-IP** of `backend-service` and update it in your `js/main.js` for API calls.

### Section 2: Security Group Configuration
#### H4 – Database Connectivity Fix
1. Identify your **EKS Node Security Group** in the EC2 console.
2. Add an **Inbound Rule** to your **RDS Security Group**:
   - **Type**: MySQL (3306)
   - **Source**: Paste the EKS Node Security Group ID.

###### H6 – Documentation Completed Successfully
