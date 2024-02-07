//
// Copyright (c) 2016-2019 Vinnie Falco (vinnie dot falco at gmail dot com)
// Portions Copyright (c) 2021 anticrisis <https://github.com/anticrisis>
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
// Official repository: https://github.com/boostorg/beast
//

//------------------------------------------------------------------------------
//
// Example: HTTP server, synchronous
//
// Changes by anticrisis marked with 'anticrisis'
//
//------------------------------------------------------------------------------


#include "httpserver.h"


// anticrisis: add namespace
namespace httpserver
{

// anticrisis: add thread_count
//std::atomic<int> thread_count;

//------------------------------------------------------------------------------
class http_session : public boost::enable_shared_from_this<http_session>{

public:

    typedef boost::shared_ptr<http_session> pointer;

    static pointer create(
        boost::asio::strand<boost::asio::io_context::executor_type> strand,
        http_handler* handler_ptr)
    {
        return pointer(new http_session(strand, handler_ptr));
    }

    tcp::socket& socket(){
        return this->stream_.socket();
    }

    void on_read(
        beast::error_code ec,
        std::size_t bytes_transferred)
    {
        boost::ignore_unused(bytes_transferred);

        // This means they closed the connection
        if(ec == http::error::end_of_stream)
            return do_close();

        if(ec)
            return fail(ec, "read");

        // Send the response
        handle_request(std::move(req_));
    }

    // Handles an HTTP server connection
    void start(){

        req_ = {};
        stream_.expires_after(std::chrono::seconds(30));

        http::async_read(
            stream_,
            buffer_,
            req_,
            beast::bind_front_handler(
                &http_session::on_read,
                shared_from_this()));
    }

private:
    http_session(boost::asio::strand<boost::asio::io_context::executor_type> strand, http_handler* handler_ptr)
        :strand_(strand),
        stream_(tcp::socket(strand)),
        deadline_timer_(strand),
        http_handler_(handler_ptr)
    {

    }


    void abort(){
        stream_.socket().cancel();
        stream_.close();
        //stream_.socket().close();
    }

    // Returns a bad request response
    http::message_generator bad_request(beast::string_view why) {
        http::response<http::string_body> res{ http::status::bad_request,
                                              req_.version() };
        res.set(http::field::server, BOOST_BEAST_VERSION_STRING);
        res.set(http::field::content_type, "text/plain");
        res.keep_alive(req_.keep_alive());
        res.body() = std::string(why);
        res.prepare_payload();
        return res;
    }

    // Returns a not found response
    http::message_generator not_found(beast::string_view target) {
        http::response<http::string_body> res{ http::status::not_found,
                                              req_.version() };
        res.set(http::field::server, BOOST_BEAST_VERSION_STRING);
        res.set(http::field::content_type, "text/plain");
        res.keep_alive(req_.keep_alive());
        res.body() = "The resource '" + std::string(target) + "' was not found.";
        res.prepare_payload();
        return res;
    }

    // Returns a server error response
    http::message_generator server_error(beast::string_view what) {
        http::response<http::string_body> res{ http::status::internal_server_error,
                                              req_.version() };
        res.set(http::field::server, BOOST_BEAST_VERSION_STRING);
        res.set(http::field::content_type, "text/plain");
        res.keep_alive(req_.keep_alive());
        res.body() = "An error occurred: '" + std::string(what) + "'";
        res.prepare_payload();
        return res;
    }

    // anticrisis
    void send_no_content(int status, tl::optional<headers>&& headers) {
        http::response<http::empty_body> res{ static_cast<http::status>(status),
                                             req_.version() };
        res.set(http::field::server, BOOST_BEAST_VERSION_STRING);
        if (headers)
            for (auto& kv: *headers)
            {
                res.base().set(kv.first, std::move(kv.second));
            }
        res.keep_alive(req_.keep_alive());
        res.prepare_payload();
        send_response(std::move(res));
    }

    void send_empty(int                      status,
                    tl::optional<headers>&& headers,
                    size_t                   content_size,
                    std::string&&            content_type) {
        http::response<http::empty_body> res{ static_cast<http::status>(status),
                                             req_.version() };
        res.set(http::field::server, BOOST_BEAST_VERSION_STRING);
        res.set(http::field::content_type, content_type);
        res.content_length(content_size);
        if (headers)
            for (auto& kv: *headers)
            {
                res.base().set(kv.first, std::move(kv.second));
            }
        res.keep_alive(req_.keep_alive());
        res.prepare_payload();
        send_response(std::move(res));
    }

