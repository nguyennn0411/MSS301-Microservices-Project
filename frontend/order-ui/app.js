const $=selector=>document.querySelector(selector);
const money=value=>new Intl.NumberFormat('vi-VN').format(value??0)+'₫';
const esc=value=>String(value??'').replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
const safeUrl=value=>{try{const url=new URL(value);return ['https:','http:'].includes(url.protocol)?url.href:'';}catch{return '';}};
const safePaymentUrl=value=>{try{const url=new URL(value);return url.protocol==='https:'&&(url.hostname==='payos.vn'||url.hostname.endsWith('.payos.vn'))?url.href:'';}catch{return '';}};
let products=[],cart=[],step=1,order=null,busy=false,ready=false,shippingFee=0,view='store',filter='all';
let paymentConfirmed=false,paymentStatus=null,paymentMessage='';
let requestKey=sessionStorage.getItem('mss-checkout-key')||crypto.randomUUID();
const product=id=>products.find(p=>p.id===id);
const count=()=>cart.reduce((sum,i)=>sum+i.quantity,0);
const subtotal=()=>cart.reduce((sum,i)=>sum+product(i.productId).price*i.quantity,0);
const variant=item=>product(item.productId).variants.find(v=>v.id===item.variantId);
function options(values,selected){return values.map(v=>`<option ${v===selected?'selected':''}>${esc(v)}</option>`).join('');}
async function api(service,path,body,method=body===undefined?'GET':'POST'){
 const response=await fetch(`/backend/${service}${path}`,{method,headers:{'Content-Type':'application/json'},body:body===undefined?undefined:JSON.stringify(body),signal:AbortSignal.timeout(50000)});
 const result=await response.json();if(!response.ok||result.success===false)throw new Error(result.message||'Không thể thực hiện yêu cầu.');return result.data;
}
function notice(text){$('#notice').innerHTML=text?`<div class="error">${esc(text)}</div>`:'';let toast=$('#store-toast');if(!toast){toast=document.createElement('div');toast.id='store-toast';toast.setAttribute('role','status');document.body.append(toast);}toast.textContent=text||'';toast.hidden=!text;}
function lock(value){busy=value;document.querySelectorAll('button,input,select,textarea').forEach(el=>{if(el.dataset.permanentDisabled!=='true')el.disabled=value;});if(!value&&ready)renderCart();else updateSummary();}
function renderCart(){
 $('#bag-count').textContent=count();$('#item-count').textContent=`${String(count()).padStart(2,'0')} sản phẩm`;
 $('#cart-items').innerHTML=cart.length?cart.map(item=>{const p=product(item.productId),v=variant(item);const colors=[...new Set(p.variants.map(v=>v.color))];const sizes=p.variants.filter(i=>i.color===v.color).map(i=>i.size);return `<article class="cart-item" data-key="${item.variantId}"><img class="product-image" src="${esc(safeUrl(p.image))}" alt="${esc(p.name)}"><div class="product-meta"><div class="item-top"><div><p>${esc(p.collection)}</p><h3>${esc(p.name)}</h3></div><button class="remove" data-action="remove" aria-label="Xóa ${esc(p.name)}">×</button></div><div class="variant-row"><label>Màu<select data-variant="color" aria-label="Màu ${esc(p.name)}">${options(colors,v.color)}</select></label><label>Size<select data-variant="size" aria-label="Size ${esc(p.name)}">${options(sizes,v.size)}</select></label></div><div class="item-bottom"><div class="quantity"><button data-action="minus" aria-label="Giảm số lượng" ${item.quantity===1?'disabled':''}>−</button><span>${item.quantity}</span><button data-action="plus" aria-label="Tăng số lượng" ${item.quantity>=10?'disabled':''}>+</button></div><strong>${money(p.price*item.quantity)}</strong></div></div></article>`;}).join(''):'<div class="empty-cart"><h3>Giỏ hàng đang trống.</h3><p>Chọn một món đồ cho ngày mới của bạn.</p><button class="primary" id="empty-shop">Khám phá bộ sưu tập ↗</button></div>';
 if($('#empty-shop'))$('#empty-shop').onclick=showStore;updateSummary();
}
function updateSummary(){
 const amount=step===3&&order?order.totalAmount-order.shippingFee:subtotal();const fee=step===3&&order?order.shippingFee:step===1||!cart.length?0:shippingFee;
 $('#subtotal').textContent=money(amount);$('#shipping-total').textContent=step===1?'Tính ở bước tiếp theo':money(fee);$('#grand-total').textContent=money(amount+fee);
 $('#summary-lines').innerHTML=step===2?cart.map(item=>{const v=variant(item),p=product(item.productId);return `<div class="summary-mini"><span>${esc(p.name)}<small>${esc(v.color)} / ${esc(v.size)} / SL ${item.quantity}</small></span><span>${money(p.price*item.quantity)}</span></div>`;}).join(''):'';
 const button=$('#continue');button.disabled=busy||!ready||(step!==3&&!cart.length);button.textContent=busy?'Đang xử lý…':step===1?'Tiếp tục thanh toán →':step===2?'Đặt hàng →':'Tiếp tục mua sắm ↗';
}
function showStore(scroll=false){view='store';paymentConfirmed=false;paymentStatus=null;paymentMessage='';$('#store-view').hidden=false;$('#checkout-shell').hidden=true;notice('');window.scrollTo({top:scroll?$('#collection').getBoundingClientRect().top+window.scrollY-80:0,behavior:'smooth'});}
function setStep(next){view='checkout';step=next;notice('');$('#store-view').hidden=true;$('#checkout-shell').hidden=false;$('#cart-view').hidden=next!==1;$('#delivery-view').hidden=next!==2;$('#complete-view').hidden=next!==3;document.querySelectorAll('[data-step]').forEach(el=>el.classList.toggle('active',Number(el.dataset.step)===next));updateSummary();window.scrollTo({top:0,behavior:'smooth'});}
async function saveCart(next){
 if(busy)return;lock(true);notice('');try{cart=await api('order','/api/shop/cart',{items:next},'PUT');requestKey=crypto.randomUUID();sessionStorage.removeItem('mss-checkout-key');renderCart();return true;}catch(error){notice(error.message);renderCart();return false;}finally{lock(false);renderCart();}
}
$('#cart-items').onclick=async event=>{const button=event.target.closest('[data-action]');if(!button||busy)return;const id=button.closest('[data-key]').dataset.key;const next=cart.map(i=>({...i}));const item=next.find(i=>i.variantId===id);if(button.dataset.action==='remove'){await saveCart(next.filter(i=>i.variantId!==id));return;}if(button.dataset.action==='plus'&&item.quantity<10)item.quantity++;if(button.dataset.action==='minus'&&item.quantity>1)item.quantity--;await saveCart(next);};
$('#cart-items').onchange=async event=>{const field=event.target.dataset.variant;if(!field||busy)return;const id=event.target.closest('[data-key]').dataset.key;const next=cart.map(i=>({...i})),item=next.find(i=>i.variantId===id),p=product(item.productId),v=variant(item);
 const chosen=field==='color'?(p.variants.find(i=>i.color===event.target.value&&i.size===v.size)||p.variants.find(i=>i.color===event.target.value)):p.variants.find(i=>i.color===v.color&&i.size===event.target.value);
 const duplicate=next.find(i=>i.variantId===chosen.id&&i!==item);if(duplicate){if(duplicate.quantity+item.quantity>10){notice('Tối đa 10 sản phẩm cho mỗi biến thể.');renderCart();return;}duplicate.quantity+=item.quantity;await saveCart(next.filter(i=>i!==item));}else{item.variantId=chosen.id;await saveCart(next);}};
