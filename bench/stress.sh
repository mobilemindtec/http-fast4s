#!/bin/bash

#httperf \
#	--hog \
#	--server localhost \
#	--port 8181 \
#	--add-header "Accept: application/json\nContent-Type: application/json\n" \
#	--method POST \
#	--num-calls 50000 \
#	--num-conns=500 \
#	--wsesslog=50000,0,post-httperf.txt



# bombardier -l http://localhost:5151/json	

bombardier  \
	-l http://localhost:8181/json \
  -c 5000 \
  -d 30s \
	-m POST \
  -t 5s \
	-b  "{ \
          \"id\": 10, \
          \"name\": \"João Amoedo\" \
        }" \
  -H "Content-Type: application/json"
