# Use the lightweight Nginx Alpine image
FROM nginx:alpine

# Copy your HTML/JS/CSS files into the Nginx public directory
COPY . /usr/share/nginx/html

# Expose port 80 for web traffic
EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]