# 🚀 Human-DevOps

This project, **Human-DevOps**, consists of two separate subprojects interacting with each other:

- **Backend**: Implemented in Java (Spring Boot)
- **Frontend**: Implemented in React

## 📋 Prerequisites

### 🔧 Backend

- Java 17
- Docker
- SSL Certificate (**HTTPS** required)

> **⚠️ Important:** Ensure the backend is running with a valid and fully trusted SSL certificate. Slack integration requires HTTPS.

### 🎨 Frontend

- Node.js
- Docker

## 📂 Project Structure

```
Human-DevOps/
├── human-factors-java-main/   # Backend Java (Spring Boot)
└── human-factors-react-master/ # Frontend React
```

## 🚀 Installation and Configuration

### 🔧 Backend Configuration

Update the following in:

📄 `human-factors-java-main/src/main/resources/application.properties`

```properties
server.port=<PORT>

com.suken27.humanfactors.jwtSecret=<JWT_SECRET>
com.suken27.humanfactors.slack.redirectURI=<URL backend>/slack/oauth_redirect
com.suken27.humanfactors.slack.redirectURIPath=/slack/oauth_redirect
com.suken27.humanfactors.slack.oauthCompletionURL=<URL backend>/slack/oauth/completion
com.suken27.humanfactors.slack.oauthCancellationURL=<URL backend>/slack/oauth/cancellation

# Slack-specific configuration
com.suken27.humanfactors.slack.signingSecret=<SIGNING_SECRET>
com.suken27.humanfactors.slack.clientID=<CLIENT_ID>
com.suken27.humanfactors.slack.clientSecret=<CLIENT_SECRET>
```

> **📝 Note:** Slack values (`SIGNING_SECRET`, `CLIENT_ID`, `CLIENT_SECRET`) are provided upon creating your Slack App.

## 🗃️ Database Information

In the current version, the backend uses an **in-memory database**. This means that **each time the server restarts, all data will be lost**, including:
- Team Manager account
- Slack integration settings

### 🔄 After Server Restart (in-memory database only)

After restarting, you must:
- **Recreate the Team Manager account**
- **Reconfigure Slack integration**

### 🐬 MySQL Persistent Database Configuration

To persist data, configure MySQL in `application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/my_database?serverTimezone=UTC&useSSL=false
spring.datasource.username=my_user
spring.datasource.password=my_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect
```

Replace `my_database`, `my_user`, and `my_password` with your actual details.

> If using MySQL 5.x, replace `MySQL8Dialect` with `MySQL5Dialect`.

Run the backend:

```bash
docker-compose up --build
```

## 🌐 Frontend Configuration

Set the backend API URL by replacing `<URL backend>` in:
- `human-factors-react-master/src/authentication/AuthService.ts`
- `human-factors-react-master/src/team/TeamScreen.tsx`

```typescript
const API_URL = '<URL backend>';
```

Run the frontend:

```bash
docker-compose up --build
```

## 💬 Slack Application Integration

Create your Slack app with:

- **Slash command**: `/questions`
  - URL: `<URL backend>/slack/events`
  - Description: Manually launch questions

- **OAuth Redirect URLs**:
  - `<URL backend>/slack/oauth_redirect`

- **Bot Scopes**:
  - `chat:write`, `commands`, `incoming-webhook`, `app_mentions:read`, `channels:read`

- **User Scopes**:
  - `users:read`, `users:read.email`

Or you can import the following slack_manifest.json (customizing with your parameters):

```json
{
    "display_information": {
        "name": "<Name>",
        "description": "<Description>"
    },
    "features": {
        "slash_commands": [
            {
                "command": "/questions",
                "url": "<URL backend>/slack/events",
                "description": "Manually launch questions",
                "usage_hint": "Manually launch questions",
                "should_escape": false
            }
    },
    "oauth_config": {
        "redirect_urls": ["<URL backend>/slack/oauth_redirect"],
        "scopes": {
            "bot": [
                "chat:write",
                "commands",
                "incoming-webhook",
                "app_mentions:read",
                "channels:read"
            },
            "user": ["users:read", "users:read.email"]
    },
    "settings": {
        "interactivity": {
            "is_enabled": true,
            "request_url": "<URL backend>/slack/events"
        },
        "org_deploy_enabled": false
    }
}
```

### ⚡ Slack Integration Steps
1. **Register as Team Manager** in the application.
2. **Integrate Slack** via the Team Management screen after registering.

## 🎬 Demo

🔗 [Example Deployed Application](https://giis.inf.um.es/humanDevOps/)

## 🎥 Demo Video

Watch a demonstration of the tool in action (click to watch on Youtube):

[![🎥 Ver video](https://img.youtube.com/vi/kJinhw-8-mg/maxresdefault.jpg)](https://youtu.be/kJinhw-8-mg)

## 🚀 Usage

After backend and frontend services are running and Slack integration is complete, access the application via the frontend URL.

Once integrated, the Team Manager has access to:

- **Graph Analysis**: View graphical representations of team responses.
- **Team Management**: Add or remove team members and launch questionnaires.
- **Recommendations**: Access recommendations based on team input.

## ❓ Support

For any questions or issues, please open an issue in this GitHub repository.

## 📜 License

MIT

---

**🏛️ Software Engineering Research Group of the University of Murcia, Spain 🇪🇸**