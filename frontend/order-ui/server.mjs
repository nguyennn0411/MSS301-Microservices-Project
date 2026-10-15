import http from 'node:http';
import {readFile} from 'node:fs/promises';
import {randomUUID} from 'node:crypto';
const files={'/':'index.html','/app.js':'app.js','/account.js':'account.js','/style.css':'style.css','/account.css':'account.css','/admin':'admin.html','/admin.js':'admin.js','/admin.css':'admin.css','/admin/products':'management.html','/admin/users':'management.html','/management.js':'management.js','/management.css':'management.css','/payment/success':'index.html','/payment/cancel':'index.html'};
const services={order:8084,payment:8085,product:8082,user:8081};
http.createServer(async(req,res)=>{
 const url=new URL(req.url,'http://localhost');
 const sessionCookie=(req.headers.cookie||'').split(';').map(v=>v.trim()).find(v=>v.startsWith('mss_session='))?.slice(12);
 const session=/^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i.test(sessionCookie||'')?sessionCookie:randomUUID();
 if(session!==sessionCookie)res.setHeader('Set-Cookie',`mss_session=${session}; HttpOnly; SameSite=Strict; Path=/; Max-Age=31536000`);
 res.setHeader('Cache-Control','no-store');
 const match=url.pathname.match(/^\/backend\/(order|payment|product|user)(\/.*)$/);
 try {
  if(match){
   if(!['GET','HEAD'].includes(req.method)&&req.headers.origin&&!['http://localhost:5173','http://127.0.0.1:5173'].includes(req.headers.origin)){res.writeHead(403).end();return;}
   const target=`http://127.0.0.1:${services[match[1]]}${match[2]}${url.search}`;
   let body='';for await(const chunk of req){body+=chunk;if(body.length>1000000){res.writeHead(413).end();return;}}
   const headers={'Content-Type':'application/json'};
   if(match[1]==='order')headers['X-Shop-Session']=session;
   if(['user','product'].includes(match[1])&&req.headers.authorization)headers.Authorization=req.headers.authorization;
   const response=await fetch(target,{method:req.method,headers,body:['GET','HEAD'].includes(req.method)?undefined:body,signal:AbortSignal.timeout(45000)});
   res.writeHead(response.status,{'Content-Type':response.headers.get('content-type')||'application/json'});res.end(await response.text());return;
  }
  const file=files[url.pathname];if(!file){res.writeHead(404).end();return;}
  res.writeHead(200,{'Content-Type':file.endsWith('.js')?'text/javascript; charset=utf-8':file.endsWith('.css')?'text/css; charset=utf-8':'text/html; charset=utf-8'});res.end(await readFile(new URL(file,import.meta.url)));
 }catch{res.writeHead(502,{'Content-Type':'application/json'});res.end(JSON.stringify({message:'Không kết nối được dịch vụ. Vui lòng thử lại sau; không đặt lại nếu chưa kiểm tra lịch sử đơn.'}));}
}).listen(5173,'127.0.0.1',()=>console.log('MSS Studio: http://localhost:5173'));