    void send_body(int status,
                   tl::optional<headers>&& headers,
                   std::string&&            body,
                   std::string&&            content_type) {
        http::response<http::string_body> res{ static_cast<http::status>(status),
                                              req_.version() };
        res.set(http::field::server, BOOST_BEAST_VERSION_STRING);
        res.set(http::field::content_type, content_type.empty() ? "text/plain" : content_type);
        res.content_length(body.size());
        if (headers)
            for (auto& kv: *headers)
                res.base().set(kv.first, std::move(kv.second));
        res.body() = std::move(body);
        res.keep_alive(req_.keep_alive());
        res.prepare_payload();
        send_response(std::move(res));
    }

    void on_write(
        bool keep_alive,
        beast::error_code ec,
        std::size_t bytes_transferred)
    {
        boost::ignore_unused(bytes_transferred);

        if(ec)
            return fail(ec, "write");

        if(! keep_alive)
        {
            // This means we should close the connection, usually because
            // the response indicated the "Connection: close" semantic.
            return do_close();
        }

    }

    void do_close()
    {

        // Send a TCP shutdown
        beast::error_code ec;
        stream_.socket().shutdown(tcp::socket::shutdown_send, ec);

        // At this point the connection is closed gracefully
    }


    //template <bool isRequest, class Body, class Fields>
    void send_response(http::message_generator&& msg)
    {

        bool keep_alive = msg.keep_alive();

        beast::async_write(
            stream_,
            std::move(msg),
            beast::bind_front_handler(
                &http_session::on_write, shared_from_this(), keep_alive));
    }

    // This function produces an HTTP response for the given
    // request. The type of the response object depends on the
    // contents of the request, so the interface requires the
    // caller to pass a generic lambda for receiving the response.
    // anticrisis: remove support for doc_root and static files; add support for
    // http_handler
    template <class Body, class Allocator>
    void handle_request(http::request<Body, http::basic_fields<Allocator>>&& req)
    {


        const auto get_headers = [&]() {
            httpserver::headers hs;
            for (auto const& kv: req.base())
            {
                hs.emplace(kv.name_string(), kv.value());
            }
            return hs;
        };


        // Make sure we can handle the method
        // anticrisis: add methods
        if (req.method() != http::verb::get && req.method() != http::verb::head
            && req.method() != http::verb::post && req.method() != http::verb::put
            && req.method() != http::verb::delete_
            && req.method() != http::verb::options)
            return send_response(bad_request("Unknown HTTP-method"));

        // Request path must be absolute and not contain "..".
        if (req.target().empty() || req.target()[0] != '/'
            || req.target().find("..") != beast::string_view::npos)
            return send_response(bad_request("Illegal request-target"));

        // anticrisis: replace doc_root support with http_handler
        if (req.method() == http::verb::options)
        {
            auto data
                = http_handler_->options({ req.target().data(), req.target().size() },
                                         { req.body().data(), req.body().size() },
                                         std::move(get_headers));
            auto status = std::get<0>(data);
            auto headers = std::get<1>(data);
            auto body = std::get<2>(data);
            auto content_type = std::get<3>(data);

            return send_body(status,
                             std::move(headers),
                             std::move(body),
                             std::move(content_type));
        }
        else if (req.method() == http::verb::head)
        {
            auto data
                = http_handler_->head({ req.target().data(), req.target().size() },
                                      std::move(get_headers));

            auto status = std::get<0>(data);
            auto headers = std::get<1>(data);
            auto size = std::get<2>(data);
            auto content_type = std::get<3>(data);

            return send_empty(status,
                              std::move(headers),
                              size,
                              std::move(content_type));
        }
        else if (req.method() == http::verb::get)
        {
            bpstd::string_view target = { req.target().data(), req.target().size() };

            if(http_handler_->use_async()){

                return http_handler_->async_get(
                    target,
                    std::move(get_headers),
                    [this](http_handler::get_r resp){

                        auto status = std::get<0>(resp);
                        auto headers = std::get<1>(resp);
                        auto body = std::get<2>(resp);
                        auto content_type = std::get<3>(resp);

                        send_body(status,
                                         std::move(headers),
                                         std::move(body),
                                         std::move(content_type));
                    });

            } else {
                auto data
                    = http_handler_->get(target, std::move(get_headers));
                auto status = std::get<0>(data);
                auto headers = std::get<1>(data);
                auto body = std::get<2>(data);
                auto content_type = std::get<3>(data);
                return send_body(status,
                                 std::move(headers),
                                 std::move(body),
                                 std::move(content_type));
            }

        }
        else if (req.method() == http::verb::post)
        {
            auto data
                = http_handler_->post({ req.target().data(), req.target().size() },
                                      { req.body().data(), req.body().size() },
                                      std::move(get_headers));
            auto status = std::get<0>(data);
            auto headers = std::get<1>(data);
            auto body = std::get<2>(data);
            auto content_type = std::get<3>(data);
            return send_body(status,
                             std::move(headers),
                             std::move(body),
                             std::move(content_type));
        }
        else if (req.method() == http::verb::put)
        {
            auto data
                = http_handler_->put({ req.target().data(), req.target().size() },
                                     { req.body().data(), req.body().size() },
                                     std::move(get_headers));
            auto status = std::get<0>(data);
            auto headers = std::get<1>(data);

            return send_no_content(status, std::move(headers));
        }
        else if (req.method() == http::verb::delete_)
        {
            auto data
                = http_handler_->delete_({ req.target().data(), req.target().size() },
                                         { req.body().data(), req.body().size() },
                                         std::move(get_headers));
            auto status = std::get<0>(data);
            auto headers = std::get<1>(data);
            auto body = std::get<2>(data);
            auto content_type = std::get<3>(data);
            return send_body(status,
                             std::move(headers),
                             std::move(body),
                             std::move(content_type));
        }

        return send_response(server_error("not implemented."));
    }

