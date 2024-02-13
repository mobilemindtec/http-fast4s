

#include "http_handler_impl.h"

namespace httpserver{



static void
async_response_callback_wrap(request_t* req, response_t* resp) {
    //std::cout << "async_response_callback_wrap" << std::endl;
    auto handler = static_cast<http_handler_impl *>(req->handler_);
    auto callback = handler->callback_response();
    callback(resp);
}

http_handler_impl::http_handler_impl(
    http_handler_callback_t http_handler_callback,
    http_handler_async_callback_t http_handler_async_callback)
    :http_handler_callback_(http_handler_callback),
     http_handler_async_callback_(http_handler_async_callback)
{}


response_t* http_handler_impl::dispatch(request_t* req) {
    return (*http_handler_callback_)(req);
}

void http_handler_impl::dispatch_async(request_t* req, std::function<callback_t<response_t*>> callback) {
    //std::cout << "dispatch_async" << std::endl;
    callback_response_ = callback;
    req->handler_ = this;
    (*http_handler_async_callback_)(req, &async_response_callback_wrap);
}


std::function<http_handler::callback_t<response_t*>>
http_handler_impl::callback_response(){
    return callback_response_;
}

}
