var http = require("http");
var url = require("url");

function start(route, handle) {

    function onRequest(request, response) {
        var postData="";
        var pathname=url.parse(request.url).pathname;
    //    console.log("Request for " + pathname + " received.");
        console.log("request:" + request);
        request.setEncoding("utf8");
        if (request.method == "GET")
        {
            console.log("GET Request")
           route(handle, pathname, response, request); 
        }
        else
        {
            request.addListener("data", function(postDataChunk){
              postData += postDataChunk;
             console.log("chunk received: '" + postDataChunk +"'.");
            })
            
            request.addListener("end",function(){
                console.log("end received");
                var data={"request" : request, "content" : postData}
                console.log(data);
                route(handle, pathname, response, data);
            })
            
            
        }
       
       
    }

    http.createServer(onRequest).listen(8888);
    console.log("Server Started");
}

exports.start = start;
