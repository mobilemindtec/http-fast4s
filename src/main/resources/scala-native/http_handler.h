#ifndef HTTP_HANDLER_H
#define HTTP_HANDLER_H

#include <cstdlib>
#include <optional>
#include <tuple>
#include <functional>
#include <unordered_map>
#include <string>


namespace httpserver {


using headers = std::unordered_map<std::string, std::string>;


class http_handler
{
public:

    using head_r = std::tuple<int, std::optional<headers>, size_t, std::string>;
    using get_r
        = std::tuple<int, std::optional<headers>, std::string, std::string>;
    using options_r      = get_r;
    using post_r         = get_r;
    using put_r          = std::tuple<int, std::optional<headers>>;
    using delete_r       = get_r;
    using headers_access = std::function<headers()>;

    template<typename resp_t>
    using callback_t = void(resp_t);

    typedef std::function<callback_t<get_r>> callback_get_async_t;


    http_handler() {}
    http_handler(bool use_async):use_async_(use_async) {}

    bool
    use_async(){
        return use_async_;
    }

    void
    use_async(bool b){
        use_async_ = b;
    }

    virtual options_r
    options(std::string_view target,
            std::string_view body,
            headers_access&& get_headers)
        = 0;

    virtual head_r
    head(std::string_view target, headers_access&& get_headers)
        = 0;

    virtual get_r
    get(std::string_view target, headers_access&& get_headers)
        = 0;

    virtual void
    async_get(std::string_view target, headers_access&& get_headers, std::function<callback_t<get_r>> callback)
        = 0;

    virtual post_r
    post(std::string_view target,
         std::string_view body,
         headers_access&& get_headers)
        = 0;

    virtual put_r
    put(std::string_view target,
        std::string_view body,
        headers_access&& get_headers)
        = 0;

    virtual delete_r
    delete_(std::string_view target,
            std::string_view body,
            headers_access&& get_headers)
        = 0;

private:
    bool use_async_ = false;
};

}

#endif // HTTP_HANDLER_H
