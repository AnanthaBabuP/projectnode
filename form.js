var http = require('http');
var fs = require('fs');
var formidable = require('formidable');
http.createServer(function(req,res){
    if(req.url == '/')
    {
        res.writeHead(200,{'content-type': 'text/html'});
        res.write('<form action ="bio_data" method = "post" enctype ="multipart/form-data" >');
        res.write('<h1> ANANTHABABU NODE PROJECT </h1>');
        res.write('NAME : <input type = "text" name = "userName"><br>');
        res.write('DOB : <input type = "date" name = "dob"><br>');
        res.write('QUALIFICATION : <input type = "text" name = "qualification"><br>');
        res.write('E_MAIL : <input type = "email" name = "email"><br>');
        res.write('PHONE NO : <input type = "text" name = "phoneNo"><br>');
        res.write('UPLOAD FILE: <input type = "file" name = "uploadFile"><br>');
        res.write('<input type = "submit">')
        res.end();
    }
}).listen(8080);