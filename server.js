const http = require("http");
const fs = require("fs");
const path = require("path");

const pagePath = path.join(__dirname, "index.html");

http.createServer((request, response) => {
    if (request.method !== "GET" || request.url !== "/") {
        response.writeHead(404);
        response.end("Not found");
        return;
    }

    response.writeHead(200, { "Content-Type": "text/html; charset=utf-8" });
    fs.createReadStream(pagePath).pipe(response);
}).listen(8080, "127.0.0.1", () => {
    console.log("ParkSmart available at http://localhost:8080");
});
