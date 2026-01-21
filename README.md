# Bakery Web Application: End-to-End DevOps Deployment
## Cloud Architecture on AWS: EC2, RDS, Docker, and Kubernetes (EKS)

This project demonstrates a full production-grade deployment of a Java-based Bakery Web Application. It covers containerization with Docker, managed database setup with RDS, and orchestration with AWS EKS.

---

## Phase 1: Local Environment & Infrastructure Setup
### Section 1: AWS RDS (Database) Setup
#### H4 – What is AWS RDS?
**Amazon Relational Database Service (RDS)** is a managed service that makes it easy to set up, operate, and scale a relational database in the cloud. Instead of managing a database on your own server (EC2), RDS handles backups, patching, and scaling automatically.

#### H4 – Initial Database Configuration
1. Go to **RDS Console** -> **Create Database**.
2. Select **MySQL 8.0** (Free Tier).
3. **DB Instance Identifier**: `bakery-db`.
4. **Master Username**: `admin`.
5. **Master Password**: `YourSecurePassword123`.
6. **Public Access**: No (Secure way).
7. **Initial Database Name**: `bakery_db`.

#### H4 – Database Initialization (SQL Commands)
Connect to your RDS instance from EC2 and run these commands to create the schema:

##### H5 – Create Database and Tables
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

## Phase 2: Server Configuration & Tools Installation
### Section 1: Installing Docker & DevOps Tools
#### H4 – Command Sequence for EC2
Connect to your EC2 via SSH and run the following in sequence:

##### H5 – Install Docker
```bash
sudo apt update -y
sudo apt install docker.io -y
sudo systemctl start docker
sudo usermod -aG docker ubuntu
```

##### H5 – Install AWS CLI v2
```bash
sudo apt install unzip curl -y
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip awscliv2.zip
sudo ./aws/install
aws --version
```

##### H5 – Install kubectl
```bash
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
sudo install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl
```

---

## Phase 3: Containerization & Image Management
### Section 1: Building Docker Images
#### H4 – Frontend and Backend Build
```bash
# Build Backend
cd backend-java
docker build -t your-dockerhub-id/bakery-backend:latest .

# Build Frontend
cd ..
docker build -t your-dockerhub-id/bakery-frontend:latest .
```

---

## Phase 4: AWS EKS (Kubernetes) Deployment
### Section 1: Connecting to EKS Cluster
#### H4 – Updating Kubeconfig
```bash
aws configure # Enter Access Key, Secret, and Region (ca-central-1)
aws eks update-kubeconfig --region ca-central-1 --name your-cluster-name
kubectl get nodes # Verify connection
```

---

## Phase 5: Kubernetes Orchestration
### Section 1: Launching Pods
#### H4 – Applying the Manifest
```bash
kubectl apply -f bakery-deployment.yaml
kubectl get pods # Wait for Running status
kubectl get svc  # Get External-IP LoadBalancer URLs
```

---

## Phase 6: Final Connectivity & Output
### Section 1: Database Security Group Fix
#### H4 – Allowing EKS to talk to RDS
1. Go to **EC2 Console** -> **Worker Node Security Group**.
2. Copy the **Security Group ID**.
3. Go to **RDS Console** -> **Database Security Group** -> **Inbound Rules**.
4. Add Rule: **MySQL (3306)**, Source: **Worker Node Security Group ID**.

### Section 2: Verifying Output
#### H4 – Final Results
1. Access the website using the **frontend-service External-IP**.
2. The website is now live at the **AWS Load Balancer URL**.
3. Form submissions are saved directly to the **AWS RDS MySQL** database.

###### H6 – End of Documentation
