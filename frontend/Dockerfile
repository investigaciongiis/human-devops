# === Build Stage ===
FROM node:20-alpine AS build

WORKDIR /app
COPY package*.json ./

ARG REACT_APP_API_URL
ENV REACT_APP_API_URL=$REACT_APP_API_URL

RUN npm install
COPY . .
RUN npm run build

# === Production Stage ===
FROM nginx:alpine

COPY --from=build /app/build /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80 443

CMD ["nginx", "-g", "daemon off;"]