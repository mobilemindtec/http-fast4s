

#include "http_handler_extern.h"

namespace httpserver{

static void async_get_callback_wrap(request* req, response* resp) {
    auto handler = static_cast<http_handler_extern *>(req->handler_);
    int status = resp->status_code;
    char* body = resp->body;
    auto callback = handler->callback_get();

    req->handler_ = NULL;
    free(req);
    //free(resp);

    callback({ status, headers{}, std::string{body}, "" });
}

http_handler::options_r http_handler_extern::options(bpstd::string_view target, bpstd::string_view body, headers_access&& get_headers) {
    return { 404, headers{}, "", "" };
}

http_handler::head_r http_handler_extern::head(bpstd::string_view target, headers_access&& get_headers) {
    return { 404, headers{}, 0, "" };
}

http_handler::get_r http_handler_extern::get(bpstd::string_view target, headers_access&& get_headers) {

    request req;
    req.target = target.c_str();

    response* resp = (*http_handler_callback_)(&req);

    int status = resp->status_code;
    auto body = std::string{resp->body};

    free(resp);

    return { status, headers{}, body, "" };
}



void http_handler_extern::async_get(bpstd::string_view target, headers_access&& get_headers, std::function<callback_t<http_handler::get_r>> callback) {
    callback_get_ = callback;
    request* req = (request *) malloc(sizeof(request));
    req->handler_ = this;
    req->target = target.c_str();
    (*http_handler_async_callback_)(req, &async_get_callback_wrap);
}


http_handler::post_r http_handler_extern::post(bpstd::string_view target, bpstd::string_view body, headers_access&& get_headers) {
    return { 404, headers{}, "", "" };
}

http_handler::put_r http_handler_extern::put(bpstd::string_view target, bpstd::string_view body, headers_access&& get_headers){
    return { 404, headers{} };
}

http_handler::delete_r http_handler_extern::delete_(bpstd::string_view target, bpstd::string_view body, headers_access&& get_headers) {
    return { 404, headers{}, "", "" };
}


}
