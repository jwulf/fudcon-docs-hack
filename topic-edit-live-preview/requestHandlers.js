var xslt = require('node_xslt');
var url = require('url');
var http = require('http');
var fs = require('fs');
var path = require('path');
var exec = require ('child_process').exec, child;
var querystring = require('querystring');

function xmlPreview(response, request){
    
    console.log("xmlPreview handler called");
    var preview="<p>Could not transform</p>";
    console.log("Message was: " + request);
    //if (request !== "")
   // {
        var stylesheet = xslt.readXsltFile("/tmp/scratch/topic-edit-live-preview/xsl/html-single.xsl");
        try
        {var xmldocument = xslt.readXmlString(request.data.toString());
        preview = xslt.transform(stylesheet, xmldocument, []);
        //previewBody=preview..getElementsbyTagName("body");
        }
        catch (err) 
        {
            preview="<p>Could not transform</p>";
        }
        console.log("Transformed: " + preview);
        
         // http://stackoverflow.com/questions/8863179/enyo-is-giving-me-a-not-allowed-by-access-control-allow-origin-and-will-not-lo
        // http://www.wilsolutions.com.br/content/fix-request-header-field-content-type-not-allowed-access-control-allow-headers
        
        
        response.writeHead(200, {
            "Content-Type": "text/xml",
            "Access-Control-Allow-Origin":"*",
            "Access-Control-Allow-Headers": "Content-Type"
        });
        response.write(preview);
        response.end();
//    }
}

function topicValidate(response,request)
{
    console.log("topicValidate handler called");
    console.log(request.content);
    var dtdstring=("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<!DOCTYPE section PUBLIC \"-//OASIS//DTD DocBook XML V4.5//EN\"\n" +
        "\"http://www.oasis-open.org/docbook/xml/4.5/docbookx.dtd\" []>");
    var filenumber=1;
    while (path.existsSync("/tmp/topic"+ filenumber))
        filenumber++;
    var filename="/tmp/topic"+filenumber;
    var errorText="";
    fs.writeFile(filename, dtdstring + request.content, function(err){
        if(err) {
                console.log(err);
        } else {
            console.log("Saved topic file" + filename);
    }});

    var onFinish=function (error, stdout, stderr){
       errorText=errorText+stderr;
    }
    var command="xmllint --noout --valid " + filename;
    child = exec(command, onFinish);
    child.addListener("exit", function(code,signal){
        console.log("Exit code: "+ code);
         fs.unlink(filename, function(err)
        {
            if (err) {console.log(err);}
            else{console.log("Successfully deleted "+ filename);}
        });
         response.writeHead(200, {
            "Content-Type": "text/xml",
            "Access-Control-Allow-Origin":"*",
            "Access-Control-Allow-Headers": "Content-Type"
        });
        if (code===0)
        {
            response.write(code.toString());
        }
        else
        {
            response.write(errorText);
        }
        console.log("sending response");
        response.end();
    });
    
    
}

function proxyRESTPut(response,request){
    console.log("proxyRESTPut handler called.");
    //console.log("Request URL:" + request.request.url);
    
    var urlparsed=url.parse(request.request.url, true);
   // console.log("Parsed:" + urlparsed);
    var requestURL=urlparsed.query.requesturl;
    var serverURL=urlparsed.query.serverurl;
    var requestPort=urlparsed.query.requestport;
    var topicID=urlparsed.query.topicid;
   // console.log("TopicID:"+topicID + " Server: " + serverURL + " Port: " + requestPort + " URL: " + requestURL)
    //console.log("Content:" + request.content);
    if (requestURL && serverURL && requestPort)
    {
        var updateString="";
        var updateObject={
        'id' : topicID,
        'configuredParameters' : ['xml'],
        'xml' : request.content};
        updateString=JSON.stringify(updateObject);
            //console.log(updateString);
        var options={
                host: serverURL,
                path: requestURL,
                port: requestPort,
                method: "PUT",
                headers: {'Content-Type' : 'application/json'}
            };
        var req = http.request(options, function(res) {
           // console.log(res.headers);
            // console.log("Status code:" + res.statusCode);
            response.writeHead(res.statusCode, {
                "Content-Type": "text/plain",
                "Access-Control-Allow-Origin":"*",
                "Access-Control-Allow-Headers": "Content-Type"
            });
          res.setEncoding('utf8');
          res.on('data', function (chunk) {
            response.write(chunk);
            //console.log("Received from endpoint: " + chunk);
            });
            res.on('end', function(){
                console.log("ending response");
            response.end();
            })
        });
        req.on('error', function(e){console.log('problem with request: ' + e.message);
        });
        req.write(updateString);
        req.end();
    }
    
    
}

function proxyRESTGet(response, request){
    var requestURL;
    var serverURL;
    var requestPort;
    
    console.log("getTopic handler called");

    console.log(request);
        console.log(request.url);
    if (request.url)
    {
        var urlparsed=url.parse(request.url, true);
        console.log(urlparsed);
        if (urlparsed.query.requesturl)  requestURL=urlparsed.query.requesturl;
        if (urlparsed.query.serverurl) serverURL=urlparsed.query.serverurl;
        if (urlparsed.query.requestport) requestPort=urlparsed.query.requestport;
        if (requestURL && serverURL && requestPort)
        {
            var topic="";
            var options={
                host: serverURL,
                path: requestURL,
                port: requestPort
            };
            
             response.writeHead(200, {
                "Content-Type": "text/xml",
                "Access-Control-Allow-Origin":"*",
                "Access-Control-Allow-Headers": "Content-Type, X-Requested-With"
            });
            
            http.get(options, function(topicresponse){
              topicresponse.on('data', function(chunk){
               console.log("chunk: " + chunk);
               response.write(chunk);
              });
              topicresponse.on('end', function(){
                response.end();
              })
              
            }).on('error', function(e){
            console.log("got error: " +  e.message)
            });
        }
        else
        {
            response.writeHead(200, {
                "Content-Type": "text/plain",
                "Access-Control-Allow-Origin":"*",
                "Access-Control-Allow-Headers": "Content-Type"
            });
            response.write("Didn't get the right parameters to get your content.");
            response.end();
        }
    }
}

exports.xmlPreview=xmlPreview;
exports.proxyRESTGet=proxyRESTGet;
exports.topicValidate=topicValidate;
exports.proxyRESTPut=proxyRESTPut;