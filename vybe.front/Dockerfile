# Use the lightweight Nginx Alpine image
FROM nginx:alpine

# Remove default Nginx static assets to ensure a clean slate
RUN rm -rf /usr/share/nginx/html/*

# Copy your files from the build context into the container
# This will copy everything from the ./vybe.front folder defined in your YAML
COPY . /usr/share/nginx/html

# Expose port 80
EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]