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
        //std::any aycnCppCb_;
    };

    typedef request request;

    struct response {
        int status_code;
        header* headers;
        char* body;
        char* content_type;
    };

    typedef response response;

    //std::tuple<int, std::optional<headers>, std::string, std::string>
    //std::string_view target, headers_access&& get_headers

    typedef void (*response_callback_t)(request* req, response* resp);

    typedef response* (*http_get_callback_t) (request* req);
    typedef void (*http_get_async_callback_t) (request* req, response_callback_t cb);
}

#endif // HTTPSERVER_EXTERN_H
