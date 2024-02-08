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

    options_r
    options(bpstd::string_view target,
            bpstd::string_view body,
            headers_access&& get_headers) override;

    void
    async_options(bpstd::string_view target,
                  bpstd::string_view body,
                  headers_access&& get_headers,
                  std::function<callback_t<options_r>> callback) override;

    head_r
    head(bpstd::string_view target,
         headers_access&& get_headers) override;

    void
    async_head(bpstd::string_view target,
               headers_access&& get_headers,
               std::function<callback_t<head_r>> callback) override;

    get_r
    get(bpstd::string_view target,
        headers_access&& get_headers) override;

    void
    async_get(bpstd::string_view target,
              headers_access&& get_headers,
              std::function<callback_t<get_r>> callback) override;

    post_r
    post(bpstd::string_view target,
         bpstd::string_view body,
         headers_access&& get_headers) override;

    void
    async_post(bpstd::string_view target,
               bpstd::string_view body,
               headers_access&& get_headers,
               std::function<callback_t<post_r>> callback) override;

    put_r
    put(bpstd::string_view target,
        bpstd::string_view body,
        headers_access&& get_headers) override;

    void
    async_put(bpstd::string_view target,
              bpstd::string_view body,
              headers_access&& get_headers,
              std::function<callback_t<put_r>> callback) override;

    delete_r
    delete_(bpstd::string_view target,
            bpstd::string_view body,
            headers_access&& get_headers) override;

    void
    async_delete_(bpstd::string_view target,
                  bpstd::string_view body,
                  headers_access&& get_headers,
                  std::function<callback_t<delete_r>> callback) override;

    std::function<callback_t<get_r>> callback_get();

    std::function<callback_t<head_r>> callback_head();

    std::function<callback_t<options_r>> callback_options();

    std::function<callback_t<post_r>> callback_post();

    std::function<callback_t<put_r>> callback_put();

    std::function<callback_t<delete_r>> callback_delete();

private:
    http_handler_callback_t http_handler_callback_;
    http_handler_async_callback_t http_handler_async_callback_;
    std::function<callback_t<options_r>> callback_options_;
    std::function<callback_t<head_r>> callback_head_;
    std::function<callback_t<get_r>> callback_get_;
    std::function<callback_t<post_r>> callback_post_;
    std::function<callback_t<put_r>> callback_put_;
    std::function<callback_t<delete_r>> callback_delete_;
};

}

#endif // HTTP_HANDLER_SERVER_H
