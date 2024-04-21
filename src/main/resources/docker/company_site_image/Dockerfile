FROM nginx:mainline-alpine
RUN rm /etc/nginx/conf.d/*
ADD company_site.conf /etc/nginx/conf.d/
ADD index.html /usr/share/nginx/html/