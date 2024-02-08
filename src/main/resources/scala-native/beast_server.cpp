
#include <stdlib.h>
#include "httpserver.h"

extern "C" {

int run(
    char* hostname,
    unsigned short port,
    unsigned short max_thread_count,
    beast_handler* handler){


    //httpserver::http_handler_mock handler;
    return httpserver::run(hostname, port, max_thread_count, handler);
}

//typedef void (*http_get_async_callback_t) (request* req, response_callback_t resp);
int run_sync(
    char* hostname,
    unsigned short port,
    unsigned short max_thread_count,
    http_handler_callback_t callback){

    beast_handler* handler = (beast_handler *) malloc(sizeof(beast_handler));
    handler->sync = callback;
    return run(hostname, port, max_thread_count, handler);
}

int run_async(
    char* hostname,
    unsigned short port,
    unsigned short max_thread_count,
    http_handler_async_callback_t callback){

    beast_handler* handler = (beast_handler *) malloc(sizeof(beast_handler));
    handler->async = callback;
    return run(hostname, port, max_thread_count, handler);
}


response* request_callback_sync(request* req){
    response* resp = (response*) malloc(sizeof(response));
    char body[] = "OK from C!";
    resp->body = body;
    resp->status_code = 200;
    return resp;
}

void request_callback_async(request* req, response_callback_t cb){
    response* resp = (response*) malloc(sizeof(response));
    char body[] = "OK from C async!";
    resp->body = body;
    resp->status_code = 200;
    cb(req, resp);
}

/*
int main(int argc, char** argv) {


    run_async("0.0.0.0", 8181, 2, &request_callback_async);

    return 0;
}*/

}
