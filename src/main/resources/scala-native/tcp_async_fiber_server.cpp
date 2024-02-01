#include <functional>
#include <iostream>
#include <memory>
#include <utility>
#include <boost/asio.hpp>
#include <boost/bind.hpp>
#include <boost/thread.hpp>
#include <boost/fiber/all.hpp>

//#include "asio/all.h"


using boost::asio::ip::tcp;
using error_code = boost::system::error_code;
using byteresult = std::tuple<std::string, error_code>;
template <typename T>
using future = boost::fibers::future<T>;
template <typename T>
using promise = boost::fibers::promise<T>;

class session_f : public std::enable_shared_from_this<session_f> {

public:
    session_f(tcp::socket socket): socket_(std::move(socket)){}

    std::string read_some(size_t size){
        promise<size_t> p;
        future<size_t> f(p.get_future());
        std::string data(size, 0);

        socket_.async_read_some(
            boost::asio::buffer(data, size),
            [p = std::move(p)](error_code ec, size_t read) mutable {
                std::cout << "read_some " << ec << ", size " << read << std::endl;
                if(!ec)
                    p.set_value(read);
                else
                    p.set_exception(std::exception_ptr(std::make_exception_ptr(ec)));
                std::cout << "read_some OK" << std::endl;
            });
        std::cout << "read block" << std::endl;


        try {
            size_t read = f.get();
            std::cout << "read block done" << std::endl;
            data.resize(read);
            return data;
        }catch(const boost::system::error_code e){
            std::cerr << "read error!\n";
            return "";
        }
    }

    void write(std::string data){
        promise<size_t> p;
        future<size_t> f(p.get_future());


        boost::asio::async_write(
            socket_, boost::asio::buffer(data, data.size()),
            [p = std::move(p)](error_code ec, size_t written) mutable {
                std::cout << "write " << ec << std::endl;
                if (!ec)
                    p.set_value(written);
                else
                    p.set_exception(std::exception_ptr(std::make_exception_ptr(ec)));
            });

        std::cout << "write block" << std::endl;
        size_t w = f.get();
        assert(w == data.size());
        std::cout << "write block done!" << std::endl;
        socket_.shutdown(tcp::socket::shutdown_send);
    }

private:
    tcp::socket socket_;


};


class server_f {
public:
    using callback_t = void(std::shared_ptr<session_f>);

    server_f(boost::asio::io_context& io, short port, std::function<callback_t> handler)
        :acceptor_(io, tcp::endpoint(tcp::v4(), port)),
        callback_(std::move(handler)){

    }

    void listen(){
        std::cout << "server listen..." << std::endl;
        do_accept();
    }


private:

    void do_accept(){      
        acceptor_.async_accept(
            [this](boost::system::error_code ec, tcp::socket socket){
                if(!ec){
                    std::cout << "do_accept " << ec << std::endl;
                    auto s  = std::make_shared<session_f>(std::move(socket));

                    boost::fibers::fiber([c = callback_, s = std::move(s)]() {
                        std::cout << "do_accept fiber!" << std::endl;                        
                        c(s);
                    }).detach();

                }
                do_accept();                
            });
    }

    tcp::acceptor acceptor_;
    std::function<callback_t> callback_;
};



void start_tcp_async_fiber_server(){



    try{

        boost::asio::io_context io;
        //boost::fibers::use_scheduling_algorithm<boost::fibers::asio::round_robin>(io);

        server_f s(io, 8181, [](std::shared_ptr<session_f> s){
            std::cout << "new client [fiber: "
                      << boost::this_fiber::get_id()
                      << "][thread " << std::this_thread::get_id()
                      << "]\n";
            std::cout.flush();
            try{
                while(true){
                    std::string data = s->read_some(1000);
                    std::cout << "recv: " << data << "\n";
                    if(!data.empty())
                        s->write("HTTP/1.0 200 OK\nContent-Type: text/plain\n\n");
                }
            }catch(const std::exception e){
                std::cerr << "caught exception: " << e.what() << "\n";
            }
        });
        s.listen();
        io.run();
        //boost::thread t1(boost::bind(&boost::asio::io_context::run, &io));
        //boost::thread t2(boost::bind(&boost::asio::io_context::run, &io));
        //boost::thread t3(boost::bind(&boost::asio::io_context::run, &io));
        //boost::thread t4(boost::bind(&boost::asio::io_context::run, &io));
        //boost::thread t5(boost::bind(&boost::asio::io_context::run, &io));
        //boost::thread t6(boost::bind(&boost::asio::io_context::run, &io));
        //boost::thread t7(boost::bind(&boost::asio::io_context::run, &io));

        //t1.join();
        //t2.join();
        //t3.join();
        //t4.join();
        //t5.join();
        //t6.join();
        //t7.join();

    }catch(const std::exception e){
        std::cerr << "Exception: " << e.what() << "\n";
    }

}


