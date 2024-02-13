#ifndef BEAST_SERVER_H
#define BEAST_SERVER_H

#include <any>
#include <stdlib.h>
#include <string.h>

extern "C" {

    typedef struct {
        const char* name;
        const char* value;
    } header_t;

    typedef struct {
        const char* body;
        const char* body_raw;
        long unsigned int size;
    } body_t;

    typedef struct {
        header_t* headers;
        int size;
    } headers_t;

    typedef struct {
        int noop;

    } request_opts;

    typedef struct {
        int noop;
    } response_opts;

    typedef struct  {
        const char* verb;
        const char* target;
        const char* content_type;
        body_t* body;
        headers_t* headers;
        request_opts* opts;
        void *handler_;
    } request_t;


    typedef struct  {
        int status_code;
        char* content_type;
        body_t* body;
        headers_t* headers;
        response_opts* opts;
    } response_t;

    typedef void (*response_callback_t)(request_t* req, response_t* resp);

    typedef response_t* (*http_handler_callback_t) (request_t* req);
    typedef void (*http_handler_async_callback_t) (request_t* req, response_callback_t cb);

    typedef struct {
        http_handler_callback_t sync;
        http_handler_async_callback_t async;
    } beast_handler_t;

    // initializers

    header_t* header_new(const char* name, const char* value);

    headers_t* headers_new(int& size);

    body_t* body_new(const char* content, const char* raw, int& size) ;

    request_t* request_new(const char* verb, const char* target);

    response_t* response_new(int status_code);

    void headers_free(headers_t* headers);

    void request_free(request_t* req);

    void response_free(response_t* resp);

}

#endif // BEAST_SERVER_H
