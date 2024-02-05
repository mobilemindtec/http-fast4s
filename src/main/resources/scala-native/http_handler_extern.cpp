

#include "http_handler_extern.h"

namespace httpserver{

http_handler::options_r http_handler_extern::options(std::string_view target, std::string_view body, headers_access&& get_headers) {
    return { 404, headers{}, "", "" };
}

http_handler::head_r http_handler_extern::head(std::string_view target, headers_access&& get_headers) {
    return { 404, headers{}, 0, "" };
}

http_handler::get_r http_handler_extern::get(std::string_view target, headers_access&& get_headers) {

    request req;
    req.target = std::string(target).data();
    response* resp = (*http_get_callback_)(&req);

    int status = resp->status_code;
    char* body = resp->body;

    return { status, headers{}, std::string{body}, "" };
}

static void async_get_wrap(request* req, response* resp) {
    //auto cb =
    //    std::any_cast<http_handler::callback_get_async_t>(req->aycnCppCb_);
    //int status = resp->status_code;
    //char* body = resp->body;
    //cb({ status, headers{}, std::string{body}, "" });
}

void http_handler_extern::async_get(std::string_view target, headers_access&& get_headers, std::function<callback_t<http_handler::get_r>> callback) {
    request req;
    req.target = std::string(target).data();
    //req.aycnCppCb_ = std::make_any<http_handler::callback_get_async_t>(callback);
    (*http_get_async_callback_)(&req, &async_get_wrap);
}


http_handler::post_r http_handler_extern::post(std::string_view target, std::string_view body, headers_access&& get_headers) {
    return { 404, headers{}, "", "" };
}

http_handler::put_r http_handler_extern::put(std::string_view target, std::string_view body, headers_access&& get_headers){
    return { 404, headers{} };
}

http_handler::delete_r http_handler_extern::delete_(std::string_view target, std::string_view body, headers_access&& get_headers) {
    return { 404, headers{}, "", "" };
}

void http_handler_extern::http_get_callback(http_get_callback_t cb){
    http_get_callback_ = cb;
}

void http_handler_extern::http_get_async_callback(http_get_async_callback_t cb){
    http_get_async_callback_ = cb;
}
}