function renderCatalog(){
 $('#catalog').innerHTML=products.length?products.map(p=>{const v=p.variants[0];return `<article class="catalog-card" data-product="${p.id}"><img src="${esc(safeUrl(p.image))}" alt="${esc(p.name)}" loading="lazy"><h3>${esc(p.name)}</h3><p>${money(p.price)}</p><div class="variant-row"><label>Màu<select class="catalog-color">${options([...new Set(p.variants.map(i=>i.color))],v.color)}</select></label><label>Size<select class="catalog-size">${options(p.variants.filter(i=>i.color===v.color).map(i=>i.size),v.size)}</select></label></div><button class="primary add-product">Thêm vào giỏ <span>+</span></button></article>`;}).join(''):'<p>Chưa có sản phẩm đang bán.</p>';
}
$('#catalog').onchange=event=>{if(!event.target.matches('.catalog-color'))return;const card=event.target.closest('[data-product]'),p=product(card.dataset.product);card.querySelector('.catalog-size').innerHTML=options(p.variants.filter(v=>v.color===event.target.value).map(v=>v.size),'');};
$('#catalog').onclick=async event=>{const button=event.target.closest('.add-product');if(!button||busy)return;const card=button.closest('[data-product]'),p=product(card.dataset.product),v=p.variants.find(v=>v.color===card.querySelector('.catalog-color').value&&v.size===card.querySelector('.catalog-size').value);const next=cart.map(i=>({...i})),existing=next.find(i=>i.variantId===v.id);if(existing&&existing.quantity>=10){button.textContent='Tối đa 10 sản phẩm';return;}if(existing)existing.quantity++;else next.push({productId:p.id,variantId:v.id,quantity:1});if(await saveCart(next)){if(step===3)setStep(1);$('#catalog-dialog').close();}};
document.querySelectorAll('.catalog-link').forEach(button=>button.onclick=()=>$('#catalog-dialog').showModal());document.querySelectorAll('dialog .close').forEach(button=>button.onclick=()=>button.closest('dialog').close());
$('#size-open').onclick=()=>$('#size-dialog').showModal();$('#bag-link').onclick=()=>setStep(1);$('#back-cart').onclick=()=>setStep(1);
$('#continue').onclick=()=>{if(step===1&&cart.length)setStep(2);else if(step===2)$('#checkout-form').requestSubmit();else if(step===3){order=null;renderCart();showStore(true);}};
$('#checkout-form').onsubmit=async event=>{event.preventDefault();if(busy||!cart.length)return;const data=Object.fromEntries(new FormData(event.target));lock(true);notice('');try{
 sessionStorage.setItem('mss-checkout-key',requestKey);
 order=await api('order','/api/shop/checkout',{buyerName:data.name.trim(),buyerEmail:data.email.trim(),buyerPhone:data.phone.trim(),shippingAddress:data.address.trim(),customerNote:data.note.trim(),paymentMethod:data.payment.toUpperCase(),requestKey});
 paymentConfirmed=false;paymentStatus=null;paymentMessage='';
 cart=[];sessionStorage.removeItem('mss-checkout-key');requestKey=crypto.randomUUID();
 if(data.payment==='payos'&&order.status==='PENDING_PAYMENT'){
  const checkoutUrl=safePaymentUrl(order.checkoutUrl);
  if(checkoutUrl){sessionStorage.setItem('mss-payos-order-code',String(order.orderCode));location.assign(checkoutUrl);return;}
  paymentMessage='Không mở được trang PayOS. Đơn hàng đã được lưu; bạn có thể mở lại thanh toán từ bên dưới.';
 }
 setStep(3);showComplete();renderCart();
 }catch(error){notice(error.message+' Bạn có thể kiểm tra lịch sử đơn trước khi thử lại.');}finally{lock(false);}};
