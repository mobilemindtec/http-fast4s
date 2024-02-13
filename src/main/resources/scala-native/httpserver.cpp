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

    void send_body(int status,
                   tl::optional<std::unordered_map<std::string, std::string>>&& headers,
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


    void create_string_response(response_t* response) {

        //std::cout << "create_string_response" << std::endl;

        http::response<http::string_body> res{ static_cast<http::status>(response->status_code),
                                              req_.version() };

        res.set(http::field::server, BOOST_BEAST_VERSION_STRING);
        res.set(http::field::content_type, response->content_type);

        headers_t* headers = response->headers;

        if(headers != NULL){
            int size = headers->size;
            header_t* hs = headers->headers;
            for(int i = 0; i < size; i++){
                std::string name {hs->name};
                std::string value {hs->value};
                res.base().set(name, std::move(value));
                hs++;
            }
        }

        body_t* body = response->body;
        if(body != NULL) {
            res.content_length(body->size);
            if(body->body != NULL){
                auto sbody = std::string { body->body };
                res.body() = std::move(sbody);
            }
        }

        res.keep_alive(req_.keep_alive());
        res.prepare_payload();
        send_response(std::move(res));
    }

    void create_buffer_response(response_t* response) {      
        http::response<http::buffer_body> res{ static_cast<http::status>(response->status_code),
                                              req_.version() };

        res.set(http::field::server, BOOST_BEAST_VERSION_STRING);
        res.set(http::field::content_type, response->content_type);
        res.content_length(response->body->size);

        headers_t* headers = response->headers;

        if(headers != NULL){
            int size = headers->size;
            header_t* hs = headers->headers;
            for(int i = 0; i < size; i++){
                res.base().set(hs->name, hs->value);
                hs++;
            }
        }

        //http::buffer_body buffer(response->body->body_raw, response->body->size);
        //boost::asio::buffer buffer();

        res.body().data = (char *)response->body->body_raw;
        res.body().size = response->body->size;

        res.keep_alive(req_.keep_alive());
        res.prepare_payload();
        send_response(std::move(res));
    }

    void send_response_t(response_t* response) {

        bool body_bytes = response->body->body_raw != NULL;

        if(body_bytes)
            create_buffer_response(response);
        else
            create_string_response(response);





        //http::response<http::string_body> res{ static_cast<http::status>(raw->status_code),
        //                                      req_.version() };
        //res.set(http::field::server, BOOST_BEAST_VERSION_STRING);
        //res.set(http::field::content_type, response->content_type);
        //res.content_length(body.size());
        ////if (headers)
        ////    for (auto& kv: *headers)
        ////        res.base().set(kv.first, std::move(kv.second));


        //res.keep_alive(req_.keep_alive());
        //res.prepare_payload();
        //send_response(std::move(res));
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

    std::string get_verb(http::verb verb){
        switch(verb) {
        case http::verb::get:
            return "GET";
            break;
        case http::verb::post:
            return "POST";
            break;
        case http::verb::put:
            return "PUT";
            break;
        case http::verb::delete_:
            return "DELETE";
            break;
        case http::verb::head:
            return "HEAD";
            break;
        case http::verb::options:
            return "OPTIONS";
            break;
        case http::verb::patch:
            return "PATCH";
            break;
        default:
            return "";
        }
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

        std::string verb = get_verb(req.method());

        if(verb.empty()){
            return send_response(bad_request("Unknown HTTP-method"));
        }

        std::string target = std::string { req.target().data(), req.target().size() };
        std::string body_str = std::string { req.body().data(), req.body().size() };
        int body_size = body_str.size();

        //auto body_data = req.body().data();
        //const auto buffer_bytes = buffer_.cdata();


        //const char* body_raw = static_cast<const char*>(buffer_bytes.data());

        request_t* request = request_new(verb.c_str(), target.c_str());

        std::vector<std::pair<std::string, std::string>> hs;
        for (auto const& kv: req.base()){

            auto name = std::string{ kv.name_string().data(), kv.name_string().size() };
            auto value = std::string{ kv.value().data(), kv.value().size() };

            if(name == "Content-Type" || name == "content-type") {
                request->content_type = value.c_str();
            }

            hs.push_back({name, value});
        }

        int hsize = hs.size();
        if(hsize > 0){
            request->headers = headers_new(hsize);
            header_t* headers = (header_t*) malloc(sizeof(header_t)*hsize);

            for(int i = 0; i < hsize; i++){
                headers[i].name = hs[i].first.c_str();
                headers[i].value = hs[i].second.c_str();
            }

            request->headers->headers = headers;
        }

        if(body_size > 0){
            body_t* body = body_new(body_str.c_str(), NULL, body_size);
            request->body = body;
        }


        if(http_handler_->use_async()) {
            return http_handler_->dispatch_async(request, [this](response_t* resp){
                send_response_t(resp);
            });
        } else {            
            response_t* resp = http_handler_->dispatch(request);
            return send_response_t(resp);
        }
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

    http_server(boost::asio::io_context& io, beast_handler_t* handler, const net::ip::address& address, unsigned short port)
        :acceptor_(io, {address, port}),
        strand_(boost::asio::make_strand(io)),
        http_handler_(handler)
    {

    }

    void serve(){


        auto http_handler = new http_handler_impl(
            http_handler_->sync,
            http_handler_->async);

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
    beast_handler_t* http_handler_;
};


// anticrisis: change main to run; remove doc_root
int run(char* address_,
        unsigned short   port,
        unsigned short   max_thread_count,
        beast_handler_t* handler){


    try
    {
        //thread_count = 0;

        auto const address = net::ip::make_address(address_);
        std::vector<std::unique_ptr<boost::thread>> thread_pool;

        // The io_context is required for all I/O
        boost::asio::io_context io;

        net::signal_set signals(io, SIGINT, SIGTERM);
        signals.async_wait(
            [&](beast::error_code const&, int)
            {
                // Stop the `io_context`. This will cause `run()`
                // to return immediately, eventually destroying the
                // `io_context` and all of the sockets in it.
                std::cout << "Server stopping.." << std::endl;
                io.stop();
            });


        for(unsigned short i = 0; i < max_thread_count - 1; i++){
            std::unique_ptr<boost::thread> t(new boost::thread(boost::bind(&boost::asio::io_context::run, &io)));
            thread_pool.push_back(std::move(t));
        }

        boost::shared_ptr<http_server> server(new http_server(io, handler, address, port));

        std::cout << "http server at http://" << address_ << ":" << port << " with " << max_thread_count << " threads" << std::endl;


        server->serve();

        io.run();
        for (auto& th : thread_pool)
            th->join();

    }
    catch (const std::exception& e)
    {
        std::cerr << "Error: " << e.what() << std::endl;
        return EXIT_FAILURE;
    }
    return EXIT_SUCCESS;
}

} // namespace httpserver


