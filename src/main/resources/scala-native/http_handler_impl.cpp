

#include "http_handler_impl.h"

namespace httpserver{


enum class callback_type {
    Options,
    Head,
    Get,
    Post,
    Put,
    Delete
};

static void
callback_dispach(request* req, response* resp, callback_type type){
    auto handler = static_cast<http_handler_impl *>(req->handler_);
    int status = resp->status_code;
    int content_size = resp->content_size;
    const char* content_type = resp->content_type;
    auto str_body = std::string{resp->body};

    free(req);

    switch(type){
    case callback_type::Get: {
        auto callback_get = handler->callback_get();
        callback_get({ status, headers{}, str_body, std::string{content_type} });
        break;
    }
    case callback_type::Head: {
        auto callback_head = handler->callback_head();
        callback_head({ status, headers{}, content_size, std::string{content_type} });
        break;
    }
    case callback_type::Options: {
        auto callback_options = handler->callback_options();
        callback_options({ status, headers{}, str_body, std::string{content_type} });
        break;
    }
    case callback_type::Post: {
        auto callback_post = handler->callback_post();
        callback_post({ status, headers{}, str_body, std::string{content_type} });
        break;
    }
    case callback_type::Put: {
        auto callback_put = handler->callback_put();
        callback_put({ status, headers{} });
        break;
    }
    case callback_type::Delete: {
        auto callback_delete = handler->callback_delete();
        callback_delete({ status, headers{}, str_body, std::string{content_type} });
        break;
    }
    }
}

static void
async_get_callback_wrap(request* req, response* resp) {
    callback_dispach(req, resp, callback_type::Get);
}

static void
async_head_callback_wrap(request* req, response* resp) {
    callback_dispach(req, resp, callback_type::Head);
}

static void
async_options_callback_wrap(request* req, response* resp) {
    callback_dispach(req, resp, callback_type::Options);
}

static void
async_post_callback_wrap(request* req, response* resp) {
    callback_dispach(req, resp, callback_type::Post);
}

static void
async_put_callback_wrap(request* req, response* resp) {
    callback_dispach(req, resp, callback_type::Put);
}

static void
async_delete_callback_wrap(request* req, response* resp) {
    callback_dispach(req, resp, callback_type::Delete);
}

http_handler_impl::http_handler_impl(
    http_handler_callback_t http_handler_callback,
    http_handler_async_callback_t http_handler_async_callback)
    :http_handler_callback_(http_handler_callback),
     http_handler_async_callback_(http_handler_async_callback)
{}


http_handler::options_r
http_handler_impl::options(bpstd::string_view target,
                           bpstd::string_view body,
                           headers_access&& get_headers) {

    request req;
    req.target = target.c_str();
    req.verb = "OPTIONS";
    req.body = body.c_str();

    response* resp = (*http_handler_callback_)(&req);

    int status = resp->status_code;
    auto resp_body = std::string{resp->body};
    auto content_type = std::string{ resp->content_type};

    free(resp);

    return { status, headers{}, resp_body, content_type };
}

void
http_handler_impl::async_options(bpstd::string_view target,
                                 bpstd::string_view body,
                                 headers_access&& get_headers,
                                 std::function<callback_t<options_r>> callback) {
    callback_options_ = callback;
    request* req = (request *) malloc(sizeof(request));
    req->handler_ = this;
    req->body = body.c_str();
    req->target = target.c_str();
    req->verb = "OPTIONS";
    (*http_handler_async_callback_)(req, &async_options_callback_wrap);
}

http_handler::head_r
http_handler_impl::head(bpstd::string_view target,
                        headers_access&& get_headers) {

    request req;
    req.target = target.c_str();
    req.verb = "HEAD";

    response* resp = (*http_handler_callback_)(&req);

    int status = resp->status_code;
    auto content_size = resp->content_size;
    auto content_type = std::string{ resp->content_type};

    free(resp);

    return { status, headers{}, content_size, content_type };
}

void
http_handler_impl::async_head(bpstd::string_view target,
                              headers_access&& get_headers,
                              std::function<callback_t<head_r>> callback) {
    callback_head_ = callback;
    request* req = (request *) malloc(sizeof(request));
    req->handler_ = this;
    req->target = target.c_str();
    req->verb = "HEAD";
    (*http_handler_async_callback_)(req, &async_head_callback_wrap);
}

http_handler::get_r
http_handler_impl::get(bpstd::string_view target,
                       headers_access&& get_headers) {

    request req;
    req.target = target.c_str();
    req.verb = "GET";

    response* resp = (*http_handler_callback_)(&req);

    int status = resp->status_code;
    auto body = std::string{resp->body};
    auto content_type = std::string{ resp->content_type};

    free(resp);

    return { status, headers{}, body, content_type };
}

void
http_handler_impl::async_get(bpstd::string_view target,
                             headers_access&& get_headers,
                             std::function<callback_t<get_r>> callback) {
    callback_get_ = callback;
    request* req = (request *) malloc(sizeof(request));
    req->handler_ = this;
    req->target = target.c_str();
    req->verb = "GET";

    std::cout << "BEAST" << req->verb << " " << req->target << std::endl;

    (*http_handler_async_callback_)(req, &async_get_callback_wrap);
}


http_handler::post_r
http_handler_impl::post(bpstd::string_view target,
                        bpstd::string_view body,
                        headers_access&& get_headers) {
    request req;
    req.target = target.c_str();
    req.verb = "POST";
    req.body = body.c_str();

    response* resp = (*http_handler_callback_)(&req);

    int status = resp->status_code;
    auto resp_body = std::string{resp->body};
    auto content_type = std::string{ resp->content_type};

    free(resp);

    return { status, headers{}, resp_body, content_type} ;
}

void
http_handler_impl::async_post(bpstd::string_view target,
                              bpstd::string_view body,
                              headers_access&& get_headers,
                              std::function<callback_t<post_r>> callback) {

    callback_post_ = callback;
    request* req = (request *) malloc(sizeof(request));
    req->handler_ = this;
    req->body = body.c_str();
    req->target = target.c_str();
    req->verb = "POST";
    std::cout << "BEAST" << req->verb << " " << req->target << ":" << req->body << std::endl;

    (*http_handler_async_callback_)(req, &async_post_callback_wrap);
}

http_handler::put_r
http_handler_impl::put(bpstd::string_view target,
                       bpstd::string_view body,
                       headers_access&& get_headers){
    request req;
    req.target = target.c_str();
    req.verb = "PUT";
    req.body = body.c_str();

    response* resp = (*http_handler_callback_)(&req);

    int status = resp->status_code;

    free(resp);

    return { status, headers{} };
}

void
http_handler_impl::async_put(bpstd::string_view target,
                             bpstd::string_view body,
                             headers_access&& get_headers,
                             std::function<callback_t<put_r>> callback) {
    callback_put_ = callback;
    request* req = (request *) malloc(sizeof(request));
    req->handler_ = this;
    req->body = body.c_str();
    req->target = target.c_str();
    req->verb = "PUT";
    (*http_handler_async_callback_)(req, &async_put_callback_wrap);
}

http_handler::delete_r
http_handler_impl::delete_(bpstd::string_view target,
                           bpstd::string_view body,
                           headers_access&& get_headers) {

    request req;
    req.target = target.c_str();
    req.verb = "DELETE";
    req.body = body.c_str();

    response* resp = (*http_handler_callback_)(&req);

    int status = resp->status_code;
    auto resp_body = std::string{resp->body};
    auto content_type = std::string{ resp->content_type };

    free(resp);

    return { status, headers{}, resp_body, content_type };
}

void
http_handler_impl::async_delete_(bpstd::string_view target,
                                 bpstd::string_view body,
                                 headers_access&& get_headers,
                                 std::function<callback_t<delete_r>> callback) {
    callback_delete_ = callback;
    request* req = (request *) malloc(sizeof(request));
    req->handler_ = this;
    req->body = body.c_str();
    req->target = target.c_str();
    req->verb = "DELETE";
    (*http_handler_async_callback_)(req, &async_delete_callback_wrap);
}

std::function<http_handler::callback_t<http_handler::get_r>>
http_handler_impl::callback_get(){
    return callback_get_;
}

std::function<http_handler::callback_t<http_handler::head_r>>
http_handler_impl::callback_head(){
    return callback_head_;
}

std::function<http_handler::callback_t<http_handler::options_r>>
http_handler_impl::callback_options(){
    return callback_options_;
}

std::function<http_handler::callback_t<http_handler::post_r>>
http_handler_impl::callback_post(){
    return callback_post_;
}

std::function<http_handler::callback_t<http_handler::put_r>>
http_handler_impl::callback_put(){
    return callback_put_;
}

std::function<http_handler::callback_t<http_handler::delete_r>>
http_handler_impl::callback_delete(){
    return callback_delete_;
}

}