    //------------------------------------------------------------------------------

    // Report a failure
    void fail(beast::error_code ec, char const* what)
    {
        // anticrisis: ignore these common errors
        if (ec == net::error::operation_aborted || ec == beast::error::timeout
            || ec == net::error::connection_reset)
            return;

        std::cerr << what << ": " << ec.message() << "\n";
    }


private:
    beast::tcp_stream stream_;
    boost::asio::deadline_timer deadline_timer_;
    boost::asio::strand<boost::asio::io_context::executor_type> strand_;
    http_handler* http_handler_;
    http::request<http::string_body> req_;
    beast::flat_buffer buffer_;
};

class http_server {

public:

    http_server(boost::asio::io_context& io, beast_handler* handler, const net::ip::address& address, unsigned short port)
        :acceptor_(io, {address, port}),
        strand_(boost::asio::make_strand(io)),
        http_handler_(handler)
    {

    }

    void serve(){


        auto http_handler = new http_handler_extern(http_handler_->sync, http_handler_->async);

        if(http_handler_->async != NULL){
            http_handler->use_async(true);
        }

        auto session = http_session::create(strand_, http_handler);

        //std::cout << "waiting by new connections..." << std::endl;

        acceptor_.async_accept(
            session->socket(),
            boost::bind(
                &http_server::accept,
                this,
                session,
                boost::asio::placeholders::error));

    }

private:
    void accept(http_session::pointer& session, const boost::system::error_code& ec){

        if(!ec){
            session->start();
        }
        serve();
    }

    tcp::acceptor acceptor_;
    boost::asio::strand<boost::asio::io_context::executor_type> strand_;
    beast_handler* http_handler_;
};


// anticrisis: change main to run; remove doc_root
int run(char* address_,
        unsigned short   port,
        unsigned short   max_thread_count,
        beast_handler* handler){
    try
    {
        //thread_count = 0;

        auto const address = net::ip::make_address(address_);
        std::vector<std::unique_ptr<boost::thread>> thread_pool;

        // The io_context is required for all I/O
        boost::asio::io_context io;

        for(unsigned short i = 0; i < max_thread_count; i++){
            std::unique_ptr<boost::thread> t(new boost::thread(boost::bind(&boost::asio::io_context::run, &io)));
            thread_pool.push_back(std::move(t));
        }

        boost::shared_ptr<http_server> server(new http_server(io, handler, address, port));

        std::cout << "http server at http://" << address_ << ":" << port << " with " << max_thread_count << " threads" << std::endl;


        server->serve();

        io.run();
        for (auto& th : thread_pool)
            th->join();

        return 0;

    }
    catch (const std::exception& e)
    {
        std::cerr << "Error: " << e.what() << std::endl;
        return EXIT_FAILURE;
    }
    return EXIT_SUCCESS;
}

//------------------------------------------------------------------------------

class http_handler_mock : public http_handler {

    bool use_async(){
        return true;
    }

    http_handler::options_r options(bpstd::string_view target, bpstd::string_view body, headers_access&& get_headers) override {
        return { 404, headers{}, "", "" };
    }

    http_handler::head_r head(bpstd::string_view target, headers_access&& get_headers) override {
        return { 404, headers{}, 0, "" };
    }

    http_handler::get_r get(bpstd::string_view target, headers_access&& get_headers) override {
        return { 200, headers{}, "", "" };
    }

    void async_get(bpstd::string_view target, headers_access&& get_headers, std::function<callback_t<http_handler::get_r>> callback) override {
        //std::cout << "use async get " << std::endl;
        callback({ 200, headers{}, "", "" });
    }


    http_handler::post_r post(bpstd::string_view target, bpstd::string_view body, headers_access&& get_headers) override {
        return { 404, headers{}, "", "" };
    }

    http_handler::put_r put(bpstd::string_view target, bpstd::string_view body, headers_access&& get_headers) override{
        return { 404, headers{} };
    }

    http_handler::delete_r delete_(bpstd::string_view target, bpstd::string_view body, headers_access&& get_headers) override {
        return { 404, headers{}, "", "" };
    }
};

} // namespace httpserver


