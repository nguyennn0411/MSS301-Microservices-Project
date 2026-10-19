import http from 'node:http';
import {readFile} from 'node:fs/promises';
import {randomUUID} from 'node:crypto';
const files={'/':'index.html','/app.js':'app.js','/style.css':'style.css','/admin':'admin.html','/admin.js':'admin.js','/admin.css':'admin.css','/payment/success':'index.html','/payment/cancel':'index.html'};
const services={order:8084,payment:8085,product:8082};
const port=Number(process.env.PORT||5173);
http.createServer(async(req,res)=>{
 const url=new URL(req.url,'http://localhost');
 const sessionCookie=(req.headers.cookie||'').split(';').map(v=>v.trim()).find(v=>v.startsWith('mss_session='))?.slice(12);
 const session=/^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i.test(sessionCookie||'')?sessionCookie:randomUUID();
 // A cross-site PayOS return does not send a Strict cookie. Keep the existing
 // browser cookie untouched so same-site API requests can use that session.
 const isPaymentReturn=url.pathname==='/payment/success'||url.pathname==='/payment/cancel';
 if(session!==sessionCookie&&!isPaymentReturn)res.setHeader('Set-Cookie',`mss_session=${session}; HttpOnly; SameSite=Strict; Path=/; Max-Age=31536000`);
 res.setHeader('Cache-Control','no-store');
 const match=url.pathname.match(/^\/backend\/(order|payment|product)(\/.*)$/);
 try {
  if(match){
   if(!['GET','HEAD'].includes(req.method)&&req.headers.origin&&![`http://localhost:${port}`,`http://127.0.0.1:${port}`].includes(req.headers.origin)){res.writeHead(403).end();return;}
   const target=`http://127.0.0.1:${services[match[1]]}${match[2]}${url.search}`;
   let body='';for await(const chunk of req){body+=chunk;if(body.length>1000000){res.writeHead(413).end();return;}}
   const response=await fetch(target,{method:req.method,headers:{'Content-Type':'application/json','X-Shop-Session':session},body:['GET','HEAD'].includes(req.method)?undefined:body,signal:AbortSignal.timeout(45000)});
   res.writeHead(response.status,{'Content-Type':response.headers.get('content-type')||'application/json'});res.end(await response.text());return;
  }
  const file=files[url.pathname];if(!file){res.writeHead(404).end();return;}
  res.writeHead(200,{'Content-Type':file.endsWith('.js')?'text/javascript; charset=utf-8':file.endsWith('.css')?'text/css; charset=utf-8':'text/html; charset=utf-8'});res.end(await readFile(new URL(file,import.meta.url)));
 }catch{res.writeHead(502,{'Content-Type':'application/json'});res.end(JSON.stringify({message:'Không kết nối được dịch vụ. Vui lòng thử lại sau; không đặt lại nếu chưa kiểm tra lịch sử đơn.'}));}
}).listen(port,'127.0.0.1',()=>console.log(`MSS Studio: http://localhost:${port}`));
