set(CMAKE_EXPORT_COMPILE_COMMANDS ON)

cmake_minimum_required(VERSION 3.5)

project(httpserver LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 11)
set(CMAKE_CXX_STANDARD_REQUIRED ON)




add_executable(httpserver
    httpserver.cpp

    asio/spawn.cpp
    asio/round_robin.hpp
    asio/yield.hpp
    asio/detail/yield.hpp

    httpserver_extern.h
    beast_server.cpp
    http_handler.h
    final_action.h
    http_handler_extern.h
    http_handler_extern.cpp
    httpserver.h
    http_handler_mock.h
    optional.h
    string_view.h
)

target_link_libraries(httpserver
    boost_thread
    boost_fiber
    boost_context
)

install(TARGETS httpserver
    LIBRARY DESTINATION ${CMAKE_INSTALL_LIBDIR}
    RUNTIME DESTINATION ${CMAKE_INSTALL_BINDIR}
)
