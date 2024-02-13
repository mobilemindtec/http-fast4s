#ifndef HTTP_HANDLER_H
#define HTTP_HANDLER_H

#include <cstdlib>
#include <tuple>
#include <functional>
#include <unordered_map>
#include <string>

#include "optional.h"
#include "string_view.h"
#include "beast_server.h"

namespace httpserver {


class http_handler
{
public:

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

    virtual response_t*
    dispatch(request_t*) = 0;

    virtual void
    dispatch_async(request_t*, std::function<callback_t<response_t*>>) = 0;


private:
    bool use_async_;
};

}

#endif // HTTP_HANDLER_H