function row(label,value){return `<div class="detail-row"><span>${esc(label)}</span><span>${esc(value)}</span></div>`;}
function stateLabel(value){return ({CONFIRMED:'Đã xác nhận · thanh toán khi nhận hàng',PENDING_PAYMENT:'Chờ thanh toán',PAID:'Đã thanh toán',CANCELLED:'Đã hủy',PAYMENT_FAILED:'Khởi tạo thanh toán thất bại',REFUNDED:'Đã hoàn tiền'})[value]||value;}
function orderDetails(data){const status=paymentConfirmed&&data.status==='PENDING_PAYMENT'?'Đã thanh toán · đang cập nhật đơn hàng':paymentStatus==='CANCELLED'&&data.status==='PENDING_PAYMENT'?'Đã hủy thanh toán · đang cập nhật đơn hàng':stateLabel(data.status);return row('Mã đơn hàng',data.orderCode)+row('Trạng thái',status)+row('Người nhận',data.buyerName)+row('Số điện thoại',data.buyerPhone)+row('Giao đến',data.shippingAddress)+(data.items||[]).map(i=>row(i.productName+` (${i.color??''} / ${i.size??''}) × ${i.quantity}`,money(i.lineTotal))).join('')+row('Phí giao hàng',money(data.shippingFee))+row('Thanh toán',data.paymentMethod==='COD'?'Thanh toán khi nhận hàng':'PayOS')+row('Tổng cộng',money(data.totalAmount));}
function addPaymentLink(target,data){const url=safePaymentUrl(data.checkoutUrl);if(url&&data.status==='PENDING_PAYMENT'&&!paymentConfirmed&&!['CANCELLED','FAILED'].includes(paymentStatus)){const a=document.createElement('a');a.href=url;a.className='primary';a.textContent='Mở trang QR PayOS ↗';target.append(a);}}
function showComplete(){
 const paid=order.status==='PAID'||paymentConfirmed;
 const heading=paid?(order.status==='PAID'?'Thanh toán thành công!':'Thanh toán thành công, đang cập nhật đơn hàng.'):
  order.status==='CANCELLED'||paymentStatus==='CANCELLED'?'Thanh toán đã hủy.':order.status==='PAYMENT_FAILED'||paymentStatus==='FAILED'?'Đơn đã lưu, thanh toán gặp lỗi.':
  order.status==='PENDING_PAYMENT'?'Đơn hàng đang chờ thanh toán.':'Đơn hàng đã được ghi nhận.';
 const description=paid?'Cảm ơn bạn đã mua sắm tại MSS Studio.':order.status==='CANCELLED'||paymentStatus==='CANCELLED'?'Đơn hàng chưa được thanh toán.':order.status==='PENDING_PAYMENT'?'Quét mã QR trên trang PayOS để hoàn tất thanh toán.':'Lưu mã đơn để theo dõi trạng thái và xem lại thông tin.';
 $('#complete-view').innerHTML=`<div class="complete"><div class="success-mark">${paid?'✓':order.status==='CANCELLED'||paymentStatus==='CANCELLED'?'−':'!'}</div><p class="eyebrow">MSS / ORDER DETAILS</p><h2>${heading}</h2><p>${description}</p>${paymentMessage?`<div class="notice-box" role="status">${esc(paymentMessage)}</div>`:''}${orderDetails(order)}${order.statusReason?`<div class="notice-box">${esc(order.statusReason)}</div>`:''}<div class="complete-actions"><button class="outline" id="copy-code">Sao chép mã đơn</button><button class="outline" id="refresh-order">Cập nhật trạng thái</button>${['CONFIRMED','PENDING_PAYMENT','PAYMENT_FAILED'].includes(order.status)&&!paid&&!['CANCELLED','FAILED'].includes(paymentStatus)?'<button class="outline" id="cancel-order">Hủy đơn hàng</button>':''}</div></div>`;
 addPaymentLink($('#complete-view .complete-actions'),order);
 $('#copy-code').onclick=async()=>{try{await navigator.clipboard.writeText(String(order.orderCode));$('#copy-code').textContent='Đã sao chép';}catch{notice('Mã đơn: '+order.orderCode);}};
 $('#refresh-order').onclick=async()=>{lock(true);try{await refreshOrderStatus();showComplete();}catch(e){notice(e.message);}finally{lock(false);}};
 if($('#cancel-order'))$('#cancel-order').onclick=async()=>{if(!confirm('Hủy đơn '+order.orderCode+'?'))return;lock(true);try{order=await api('order','/api/shop/orders/'+order.orderCode+'/cancel',{reason:'Khách hàng yêu cầu hủy'});showComplete();}catch(e){notice(e.message);}finally{lock(false);}};
}
async function refreshOrderStatus(){
 const code=order.orderCode;
 if(order.paymentMethod==='PAYOS'&&order.status==='PENDING_PAYMENT'){
  const payment=await api('payment','/api/payments/payos/'+code+'/verify',{});
  if(payment.orderId!==order.id||String(payment.orderCode)!==String(code)||payment.provider!=='PAYOS')throw new Error('Không xác minh được thanh toán cho đơn hàng này.');
  paymentStatus=payment.status;
  paymentConfirmed=payment.status==='PAID';
  if(payment.status==='CANCELLED')paymentMessage='Giao dịch đã hủy trên PayOS.';
  if(payment.status==='FAILED')paymentMessage='Thanh toán PayOS không thành công.';
 }
 order=await api('order','/api/shop/orders/'+code);
 if(order.status==='PAID')paymentConfirmed=true;
 return order;
}
async function resumePaymentReturn(){
 const params=new URLSearchParams(location.search);
 const rawCode=params.get('orderCode')||sessionStorage.getItem('mss-payos-order-code');
 if(!rawCode||!/^\d{1,18}$/.test(rawCode)){notice('Không tìm thấy mã đơn PayOS. Vui lòng tra cứu lịch sử đơn hàng.');$('#track-dialog').showModal();await history();return;}
 try{
  order=await api('order','/api/shop/orders/'+encodeURIComponent(rawCode));
  if(order.paymentMethod!=='PAYOS')throw new Error('Đơn hàng này không thanh toán qua PayOS.');
  paymentMessage=location.pathname==='/payment/cancel'?'Đang kiểm tra trạng thái giao dịch đã hủy…':'Đang xác minh giao dịch với PayOS…';
  setStep(3);showComplete();renderCart();
  for(let attempt=0;attempt<15&&view==='checkout'&&step===3;attempt++){
   try{await refreshOrderStatus();}catch(error){paymentMessage='Chưa kết nối được PayOS để xác minh. Vui lòng thử cập nhật trạng thái.';showComplete();break;}
   if(['CANCELLED','FAILED'].includes(paymentStatus)||['PAID','CANCELLED','PAYMENT_FAILED','REFUNDED'].includes(order.status)){
    paymentMessage=order.status==='PAID'||paymentConfirmed?'':order.status==='CANCELLED'?'Giao dịch đã hủy.':'';
    sessionStorage.removeItem('mss-payos-order-code');showComplete();return;
   }
   paymentMessage=paymentConfirmed?'PayOS đã xác nhận thanh toán. Đang cập nhật trạng thái đơn hàng…':location.pathname==='/payment/cancel'?'Thanh toán chưa hoàn tất. Đang xác minh với PayOS…':'Đang chờ PayOS xác nhận thanh toán…';
   showComplete();await new Promise(resolve=>setTimeout(resolve,2000));
  }
  if(view==='checkout'&&step===3){paymentMessage=paymentConfirmed?'PayOS đã xác nhận thanh toán. Đơn hàng đang được cập nhật; hãy bấm “Cập nhật trạng thái”.':'Chưa xác nhận được thanh toán. Vui lòng bấm “Cập nhật trạng thái” hoặc xem lại đơn sau.';showComplete();}
 }catch(error){notice(error.message);$('#track-dialog').showModal();await history();}
}
async function history(){const target=$('#track-result');target.textContent='Đang tải lịch sử đơn…';try{const data=await api('order','/api/shop/orders');target.innerHTML=data.length?'<h3 style="margin-top:24px">Đơn hàng của bạn</h3>'+data.map(o=>`<button class="history-order outline" data-code="${o.orderCode}" style="width:100%;text-align:left;margin:6px 0">#${o.orderCode} · ${money(o.totalAmount)}<br><small>${esc(stateLabel(o.status))}</small></button>`).join(''):'<p>Chưa có đơn hàng trong phiên mua sắm này.</p>';}catch(e){target.textContent=e.message;}}
$('#track-open').onclick=()=>{$('#track-dialog').showModal();history();};
$('#track-result').onclick=event=>{const button=event.target.closest('[data-code]');if(!button)return;$('#track-code').value=button.dataset.code;$('#track-form').requestSubmit();};
$('#track-form').onsubmit=async event=>{event.preventDefault();const code=$('#track-code').value.trim(),target=$('#track-result'),button=event.target.querySelector('button');button.disabled=true;target.textContent='Đang tìm đơn hàng…';try{if(!/^\d+$/.test(code))throw new Error('Mã đơn hàng phải là số.');order=await api('order','/api/shop/orders/'+encodeURIComponent(code));paymentConfirmed=order.status==='PAID';paymentStatus=null;paymentMessage='';target.innerHTML=orderDetails(order);addPaymentLink(target,order);const details=document.createElement('button');details.className='outline';details.textContent='Xem và quản lý đơn hàng';details.onclick=()=>{$('#track-dialog').close();setStep(3);showComplete();};target.append(details);}catch(e){target.innerHTML=`<div class="error">${esc(e.message)}</div>`;}finally{button.disabled=false;}};
async function initialize(){
 lock(true);$('#cart-items').textContent='Đang tải giỏ hàng…';
 try{
  const [catalog,items,settings,capabilities]=await Promise.all([api('product','/api/v1/products'),api('order','/api/shop/cart'),api('order','/api/shop/settings'),api('payment','/api/payments/capabilities')]);
  products=catalog.filter(p=>p.variants?.length);cart=items;shippingFee=Number(settings.shippingFee);
  if(cart.some(i=>!products.some(p=>p.id===i.productId&&p.variants.some(v=>v.id===i.variantId))))throw new Error('Giỏ hàng có sản phẩm ngừng bán. Vui lòng xóa giỏ để chọn lại.');
  $('#delivery-fee').textContent=money(shippingFee);
  if(!capabilities.payosEnabled){const input=$('input[value="payos"]');input.disabled=true;input.dataset.permanentDisabled='true';$('#payos-description').textContent='Chưa khả dụng — vui lòng chọn COD';}
  ready=true;renderCart();renderCatalog();renderStore();
 }catch(e){ready=false;cart=[];$('#cart-items').innerHTML='<div class="error">Không tải được dữ liệu cửa hàng. <button id="retry-load" class="text-button">Thử lại</button> <button id="reset-cart" class="text-button">Xóa giỏ hàng</button></div>';notice(e.message);$('#retry-load').onclick=initialize;$('#reset-cart').onclick=async()=>{try{await api('order','/api/shop/cart',{items:[]},'PUT');await initialize();}catch(error){notice(error.message);}};}finally{lock(false);}
}
await initialize();
if(location.pathname==='/payment/success'||location.pathname==='/payment/cancel')await resumePaymentReturn();

