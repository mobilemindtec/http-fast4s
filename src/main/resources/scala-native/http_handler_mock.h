#ifndef HTTP_HANDLER_MOCK_H
#define HTTP_HANDLER_MOCK_H

#include "http_handler.h"
#include "http_handler_extern.h"

namespace httpserver{
class http_handler_mock : public http_handler {

    bool use_async(){
        return true;
    }

    http_handler::options_r options(std::string_view target, std::string_view body, headers_access&& get_headers) override {
        return { 404, headers{}, "", "" };
    }

    http_handler::head_r head(std::string_view target, headers_access&& get_headers) override {
        return { 404, headers{}, 0, "" };
    }

    http_handler::get_r get(std::string_view target, headers_access&& get_headers) override {
        return { 200, headers{}, "", "" };
    }

    void async_get(std::string_view target, headers_access&& get_headers, std::function<callback_t<http_handler::get_r>> callback) override {
        //std::cout << "use async get " << std::endl;
        callback({ 200, headers{}, "", "" });
    }


    http_handler::post_r post(std::string_view target, std::string_view body, headers_access&& get_headers) override {
        return { 404, headers{}, "", "" };
    }

    http_handler::put_r put(std::string_view target, std::string_view body, headers_access&& get_headers) override{
        return { 404, headers{} };
    }

    http_handler::delete_r delete_(std::string_view target, std::string_view body, headers_access&& get_headers) override {
        return { 404, headers{}, "", "" };
    }
};
}

#endif // HTTP_HANDLER_MOCK_H