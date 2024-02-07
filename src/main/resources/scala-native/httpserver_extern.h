#ifndef HTTPSERVER_EXTERN_H
#define HTTPSERVER_EXTERN_H

#include <any>

extern "C" {
    struct header {
        char* name;
        char* value;
    };

    typedef header header;

    struct request {
        const char* target;
        header* headers;
        void *handler_;
    };

    typedef request request;

    struct response {
        int status_code;
        header* headers;
        char* body;
        char* content_type;
    };

    typedef response response;

    typedef void (*response_callback_t)(request* req, response* resp);

    typedef response* (*http_handler_callback_t) (request* req);
    typedef void (*http_handler_async_callback_t) (request* req, response_callback_t cb);

    struct beast_handler {
        http_handler_callback_t sync;
        http_handler_async_callback_t async;
    };

    typedef beast_handler beast_handler;


}

#endif // HTTPSERVER_EXTERN_H
