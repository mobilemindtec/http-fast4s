
#include <stdlib.h>
#include "httpserver_extern.h"
#include "http_handler_extern.h"
#include "httpserver.h"

extern "C" {

void* create_http_handler(){
    return new httpserver::http_handler_extern();
}

void* create_http_handler_async(){
    httpserver::http_handler_extern* h = new httpserver::http_handler_extern();
    h->use_async(true);
    return h;
}

void add_http_get_handler(void* http_handler_ptr, http_get_callback_t cb){
    httpserver::http_handler_extern* ptr =
        static_cast<httpserver::http_handler_extern *>(http_handler_ptr);
    ptr->http_get_callback(cb);
}

void add_http_get_async_handler(void* http_handler_ptr, http_get_async_callback_t cb){
    httpserver::http_handler_extern* ptr =
        static_cast<httpserver::http_handler_extern *>(http_handler_ptr);
    ptr->http_get_async_callback(cb);
}

response* create_response(int status_code, char* body, char* content_type){
    response* resp = (response *) malloc(sizeof(response));
    resp->status_code = status_code;
    resp->body = body;
    resp->content_type = content_type;
    return resp;
}

//typedef void (*http_get_async_callback_t) (request* req, response_callback_t resp);

int run(
    char* hostname,
    unsigned short port,
    unsigned short max_thread_count,
    void* http_handler_ptr){

    httpserver::http_handler_extern* ptr =
        static_cast<httpserver::http_handler_extern *>(http_handler_ptr);

    //httpserver::http_handler_mock handler;
    return httpserver::run(hostname, port, ptr, max_thread_count);
}

response* c_get(request* req){
    response* resp = (response*) malloc(sizeof(response));
    char body[] = "OK from C!";
    resp->body = body;
    resp->status_code = 200;
    return resp;
}

void c_ge_async(request* req, response_callback_t cb){
    response* resp = (response*) malloc(sizeof(response));
    char body[] = "OK from C async!";
    resp->body = body;
    resp->status_code = 200;
    cb(req, resp);
}

/*
    int main(int argc, char** argv) {

        void* handler = create_http_handler_async();
        add_http_get_async_handler(handler, &c_ge_async);


        run("0.0.0.0", 8181, 2, handler);
        //httpserver::run("0.0.0.0", 8181, &handler,6);
        return 0;
    }
*/
}
