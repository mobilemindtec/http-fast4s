#ifndef HTTP_HANDLER_H
#define HTTP_HANDLER_H

#include <cstdlib>
#include <tuple>
#include <functional>
#include <unordered_map>
#include <string>

#include "optional.h"
#include "string_view.h"


namespace httpserver {


using headers = std::unordered_map<std::string, std::string>;


class http_handler
{
public:

    using head_r = std::tuple<int, tl::optional<headers>, size_t, std::string>;
    using get_r
        = std::tuple<int, tl::optional<headers>, std::string, std::string>;
    using options_r      = get_r;
    using post_r         = get_r;
    using put_r          = std::tuple<int, tl::optional<headers>>;
    using delete_r       = get_r;
    using headers_access = std::function<headers()>;

    template<typename resp_t>
    using callback_t = void(resp_t);



    http_handler()
        :use_async_(false)
    {}

    bool
    use_async(){
        return use_async_;
    }

    void
    use_async(bool b){
        use_async_ = b;
    }

    virtual http_handler::options_r
    options(bpstd::string_view target,
            bpstd::string_view body,
            headers_access&& get_headers) = 0;

    virtual void
    async_options(bpstd::string_view target,
                  bpstd::string_view body,
                  headers_access&& get_headers,
                  std::function<http_handler::callback_t<options_r>> callback) = 0;

    virtual http_handler::head_r
    head(bpstd::string_view target,
         headers_access&& get_headers) = 0;

    virtual void
    async_head(bpstd::string_view target,
               headers_access&& get_headers,
               std::function<http_handler::callback_t<head_r>> callback) = 0;

    virtual http_handler::get_r
    get(bpstd::string_view target,
        headers_access&& get_headers) = 0;

    virtual void
    async_get(bpstd::string_view target,
              headers_access&& get_headers,
              std::function<http_handler::callback_t<get_r>> callback) = 0;

    virtual http_handler::post_r
    post(bpstd::string_view target,
         bpstd::string_view body,
         headers_access&& get_headers) = 0;

    virtual void
    async_post(bpstd::string_view target,
               bpstd::string_view body,
               headers_access&& get_headers,
               std::function<http_handler::callback_t<post_r>> callback) = 0;

    virtual http_handler::put_r
    put(bpstd::string_view target,
        bpstd::string_view body,
        headers_access&& get_headers) = 0;

    virtual void
    async_put(bpstd::string_view target,
              bpstd::string_view body,
              headers_access&& get_headers,
              std::function<http_handler::callback_t<put_r>> callback) = 0;

    virtual http_handler::delete_r
    delete_(bpstd::string_view target,
            bpstd::string_view body,
            headers_access&& get_headers) = 0;

    virtual void
    async_delete_(bpstd::string_view target,
                  bpstd::string_view body,
                  headers_access&& get_headers,
                  std::function<http_handler::callback_t<delete_r>> callback) = 0;

private:
    bool use_async_;
};

}

#endif // HTTP_HANDLER_H
