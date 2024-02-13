#ifndef HTTP_HANDLER_IMPL_H
#define HTTP_HANDLER_IMPL_H

#include <cstdlib>
#include <iostream>
#include <functional>
#include <unordered_map>
#include <string>

#include "http_handler.h"
#include "beast_server.h"


namespace httpserver {

class http_handler_impl: public http_handler {

public:

    http_handler_impl() : http_handler() {}
    http_handler_impl(http_handler_callback_t,
                        http_handler_async_callback_t);

    response_t*
    dispatch(request_t *) override;

    void
    dispatch_async(request_t *, std::function<callback_t<response_t*>>) override;

    std::function<callback_t<response_t*>> callback_response();

private:
    http_handler_callback_t http_handler_callback_;
    http_handler_async_callback_t http_handler_async_callback_;
    std::function<callback_t<response_t*>> callback_response_;
};

}

#endif // HTTP_HANDLER_SERVER_H
