#ifndef HTTP_HANDLER_EXTERN_H
#define HTTP_HANDLER_EXTERN_H

#include <cstdlib>
#include <optional>
#include <functional>
#include <unordered_map>
#include <string>

#include "http_handler.h"
#include "httpserver_extern.h"

namespace httpserver {

class http_handler_extern: public http_handler {

public:

    http_handler_extern(){}

    http_handler::options_r
    options(std::string_view target,
            std::string_view body,
            headers_access&& get_headers) override;

    http_handler::head_r
    head(std::string_view target, headers_access&& get_headers) override;

    http_handler::get_r
    get(std::string_view target, headers_access&& get_headers) override;

    void
    async_get(std::string_view target, headers_access&& get_headers, std::function<http_handler::callback_t<get_r>> callback) override;

    http_handler::post_r
    post(std::string_view target,
         std::string_view body,
         headers_access&& get_headers) override;

    http_handler::put_r
    put(std::string_view target,
        std::string_view body,
        headers_access&& get_headers) override;

    http_handler::delete_r
    delete_(std::string_view target,
            std::string_view body,
            headers_access&& get_headers) override;

    void http_get_callback(http_get_callback_t cb);

    void http_get_async_callback(http_get_async_callback_t cb);



private:
    http_get_callback_t http_get_callback_;
    http_get_async_callback_t http_get_async_callback_;
};

}

#endif // HTTP_HANDLER_SERVER_H
