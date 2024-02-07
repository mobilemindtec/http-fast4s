#ifndef HTTP_HANDLER_EXTERN_H
#define HTTP_HANDLER_EXTERN_H

#include <cstdlib>
#include <iostream>
#include <functional>
#include <unordered_map>
#include <string>

#include "http_handler.h"
#include "httpserver_extern.h"


namespace httpserver {

class http_handler_extern: public http_handler {

public:

    http_handler_extern(http_handler_callback_t http_handler_callback,
                        http_handler_async_callback_t http_handler_async_callback)
        :http_handler_callback_(http_handler_callback),
         http_handler_async_callback_(http_handler_async_callback)
    {}

    http_handler::options_r
    options(bpstd::string_view target,
            bpstd::string_view body,
            headers_access&& get_headers) override;

    http_handler::head_r
    head(bpstd::string_view target, headers_access&& get_headers) override;

    http_handler::get_r
    get(bpstd::string_view target, headers_access&& get_headers) override;

    void
    async_get(bpstd::string_view target, headers_access&& get_headers, std::function<http_handler::callback_t<get_r>> callback) override;

    http_handler::post_r
    post(bpstd::string_view target,
         bpstd::string_view body,
         headers_access&& get_headers) override;

    http_handler::put_r
    put(bpstd::string_view target,
        bpstd::string_view body,
        headers_access&& get_headers) override;

    http_handler::delete_r
    delete_(bpstd::string_view target,
            bpstd::string_view body,
            headers_access&& get_headers) override;

    std::function<callback_t<http_handler::get_r>> callback_get(){
        return callback_get_;
    }

private:
    http_handler_callback_t http_handler_callback_;
    http_handler_async_callback_t http_handler_async_callback_;
    std::function<callback_t<http_handler::get_r>> callback_get_;
};

}

#endif // HTTP_HANDLER_SERVER_H
