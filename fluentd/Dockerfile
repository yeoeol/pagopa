FROM fluent/fluentd:v1.16-1

# 플러그인 설치를 위해 root 권한 사용
USER root

# 필요한 빌드 도구 및 fluent-plugin-elasticsearch 설치
RUN apk add --no-cache --update --virtual .build-deps \
        sudo build-base ruby-dev \
 && sudo gem install fluent-plugin-elasticsearch \
 && sudo gem sources --clear-all \
 && apk del .build-deps \
 && rm -rf /tmp/* /var/tmp/* /usr/lib/ruby/gems/*/cache/*.gem

# 보안을 위해 다시 fluent 유저로 전환
USER fluent