function shownProducts(){
 const term=$('#product-search').value.trim().toLocaleLowerCase('vi');
 const items=products.filter(p=>{
  const name=p.name.toLocaleLowerCase('vi');
  return (!term||(name+' '+p.collection.toLocaleLowerCase('vi')).includes(term)) &&
   (filter==='all'||(filter==='top'?name.includes('áo'):name.includes('quần')));
 });
 const sort=$('#product-sort').value;
 if(sort==='price-asc')items.sort((a,b)=>a.price-b.price);
 if(sort==='price-desc')items.sort((a,b)=>b.price-a.price);
 if(sort==='name')items.sort((a,b)=>a.name.localeCompare(b.name,'vi'));
 return items;
}
function renderStore(){
 const items=shownProducts();
 $('#product-count').textContent=items.length+' sản phẩm';
 $('#store-catalog').innerHTML=items.length?items.map((p,index)=>
  '<article class="store-card"><button class="store-image" data-product="'+esc(p.id)+'" aria-label="Xem '+esc(p.name)+'"><img src="'+esc(safeUrl(p.image))+'" alt="'+esc(p.name)+'" loading="lazy"><span class="store-image-action">Xem sản phẩm ↗</span></button><div class="store-card-meta"><span>'+esc(p.collection||'MSS STUDIO')+'</span><span>0'+(index+1)+'</span></div><button class="store-card-title" data-product="'+esc(p.id)+'">'+esc(p.name)+'</button><p>'+money(p.price)+'</p></article>'
 ).join(''):'<div class="no-results">Không tìm thấy sản phẩm phù hợp. <button id="clear-filters" class="text-button">Xóa bộ lọc ↗</button></div>';
 if($('#clear-filters'))$('#clear-filters').onclick=()=>{$('#product-search').value='';filter='all';document.querySelectorAll('[data-filter]').forEach(b=>b.classList.toggle('selected',b.dataset.filter==='all'));renderStore();};
 if(products[0]){$('#hero-image').src=safeUrl(products[0].image);$('#hero-image').alt=products[0].name;}
}
function renderProductDetail(p,chosenColor,chosenSize,quantity=1){
 const colors=[...new Set(p.variants.map(v=>v.color))];
 const color=colors.includes(chosenColor)?chosenColor:colors[0];
 const sizes=p.variants.filter(v=>v.color===color).map(v=>v.size);
 const size=sizes.includes(chosenSize)?chosenSize:sizes[0];
 $('#product-detail').innerHTML=
  '<div class="product-detail-image"><img src="'+esc(safeUrl(p.image))+'" alt="'+esc(p.name)+'"></div>'+
  '<div class="product-detail-info"><p class="eyebrow">MSS STUDIO / '+esc(p.collection||'COLLECTION')+'</p><h2>'+esc(p.name)+'</h2><strong class="product-detail-price">'+money(p.price)+'</strong><p class="product-intro">Thiết kế từ bộ sưu tập MSS Studio, dễ phối cho những ngày thường.</p>'+
  '<div class="detail-choice"><span>Màu sắc</span><div class="choice-pills" id="detail-colors">'+colors.map(c=>'<button data-color="'+esc(c)+'" class="'+(c===color?'selected':'')+'">'+esc(c)+'</button>').join('')+'</div></div>'+
  '<div class="detail-choice"><span>Kích thước</span><div class="choice-pills" id="detail-sizes">'+sizes.map(s=>'<button data-size="'+esc(s)+'" class="'+(s===size?'selected':'')+'">'+esc(s)+'</button>').join('')+'</div></div>'+
  '<button id="detail-size-guide" class="text-button">Xem bảng size ↗</button><div class="detail-buy"><div class="quantity"><button id="detail-minus" aria-label="Giảm số lượng">−</button><span>'+quantity+'</span><button id="detail-plus" aria-label="Tăng số lượng">+</button></div><button id="detail-add" class="primary">Thêm vào giỏ <span>→</span></button></div><p class="detail-footnote">Giá và biến thể được xác nhận từ Product Catalog khi đặt hàng.</p></div>';
 $('#detail-colors').onclick=e=>{const b=e.target.closest('[data-color]');if(b)renderProductDetail(p,b.dataset.color,size,quantity);};
 $('#detail-sizes').onclick=e=>{const b=e.target.closest('[data-size]');if(b)renderProductDetail(p,color,b.dataset.size,quantity);};
 $('#detail-minus').onclick=()=>renderProductDetail(p,color,size,Math.max(1,quantity-1));
 $('#detail-plus').onclick=()=>renderProductDetail(p,color,size,Math.min(10,quantity+1));
 $('#detail-size-guide').onclick=()=>$('#size-dialog').showModal();
 $('#detail-add').onclick=async()=>{
  const selected=p.variants.find(v=>v.color===color&&v.size===size);
  const next=cart.map(i=>({...i})),existing=next.find(i=>i.variantId===selected.id);
  if(existing&&existing.quantity+quantity>10){notice('Tối đa 10 sản phẩm cho mỗi biến thể.');return;}
  if(existing)existing.quantity+=quantity;else next.push({productId:p.id,variantId:selected.id,quantity});
  if(await saveCart(next)){$('#product-dialog').close();setStep(1);}
 };
}
$('#store-catalog').onclick=e=>{const button=e.target.closest('[data-product]');if(!button)return;const p=product(button.dataset.product);if(p){renderProductDetail(p);$('#product-dialog').showModal();}};
$('#product-search').oninput=renderStore;
$('#product-sort').onchange=renderStore;
document.querySelectorAll('[data-filter]').forEach(b=>b.onclick=()=>{filter=b.dataset.filter;document.querySelectorAll('[data-filter]').forEach(x=>x.classList.toggle('selected',x===b));renderStore();});
$('#hero-shop').onclick=()=>showStore(true);
$('#nav-home').onclick=()=>showStore();
$('#breadcrumb-home').onclick=()=>showStore();
document.querySelectorAll('.catalog-link').forEach(b=>b.onclick=()=>showStore(true));
if(!location.pathname.startsWith('/payment/'))showStore();
