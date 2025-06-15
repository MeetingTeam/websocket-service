# MeetingTeam App

Building a microservice chat website using ReactJS and Spring Boot. This build covers basic functionalities of a video call application such as sending and receiving text messages, uploading files, and video calls (using ZegoCloud API).

## Tentative Technologies and Frameworks

- **Frontend**: ReactJS  
- **Backend**: Spring Boot  
- **Message Broker**: RabbitMQ  
- **Deployment**: Kubernetes, AWS  
- **CI/CD**: Jenkins, ArgoCD, Argo Rollouts  
- **Third-party APIs**: ZegoCloud API (for video call feature)

---

## Architecture

### 1. System Architecture

<p align="center">
  <img src="https://github.com/user-attachments/assets/c1e77dd8-0334-4a65-a37f-0b07673727fc" alt="System Architecture" width="800"/>
</p>

---

### 2. Database Design

<p align="center">
  <img src="https://github.com/user-attachments/assets/1ff0b70a-4c22-4348-9ec6-a3cabef08db1" alt="Database Design" width="700"/>
</p>

---

### 3. Cloud Architecture

**Dev VPC**  
<p align="center">
  <img src="https://github.com/user-attachments/assets/07e226fe-117a-41af-bebe-1634d2f0b0b0" alt="Dev VPC Architecture" width="700"/>
</p>

**Prod VPC**  
<p align="center">
  <img src="https://github.com/user-attachments/assets/6bb0e706-9d36-4e36-bef9-eff2bbd5a0a7" alt="Prod VPC Architecture" width="700"/>
</p>

---

### 4. Development Jenkins Pipeline

<p align="center">
  <img src="https://github.com/user-attachments/assets/d500f9b6-d573-4f7e-83db-5e3b8d2d0c5a" alt="Jenkins CI/CD Pipeline" width="800"/>
</p>

