#ifndef BEAST_SERVER_H
#define BEAST_SERVER_H

#include <any>

extern "C" {

    struct header {
        const char* name;
        const char* value;
    };

    typedef header header;

    struct request {
        const char* verb;
        const char* target;
        const char* body;
        header* headers;
        void *handler_;
    };

    typedef request request;

    struct response {
        int status_code;
        const char* body;
        const char* content_type;
        int content_size;
        header* headers;
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

#endif // BEAST_SERVER_H
