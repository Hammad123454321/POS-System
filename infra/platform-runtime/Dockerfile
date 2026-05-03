FROM php:8.4-cli-bookworm
COPY --from=mlocati/php-extension-installer:2 /usr/bin/install-php-extensions /usr/local/bin/

RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        curl \
        git \
        unzip \
    && install-php-extensions \
        intl \
        mbstring \
        pcntl \
        pdo_pgsql \
        pdo_sqlite \
        pgsql \
        sqlite3 \
        zip \
    && git clone --depth 1 --branch 6.2.0 https://github.com/phpredis/phpredis.git /usr/src/php/ext/redis \
    && docker-php-ext-install redis \
    && rm -rf /var/lib/apt/lists/*

COPY --from=composer:2 /usr/bin/composer /usr/bin/composer

WORKDIR /workspace/apps/platform
