# 🚀 Human-DevOps
![version](https://img.shields.io/badge/version-v1.0.0-blue)

This project, **Human-DevOps**, consists of two separate subprojects interacting with each other:

- **Backend**: Implemented in Java (Spring Boot)
- **Frontend**: Implemented in React

## 📋 Prerequisites

- Java 17
- MySQL 8
- Docker >= 20.10
- Docker Compose >=2.5
- API Slack
- React 18.2.0
- SSL Certificate (**HTTPS** required)

> ⚠️ **Important:** Make sure the backend runs with a valid and fully trusted SSL certificate.  
> Slack integration **requires HTTPS** with a full chain SSL certificate.  
> If not, Slack integration **will not work**.


### 🖥️ Supported host operating systems
- **Linux**
- **macOS**
- **Windows**

> ℹ️ **Note for Windows users:** Docker Desktop requires WSL 2.


## 📂 Project Structure

```
Human-DevOps/
├── backend/	# Back-end Java (Spring Boot)
└── frontend/	# Front-end React
```

## 🚀 Installation and Configuration

### 🎥 Installation & Demo Video

Watch a single video covering installation, configuration, and a demo of the application:

> **ℹ️ Note:** This video shows the full end-to-end installation process and a demonstration of the tool. Some URLs or interfaces may have changed slightly since recording.

> **🔒 Security disclaimer:** The SSL certificate (`fullchain.pem`) and private key (`privatekey.pem`) used in the video are *dummy, self‑signed files* included **only** for demonstration purposes. They do **not** contain valid credentials and must **never** be reused in production. Always generate your own trusted certificates when deploying.


[![🎥 Installation & Demo](https://img.youtube.com/vi/p8EfvTKImXs/maxresdefault.jpg)](https://youtu.be/p8EfvTKImXs)


### 🧾 1. Clone the repository
First, clone the repository:

```bash
git clone https://github.com/investigaciongiis/human-devops.git
cd human-devops
```

### 🔐 2. Prepare SSL Certificates

To enable HTTPS and ensure Slack integration works correctly, you must provide valid SSL files with the following requirements:

nginx/ssl/fullchain.pem – This file must include the full certificate chain (leaf + intermediate + root certificates).

nginx/ssl/privatekey.pem – This file must contain the private key corresponding to the certificate.

⚠️ Important: If these files are invalid, incomplete, or missing, the application will fail to start correctly, and Slack OAuth will not function.

Make sure both files are placed in the appropriate directory:

```
nginx/
└── ssl/
    ├── fullchain.pem        # Full certificate chain
    └── privatekey.pem      # Private key
```

### 📄 3. Configuration of `nginx.conf` file

Make sure to update the `server_name` in your NGINX configuration. Replace `<DNS_DOMAIN>` with your actual domain:

```nginx

# Redirect HTTP → HTTPS
server {
  listen 80;
  server_name <DNS_DOMAIN> www.<DNS_DOMAIN>;
  return 301 https://$host$request_uri;
}

# HTTPS server
server {
  listen 443 ssl;
  server_name <DNS_DOMAIN> www.<DNS_DOMAIN>;

  ssl_certificate /etc/nginx/ssl/fullchain.pem;
  ssl_certificate_key /etc/nginx/ssl/privatekey.pem;

  root /usr/share/nginx/html;
  index index.html;

  location / {
    try_files $uri /index.html;
  }

  location /api/ {
    proxy_pass http://human-factors-app:8080/;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
  }

  error_page 404 /index.html;
}

```

📝 Note: Replace <DNS_DOMAIN> with your actual domain (e.g., mydomain.com). This is required for HTTPS redirection and Slack OAuth integration.


### 💬 4. Slack Integration

To integrate with Slack, we **recommend importing the preconfigured manifest**:

> ✅ **Recommended Option**: Import `slack_manifest.json`

1. Go to [https://api.slack.com/apps](https://api.slack.com/apps)
2. Log in to Slack
3. Select/create your workspace
4. Click **"Create New App"** → **"From an app manifest"**
5. Choose **"JSON"** and paste the contents of:

```bash
./resources/slack_manifest.json
```

Customize the values (e.g., URLs or app name) as needed

This will automatically configure:

- **Slash command**: `/questions`  
  → `<URL backend>/slack/events`
- **OAuth Redirect URI**:  
  → `<URL backend>/slack/oauth_redirect`
- **Bot Scopes**:  
  `chat:write`, `commands`, `incoming-webhook`, `app_mentions:read`, `channels:read`
- **User Scopes**:  
  `users:read`, `users:read.email`

> ℹ️ **Note:** If you prefer to configure it manually, you can use the values shown in the `slack_manifest.json` file as reference.


### 🔧 5. Server Configuration

Update the following in:

📄 [`.env`](https://github.com/investigaciongiis/human-devops/blob/main/.env)

```properties

SERVER_PORT_API=<SERVER_PORT_API>

JWT_SECRET=Omes56OgP3o12345adsf434mes56Ogmes56Og345adsf46y2  # Minimum 48 characters required

SLACK_CLIENT_ID=<SLACK_CLIENT_ID>
SLACK_SIGNING_SECRET=<SLACK_SIGNING_SECRET>
SLACK_CLIENT_SECRET=<SLACK_CLIENT_SECRET>
SLACK_REDIRECT_URI=<SERVER_DOMAIN>/api/slack/oauth_redirect

SLACK_COMPLETION_URL=<SERVER_DOMAIN>/api/slack/oauth/completion
SLACK_CANCELLATION_URL=<SERVER_DOMAIN>/api/slack/oauth/cancellation

REACT_APP_API_URL=<SERVER_DOMAIN>/api/

```

> **📝 Note 1:** Slack values (SLACK_CLIENT_ID, SLACK_SIGNING_SECRET, SLACK_CLIENT_SECRET) are provided when you create your Slack App. Example:
```properties
SLACK_CLIENT_ID=1443505669971.9147110231784
SLACK_SIGNING_SECRET=f93d1ca7b2e64c3f9ad8e503a7c2e8b1
SLACK_CLIENT_SECRET=b7f4e19a62c83dd5fa3b7ae29d54c0e1
SERVER_DOMAIN=https://mydomain.com
```
> **📝 Note 2:** It is recommended that JWT_SECRET be a secure, random string of at least 48 characters. Example:
```properties
JWT_SECRET=Omes56OgP3o12345adsf434mes56Ogmes56Og345adsf46y2

```

### 🛠 6. Build and Start all services

> ℹ️ **Note:** Make sure you have Docker (>= 20.10) and Docker Compose (>= 2.5) installed on your machine before running the following command.

Build and launch all services using Docker Compose **from the repository root**:

```bash

docker compose up --build -d

```
### 7. First Steps and Testing

Once all services are up and running:

- **Access the application web UI**  
  Open your frontend URL in a browser (e.g., `https://<YOUR_DOMAIN>/`).

- **Create a Team Manager account**  
  Register as a new user and select the “Team Manager” role.

- **Log in with the Team Manager account**  
  Sign in using the credentials you just created.

- **Initiate Slack installation**  
  From the application, in team click on “Add to Slack” and complete the installation.

- **Return to Slack, refresh, and reinstall if needed**  
  In your Slack workspace, reload the app and repeat the installation flow if required.

- **Go to app channel and run `/questions`**  
  In Slack, type the slash command to launch the questionnaire.

- **Verify integration in the app**  
  Return to the web UI, go to the **Team** screen, and verify you see “Slack integration has been completed.”

- **Add team members**  
  First invite them with Slack, then with "Add selection" in home.

- **Schedule questionnaire delivery time**  
  Set in the app what time (daily) the questionnaire should be sent automatically.

> ✔️ With these steps, your Human DevOps environment is ready for full testing and use.


## 📦 Additional Information

### 🗃️ Database Information (MySQL 8)

The application uses MySQL 8, configured via Docker. On the first run:

* A database called my_database is created.

* User my_user with password my_password is granted access.

* Spring Boot connects and automatically creates the required tables using JPA.

* Initial data is optionally inserted via a Java seeder or data.sql.

* All data is persisted using Docker volumes.

Below is the database schema diagram:

![Database Schema](resources/db_schema.png)


### 🔁 Resetting the Docker environment (SSL or other config errors)

ℹ️ When to use this: If you started the stack with the wrong SSL files, an incorrect .env, or any other mis‑configuration, it’s usually faster to wipe everything and start fresh.

⚠️ **Warning:** This command stops every container and removes all Docker volumes, deleting all data (database, uploads, etc.).

Run it **from the project root** (where docker-compose.yml is located):

```bash

docker compose down -v

```

## 🎥 Demo Video

Watch a single video a demo of the application:

> **ℹ️ Note:** This video shows the full end-to-end installation process and a demonstration of the tool. Some URLs or interfaces may have changed slightly since recording.


[![🎥 Demo](https://img.youtube.com/vi/3ZEJ9GrQfIk/maxresdefault.jpg)](https://youtu.be/3ZEJ9GrQfIk)

## 🎬 Live Demo

🔗 [Example Deployed Application](https://giis.inf.um.es/humanDevOps/)


## 📘 API Documentation

You can explore the full API via Swagger:

[![API Docs](https://img.shields.io/badge/API-Swagger-blue)](https://editor.swagger.io/?url=https://github.com/investigaciongiis/human-devops/blob/main/resources/swagger.yaml)

## 🧪 Postman Collection

You can test the API using Postman with the following files available in the repository:

- 📁 [Postman Collection (JSON)](https://github.com/investigaciongiis/human-devops/blob/main/resources/postman_collection.json)  
- 🌐 [Postman Environment (JSON)](https://github.com/investigaciongiis/human-devops/blob/main/resources/postman_environment.json)

> ✅ **Recommended:** Import both files into Postman to test the endpoints using environment variables.

## 🚀 Usage

After backend and frontend services are running and Slack integration is complete, access the application via the frontend URL.

Once integrated, the Team Manager has access to:

- **Graph Analysis**: View graphical representations of team responses.
- **Team Management**: Add or remove team members and launch questionnaires.
- **Recommendations**: Access recommendations based on team input.


## 🤝 How to contribute

Thank you for your interest in contributing to this project!

To contribute, follow these steps:

1. Fork the repository.
2. Create a new branch: `git checkout -b my-feature`
3. Make your changes and commit: `git commit -m 'Add new feature'`
4. Push to your fork: `git push origin my-feature`
5. Open a Pull Request

Please make sure your code follows the project's style and structure.

If you have questions or suggestions, feel free to open an issue.


## ❓ Support

For any questions or issues, please open an issue in this GitHub repository.

## 📬 Contact

If you have questions or prefer to reach out by email, you can write to:  
**investigaciongiis@gmail.com**

## 📜 License

MIT

---

**🏛️ Software Engineering Research Group of the University of Murcia, Spain 🇪🇸**

